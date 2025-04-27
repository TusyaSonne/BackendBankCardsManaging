package ru.dzhenbaz.BackendBankCardsManaging.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.dzhenbaz.BackendBankCardsManaging.dto.LimitResponseDto;
import ru.dzhenbaz.BackendBankCardsManaging.model.Limit;
import ru.dzhenbaz.BackendBankCardsManaging.repository.LimitRepository;

import java.math.BigDecimal;

/**
 * Сервис для управления лимитами операций.
 * Отвечает за получение и обновление дневного лимита.
 */
@Service
@Transactional(readOnly = true)
public class LimitService {

    private final LimitRepository limitRepository;

    @Autowired
    public LimitService(LimitRepository limitRepository) {
        this.limitRepository = limitRepository;
    }

    /**
     * Получает текущий дневной лимит.
     * Если лимит отсутствует, создаёт его с дефолтным значением.
     *
     * @return DTO с информацией о лимите
     */
    @Transactional
    public LimitResponseDto getDailyLimit() {
        Limit limit = limitRepository.findByName("daily_limit");
        if (limit == null) {
            limit = new Limit();
            limit.setName("daily_limit");
            limit.setLimitValue(BigDecimal.valueOf(1000000.00));
            limit = limitRepository.save(limit);
        }

        return new LimitResponseDto(limit.getName(), limit.getLimitValue());
    }

    /**
     * Обновляет значение дневного лимита.
     * Доступно только администраторам.
     *
     * @param newValue новое значение лимита
     * @return обновленный лимит в виде DTO
     */
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public LimitResponseDto updateDailyLimit(BigDecimal newValue) {
        Limit limit = limitRepository.findByName("daily_limit");
        if (limit == null) {
            limit = new Limit();
            limit.setName("daily_limit");
        }
        limit.setLimitValue(newValue);
        limitRepository.save(limit);
        return new LimitResponseDto(limit.getName(), limit.getLimitValue());
    }
}
