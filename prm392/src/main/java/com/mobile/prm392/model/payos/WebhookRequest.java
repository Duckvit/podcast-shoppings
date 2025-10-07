package com.mobile.prm392.model.payos;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WebhookRequest {
    private String orderCode;
    private String status;
}
