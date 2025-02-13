package com.serenity.integration.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.serenity.integration.models.AllergyIntolerance;

public interface AllergyRepository extends JpaRepository<AllergyIntolerance,Long>{

}
