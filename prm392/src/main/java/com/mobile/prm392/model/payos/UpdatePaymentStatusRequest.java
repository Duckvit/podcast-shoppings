package com.mobile.prm392.model.payos;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Getter
@Setter
@NoArgsConstructor
public class UpdatePaymentStatusRequest {
    private String orderCode;
    private String status;
}
