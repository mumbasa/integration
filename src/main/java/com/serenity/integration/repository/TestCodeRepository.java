package com.serenity.integration.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.serenity.integration.models.TestCode;
@Repository
public interface TestCodeRepository extends JpaRepository<TestCode,String> {

}
