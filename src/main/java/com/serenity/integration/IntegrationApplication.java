package com.serenity.integration;

import java.io.UnsupportedEncodingException;
import java.time.LocalDate;
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
	
//	dService.getLegacyDiagnosis();
	//noteService.getLegacyCarePlan(1000);
	//noteService.getLegacyVisitNotesEncounters(1000);
//noteService.getLegacyEncounters(1000);
		diagnosisService.getLegacyDiagnosticReport(1000);
		referalService.getLegacyReferral(1000);
		observationService.getLegacyObservations(1000);
		serviceRequestService.getLegacyRequest(1000);
		//observationService.migrateObservationThread(10);
		
		//patientService.getLegacyAllPatients2(1000,1000);
		//practitionerService.getLegacyPractitioner();
		//visitService.getLegacyVisit(1000);
		//chargeItemService.getLegacyChargeItem(1000);
		//patientMigrationService.getPatientsThreads();
		//encounterService.getLegacyEncounters(1000);
		//observationService.getLegacyObservations(1000);
		//visitService.getLegacyVisit(1000);
		//visitService.updateVisits();
	

	
	
		//encounterService.getLegacyEncounters(1000);
		//noteService.getLegacyEncounters(1000);
		//noteService.getLegacyVisitNotesEncounters(1000);
		//patientService.getLegacyAllPatients(1000);
		long stop = System.currentTimeMillis();
		System.err.println((stop-start)/60000+" time taken to finish");

		//encounterService.encounterLegacythread();
	    //noteService.moveVisitNote();
		//diagnosisService.getNursingDiagnosis();
		// diagnosisService.getProvisionalDiagnosis();;
		
		//diagnosisService.getLegacyDiagnosis();

//		diagnosisService.migrationThread();

		logger.info("finishi.ng import");

	}

	

}
