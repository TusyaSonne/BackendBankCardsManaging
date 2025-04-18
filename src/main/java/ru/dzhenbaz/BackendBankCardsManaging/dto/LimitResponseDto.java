package ru.dzhenbaz.BackendBankCardsManaging.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Ответ с текущим лимитом")
public class LimitResponseDto {

    @Schema(description = "Имя лимита", example = "daily_limit")
    private String name;

    @Schema(description = "Значение лимита в рублях", example = "50000.00")
    private BigDecimal value;

    public LimitResponseDto() {
    }

    public LimitResponseDto(String name, BigDecimal value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }
}
