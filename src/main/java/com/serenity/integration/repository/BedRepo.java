package com.serenity.integration.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.serenity.integration.models.BedDTO;
@Repository
public interface BedRepo extends JpaRepository<BedDTO,Long> {

}
