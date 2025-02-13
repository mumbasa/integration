package com.serenity.integration.models;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Entity
@Table(name = "service_request")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    private String uuid;
    private String visitId;
    private String patientId;
    private String encounterId;
    private String accessionNumber;
    @Column(columnDefinition = "TEXT")
    private String display;
    @Column(columnDefinition = "TEXT")

    private String notes;
    private String category;
    private String code;
    private String diagnosticServiceSection;
    private String dueDate;
    private String purpose;
    private String sampleReceivedDateTime;
    private String priority;
    private String healthcareServiceId;
    private String healthcareServiceName;
    private String chargeItemId;
    private UUID priceTierId;
    private UUID replacesId;
    private String status;
    private String statusReason;
    private String groupIdentifier;
    private String bodySite;
    private String intent;
    private boolean doNotPerform;
    private boolean isPaid;
    private double charge;
    private String practitionerName;
    private String encounterClass;
    private boolean paymentRequired;
    private String patientMrNumber;
    private String patientMobile;
    private String patientBirthDate;
    private String patientGender;
    private String patientFullName;
    private String occurence;
    @Column(columnDefinition = "TEXT")
    private String practitionerId;
    private String modifiedAt;
    private String note;
    @Column(columnDefinition = "TEXT")
    private String createdAt;
}
