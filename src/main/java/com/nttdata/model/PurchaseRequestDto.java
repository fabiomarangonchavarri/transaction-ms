package com.nttdata.model;

import java.time.OffsetDateTime;

public record PurchaseRequestDto(String customerDocumentNumber, String cardNumber, String currency, Double amount, String description) {
}
