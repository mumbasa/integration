package com.serenity.integration.service;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Service;

import com.serenity.integration.models.Encounter;
import com.serenity.integration.models.EncounterNote;
import com.serenity.integration.models.Observation;
import com.serenity.integration.models.PatientData;
import com.serenity.integration.repository.DoctorRepository;
import com.serenity.integration.repository.EncounterNoteRepository;
import com.serenity.integration.repository.EncounterRepository;
import com.serenity.integration.repository.ObservationRepository;
import com.serenity.integration.repository.PatientRepository;
import com.serenity.integration.repository.VisitRepository;

@Service
public class EncounterService {

    @Autowired
    EncounterRepository encounterRepository;

    @Autowired
    EncounterNoteRepository encounterNoteRepository;

    @Autowired
    @Qualifier("serenityJdbcTemplate")
    JdbcTemplate serenityJdbcTemplate;

    @Autowired
    @Qualifier(value = "hisJdbcTemplate")
    JdbcTemplate hisJdbcTemplate;

    @Autowired
    @Qualifier(value = "legJdbcTemplate")
    JdbcTemplate legJdbcTemplate;

    @Autowired
    @Qualifier(value = "vectorJdbcTemplate")
    JdbcTemplate vectorJdbcTemplate;

    @Autowired
    PatientRepository patientRepository;

    @Autowired
    DoctorRepository doctorRepository;

    @Autowired
    ObservationRepository observationRepository;

    @Autowired
    VisitRepository visitRepository;
    Logger logger = LoggerFactory.getLogger(this.getClass().getCanonicalName());

