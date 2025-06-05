package com.serenity.integration.service;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
@Service
@EnableScheduling
public class MigrationCron {
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

    @Autowired
    @Qualifier("legJdbcTemplate")
    JdbcTemplate legJdbcTemplate;

    @Autowired
    @Qualifier("serenityJdbcTemplate")
    JdbcTemplate serenityJdbcTemplate;

    Logger logger = LoggerFactory.getLogger(getClass());

    @Scheduled(cron = "0 57 9 * * *")
    public void migration() {
        String sql ="SELECT date(max(created_at)) from observations";
        String maxDate= serenityJdbcTemplate.queryForObject(sql, String.class);

        logger.info("Starting import for =>"+maxDate);
        long start = System.currentTimeMillis();


        LocalDateTime starts = LocalDateTime.now();
        LocalDate date = LocalDate.now().minusDays(1);
        System.err.println(date + " is ...");

        LocalDateTime ends = LocalDateTime.now();
        
        patientService.getLegacyAllPatients2(maxDate, date);
        practitionerService.getLegacyPractitioner(maxDate, date);
        visitService.getLegacyVisit(maxDate, date);
        encounterService.getLegacyEncounters(maxDate, date);
        allergyService.getLegacyAllergies(maxDate, date);
        referalService.getLegacyReferral(1000, maxDate, date);
        dService.getLegacyDiagnosis(1000, maxDate, date);
        diagnosisService.getLegacyDiagnosticReport(1000, maxDate, date);
        medicalRequestService.getLegacyRequest2(maxDate, date);
        chargeItemService.getLegacyChargeItem(2000, maxDate, date);
        invoiceService.getLegacyChargeItem(4000, maxDate, date);
        observationService.getLegacyObservations(5000, maxDate, date);
        noteService.getLegacyVisitNotesEncounters(5000, maxDate, date);
        noteService.getLegacyCarePlan(30000, maxDate, date);
        noteService.getLegacyEncounters(20000, maxDate, date);
        serviceRequestService.getLegacyRequest(maxDate, date);

        patientMigrationService.patientUpdate(maxDate, date.toString());
        practitionerService.updatePractitioners(maxDate, date.toString());
        visitMigration.updateVisit(maxDate, date.toString());
        encounterService.updateEncounter(maxDate, date.toString());
        allergyService.update(maxDate, date.toString());
        referalService.updateReferral(maxDate, date.toString());
        diagnosisService.updateDiagReports(maxDate, date.toString());
        dService.updateDianosis(maxDate, date.toString());
        medicalRequestService.updateMedicalRequest(maxDate, date.toString());
        chargeItemService.updateChargeItems(maxDate, date.toString());
        invoiceService.updateInvoices(maxDate, date.toString());
        noteService.updateNotes(maxDate, date.toString());
        observationService.updateObservations(maxDate, date.toString());
        serviceRequestService.updateServiceRequest(maxDate, date.toString());
 
        long stop = System.currentTimeMillis();
        System.err.println((stop - start) / 6000 + " time taken to finish");
        System.err.println(starts.toString() + "\t" +"\t" + ends.toString());

        logger.info("finishi.ng import");

    }

}
