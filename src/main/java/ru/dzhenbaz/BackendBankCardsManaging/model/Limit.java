package ru.dzhenbaz.BackendBankCardsManaging.model;

import jakarta.persistence.*;

import java.math.BigDecimal;

/**
 * Entity-класс, представляющий лимит на операции.
 * Содержит название лимита и его максимальное значение.
 */
@Entity
@Table(name = "limits")
public class Limit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String name;
    private BigDecimal limitValue;

    public Limit() {
    }

    public Limit(Integer id, String name, BigDecimal limitValue) {
        this.id = id;
        this.name = name;
        this.limitValue = limitValue;
    }

    public Limit(String name, BigDecimal limitValue) {
        this.name = name;
        this.limitValue = limitValue;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getLimitValue() {
        return limitValue;
    }

    public void setLimitValue(BigDecimal limitValue) {
        this.limitValue = limitValue;
    }
}
