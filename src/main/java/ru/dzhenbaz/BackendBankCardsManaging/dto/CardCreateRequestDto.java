package ru.dzhenbaz.BackendBankCardsManaging.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Schema(description = "Запрос на создание карты")
public class CardCreateRequestDto {

    @Schema(description = "ID пользователя, которому будет принадлежать карта", example = "1")
    @NotNull(message = "User ID is required")
    private Long userId;

    @Schema(description = "Начальный баланс карты", example = "100000.00")
    @NotNull(message = "Balance is required")
    @DecimalMin(value = "0.00", message = "Balance must be zero or greater")
    private BigDecimal balance;

    public CardCreateRequestDto() {
    }

    public CardCreateRequestDto(Long userId, BigDecimal balance) {
        this.userId = userId;
        this.balance = balance;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }
}
