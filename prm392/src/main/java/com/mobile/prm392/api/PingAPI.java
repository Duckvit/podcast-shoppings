package com.mobile.prm392.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PingAPI {
    @GetMapping("/ping")
    public String ping() {
        return "pong";
    }
}
