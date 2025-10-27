package com.mobile.prm392.model.email;

// Importing required classes
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Annotations
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmailRequest {

    // Class data members
    private String recipient;
    private String msgBody;
    private String subject;
    private String attachment;
}
