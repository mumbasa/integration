package com.serenity.integration.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.serenity.integration.models.Observation;
@Repository
public interface ObservationRepository extends JpaRepository<Observation,Long>{
    @Query(value = "SELECT * from observations o  order by id OFFSET ?1 LIMIT ?2",nativeQuery = true)
List<Observation> findByPractitionerIdNotNull(int offset,int limit);

@Query(value = "SELECT count(*) from observations where patientid is null",nativeQuery = true)
long findCleanDaas();

@Query(value = "SELECT * from observations where encounterid =?1",nativeQuery = true)
List<Observation> findByEncounterId(String uuid);

@Query(value = "SELECT * from observations where createdat::date >?1 and createdat::date <=?2  order by id",nativeQuery = true)
List<Observation> findUpdates(LocalDate localDate, LocalDate localDate2);
    
} 
