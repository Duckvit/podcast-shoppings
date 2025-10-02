package com.mobile.prm392.api;

import com.mobile.prm392.services.PayOSService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payment")
public class PayOSAPI {

    private final PayOSService payOSService;

    public PayOSAPI(PayOSService payOSService) {
        this.payOSService = payOSService;
    }

    @PostMapping("/create")
    public String createPayment(@RequestParam Long orderCode,
                                @RequestParam Long amount,
                                @RequestParam String description,
                                @RequestParam String returnUrl,
                                @RequestParam String cancelUrl) {
        return payOSService.createPaymentLink(orderCode, amount, description, returnUrl, cancelUrl).block();
    }
}

