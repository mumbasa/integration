package com.serenity.integration.models;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.*;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;


@Entity
@Table(name = "diagnostic_report")
@NoArgsConstructor
@AllArgsConstructor
public class DiagnosticReport {

     @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID uuid;
    
    private ZonedDateTime issuedDate;
    private UUID patientId;
    private String performerName;
    private UUID performerId;
    
    @CreationTimestamp
    private ZonedDateTime createdAt;
    
    @ElementCollection
    private List<String> results;
    
    @ElementCollection
    private List<String> media;
    
    private UUID basedOnId;
    private String visitId;
    private String serviceProviderId;
    private String conclusion;
}
