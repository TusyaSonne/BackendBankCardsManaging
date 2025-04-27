package ru.dzhenbaz.BackendBankCardsManaging.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import ru.dzhenbaz.BackendBankCardsManaging.model.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO для ответа при успешном снятии средств с карты.
 * Содержит информацию о транзакции: номер карты, тип операции, сумму, дату и описание.
 */
@Schema(description = "Ответ при успешном снятии средств")
public class WithdrawResponseDto {

    @Schema(description = "ID транзакции", example = "5")
    private Long Id;

    @Schema(description = "Маскированный номер карты", example = "**** **** **** 2222")
    private String cardNumber;

    @Schema(description = "Тип транзакции", example = "WITHDRAW")
    private TransactionType type;

    @Schema(description = "Сумма списания", example = "1000.00")
    private BigDecimal amount;

    @Schema(description = "Дата и время транзакции", example = "2025-04-18T16:17:42.4058466")
    private LocalDateTime timestamp;

    @Schema(description = "Описание транзакции", example = "Снятие наличных")
    private String description;

    public WithdrawResponseDto() {
    }

    public WithdrawResponseDto(Long id, String cardNumber, TransactionType type,
                               BigDecimal amount, LocalDateTime timestamp, String description) {
        Id = id;
        this.cardNumber = cardNumber;
        this.type = type;
        this.amount = amount;
        this.timestamp = timestamp;
        this.description = description;
    }

    public Long getId() {
        return Id;
    }

    public void setId(Long id) {
        Id = id;
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
