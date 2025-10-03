package com.mobile.prm392.api;

import com.mobile.prm392.model.payos.ApiResponse;
import com.mobile.prm392.model.payos.CreatePaymentLinkRequestBody;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.payos.PayOS;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkRequest;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;
import vn.payos.model.v2.paymentRequests.PaymentLinkItem;

@RestController
@RequestMapping("/payos")
@CrossOrigin("*")
public class PayOSAPI {
    private final PayOS payOS;

    public PayOSAPI(PayOS payOS) {
        super();
        this.payOS = payOS;
    }

    @PostMapping(path = "/create")
    public ApiResponse<CreatePaymentLinkResponse> createPaymentLink(
            @RequestBody CreatePaymentLinkRequestBody RequestBody) {
        try {
//            System.out.println("DEBUG >>> " + RequestBody);
//            System.out.println("Name >>> " + RequestBody.getName());


            final String productName = RequestBody.getName();
            final String description = RequestBody.getDescription();
            final String returnUrl = RequestBody.getReturnUrl();
            final String cancelUrl = RequestBody.getCancelUrl();
            final long price = RequestBody.getPrice();
            long orderCode = System.currentTimeMillis() / 1000;
            PaymentLinkItem item =
                    PaymentLinkItem.builder().name(productName).quantity(1).price(price).build();

            CreatePaymentLinkRequest paymentData =
                    CreatePaymentLinkRequest.builder()
                            .orderCode(orderCode)
                            .description(description)
                            .amount(price)
                            .item(item)
                            .returnUrl(returnUrl)
                            .cancelUrl(cancelUrl)
                            .build();

            CreatePaymentLinkResponse data = payOS.paymentRequests().create(paymentData);
            return ApiResponse.success(data);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error("fail");
        }
    }
}
