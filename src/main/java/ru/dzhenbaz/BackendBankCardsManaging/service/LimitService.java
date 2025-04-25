package ru.dzhenbaz.BackendBankCardsManaging.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.dzhenbaz.BackendBankCardsManaging.dto.LimitResponseDto;
import ru.dzhenbaz.BackendBankCardsManaging.model.Limit;
import ru.dzhenbaz.BackendBankCardsManaging.repository.LimitRepository;

import java.math.BigDecimal;

@Service
@Transactional(readOnly = true)
public class LimitService {

    private final LimitRepository limitRepository;

    @Autowired
    public LimitService(LimitRepository limitRepository) {
        this.limitRepository = limitRepository;
    }

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