    public void generateOPDEncounter() {
        int rows = visitRepository.countByEncounterClass("ambulatory");
        logger.info(rows + " number of rows");
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        try {
            List<Future<Integer>> futures = executorService.invokeAll(submitTask2(100, 1000));
            for (Future<Integer> future : futures) {
                System.out.println("future.get = " + future.get());
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        executorService.shutdown();
        System.err.println("patiend count is " + rows);

    }

    public Set<Callable<Integer>> submitTask2(int batchSize, long rows) {

        Set<Callable<Integer>> callables = new HashSet<>();
        int totalSize = (int) rows;
        int batches = (totalSize + batchSize - 1) / batchSize; // Ceiling division

        for (int i = 0; i < batches; i++) {
            final int batchNumber = i; // For use in lambda

            callables.add(() -> {
                int startIndex = batchNumber * batchSize;
                int endIndex = Math.min(startIndex + batchSize, totalSize);
                logger.debug("Processing batch {}/{}, indices [{}]",
                        batchNumber + 1, batches, startIndex);
                List<Encounter> notes = encounterRepository.getfirst100k(startIndex);

            
                    saveEncounters(notes);
               

                return 1;
            });
        }

        return callables;
    }


    public Set<Callable<Integer>> submitLegacyTask2(int batchSize, long rows) {

        Set<Callable<Integer>> callables = new HashSet<>();
        int totalSize = (int) rows;
        int batches = (totalSize + batchSize - 1) / batchSize; // Ceiling division

        for (int i = 0; i < batches; i++) {
            final int batchNumber = i; // For use in lambda

            callables.add(() -> {
                int startIndex = batchNumber * batchSize;
                int endIndex = Math.min(startIndex + batchSize, totalSize);
                logger.debug("Processing batch {}/{}, indices [{}]",
                        batchNumber + 1, batches, startIndex);
                List<Encounter> notes = encounterRepository.getfirstOPD100k(startIndex,batchSize);

                try {
                    saveEncounters(notes);
                } catch (Exception e) {
                    // TODO: handle exception
                    e.printStackTrace();
                    logger.info("error adding note");

                }

                return 1;
            });
        }

        return callables;
    }

    public void insertNote(EncounterNote note) {
        System.err.println("inserting note");
        String sql = "INSERT INTO public.encounter_notes\n" + //
                "(created_at, pk, encounter_id, patient_id," +
                " visit_id, \"uuid\", note, is_formatted, note_type," +
                "is_edited, is_recalled, practitioner_id, encounter_date, practitioner_name," +
                "practitioner_role_type, encounter_type, patient_mr_number)\n" + //
                "VALUES(to_timestamp(?, 'YYYY-MM-DD HH24:MI:SS'), nextval('encounter_notes_pk_seq'::regclass), uuid(?), (select uuid from patients p where external_id =?), uuid(?), uuid(?), ?, false, ?, false, false, uuid(?), to_timestamp(?, 'YYYY-MM-DD HH24:MI:SS'), ?, ?, ?, ?)";

        serenityJdbcTemplate.update(sql, new PreparedStatementSetter() {

            @Override
            public void setValues(PreparedStatement ps) throws SQLException {
                ps.setString(1, note.getEncounterDate().replaceAll("T|Z", " ").strip());
                ps.setString(2, note.getUuid().split(":")[1]);
                ps.setString(3, note.getPatientMrNumber());
                ps.setString(4, note.getUpdatedAt());
                ps.setString(5, UUID.randomUUID().toString());
                ps.setString(6, note.getNote());
                ps.setString(7, note.getNoteType());
                ps.setString(8, note.getPractitionerRoleType().equalsIgnoreCase("unknown") ? null
                        : note.getPractitionerRoleType());
                ps.setString(9, note.getEncounterDate().replaceAll("T|Z", " ").strip());
                ps.setString(10, note.getPractitionerName());
                ps.setString(11, "Doctor");
                ps.setString(12, note.getEncounterType());
                ps.setString(13, note.getPatientMrNumber());

            }

        });

    }

    public int encountersData(int size) {
        List<Encounter> encounters = new ArrayList<>();
        String sql = "select * from encounter e join visits v on date(e.created_at)=date(v.createdat) and e.patient_id=v.patientid and  e.created_at !='0000-00-00' and assigned_to_id is not null OFFSET ? LIMIT 1000";
        SqlRowSet set = vectorJdbcTemplate.queryForRowSet(sql, size);
        while (set.next()) {
            Encounter encounter = new Encounter();
            encounter.setCreatedAt(set.getString(5));
            encounter.setAssignedToId(set.getString(2));
            encounter.setAssignedToName(set.getString("assigned_to_name"));
            encounter.setDisplay(set.getString(8));
            encounter.setEncounterClass(set.getString("encounterclass"));
            encounter.setExternalId(set.getString(11));
            encounter.setExternalSystem(set.getString(12));
            encounter.setLocationId(set.getString(13));
            encounter.setLocationName(set.getString(14));
            encounter.setPatientBirthDate(set.getString("patient_birth_date"));
            encounter.setPatientFullName(set.getString("patient_full_name"));
            encounter.setPatientGender(set.getString("patient_gender"));
            encounter.setPatientMobile(set.getString("patient_mobile"));
            encounter.setPatientId(set.getString("patient_id"));
            encounter.setPatientMrNumber(set.getString("patient_mr_number"));
            encounter.setServiceProviderId("161380e9-22d3-4627-a97f-0f918ce3e4a9");
            encounter.setServiceProviderName("Nyaho Medical Center");
            encounter.setStatus("finished");
            encounter.setVisitId(set.getString(53));
            encounters.add(encounter);
        }

        saveEncounters(encounters);

        return size;

    }


    public void updateEncounter(String current,String now){
List<Encounter> encounters = encounterRepository.getUpdates(LocalDate.parse(current),LocalDate.parse(now));
System.err.println("Encounters size=>"+encounters.size());
saveEncounters(encounters);
    }
    public void saveEncounters(List<Encounter> notes) {
        String sql = "INSERT INTO public.encounters " + //
                "(created_at,  id,  uuid, encounter_class, status, " +
                "external_id, external_system,  service_provider_id, patient_mr_number," +
                "patient_id, patient_full_name, patient_mobile, patient_birth_date, patient_gender," +
                "encounter_type, practitioner_name, practitioner_id, service_provider_name,  visit_id," +
                "has_prescriptions,has_service_requests,slot_id,service_type_id,service_type_name,started_at,location_id,location_name,updated_at,ended_at,display)" + //
                "VALUES(to_timestamp(?, 'YYYY-MM-DD HH24:MI:SS'),  ?,  uuid(?),?,?,"
                +
                "?, ?,uuid(?),?,"+
                "uuid(?), ?,?,to_date(?, 'YYYY-MM-DD'),?," +
                "?,?,uuid(?),?, uuid(?),"+
                "?,?,uuid(?),uuid(?),?,to_timestamp(?, 'YYYY-MM-DD HH24:MI:SS'),uuid(?),?,?::timestamp,?::timestamp,?)";

        serenityJdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {

            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
             
                ps.setString(1, notes.get(i).getCreatedAt());
                ps.setLong(2, notes.get(i).getId());
                ps.setString(3, notes.get(i).getUuid());
                ps.setString(4, notes.get(i).getEncounterClass());
                ps.setString(5, "finished");
                ps.setString(6, notes.get(i).getExternalId());
                ps.setString(7, notes.get(i).getExternalSystem());
                ps.setString(8, "161380e9-22d3-4627-a97f-0f918ce3e4a9");
                ps.setString(9, notes.get(i).getPatientMrNumber());
                ps.setString(10, notes.get(i).getPatientId());
                ps.setString(11, notes.get(i).getPatientFullName());
                ps.setString(12, notes.get(i).getPatientMobile() == null ? "" : notes.get(i).getPatientMobile());
                ps.setString(13, notes.get(i).getPatientBirthDate()== null ? "":notes.get(i).getPatientBirthDate());
                ps.setString(14, notes.get(i).getPatientGender()==null?"":notes.get(i).getPatientGender());

                ps.setString(15, notes.get(i).getEncounterType());
                ps.setString(16, notes.get(i).getAssignedToName());
                ps.setString(17, notes.get(i).getAssignedToId());
                ps.setString(18, "Nyaho Medical Centre");
                ps.setString(19, notes.get(i).getVisitId());
                ps.setBoolean(20, false);
                ps.setBoolean(21, false);
                ps.setString(22, notes.get(i).getSlotId());
                ps.setString(23,notes.get(i).getServiceTypeId());
                ps.setString(24, notes.get(i).getServiceTypeName());
                ps.setString(25, notes.get(i).getStartedAt());
                ps.setString(26,notes.get(i).getLocationId());
                ps.setString(27,notes.get(i).getLocationName());
                ps.setString(28,notes.get(i).getUpdatedAt());
                ps.setString(29,notes.get(i).getEndedAt());
                ps.setString(30,notes.get(i).getDisplay()==null?"":notes.get(i).getDisplay());


            }

            @Override
            public int getBatchSize() {
                // TODO Auto-generated method
                return notes.size();
            }

        });
    }

