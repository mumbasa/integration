package com.serenity.integration.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.serenity.integration.models.Encounter;
@Repository
public interface EncounterRepository extends JpaRepository<Encounter,Long>{
@Query(value = "select * from encounter e where patient_id =(select \"uuid\" from patient_information pi2 where externalid=?1) and date(created_at)=date(?2) and assigned_to_id=(select serenityuuid from doctors d where externalid=?3) LIMIT 1" 
        ,nativeQuery = true)
    Optional<Encounter> findEcounterByPatientDateDoctor(String patient,String date,String doctor);

    @Query(value = "SELECT * FROM encounter order by id OFFSET ?  LIMIT 1000 ",nativeQuery = true)
    List<Encounter> getfirst100k(int offset);

    @Query(value = "select * from encounter e where patient_id in (select \"uuid\" from patient_information )  and visit_id::uuid IN (SELECT \"uuid\" from visits) order by id asc",nativeQuery = true)
    List<Encounter> getfirst100();

    @Query(value = "select * from encounter order by id OFFSET ?1  LIMIT ?2 ",nativeQuery = true)
    List<Encounter> getfirstOPD100k(int offset,int limit);

    @Query(value = "select * from encounter e   OFFSET 0  LIMIT 100000",nativeQuery = true)
    List<Encounter> getfirst100k();

    @Query(value = "select * from encounter e  where created_at::date >?1 and created_at::date <=?2 order by id",nativeQuery = true)
    List<Encounter> getUpdates(LocalDate current,LocalDate now);

    List<Encounter> findByExternalIdAndAssignedToId(String externalId,String patient);
   
    @Query(value = "select * from encounter e where uuid=?1 LIMIT 1",nativeQuery = true)
    Encounter findByUuid(String uuid);

    
    @Query(value = "select count(*) from encounter e  where external_system='opd'",nativeQuery = true)
    int getOOPCount();
}
