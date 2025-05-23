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
	
	LocalDateTime mid =(LocalDateTime.now());

	LocalDateTime starts = LocalDateTime.now();
/* patientService.getLegacyAllPatients2(5000, 5000);
practitionerService.getLegacyPractitioner();
visitService.getLegacyVisit(10000);
encounterService.getLegacyEncounters(10000);
allergyService.getLegacyAllergies(2000);
referalService.getLegacyReferral(5000);
//serviceRequestService.getLegacyRequest(10000);
dService.getLegacyDiagnosis(10000);
*/
/* diagnosisService.getLegacyDiagnosticReport(10000);
medicalRequestService.getLegacyRequest2();
noteService.getLegacyVisitNotesEncounters(10000);
noteService.getLegacyCarePlan(10000);
noteService.getLegacyEncounters(5000) */; 

/* 	
chargeItemService.getLegacyChargeItem(5000);
invoiceService.getLegacyChargeItem(10000);
observationService.getLegacyObservations(10000);  */


//patientMigrationService.migratePatientThread(1000);
//practitionerService.migrateDoctors();
//visitMigration.getVisitThreads(1000);

//encounterService.getLegacyEncounters(10000);
//encounterService.encounterLegacythread();
//allergyService.migrateAllergyThread(1000);
//referalService.getLegacyReferral(1000);
//referalService.migrateReferalThread(1000);
//diagnosisService.migrateDiagReportThread(5000);

observationService.getLegacyObservations(50000); 
observationService.migrateObservationThread(5000);
//invoiceService.migrateinvoiceThread(10);
//noteService.noteThread(4000);
//medicalRequestService.saveMedicalRequestThread();
//dService.migrationThread(5000);
//chargeItemService.chargeThread(5000);
//invoiceService.migrateinvoiceThread(10000);
//serviceRequestService.migrateThread(2000);
LocalDateTime ends = LocalDateTime.now();


long stop = System.currentTimeMillis();
		System.err.println((stop-start)/6000+" time taken to finish");
		System.err.println(starts.toString() +"\t"+mid.toString() +"\t"+ends.toString());
	
		logger.info("finishi.ng import");

	}

	

}
