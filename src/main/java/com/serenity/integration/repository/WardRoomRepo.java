package com.serenity.integration.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.serenity.integration.models.WardRoom;

@Repository
public interface WardRoomRepo extends JpaRepository<WardRoom,Long> {


}
