package com.serenity.integration.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.serenity.integration.models.Observation;
@Repository
public interface ObservationRepository extends JpaRepository<Observation,Long>{

    
} 
