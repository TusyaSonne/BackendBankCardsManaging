package ru.dzhenbaz.BackendBankCardsManaging.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import ru.dzhenbaz.BackendBankCardsManaging.model.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Информация о транзакции")
public class TransactionResponseDto {

    @Schema(description = "Идентификатор транзакции", example = "1")
    private Long id;

    @Schema(description = "Маскированный номер карты", example = "**** **** **** 1234")
    private String cardNumber;

    @Schema(description = "Тип транзакции", example = "WITHDRAW")
    private TransactionType type;

    @Schema(description = "Сумма транзакции", example = "5000.00")
    private BigDecimal amount;

    @Schema(description = "Дата и время транзакции", example = "2025-04-17T21:51:36.041976")
    private LocalDateTime timestamp;

    @Schema(description = "Описание транзакции (опционально)", example = "Test Withdrawal")
    private String description;

    public TransactionResponseDto() {
    }

    public TransactionResponseDto(Long id, String cardNumber, TransactionType type, BigDecimal amount,
                                  LocalDateTime timestamp, String description) {
        this.id = id;
        this.cardNumber = cardNumber;
        this.type = type;
        this.amount = amount;
        this.timestamp = timestamp;
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
