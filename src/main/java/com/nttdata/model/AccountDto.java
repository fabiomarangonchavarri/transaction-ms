package com.nttdata.model;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.OffsetDateTime;

public record AccountDto(
        String id,
        String customerDocumentNumber,
        String accountNumber,
        String accountType,
        String currency,
        Double balance,
        String status,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX") OffsetDateTime lastUpdated
) {
}
