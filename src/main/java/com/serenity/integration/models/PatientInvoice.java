package com.serenity.integration.models;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "patient_invoice")
public class PatientInvoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String uuid;

    private String patientId;
    private String patientName;
    private String patientMrNumber;
    private String patientBirthDate;
    private String patientGender;
    private String patientMobile;
    private String payerName;
    private String managingOrganizationId;
    private String payerId;
    private String payerType;
    private String currency;
    private String visitId;
    private String paymentMethod;
    private String invoiceDate;
    private BigDecimal amountPaid;
    private LocalDateTime dueDate;
    private String externalId;
    private String externalSystem;

    @Column(nullable = false, updatable = false)
    private String createdAt;

    private String updatedAt;

    

}
