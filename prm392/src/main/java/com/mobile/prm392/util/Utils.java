package com.mobile.prm392.util;

import java.security.SecureRandom;

public class Utils {

    private static final int OTP_LENGTH = 5;
    private static final int RANDOM_STRING_LENGTH = 8;
    private static final SecureRandom random = new SecureRandom();
    private static final String CHARACTERS = "abcdefghijklmnopqrstuvwxyz0123456789";

    public static String generateOTP() {
        StringBuilder otp = new StringBuilder(OTP_LENGTH);
        for (int i = 0; i < OTP_LENGTH; i++) {
            // Tạo số ngẫu nhiên từ 0 đến 9
            int digit = random.nextInt(10);
            otp.append(digit);
        }
        return otp.toString();
    }

    public static String generateRandomString() {
        StringBuilder randomString = new StringBuilder(RANDOM_STRING_LENGTH);
        for (int i = 0; i < RANDOM_STRING_LENGTH; i++) {
            int index = random.nextInt(CHARACTERS.length());
            randomString.append(CHARACTERS.charAt(index));
        }
        return randomString.toString();
    }
}
