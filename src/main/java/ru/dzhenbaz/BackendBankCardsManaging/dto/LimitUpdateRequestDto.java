package ru.dzhenbaz.BackendBankCardsManaging.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Schema(description = "Запрос на обновление лимита")
public class LimitUpdateRequestDto {

    @Schema(description = "Новое значение лимита", example = "75000.00", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Status value is required")
    private BigDecimal newValue;

    public LimitUpdateRequestDto() {
    }

    public LimitUpdateRequestDto(BigDecimal newValue) {
        this.newValue = newValue;
    }

    public BigDecimal getNewValue() {
        return newValue;
    }

    public void setNewValue(BigDecimal newValue) {
        this.newValue = newValue;
    }
}
