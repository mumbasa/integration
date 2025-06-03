package com.serenity.integration.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Table
@Entity
@Setter
@Getter
public class Ward {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long pk;
private String ward;
@Column(name = "Ward_id")
private String wardId;
}
