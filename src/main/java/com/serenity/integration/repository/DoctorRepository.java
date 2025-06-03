package com.serenity.integration.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.serenity.integration.models.Doctors;

public interface DoctorRepository extends JpaRepository<Doctors,Long>{
public Optional<Doctors> findByExternalId(String id);
public List<Doctors> NationalMobileNumber(String mobile);
@Query(value = "select * from doctors where externalsystem ='his'",nativeQuery = true)
public List<Doctors> findHisPractitioners();

@Query(value = "select * from doctors where externalsystem ='opd'",nativeQuery = true)
public List<Doctors> findOPDPractitioners();
@Query(value = "select * from doctors where externalsystem ='his' offset ? LIMIT 10",nativeQuery = true)
public List<Doctors> getfirst100k(int startIndex);

@Query(value = "select * from doctors where createdat::date > ?1 and createdat::date <=?2",nativeQuery = true)
public List<Doctors> getUpdates(LocalDate current,LocalDate now);
}
