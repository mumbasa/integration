package com.serenity.integration.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.serenity.integration.models.SensorData;
import com.serenity.integration.repository.NoiseRepo;

@RestController
@CrossOrigin(origins = "*")
public class DataController {
@Autowired
NoiseRepo noiseRepo;
@GetMapping("/data")
public ResponseEntity<List<SensorData>> getData(){
    return ResponseEntity.ok(noiseRepo.findAll());
}
}
