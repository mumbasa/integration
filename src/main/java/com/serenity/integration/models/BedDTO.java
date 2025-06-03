package com.serenity.integration.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;
import com.serenity.integration.setup.Bed;

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
@Table(name="beds")
@Entity
public class BedDTO {

@Id
@GeneratedValue(strategy = GenerationType.AUTO)


@JsonIgnore
private long pk;

   @SerializedName("room_uuid")
    private String roomUuid;

    @SerializedName("room_uuid")
    @Column(name = "ward_uuid")
    private String wardUuid;


    @JsonProperty("name")
    private String bed;

    @JsonIgnore
    private String room;

    @JsonIgnore
    private String ward;


    public BedDTO (Bed beds){
        bed =beds.getName();
        roomUuid=beds.getRoomUuid();

    }

    public BedDTO(){};
}
