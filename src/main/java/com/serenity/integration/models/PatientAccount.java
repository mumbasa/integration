package com.serenity.integration.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;


@Setter
@Getter
public class PatientAccount {

    private long id;
    private String status;
    private String accountType;
    private String currency;
    private String servicePeriodStart;
    private String  servicePeriodEnd;
    private BigDecimal balance;
    private BigDecimal limit;
    private String managingOrganizationId;
    private String ownerId;
    private String coverageId;
    private String modeOfPayment;
}