    public void saveEncounter(Encounter notes) {
        String sql = "INSERT INTO public.encounters " + //
                "(created_at,  id,  uuid, encounter_class, status, " +
                "display,  external_id, external_system,  service_provider_id, patient_mr_number," +
                "patient_id, patient_full_name, patient_mobile, patient_birth_date, patient_gender," +
                "encounter_type, practitioner_name, practitioner_id, service_provider_name,  visit_id," +
                "has_prescriptions,has_service_requests)" + //
                "VALUES(to_timestamp(?, 'YYYY-MM-DD HH24:MI:SS'),  nextval('encounters_id_seq'::regclass),  uuid(?),?,?,"
                +
                "'',?, ?,uuid(?),?,uuid(?), ?," +
                "(SELECT mobile from patients where mr_number=?),to_date(?, 'YYYY-MM-DD'),?,?,?,uuid(?),?, uuid(?),?,?)";

        serenityJdbcTemplate.update(sql, new PreparedStatementSetter() {

            @Override
            public void setValues(PreparedStatement ps) throws SQLException {
                // TODO Auto-generated method stub
                ps.setString(1, notes.getCreatedAt().replaceAll("T|Z", " ").strip());
                ps.setString(2, notes.getUuid());
                ps.setString(3, "ambulatory");
                ps.setString(4, "finished");
                ps.setString(5, UUID.randomUUID().toString());
                ps.setString(6, "his");
                ps.setString(7, "161380e9-22d3-4627-a97f-0f918ce3e4a9");
                ps.setString(8, notes.getPatientMrNumber());
                ps.setString(9, notes.getPatientId());
                ps.setString(10, notes.getPatientFullName());
                ps.setString(11, notes.getPatientMrNumber());
                ps.setString(12, notes.getPatientBirthDate());
                ps.setString(13, notes.getPatientGender());

                ps.setString(14, notes.getEncounterClass());
                ps.setString(15, notes.getAssignedToName());
                ps.setString(16, notes.getAssignedToId());

                ps.setString(17, "Nyaho Medical Centre");
                ps.setString(18, notes.getVisitId());
                ps.setBoolean(19, false);
                ps.setBoolean(20, false);

            }

        });

    }

