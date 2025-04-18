package ru.dzhenbaz.BackendBankCardsManaging.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "Ответ при успешном переводе между картами")
public class TransferResponseDto {

    @Schema(description = "Сообщение об успешной операции", example = "Transfer completed successfully")
    private String message;

    @Schema(description = "Номер карты-отправителя", example = "**** **** **** 1111")
    private String fromCardNumber;

    @Schema(description = "Номер карты-получателя", example = "**** **** **** 2222")
    private String toCardNumber;

    @Schema(description = "Сумма перевода", example = "1500.00")
    private BigDecimal amount;

    @Schema(description = "Описание перевода", example = "Перевод на резервную карту")
    private String description;

    public TransferResponseDto() {
    }

    public TransferResponseDto(String message, String fromCardNumber,
                               String toCardNumber, BigDecimal amount, String description) {
        this.message = message;
        this.fromCardNumber = fromCardNumber;
        this.toCardNumber = toCardNumber;
        this.amount = amount;
        this.description = description;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getFromCardNumber() {
        return fromCardNumber;
    }

    public void setFromCardNumber(String fromCardNumber) {
        this.fromCardNumber = fromCardNumber;
    }

    public String getToCardNumber() {
        return toCardNumber;
    }

    public void setToCardNumber(String toCardNumber) {
        this.toCardNumber = toCardNumber;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getDescpription() {
        return description;
    }

    public void setDescpription(String descpription) {
        this.description = descpription;
    }
}
