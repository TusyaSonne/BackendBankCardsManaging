package ru.dzhenbaz.BackendBankCardsManaging.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Ответ с информацией о банковской карте")
public class CardResponseDto {

    @Schema(description = "Идентификатор карты", example = "1")
    private Long id;

    @Schema(description = "Маскированный номер карты", example = "**** **** **** 1234")
    private String maskedCardNumber;

    @Schema(description = "Дата окончания срока действия", example = "2026-12-31")
    private LocalDate expirationDate;

    @Schema(description = "Статус карты", example = "ACTIVE")
    private String status;

    @Schema(description = "Баланс карты", example = "100000.00")
    private BigDecimal balance;

    @Schema(description = "ID владельца карты", example = "5")
    private Long ownerId;

    public CardResponseDto() {}

    public CardResponseDto(Long id, String maskedCardNumber,
                           LocalDate expirationDate, String status,
                           BigDecimal balance,
                           Long ownerId) {
        this.id = id;
        this.maskedCardNumber = maskedCardNumber;
        this.expirationDate = expirationDate;
        this.status = status;
        this.balance = balance;
        this.ownerId = ownerId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMaskedCardNumber() {
        return maskedCardNumber;
    }

    public void setMaskedCardNumber(String maskedCardNumber) {
        this.maskedCardNumber = maskedCardNumber;
    }

    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(LocalDate expirationDate) {
        this.expirationDate = expirationDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }
}