    public void saveEncounter() {
        String sql = "INSERT INTO public.encounters " + //
                "(created_at,  id,  uuid, encounter_class, status, " +
                "display,  external_id, external_system,  service_provider_id, patient_mr_number," +
                "patient_id, patient_full_name, patient_mobile, patient_birth_date, patient_gender," +
                "encounter_type, practitioner_name, practitioner_id, service_provider_name,  visit_id," +
                "has_prescriptions,has_service_requests)" + //
                "VALUES(to_timestamp(?, 'YYYY-MM-DD HH24:MI:SS'),  nextval('encounters_id_seq'::regclass),  uuid(?),?,?,"
                +
                "'',?, ?,uuid(?),?,uuid(?), ?," +
                "(SELECT mobile from patients where mr_number=?),to_date(?, 'YYYY-MM-DD'),?,?,?,uuid(?),?, uuid(?),?,?)";

        serenityJdbcTemplate.update(sql, new PreparedStatementSetter() {
            Encounter notes = encounterRepository.getfirst100k().get(0);

            @Override
            public void setValues(PreparedStatement ps) throws SQLException {
                // TODO Auto-generated method stub
                ps.setString(1, notes.getCreatedAt().replaceAll("T|Z", " ").strip());
                ps.setString(2, notes.getUuid());
                ps.setString(3, "ambulatory");
                ps.setString(4, "finished");
                ps.setString(5, UUID.randomUUID().toString());
                ps.setString(6, "his");
                ps.setString(7, "161380e9-22d3-4627-a97f-0f918ce3e4a9");
                ps.setString(8, notes.getPatientMrNumber());
                ps.setString(9, notes.getPatientId());
                ps.setString(10, notes.getPatientFullName());
                ps.setString(11, notes.getPatientMrNumber());
                ps.setString(12, notes.getPatientBirthDate());
                ps.setString(13, notes.getPatientGender());

                ps.setString(14, notes.getEncounterClass());
                ps.setString(15, notes.getAssignedToName());
                ps.setString(16, notes.getAssignedToId());

                ps.setString(17, "Nyaho Medical Centre");
                ps.setString(18, notes.getVisitId());
                ps.setBoolean(19, false);
                ps.setBoolean(20, false);

            }

        });

    }

