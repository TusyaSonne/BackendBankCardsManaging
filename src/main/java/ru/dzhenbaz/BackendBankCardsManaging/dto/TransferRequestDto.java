package ru.dzhenbaz.BackendBankCardsManaging.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * DTO для запроса на перевод средств между картами одного пользователя.
 * Содержит информацию о карте-отправителе, карте-получателе, сумме и описании перевода.
 */
@Schema(description = "Запрос на перевод средств между картами пользователя")
public class TransferRequestDto {

    @Schema(description = "ID карты-отправителя", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Source card ID is required")
    private Long fromCardId;

    @Schema(description = "ID карты-получателя", example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Destination card ID is required")
    private Long toCardId;

    @Schema(description = "Сумма перевода", example = "5000.00", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Transfer amount is required")
    @DecimalMin(value = "0.01", message = "Transfer amount must be greater than 0")
    private BigDecimal amount;

    @Schema(description = "Описание перевода (необязательное)", example = "Transfer to savings card")
    private String description;

    public TransferRequestDto() {
    }

    public TransferRequestDto(Long fromCardId, Long toCardId, BigDecimal amount, String description) {
        this.fromCardId = fromCardId;
        this.toCardId = toCardId;
        this.amount = amount;
        this.description = description;
    }

    public Long getFromCardId() {
        return fromCardId;
    }

    public void setFromCardId(Long fromCardId) {
        this.fromCardId = fromCardId;
    }

    public Long getToCardId() {
        return toCardId;
    }

    public void setToCardId(Long toCardId) {
        this.toCardId = toCardId;
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
