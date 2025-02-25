package com.serenity.integration.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.serenity.integration.models.SensorData;

@Repository
public interface NoiseRepo extends JpaRepository<SensorData,Long>{

}
