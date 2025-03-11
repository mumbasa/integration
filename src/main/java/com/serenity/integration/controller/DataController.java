package com.serenity.integration.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.serenity.integration.models.HealthCareServices;
import com.serenity.integration.models.HealthcareServiceResponse;
import com.serenity.integration.models.SensorData;
import com.serenity.integration.repository.HealthCareRepository;
import com.serenity.integration.repository.NoiseRepo;
import com.serenity.integration.service.SetupService;

@RestController
@CrossOrigin(origins = "*")
public class DataController {
@Autowired
NoiseRepo noiseRepo;
@Autowired
HealthCareRepository repository;
@Autowired
SetupService setupService;
@GetMapping("/data")
public ResponseEntity<List<SensorData>> getData(){
    return ResponseEntity.ok(noiseRepo.findAll());
}

@GetMapping("/services")
public ResponseEntity<String> getDatas(){
setupService.getFromObject();
return ResponseEntity.ok("dome");
    //return ResponseEntity.ok(setupService.addHealthService("161380e9-22d3-4627-a97f-0f918ce3e4a9", payload));
}
@GetMapping("/all")
public ResponseEntity<List<HealthCareServices>> all(){
    return ResponseEntity.ok(repository.findAll());
}
@GetMapping("/ids")
public ResponseEntity<List<Long>> allids(){
    return ResponseEntity.ok(repository.findAllId());
}

}
