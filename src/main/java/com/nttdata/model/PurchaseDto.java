package com.nttdata.model;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.OffsetDateTime;

public record PurchaseDto(String id, String cardId, String description, Double amount, String currency, @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX") OffsetDateTime timestamp) {
}
