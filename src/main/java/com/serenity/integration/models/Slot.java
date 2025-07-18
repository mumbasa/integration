package com.serenity.integration.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Table
@Entity

public class Slot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long pk;
    private String id;
    private String plannedStart;
    private String plannedEnd;
}
