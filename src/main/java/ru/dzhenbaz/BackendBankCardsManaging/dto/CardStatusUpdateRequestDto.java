package ru.dzhenbaz.BackendBankCardsManaging.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import ru.dzhenbaz.BackendBankCardsManaging.model.enums.CardStatus;

@Schema(description = "Запрос на изменение статуса карты")
public class CardStatusUpdateRequestDto {

    @Schema(description = "Новый статус карты", example = "BLOCKED")
    @NotNull(message = "New card status is required")
    private CardStatus newStatus;

    public CardStatusUpdateRequestDto() {
    }

    public CardStatus getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(CardStatus newStatus) {
        this.newStatus = newStatus;
    }
}
