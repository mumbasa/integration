package com.serenity.integration.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Table(name = "noise_data")
@Entity
public class SensorData {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
private long id;
private  long timestamp;
@Column(name = "noise_level")
private double data;
}
