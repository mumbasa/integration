package com.serenity.integration.models;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@Entity
@Table
@ToString
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonProperty("id")
    private Long id;
    
    @JsonProperty("patient_id")
    private String patientId;
    
    @JsonProperty("uuid")
    private String uuid;
    
    @JsonProperty("use")
    private String use;
    
    @JsonProperty("line")
    private String line;
    
    @JsonProperty("type")
    private String type;
    
    @JsonProperty("created_at")
    private String createdAt;
    
    @JsonProperty("updated_at")
    private String updatedAt;
    
    @JsonProperty("city")
    private String city;
    
    @JsonProperty("state")
    private String state;
    
    @JsonProperty("country")
    private String country;
    
    @JsonProperty("district")
    private String district;
    
    @JsonProperty("postal_code")
    private String postalCode;


}
