package com.swappy.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DemoPaymentRequest(
        @NotBlank(message = "Payment token is required")
        @Size(max = 64, message = "Payment token is too long")
        String paymentToken,

        @NotBlank(message = "Cardholder name is required")
        @Size(max = 100, message = "Cardholder name must be at most 100 characters")
        String cardholderName) {
}
