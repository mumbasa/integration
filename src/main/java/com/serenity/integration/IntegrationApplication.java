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
import com.serenity.integration.service.WardSetupService;

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
	WardSetupService wardSetupService;



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
	LocalDate date= LocalDate.now().minusDays(1);
	System.err.println(date +" is ...");

//



LocalDateTime ends = LocalDateTime.now();
/* patientService.getLegacyAllPatients2( "2025-05-27", date);
practitionerService.getLegacyPractitioner("2025-05-27",date);
visitService.getLegacyVisit("2025-05-27", date);
encounterService.getLegacyEncounters("2025-05-27", date);
allergyService.getLegacyAllergies("2025-05-27", date);
referalService.getLegacyReferral(1000,"2025-05-27", date);
dService.getLegacyDiagnosis(1000, "2025-05-27",date);
diagnosisService.getLegacyDiagnosticReport(1000,"2025-05-27", date);
medicalRequestService.getLegacyRequest2("2025-05-27",date);
chargeItemService.getLegacyChargeItem(500, "2025-05-27",date);
invoiceService.getLegacyChargeItem(1000,"2025-05-27", date);
observationService.getLegacyObservations(1000,"2025-05-27", date);
noteService.getLegacyVisitNotesEncounters(1000,"2025-05-27",date);
noteService.getLegacyCarePlan(30000,"2025-05-27",date);
noteService.getLegacyEncounters(20000,"2025-05-27",date) ;	 
serviceRequestService.getLegacyRequest("2025-05-27", date); */

//patientMigrationService.patientUpdate("2025-05-27",date.toString());
 //practitionerService.updatePractitioners("2025-05-27",date.toString());
visitMigration.updateVisit("2025-05-27",date.toString());
/*encounterService.updateEncounter("2025-05-27",date.toString());
allergyService.update("2025-05-27",date.toString());
referalService.updateReferral("2025-05-27",date.toString());
diagnosisService.updateDiagReports("2025-05-27",date.toString());
dService.updateDianosis("2025-05-27",date.toString());
medicalRequestService.updateMedicalRequest("2025-05-27",date.toString());
chargeItemService.updateChargeItems("2025-05-27",date.toString());
invoiceService.updateInvoices("2025-05-27",date.toString());
noteService.updateNotes("2025-05-27",date.toString());
observationService.updateObservations("2025-05-27",date.toString());
serviceRequestService.updateServiceRequest("2025-05-27",date.toString()); */
long stop = System.currentTimeMillis();
		System.err.println((stop-start)/6000+" time taken to finish");
		System.err.println(starts.toString() +"\t"+mid.toString() +"\t"+ends.toString());
	
		logger.info("finishi.ng import");

	}

	public void setup(LocalDate date){


		patientService.getLegacyAllPatients2(10000, 10000, date);	
		practitionerService.getLegacyPractitioner(date);
		visitService.getLegacyVisit(20000, date);
		encounterService.getLegacyEncounters(10000,date);
		allergyService.getLegacyAllergies(1000,date);
		referalService.getLegacyReferral(1000,date);
		serviceRequestService.getLegacyRequest(3000,date);
		dService.getLegacyDiagnosis(30000,date);
		diagnosisService.getLegacyDiagnosticReport(30000,date);
		medicalRequestService.getLegacyRequest2(date);
		noteService.getLegacyVisitNotesEncounters(30000,date);
		noteService.getLegacyCarePlan(30000,date);
		noteService.getLegacyEncounters(20000,date) ;	 
		chargeItemService.getLegacyChargeItem(30000,date);
		invoiceService.getLegacyChargeItem(30000,date);
		observationService.getLegacyObservations(50000,date);  
		
		patientMigrationService.migratePatientThread(3000);
		practitionerService.migrateDoctors();
		visitMigration.getVisitThreads(5000);
		encounterService.encounterLegacythread(1000);
		allergyService.migrateAllergyThread(1000);
		referalService.migrateReferalThread(1000);
		chargeItemService.chargeThread(5000);
		noteService.noteThread(4000);
		serviceRequestService.migrateThread(3000);
		invoiceService.migrateinvoiceThread(3000);
		dService.migrationThread(5000);
		diagnosisService.migrateDiagReportThread(5000);
		medicalRequestService.saveMedicalRequestThread(); 
		diagnosisService.migrateDiagReportThread(5000);
		observationService.migrateObservationThread(5000);  

	}

}
