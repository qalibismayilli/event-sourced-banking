package org.example.accountservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.example.accountservice.model.Currency;

import java.math.BigDecimal;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AccountRequestDto{

    @NotBlank(message = "Owner name cannot be blank")
    String ownerName;

    @NotNull(message = "Currency cannot be null")
    Currency currency;

    BigDecimal limit;
}