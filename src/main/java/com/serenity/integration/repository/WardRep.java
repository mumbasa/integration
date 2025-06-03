package com.serenity.integration.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.serenity.integration.models.Ward;

@Repository
public interface WardRep extends JpaRepository<Ward,Long>{

}
