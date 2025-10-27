package com.mobile.prm392.model.email;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ScheduleEmailRequest {
    private String title;
    private String description;
    private LocalDateTime sendTime;
}
