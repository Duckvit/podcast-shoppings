package com.mobile.prm392.config;


import com.mobile.prm392.entities.User;
import com.mobile.prm392.services.TokenService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.security.auth.message.AuthException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.util.List;

@Component // bien no thanh thu vien khi can dung chi can autowired
public class Filter extends OncePerRequestFilter {
    @Autowired
    TokenService tokenService;

    @Autowired
    @Qualifier("handlerExceptionResolver")
    HandlerExceptionResolver resolver;

    private final List<String> AUTH_PERMISSION = List.of(
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/swagger-resources/**",
            "/api/auth/register",
            "/api/auth/login",
            "/api/gemini/chat",
            "/api/auth/email-existed",
            "/api/auth/forgot-password"
    );

    public boolean checkIsPublicAPI(String uri) {
        //uri : /api/register
        //nếu gặp những api trong list trên => cho phép truy cập lun => true
        AntPathMatcher patchMatch = new AntPathMatcher();
        //check token = > false
        return AUTH_PERMISSION.stream().anyMatch(pattern -> patchMatch.match(pattern, uri));
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        //filterChain.doFilter(request, response);
        boolean isPublicAPI = checkIsPublicAPI(request.getRequestURI());

        if (isPublicAPI) {
            filterChain.doFilter(request, response);
        } else {
            String token = getToken(request);
            if (token == null) {
                //ko đc phép truy cập
                resolver.resolveException(request, response, null, new AuthException("Empty token!"));
                return;
            }
            //=> có token
            //check xem token có đúng hay ko => lấy thông tin account từ token
            User user;

            try {
                user = tokenService.getUserByToken(token);

            } catch (ExpiredJwtException e) {
                //response token hết hạn
                resolver.resolveException(request, response, null, new AuthException("Expired token!"));
                return;
            } catch (MalformedJwtException malformedJwtException) {
                //response token sai
                resolver.resolveException(request, response, null, new AuthException("Invalid token!"));
                return;
            }

            //=> token chuẩn
            //=> cho phép truy cập
            //lưu lại thông tin account
            if (user != null) {
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                        user,
                        token,
                        user.getAuthorities()
                );
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            } else {
                resolver.resolveException(request, response, null, new AuthException("Invalid token!"));
                return;
            }
            //token ok, cho vào
            filterChain.doFilter(request, response);
        }
    }

    public String getToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null) return null;
        return authHeader.substring(7);
    }
}
