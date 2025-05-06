package com.serenity.integration.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "observations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Observation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String uuid;
    
    private String status;
    private String category;
    private String tag;
    private String code;
    private String display;
    private String subCategory;
    private String system;
    private String issued;
    private String unit;
    @Column(columnDefinition = "TEXT")
    private String value;
    private String score;
    private String hexColorCode;
    private int rank;
    private String referenceRangeHigh;
    private String referenceRangeLow;
    private String referenceRangeText;
    private String interpretation;
    private String bodySite;
    private String method;
    private String specimen;
    private String effectiveDateTime;
    private String  serviceRequestId;
    private String diagnosticReportId;
    private String practitionerName;
    private String practitionerId;
    private String patientId;
    private String encounterId;
    private String enconterType;
    private String visitId;
    private String updatedAt;

    private String createdAt;
}