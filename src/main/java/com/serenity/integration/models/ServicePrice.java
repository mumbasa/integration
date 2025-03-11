package com.serenity.integration.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "service_prices")
@Setter
@Getter
public class ServicePrice {


     @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty("id") // Maps JSON key to this field
    private Long id;

    @Column(name = "uuid", unique = true, nullable = false)
    @JsonProperty("uuid")
    private UUID uuid;

    @Column(name = "display")
    @JsonProperty("display")
    private String display;

    @Column(name = "charge", precision = 10, scale = 2)
    @JsonProperty("charge")
    private BigDecimal charge;

    @Column(name = "currency", length = 3)
    @JsonProperty("currency")
    private String currency;

    @Column(name = "priority")
    @JsonProperty("priority")
    private String priority;

    @Column(name = "description" ,columnDefinition="TEXT")
    @JsonProperty("description")
    private String description;

    @Column(name = "healthcare_service_name", nullable = false)
    @JsonProperty("healthcare_service_name")
    @JsonIgnore
    private String healthcareServiceName;

    @Column(name = "turnaround_time_value")
    @JsonProperty("turnaround_time_value")
    private Integer turnaroundTimeValue;

    @Column(name = "turnaround_time_unit")
    @JsonProperty("turnaround_time_unit")
    private String turnaroundTimeUnit;

    @Column(name = "healthcare_service_pk")
    @JsonProperty("healthcare_service_pk")
    @JsonIgnore
    private Long healthcareServiceId;

    @Column(name = "created_at", updatable = false)
    @JsonProperty("created_at")
    private String createdAt;

    @Column(name = "modified_at")
    @JsonProperty("modified_at")
    @JsonIgnore
    private String modifiedAt;




}
