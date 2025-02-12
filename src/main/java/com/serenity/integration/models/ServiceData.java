package com.serenity.integration.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table
@Setter
@Getter
public class ServiceData {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
private long id;
@Column(columnDefinition="TEXT")
private String data;

@Column(columnDefinition="TEXT")
private String serviceName;
}
