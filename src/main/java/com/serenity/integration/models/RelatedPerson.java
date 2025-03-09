package com.serenity.integration.models;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.annotation.Generated;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
@Setter
@Getter
@Table
@Entity
public class RelatedPerson {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
   
    @JsonProperty("patient_id")
    private String patientId;
    
    @JsonProperty("uuid")
    private String uuid;
    
    @JsonProperty("mobile")
    private String mobile;
    
    @JsonProperty("first_name")
    private String firstName;
    
    @JsonProperty("last_name")
    private String lastName;
    
    @JsonProperty("is_active")
    private boolean isActive;
    
    @JsonProperty("relationship")
    private String relationship;
    
    @JsonProperty("period_start")
    private String periodStart;
    
    @JsonProperty("period_end")
    private String periodEnd;
    
    @JsonProperty("created_at")
    private String createdAt;
    
    @JsonProperty("updated_at")
    private String updatedAt;
    
    @JsonProperty("national_mobile_number")
    private String nationalMobileNumber;
    
    @JsonProperty("email")
    private String email;
    
    @JsonProperty("birth_date")
    private String birthDate;
    
    @JsonProperty("gender")
    private String gender;
    
    @JsonProperty("other_names")
    private String otherNames;
    
    @JsonProperty("line_address")
    private String lineAddress;
    
    @JsonProperty("place_of_work")
    private String placeOfWork;
}
