package com.serenity.integration.models;

import javax.sql.rowset.serial.SerialArray;

import org.apache.commons.csv.CSVRecord;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.gson.annotations.SerializedName;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Setter
@Getter
@Entity
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown=true)
@Table(name = "patient_information")
public class PatientData {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private long id;


  private String  uuid;
  
  @Column(nullable = true)
  @SerializedName("external_id")
  private String externalId;

  @SerializedName("external_system")
  private String externalSystem;

  @SerializedName("payer_id")
  private String payerId;

  @SerializedName("policy_number")
  private String policyNumber;

  @SerializedName("payer_name")
  private String payerName;

  private String photo;


  @Column(nullable = true)

  @SerializedName("created_at")
  private String createdAt;
  @Column(columnDefinition = "TEXT")
  private String mobile;
  @Column(nullable = true)
  @SerializedName("national_mobile_number")

  private String nationalMobileNumber;
  @SerializedName("first_name")
  private String firstName;
  @Column(nullable = true)
private boolean deleted;
@Column(nullable = true)

private String updatedAt;
@SerializedName("last_name")
  private String lastName;
  @SerializedName("full_name")

  private String fullName;

  @Column(nullable = true)
  private String title;
  @Column(nullable = true)

  private String occupation;
  @Column(nullable = true)
  private String employer;
  @Column(nullable = true)

  private String email;
  @SerializedName("birth_date")

  private String birthDate;
  @Column(nullable = true)
  @SerializedName("marital_status")

  private String maritalStatus;

  private String gender;
  @Column(nullable = true)

  private String nationality;

  @Column(nullable = true)

  private String otherNames;
  @Column(nullable = true,columnDefinition = "TEXT")
  private String address;
  @Column(nullable = true,columnDefinition = "TEXT")
  private String relatedPerson;

  @SerializedName("mr_number")
  private String mrNumber;

  @Column(nullable = true)
  @SerializedName("blood_type")

  private String bloodType;
  @Column(nullable = true)
  @SerializedName("passport_number")
  
  private boolean isDeceased;
  private boolean isActive;
  private boolean isMultipleBirth;
  private int multipleBirthInteger;

  private String passportNumber;

  @Column(nullable = true)

  private String birthTime;

  @Column(name = "managing_organization_id")
  @SerializedName("managing_organization_id")
  private String managingOrganizationId;
  @Column(name = "managing_organization")
  @SerializedName("managing_organization")
  private String managingOrganization;
  private String religiousAffiliation;
  private String paymentMethod;
  private String previousPatientAccountUuid;
  private String paymentCurrency;
private double contributionValue;
private String contributionType;
  public PatientData() {

  }

  public PatientData(CSVRecord record) {

    mrNumber = record.get("patient_id");
    lastName = record.get("plastname");
    firstName = record.get("pfirstname");
    mobile = record.get("mobile").replaceAll("-", "");
    email = record.get("email").isBlank()?null:record.get("email");
    birthDate = record.get("dob");
    // nationalId=record.get("countryid");
    gender = record.get("gender");
    externalSystem = "his";
    nationalMobileNumber = record.get("phone");
    fullName = record.get("pname");
    title = record.get("title");
    occupation = record.get("occupation");
    employer = record.get("employer");
    bloodType = record.get("bloodgroup");
    maritalStatus = record.get("maritalstatus");
    nationality = record.get("country");
    passportNumber = record.get("passport_no");
    // active=Boolean.parseBoolean(record.get("active"));
    birthTime = record.get("timeofbirth");
    // managingOrganizationId=record.get("membership");

  }


}
