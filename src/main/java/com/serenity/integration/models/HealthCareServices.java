package com.serenity.integration.models;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "healthcareservice")
@Setter
@Getter
public class HealthCareServices {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pk")
    @JsonIgnore
    private Long pk;

    @Column(name = "uuid", unique = true, nullable = false)
    @JsonIgnore
    private UUID uuid;

    @Column(name = "id", unique = true, nullable = false)
    @JsonIgnore
    private UUID id;

    @Column(name = "service_name", nullable = false,columnDefinition = "TEXT")
    private String serviceName;

    @Column(name = "service_class")
    private String serviceClass;

    @Column(name = "service_request_category")
    private String serviceRequestCategory;

    @Column(name = "diagnostic_service_section")
    private String diagnosticServiceSection;

    @Column(name = "order_code")
    private String orderCode;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    @Column(name = "is_outsourced")
    private boolean isOutsourced;

    @Column(name = "is_virtual")
    private boolean isVirtual;

    @Column(name = "is_appointment_required")
    private boolean isAppointmentRequired;

    @Column(name = "slot_duration_in_minutes")
    private Integer slotDurationInMinutes;

    @Column(name = "location")
    private String location;

    @Column(name = "revenue_tag_display")
    private String revenueTagDisplay;

    @Column(name = "service_specialty")
    private String serviceSpecialty;

    @Column(name = "service_type")
    private String serviceType;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "modified_at")
    private LocalDateTime modifiedAt;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "healthcare_service_pk") // Foreign key in `service_details` table
    private List<ServicePrice> priceTiers;
}
