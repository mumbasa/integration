package com.serenity.integration.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.serenity.integration.models.Slot;
@Repository
public interface SlotRepo extends JpaRepository<Slot,Long> {

}
