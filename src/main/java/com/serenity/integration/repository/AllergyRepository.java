package com.serenity.integration.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.serenity.integration.models.AllergyIntolerance;
@Repository
public interface AllergyRepository extends JpaRepository<AllergyIntolerance,Long>{
@Query(value="SELECT * FROM allergy_intolerance  order by id OFFSET ?1 LIMIT ?2",nativeQuery=true)
List<AllergyIntolerance> findBatch(int offset ,int limit);


@Query(value="SELECT * FROM allergy_intolerance  where created_at::date > ?1 and created_at::date <=?2 order by id",nativeQuery=true)
List<AllergyIntolerance> getUpdates(LocalDate offset ,LocalDate now);
}
