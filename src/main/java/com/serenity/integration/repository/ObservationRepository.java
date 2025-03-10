package com.serenity.integration.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.serenity.integration.models.Observation;
@Repository
public interface ObservationRepository extends JpaRepository<Observation,Long>{
    @Query(value = "SELECT * from observations where patientid is not null order by id OFFSET ?1 LIMIT ?2",nativeQuery = true)
List<Observation> findByPractitionerIdNotNull(int offset,int limit);

@Query(value = "SELECT count(*) from observations",nativeQuery = true)
long findCleanDatas();
    
} 
