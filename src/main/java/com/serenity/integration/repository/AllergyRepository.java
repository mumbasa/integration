package com.serenity.integration.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.serenity.integration.models.AllergyIntolerance;
@Repository
public interface AllergyRepository extends JpaRepository<AllergyIntolerance,Long>{
@Query(value="SELECT * FROM allergy_intolerance where visit_id is not null and encounter_id in (SELECT uuid from encounter) order by id OFFSET ?1 LIMIT ?2",nativeQuery=true)
List<AllergyIntolerance> findBatch(int offset ,int limit);
}
