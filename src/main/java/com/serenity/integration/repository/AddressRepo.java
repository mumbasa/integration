package com.serenity.integration.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.serenity.integration.models.Address;

@Repository
public interface AddressRepo extends JpaRepository<Address,Long> {
    List<Address> findByPatientId(String patientId);
    @Query(value = "SELECT * From address order by id",nativeQuery = true)
    List<Address> findByAll();

}
