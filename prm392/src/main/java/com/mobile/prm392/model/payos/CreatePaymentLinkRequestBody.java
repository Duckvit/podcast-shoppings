package com.mobile.prm392.model.payos;

import lombok.*;

@Data
@Getter
@Setter
@NoArgsConstructor
public class CreatePaymentLinkRequestBody {
    private String name;
    private String description;
    private String returnUrl;
    private int price;
    private String cancelUrl;
}
