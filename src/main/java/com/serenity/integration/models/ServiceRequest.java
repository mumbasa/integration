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
    @Column(nullable = true)

    private String updatedAt;
    @Column(nullable = true)
    private boolean deleted;
    @Column(columnDefinition = "TEXT")
    private String display;
    @Column(columnDefinition = "TEXT")

    private String notes;
    @Column(columnDefinition = "TEXT")

    private String category;
    @Column(columnDefinition = "TEXT")
    private String code;
    @Column(columnDefinition = "TEXT")

    private String diagnosticServiceSection;
    @Column(columnDefinition = "TEXT")

    private String dueDate;
    @Column(columnDefinition = "TEXT")

    private String purpose;
    private String sampleReceivedDateTime;
    private String priority;
    @Column(columnDefinition = "TEXT")

    private String healthcareServiceId;
    @Column(columnDefinition = "TEXT")
    private String healthcareServiceName;
    private String chargeItemId;
    private UUID priceTierId;
    private UUID replacesId;
    @Column(columnDefinition = "TEXT")

    private String status;
    @Column(columnDefinition = "TEXT")

    private String statusReason;
    @Column(columnDefinition = "TEXT")

    private String groupIdentifier;
    @Column(columnDefinition = "TEXT")

    private String bodySite;
    @Column(columnDefinition = "TEXT")

    private String intent;
    private boolean doNotPerform;
    private boolean isPaid;
    private double charge;
    @Column(columnDefinition = "TEXT")

    private String practitionerName;
    private String encounterClass;
    private boolean paymentRequired;
    private String patientMrNumber;
    private String patientMobile;
    private String patientBirthDate;
    private String patientGender;
    @Column(columnDefinition = "TEXT")

    private String patientFullName;
    @Column(columnDefinition = "TEXT")

    private String occurence;
    @Column(columnDefinition = "TEXT")
    private String practitionerId;
    private String modifiedAt;

    @Column(columnDefinition = "TEXT")
    private String note;
    @Column(columnDefinition = "TEXT")
    private String createdAt;
}
