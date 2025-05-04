package com.serenity.integration;

import java.io.UnsupportedEncodingException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;


import com.serenity.integration.service.AllergyService;
import com.serenity.integration.service.ChargeItemService;
import com.serenity.integration.service.DiagnosisService;
import com.serenity.integration.service.DiagnosticReportService;
import com.serenity.integration.service.EncounterService;
import com.serenity.integration.service.InvoiceService;
import com.serenity.integration.service.MedicalRequestService;
import com.serenity.integration.service.NoteService;
import com.serenity.integration.service.ObservationService;
import com.serenity.integration.service.PatientMigrationService;
import com.serenity.integration.service.PatientService;
import com.serenity.integration.service.PractitionerService;
import com.serenity.integration.service.ReferalService;
import com.serenity.integration.service.ServiceRequestService;
import com.serenity.integration.service.SetupService;
import com.serenity.integration.service.VisitMigration;
import com.serenity.integration.service.VisitService;

import jakarta.annotation.PostConstruct;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.serenity.integration.repository")
public class IntegrationApplication {

	@Autowired
	PatientService service;

	@Autowired
	NoteService noteService;

	@Autowired
	PractitionerService practitionerService;

	@Autowired
	VisitService visitService;

	@Autowired
	VisitMigration visitMigration;

	@Autowired
	PatientMigrationService v;

	@Autowired
	SetupService setupService;
	@Autowired
	PatientMigrationService patientMigrationService;
	@Autowired
	EncounterService encounterService;
	@Autowired
	PatientService patientService;

	@Autowired
	PatientMigrationService migrationService;

	@Autowired
	ReferalService referalService;

	@Autowired
	DiagnosticReportService diagnosisService;

	@Autowired
	MedicalRequestService medicalRequestService;

	@Autowired
	DiagnosisService dService;

	@Autowired
	ServiceRequestService serviceRequestService;

	@Autowired
	ObservationService observationService;

	@Autowired
	AllergyService allergyService;

	@Autowired
	ChargeItemService chargeItemService;

@Autowired
InvoiceService invoiceService;
	
	Logger logger = LoggerFactory.getLogger(getClass());

	public static void main(String[] args) {
		SpringApplication.run(IntegrationApplication.class, args);
	}

	@PostConstruct
	public void coke() {
		logger.info("Starting import");
		long start = System.currentTimeMillis();
	
	//patientService.getLegacyAllPatients2(1000, 1000);
	//practitionerService.getLegacyPractitioner();
	LocalDateTime starts = LocalDateTime.now();

	//visitService.getLegacyVisit(2000);
	//encounterService.getLegacyEncounters(2000);
	LocalDateTime mid =(LocalDateTime.now());
	//allergyService.getLegacyAllergies(2000);
	//referalService.getLegacyReferral(2000);
	//noteService.getLegacyCarePlan(2000);
	//noteService.getLegacyVisitNotesEncounters(2000);
	//noteService.getLegacyEncounters(2000);
	//dService.getLegacyDiagnosis(2000);;
	diagnosisService.getLegacyDiagnosticReport(5000);
	//observationService.getLegacyObservations(3000);
	
	
	

	

	LocalDateTime ends = LocalDateTime.now();


long stop = System.currentTimeMillis();
		System.err.println((stop-start)/6000+" time taken to finish");
		System.err.println(starts.toString() +"\t"+mid.toString() +"\t"+ends.toString());
//setupService.sethealthcareServicePayload();
	

		logger.info("finishi.ng import");

	}

	

}