    public void encounterthread() {
        logger.info("kooooooooooooooading");
        int dataSize = 1878637;
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        try {
            List<Future<Integer>> futures = executorService.invokeAll(submitTask2(1000, dataSize));
            for (Future<Integer> future : futures) {
                System.out.println("future.get = " + future.get());
            }
        } catch (InterruptedException | ExecutionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        executorService.shutdown();
        System.err.println("patiend count is " + dataSize);

    }



    public void encounterLegacythread(int batchSize) {

     
        logger.info("kooooooooooooooading");
        long dataSize = encounterRepository.count();
       
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        try {
            List<Future<Integer>> futures = executorService.invokeAll(submitLegacyTask2(batchSize, dataSize));
            for (Future<Integer> future : futures) {
                System.out.println("future.get = " + future.get());
            }
        } catch (InterruptedException | ExecutionException e) {
           
            e.printStackTrace();
        }

        executorService.shutdown();
        System.err.println("patiend count is " + dataSize);

    }

    public void encounterOPDthread() {
        logger.info("kooooooooooooooading");
        long dataSize = encounterRepository.count();
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        try {
            List<Future<Integer>> futures = executorService.invokeAll(submitTask2(1000, dataSize));
            for (Future<Integer> future : futures) {
                System.out.println("future.get = " + future.get());
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        executorService.shutdown(); 
        System.err.println("patiend count is " + dataSize);

    }


    public void getter(int batchSize){
        List<Encounter> encountersD = new ArrayList<Encounter>();
        Set<String> data = new HashSet<String>();

        List<Encounter> encounters =encounterRepository.getfirst100();
        for(Encounter encounter : encounters){
            if(!data.contains(encounter.getUuid())){
                data.add(encounter.getUuid());
                encountersD.add(encounter);
            }
            
               
        }
System.err.println(encountersD.size() +" ------------------");
        long totalSize = encountersD.size();
        long batches = (totalSize + batchSize - 1) / batchSize; //

         for (int i = 0; i < batches; i++) {
            List<Encounter> serviceRequests = encountersD.subList(i*batchSize,(i*batchSize)+batchSize);
            try{
            saveEncounters(serviceRequests);
            }catch (Exception e){
            
                e.printStackTrace();}
         }



    }

    public void getLegacyEncounters(String current, LocalDate date) {

        Map<String, String> locationMap = new HashMap<>();
        locationMap.put("6b46da79-5613-4827-91ae-f46aaf65d4da", "Accra Central (Octagon)");
        locationMap.put("23f59485-8518-4f4e-9146-d061dfe58175", "Airport Primary Care");
        locationMap.put("b60c55f5-63dd-4ba2-9fe9-8192f57aaed2", "Tema Primary Care");
        locationMap.put("a79ae42b-03b7-4f5e-ac1a-cd42729c0b04", "Takoradi Primary Care");
        locationMap.put("29e22113-9d7b-46a6-a857-810ca3567ca7", "Airport Main");
        locationMap.put("2550dc16-3f64-4cee-b808-6c13b255d159", "Ward - Airport Main");
        
                             String sqlRow = "SELECT count(*) from encounter where created_at::date >?::date and created_at::date <= ?";
                        long rows = legJdbcTemplate.queryForObject(sqlRow, new Object[]{current,date}, Long.class);
                logger.info("New Encounters =>"+rows);
                       
                       
                List<Encounter> encounters = new ArrayList<>();
                String sql = """
                SELECT e.id as "uuid", e.created_at, e.is_deleted,e.modified_at, e.status, encounter_class,  "type", priority, start_time, end_time, length,appointment_id, charge_item_id, part_of_id, p."uuid" as patient_id, price_tier_id, service_provider_id, service_type_id, slot_id, visit_id, primary_location_id, charge_item_status, hs.name  as service_type_name, slot_practitioner_name, status_comment, title,history_of_presenting_illness_author_id, history_of_presenting_illness_editor_uuids, history_of_presenting_illness_editors_display, has_prescriptions, hospitalization_id,  p.birth_date, p.email, p.first_name, p.gender, p.last_name, p.mobile,p.other_names
        FROM encounter e left join patient p on p.id =e.patient_id left join healthcare_service hs on hs.id=e.service_type_id   
        
        where e.created_at::date > ?::date and e.created_at::date <= ?  order by  E.created_at ;        
        
                        """;
                SqlRowSet set = legJdbcTemplate.queryForRowSet(sql,current,date);
                while (set.next()) {
                    //System.err.println(set.getString("mr_number")+"-----------------");
                 ;
                    Encounter encounter = new Encounter();
                    encounter.setUuid(set.getString("uuid"));
                    encounter.setEncounterType(set.getString("type"));
                    encounter.setExternalId(set.getString("uuid"));
                    encounter.setCreatedAt(set.getString("created_at"));
                    encounter.setStartedAt(set.getString("start_time"));
                    encounter.setEndedAt(set.getString("end_time"));
                    encounter.setUpdatedAt(set.getString("modified_at"));
                    encounter.setEncounterClass(set.getString("encounter_class"));
                    encounter.setPriority(set.getString("priority"));
                    encounter.setPatientId(set.getString("patient_id"));
                     encounter.setExternalSystem("opd");
                    encounter.setDeleted(set.getBoolean("is_deleted"));
                    encounter.setEncounterType("outpatient-consultation");
                    encounter.setPrescription(false);
                    encounter.setSlotId(set.getString("slot_id"));
                    encounter.setServiceTypeId(set.getString("service_type_id"));
                    encounter.setServiceTypeName(set.getString("service_type_name"));
                    encounter.setDisplay(set.getString("title"));
                    encounter.setLocationId(set.getString("primary_location_id")==null?"23f59485-8518-4f4e-9146-d061dfe58175":set.getString("primary_location_id"));
                   try{
                    encounter.setLocationName(locationMap.get(encounter.getLocationId()));
                   }catch(NullPointerException e){
                  //  encounter.setLocationName("Airport Main");
                   // encounter.setLocationId("23f59485-8518-4f4e-9146-d061dfe58175");
        
                   }
                    encounter.setVisitId(set.getString("visit_id"));
                    encounter.setServiceProviderId("161380e9-22d3-4627-a97f-0f918ce3e4a9");
                    encounter.setServiceProviderName("Nyaho Medical Centre");
                    encounter.setStatus(set.getString("status"));
                    encounter.setPrescription(set.getBoolean("has_prescriptions"));
                    encounters.add(encounter);
        
                  
                }
                  logger.info("adding encounter");
        encounterRepository.saveAll(encounters);
                        
        
             cleanEncounter();
               populateWithVisits();
            }
        



    public void getLegacyEncounters(int batchSize,LocalDate date) {

Map<String, String> locationMap = new HashMap<>();
locationMap.put("6b46da79-5613-4827-91ae-f46aaf65d4da", "Accra Central (Octagon)");
locationMap.put("23f59485-8518-4f4e-9146-d061dfe58175", "Airport Primary Care");
locationMap.put("b60c55f5-63dd-4ba2-9fe9-8192f57aaed2", "Tema Primary Care");
locationMap.put("a79ae42b-03b7-4f5e-ac1a-cd42729c0b04", "Takoradi Primary Care");
locationMap.put("29e22113-9d7b-46a6-a857-810ca3567ca7", "Airport Main");
locationMap.put("2550dc16-3f64-4cee-b808-6c13b255d159", "Ward - Airport Main");

                     String sqlRow = "SELECT count(*) from encounter where created_at::date <= ?";
                long rows = legJdbcTemplate.queryForObject(sqlRow, new Object[]{date}, Long.class);
       
                long totalSize = rows;
                long batches = (totalSize + batchSize - 1) / batchSize; // Ceiling division
        
                for (int i = 0; i < batches; i++) {
        
                    int startIndex = i * batchSize;
        List<Encounter> encounters = new ArrayList<>();
        String sql = """
        SELECT e.id as "uuid", e.created_at, e.is_deleted,e.modified_at, e.status, encounter_class,  "type", priority, start_time, end_time, length,appointment_id, charge_item_id, part_of_id, p."uuid" as patient_id, price_tier_id, service_provider_id, service_type_id, slot_id, visit_id, primary_location_id, charge_item_status, hs.name  as service_type_name, slot_practitioner_name, status_comment, title,history_of_presenting_illness_author_id, history_of_presenting_illness_editor_uuids, history_of_presenting_illness_editors_display, has_prescriptions, hospitalization_id,  p.birth_date, p.email, p.first_name, p.gender, p.last_name, p.mobile,p.other_names
FROM encounter e left join patient p on p.id =e.patient_id left join healthcare_service hs on hs.id=e.service_type_id   where e.created_at::date <= ? order by  E.created_at offset ? limit ? ;        

                """;
        SqlRowSet set = legJdbcTemplate.queryForRowSet(sql,date,startIndex,batchSize);
        while (set.next()) {
            //System.err.println(set.getString("mr_number")+"-----------------");
         ;
            Encounter encounter = new Encounter();
            encounter.setUuid(set.getString("uuid"));
            encounter.setEncounterType(set.getString("type"));
            encounter.setExternalId(set.getString("uuid"));
            encounter.setCreatedAt(set.getString("created_at"));
            encounter.setStartedAt(set.getString("start_time"));
            encounter.setEndedAt(set.getString("end_time"));
            encounter.setUpdatedAt(set.getString("modified_at"));
            encounter.setEncounterClass(set.getString("encounter_class"));
            encounter.setPriority(set.getString("priority"));
            encounter.setPatientId(set.getString("patient_id"));
             encounter.setExternalSystem("opd");
            encounter.setDeleted(set.getBoolean("is_deleted"));
            encounter.setEncounterType("outpatient-consultation");
            encounter.setPrescription(false);
            encounter.setSlotId(set.getString("slot_id"));
            encounter.setServiceTypeId(set.getString("service_type_id"));
            encounter.setServiceTypeName(set.getString("service_type_name"));
            encounter.setDisplay(set.getString("title"));
            encounter.setLocationId(set.getString("primary_location_id")==null?"23f59485-8518-4f4e-9146-d061dfe58175":set.getString("primary_location_id"));
           try{
            encounter.setLocationName(locationMap.get(encounter.getLocationId()));
           }catch(NullPointerException e){
          //  encounter.setLocationName("Airport Main");
           // encounter.setLocationId("23f59485-8518-4f4e-9146-d061dfe58175");

           }
            encounter.setVisitId(set.getString("visit_id"));
            encounter.setServiceProviderId("161380e9-22d3-4627-a97f-0f918ce3e4a9");
            encounter.setServiceProviderName("Nyaho Medical Centre");
            encounter.setStatus(set.getString("status"));
            encounter.setPrescription(set.getBoolean("has_prescriptions"));
            encounters.add(encounter);

          
        }
          logger.info("adding encounter");
encounterRepository.saveAll(encounters);
                }

       cleanEncounter();
       populateWithVisits();
    }

    public Set<Callable<Integer>> submitNote(List<Encounter> notes, int batchSize) {

        Set<Callable<Integer>> callables = new HashSet<>();
        int totalSize = notes.size();
        int batches = (totalSize + batchSize - 1) / batchSize; // Ceiling division

        for (int i = 0; i < batches; i++) {
            final int batchNumber = i; // For use in lambda

            callables.add(() -> {
                int startIndex = batchNumber * batchSize;
                int endIndex = Math.min(startIndex + batchSize, totalSize);
                logger.debug("Processing batch {}/{}, indices [{}]",
                        batchNumber + 1, batches, startIndex);
                encounterRepository.saveAll(notes.subList(startIndex, endIndex));
                return 1;
            });
        }

        return callables;
    }

    public void populateWithVisits() {
        String sql = """
                        update encounter m
                set assigned_to_id = e.practitionerid ,
                assigned_to_name =e.assignedtoname
                from visits e
                where e.externalid = m.visit_id
                and m.assigned_to_id is null and m.external_system ='opd'

                        """;
        vectorJdbcTemplate.update(sql);
        sql= """
                update encounter m
               set visit_id =e.uuid
                from visits e
                where e.externalid = m.visit_id
                 and m.external_system ='opd'
                """;
                vectorJdbcTemplate.update(sql);

        sql ="""
                update encounter m
                set patient_gender = e.gender          
                from patient_information e
                where e.uuid = m.patient_id and m.external_system ='opd'
                """;
                vectorJdbcTemplate.update(sql);
        sql="""
                update encounter 
set patient_birth_date =p.birthdate ,patient_full_name=concat(firstname,' ',p.lastname) ,patient_gender =p.gender ,patient_mobile =p.mobile ,patient_mr_number = p.mrnumber 
from patient_information p
where p."uuid" = encounter.patient_id 
                """;
                vectorJdbcTemplate.update(sql);


    }

    public void removeDupes() {
        // TODO Auto-generated method stub
String sql ="""
        DELETE FROM encounter T1
USING   encounter T2
WHERE   T1.ID < T2.ID  
AND T1."uuid"  = T2."uuid";

        """;  
    vectorJdbcTemplate.update(sql);
    }



    public Encounter getEncounter(String uuid){
        Encounter e=   encounterRepository.findByUuid(uuid);
        e.setObservations(observationRepository.findByEncounterId(uuid));
        return e;
    }

    public void cleanEncounter(){
              String sql ="""
                update encounter set assigned_to_id =v.assignedtoid ,assigned_to_name=v.assignedtoname 
from visits v  where v."uuid" =visit_id::uuid
                """;
               
            vectorJdbcTemplate.update(sql);
            sql ="""
                    update encounter set patient_birth_date ='' where patient_birth_date is null;

                    """;
                    vectorJdbcTemplate.update(sql);

                    sql="""
                        update encounter 
        set patient_birth_date =p.birthdate ,patient_full_name=concat(firstname,' ',p.lastname) ,patient_gender =p.gender ,patient_mobile =p.mobile ,patient_mr_number = p.mrnumber 
        from patient_information p
        where p."uuid" = encounter.patient_id and patient_mr_number is null
                        """;
                        vectorJdbcTemplate.update(sql);
    }

}
