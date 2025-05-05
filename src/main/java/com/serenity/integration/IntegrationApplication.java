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

	//patientMigrationService.getPatientsThreads();
	//visitMigration.getVisitThreads(3000);
	//encounterService.encounterLegacythread();
	//allergyService.migrateAllergyThread(1000);
	//referalService.migrateReferalThread(2000);
	//noteService.noteThread(5000);
	
	//dService.getLegacyDiagnosis(2000);;
	//
	//diagnosisService.migrateDiagReportThread(5000);
	medicalRequestService.saveMedicalRequestThread();
	serviceRequestService.getLegacyRequest(2000);
	serviceRequestService.migrateThread(3000);

	dService.getLegacyDiagnosis(3000);
   dService.migrationThread(5000);
	//serviceRequestService.migrate(1000);
	//medicalRequestService.saveMedicalRequestThread();

	//chargeItemService.chargeThread(1000);;

	//observationService.migrateObservationThread(1000);
	

	
	//diagnosisService.getLegacyDiagnosticReport(5000);
	//medicalRequestService.getLegacyRequest2();
	//observationService.getLegacyObservations(3000);
	//setupService.migrate("161380e9-22d3-4627-a97f-0f918ce3e4a9");
	//setupService.setHealthcareIds();

	

LocalDateTime ends = LocalDateTime.now();


long stop = System.currentTimeMillis();
		System.err.println((stop-start)/6000+" time taken to finish");
		System.err.println(starts.toString() +"\t"+mid.toString() +"\t"+ends.toString());
	
		logger.info("finishi.ng import");

	}

	

}
