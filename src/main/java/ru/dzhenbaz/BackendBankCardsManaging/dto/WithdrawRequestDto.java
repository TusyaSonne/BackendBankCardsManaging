package ru.dzhenbaz.BackendBankCardsManaging.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Schema(description = "Запрос на снятие средств с карты")
public class WithdrawRequestDto {

    @Schema(description = "ID карты", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Card ID is required")
    private Long cardId;

    @Schema(description = "Сумма для снятия", example = "1000.00", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be positive")
    private BigDecimal amount;

    @Schema(description = "Описание операции (необязательное)", example = "Cash withdrawal")
    private String description;

    public WithdrawRequestDto() {
    }

    public WithdrawRequestDto(Long cardId, BigDecimal amount, String description) {
        this.cardId = cardId;
        this.amount = amount;
        this.description = description;
    }

    public Long getCardId() {
        return cardId;
    }

    public void setCardId(Long cardId) {
        this.cardId = cardId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
