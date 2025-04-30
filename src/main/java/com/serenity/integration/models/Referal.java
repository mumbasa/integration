package com.serenity.integration.models;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "referrals")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Referal {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id ;
    
    private String uuid;

    @Column(name = "priority", nullable = false)
    private String priority;

    @Column(name = "specialty")
    private String specialty;

    @Column(name = "reason",columnDefinition = "TEXT")
    private String reason;

    @Column(name = "description")
    private String description;

    @Column(name = "referral_type", nullable = false)
    private String referralType;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "recipient_extra_detail")
    private String recipientExtraDetail;

    @Column(name = "recipient_name")
    private String recipientName;

    @Column(name = "recipient_id")
    private String recipientId;

    @Column(name = "requesting_organization_id")
    private String requestingOrganizationId;

    @Column(name = "requester_id")
    private String requesterId;

    @Column(name = "requester_name")
    private String requesterName;

    @Column(name = "replaces_id")
    private String replacesId;

    @Column(name = "patient_id", nullable = false)
    private String patientId;

    @Column(name = "encounter_id")
    private String encounterId;

    @Column(name = "created_at", updatable = false)
    private String createdAt;
}
