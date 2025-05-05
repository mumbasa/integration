package com.serenity.integration.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.serenity.integration.models.Encounter;
import com.serenity.integration.models.HealthCareServices;
import com.serenity.integration.models.HealthcareServiceResponse;
import com.serenity.integration.models.SensorData;
import com.serenity.integration.repository.EncounterNoteRepository;
import com.serenity.integration.repository.EncounterRepository;
import com.serenity.integration.repository.HealthCareRepository;
import com.serenity.integration.repository.NoiseRepo;
import com.serenity.integration.service.EncounterService;
import com.serenity.integration.service.SetupService;

@RestController
@CrossOrigin(origins = "*")
public class DataController {
@Autowired
NoiseRepo noiseRepo;
@Autowired
HealthCareRepository repository;

@Autowired
EncounterService encounterService;
@Autowired
SetupService setupService;
@GetMapping("/data")
public ResponseEntity<String> getData(int key){

    return ResponseEntity.ok(setupService.getFromObject(key));
}

@GetMapping("/services")
public ResponseEntity<String> getDatas(){
return ResponseEntity.ok(setupService.loadToProd());

    //return ResponseEntity.ok(setupService.addHealthService("161380e9-22d3-4627-a97f-0f918ce3e4a9", payload));
}



@GetMapping("/ids")
public ResponseEntity<List<Long>> allids(){
    return ResponseEntity.ok(repository.findAllId());
}


@GetMapping("/encounter/{id}")
public ResponseEntity<Encounter> allids(@PathVariable String id){
    return ResponseEntity.ok(encounterService.getEncounter(id));
}

}
