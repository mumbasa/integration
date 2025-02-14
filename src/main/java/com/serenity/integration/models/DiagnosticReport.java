package com.serenity.integration.models;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.*;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.Getter;


@Entity
@Table(name = "diagnostic_report")
@NoArgsConstructor
@Setter
@Getter
@AllArgsConstructor
public class DiagnosticReport {

     @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private long id ;
    private String createdAt;
    private String issuedDate;
    private String sampleReceivedDateTime;
    private String approvedDateTime;
    private String reviewedDateTime;
    private String effectiveDateTime;

    // Turnaround Times
    private double billingTurnaroundTime;
    private double totalTurnaroundTime;
    private double intraLaboratoryTurnaroundTime;

    // Identifiers
    private String uuid;
    private String display;
    private String category;
    private String code;
    private String system;

    // Descriptive Fields
    private String conclusion;
    private String purpose;
    private String passportNumber;

    // Service and Provider Information
    private String healthcareServiceId;
    private String serviceProviderId;

    // Status Information
    private String status;
    private String statusReason;

    // Performer and Approver Information
    private String performerName;
    private String performerId;
    private String approvedByName;
    private String approvedById;
    private String reviewedByName;
    private String reviewedById;

    // Encounter and Patient Information
    private String encounterId;
    private String basedOnId;
    private String patientId;
    private String visitId;
    private String serviceRequestCategory;

    // Patient Details
    private String patientMrNumber;
    private String patientFullName;
    private String patientMobile;
    private String patientBirthDate;
    private String patientGender;
}
