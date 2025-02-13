package com.serenity.integration.models;


import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
@Setter
@Getter
@Entity
@Table(name = "charge_item")
public class ChargeItem {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    private String uuid;

    private String appointmentId;
    private String batchNumber;
    private String category;
    private double charge;
    private String createdByName;
    private String currency;
    private String encounterId;
    private String locationId;
    private String locationName;
    private String medicationRequestId;
    private double patientContribution;
    private double payerContribution;
    private String practitionerId;
    private String practitionerName;
    private String productId;
    private String providerId;
    private String providerName;
    private int quantity;
    private String revenueTagDisplay;
    private String relationship;
    private String serialNumber;
    private String orderCode;
    private String orderCodeSystem;
    
    private String cancellationRequestedAt;
    private String cancellationRequestedByName;
    private String cancellationRequestedById;

    private String cancellationApprovedAt;
    private String cancellationApprovedName;
    private String cancellationApprovedById;

    private String canceledAt;
    private String canceledByName;
    private String canceledById;

    private String serviceId;
    private String serviceOrProductName;
    private String serviceRequestId;
    private double unitPrice;
    private String userFriendlyId;
    private String visitId;
}
