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
    private String uuid;
    
    private String issuedDate;
    private String patientId;
    private String performerName;
    private String performerId;
    
    @CreationTimestamp
    private String createdAt;
    
    @ElementCollection
    private List<String> results;
    
    @ElementCollection
    private List<String> media;
    
    private String basedOnId;
    private String visitId;
    private String serviceProviderId;
    private String conclusion;
    private String encounterId;
}
