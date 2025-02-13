package com.serenity.integration.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.util.UUID;

@Entity
@Table(name = "allergy_intolerance")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AllergyIntolerance {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    private UUID uuid;

    @Column(name = "clinical_status", nullable = false)
    private String clinicalStatus;

    @Column(name = "verification_status", nullable = false)
    private String verificationStatus;

    @Column(name = "allergy_intolerance_type", nullable = false)
    private String allergyIntoleranceType;

    @Column(name = "category")
    private String category;

    @Column(name = "criticality")
    private String criticality;

    @Column(name = "code")
    private String code;

    @Column(name = "display")
    private String display;

    @Column(name = "onset_period_start")
    private String onsetPeriodStart;

    @Column(name = "onset_period_end")
    private String onsetPeriodEnd;

    @Column(name = "recorded_date")
    @CreationTimestamp
    private String recordedDate;

    @Column(name = "practitioner_id")
    private String practitionerId;

    @Column(name = "practitioner_name")
    private String practitionerName;

    @Column(name = "visit_id")
    private String visitId;

    @Column(name = "service_provider_id")
    private String serviceProviderId;

    @Column(name = "patient_id", nullable = false)
    private String patientId;
}
