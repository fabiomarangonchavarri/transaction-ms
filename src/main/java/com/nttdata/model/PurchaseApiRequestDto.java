package com.nttdata.model;

public record PurchaseApiRequestDto(String cardNumber, String currency, Double amount, String description) {
}
