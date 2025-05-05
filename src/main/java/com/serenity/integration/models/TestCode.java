package com.serenity.integration.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "test_code")
@Setter
@Getter
@ToString
public class TestCode {
    @Id
    @Column(name = "test_id")
    private String testId;

    @Column(name = "test_order_name",columnDefinition = "TEXT")
    private String testOrderName;

    @Column(name = "alias")
    private String alias;

    @Column(name = "order_loinc_code",columnDefinition = "TEXT")
    private String orderLoincCode;

    @Column(name = "loinc_attributes",columnDefinition = "TEXT")
    private String loincAttributes;

    @Column(name = "method_name",columnDefinition = "TEXT")
    private String methodName;

}
