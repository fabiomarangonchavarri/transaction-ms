package com.nttdata.model;

public record ExchangeRateDto(
        String pair,
        Double buyRate,
        Double sellRate
) {
}
