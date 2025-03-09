package com.serenity.integration.models;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Getter;
import lombok.Setter;
@Setter
@Getter
public class RelatedPerson {

    private Long id;
    private String uuid;
    private String patientId;
    private String mobile;
    private String firstName;
    private String lastName;
    private boolean isActive;
    private String relationship;
    private String periodStart;
    private String periodEnd;
    private String createdAt;
    private String updatedAt;
    private String nationalMobileNumber;
    private String email;
    private String birthDate;
    private String gender;
    private String otherNames;
    private String lineAddress;
    private String placeOfWork;
}
