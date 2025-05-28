package com.serenity.integration.service;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;
import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;
import com.serenity.integration.models.BedDTO;
import com.serenity.integration.models.Healthcare;
import com.serenity.integration.models.HealthcareService;
import com.serenity.integration.models.HealthcareServiceResponse;
import com.serenity.integration.models.User;
import com.serenity.integration.models.V1Response;
import com.serenity.integration.models.WardRoom;
import com.serenity.integration.repository.BedRepo;
import com.serenity.integration.repository.ReportRepo;
import com.serenity.integration.repository.ServiceDataRepo;
import com.serenity.integration.repository.WardRoomRepo;
import com.serenity.integration.setup.Bed;
import com.serenity.integration.setup.HealthcareResponse;
import com.serenity.integration.setup.Location;
import com.serenity.integration.setup.Room;
import com.serenity.integration.setup.RoomDTO;
import com.serenity.integration.setup.BedResponse;
import com.serenity.integration.setup.RoomResponse;
import com.serenity.integration.setup.Ward;
import com.serenity.integration.setup.WardResponse;

@Service
public class WardSetupService {
    @Value("${access.token}")
    String token;

    @Autowired
    ServiceDataRepo serviceDataRepo;

    @Autowired
    @Qualifier(value = "legJdbcTemplate")
    JdbcTemplate legJdbcTemplate;

    @Autowired
    WardRoomRepo wardRoomRepo;

    @Autowired
    BedRepo bedRepo;

    @Autowired
    ReportRepo reportRepo;
    Logger LOGGER = LoggerFactory.getLogger("Ward Setup");

    @Autowired
    @Qualifier(value = "vectorJdbcTemplate")
    JdbcTemplate vectorJdbcTemplate;

    public V1Response getProdToken() {
        User user = new User();
        // user.setEmail("rejoicehormeku@gmail.com");
        // user.setPassword("5CYYkZhr92HwiPq");
        user.setEmail("chris@clearspacelabs.com");
        user.setPassword("charlehaschanged");
        Gson g = new Gson();
        String data = g.toJson(user);
        System.err.println(data);
        String url = "https://api.serenity.health/v1/providers/auth/login";
        System.err.println(url);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        headers.add("Authorization", "Bearer " + token);
        headers.add("PROVIDER-PORTAL-ID", "J9DG4WcX+eV<;5xuKtY[yp8g&Sa@~R%wUMnE_6^.jbH{=Lf)>d");
        HttpEntity<String> httpEntity = new HttpEntity<>(data, headers);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<V1Response> response = restTemplate.exchange(url, HttpMethod.POST, httpEntity,
                V1Response.class);
        System.err.println(response.getBody());
        return (response.getBody());
    }

    public Ward addWard(String orgId, String data, String tokens) {
        // LOGGER.info("Searching for "+stock.getFullName());
        String url = "https://staging.nyaho.serenity.health/v1/providers/" + orgId
                + "/administration/healthcareservices";
        System.err.println(url);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.add("Authorization", "Bearer " + tokens);
        headers.add("PROVIDER-PORTAL-ID", "j&4P8F<6+dF7/HASJ^hI92/6a&jdJOj*O\"[pHsh}t{o\"&7]\"}1~wg&SI%--,h{/");
        HttpEntity<String> httpEntity = new HttpEntity<>(data, headers);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<WardResponse> response = restTemplate.exchange(url, HttpMethod.POST, httpEntity,
                WardResponse.class);

        System.err.println(response.getBody());

        return response.getBody().getData();
    }

    public Ward addWard(String data) {
        // LOGGER.info("Searching for "+stock.getFullName());
        String url = "https://dev.api.serenity.health/v1/providers/161380e9-22d3-4627-a97f-0f918ce3e4a9"
                + "/administration/healthcareservices";
        System.err.println(url);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.add("Authorization", "Bearer " + getProdToken().getAccess());
        headers.add("PROVIDER-PORTAL-ID", "J9DG4WcX+eV<;5xuKtY[yp8g&Sa@~R%wUMnE_6^.jbH{=Lf)>d");
        HttpEntity<String> httpEntity = new HttpEntity<>(data, headers);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<WardResponse> response = restTemplate.exchange(url, HttpMethod.POST, httpEntity,
                WardResponse.class);

        System.err.println(response.getBody());

        return response.getBody().getData();
    }

    public List<Room> addRoom(String orgId, List<RoomDTO> rooms, String tokens) {
        // LOGGER.info("Searching for "+stock.getFullName());
        String url = "https://staging.nyaho.serenity.health/v1/providers/" + orgId
                + "/rooms";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.add("Authorization", "Bearer " + tokens);
        headers.add("PROVIDER-PORTAL-ID", "j&4P8F<6+dF7/HASJ^hI92/6a&jdJOj*O\"[pHsh}t{o\"&7]\"}1~wg&SI%--,h{/");
        HttpEntity<List<RoomDTO>> httpEntity = new HttpEntity<>(rooms, headers);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<RoomResponse> response = restTemplate.exchange(url, HttpMethod.POST, httpEntity,
                RoomResponse.class);

        System.err.println(response.getBody());

        return response.getBody().getResults();
    }

    public List<Room> addRooms(String rooms) {
        // LOGGER.info("Searching for "+stock.getFullName());
        String url = "https://api.staging.serenity.health/v1/providers/161380e9-22d3-4627-a97f-0f918ce3e4a9/rooms";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.add("Authorization", "Bearer " + getProdToken().getAccess());
        headers.add("PROVIDER-PORTAL-ID", "J9DG4WcX+eV<;5xuKtY[yp8g&Sa@~R%wUMnE_6^.jbH{=Lf)>d");
        HttpEntity<String> httpEntity = new HttpEntity<>(rooms, headers);

        System.err.println(httpEntity.getBody());
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<RoomResponse> response = restTemplate.exchange(url, HttpMethod.POST, httpEntity,
                RoomResponse.class);


        return response.getBody().getResults();
    }

    public List<Bed> addBed(BedDTO data) {
        // LOGGER.info("Searching for "+stock.getFullName());
        String url = "https://api.staging.serenity.health/v1/providers/161380e9-22d3-4627-a97f-0f918ce3e4a9/beds";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.add("Authorization", "Bearer " + getProdToken().getAccess());
        headers.add("PROVIDER-PORTAL-ID", "J9DG4WcX+eV<;5xuKtY[yp8g&Sa@~R%wUMnE_6^.jbH{=Lf)>d");
        HttpEntity<BedDTO> httpEntity = new HttpEntity<>(data, headers);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<BedResponse> response = restTemplate.exchange(url, HttpMethod.POST, httpEntity,
                BedResponse.class);

        System.err.println(response.getBody());

        return response.getBody().getResults();
    }

    public Map<String, String> getWards(String orgId) {
        List<HealthcareService> cares = new ArrayList<>();
        // LOGGER.info("Searching for "+stock.getFullName());
        String url = "https://staging.nyaho.serenity.health/v1/providers/" + orgId
                + "/administration/healthcareservices";
        System.err.println(url);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.add("Authorization", "Bearer " + getToken().getAccess());
        headers.add("PROVIDER-PORTAL-ID", "J9DG4WcX+eV<;5xuKtY[yp8g&Sa@~R%wUMnE_6^.jbH{=Lf)>d");
        HttpEntity<String> httpEntity = new HttpEntity<>(headers);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<HealthcareResponse> response = restTemplate.exchange(url, HttpMethod.GET, httpEntity,
                HealthcareResponse.class);

        for (HealthcareService c : response.getBody().getData()) {
            try {
                if (!c.getWardType().isBlank()) {
                    cares.add(c);

                }
            } catch (Exception e) {

            }
        }
        return cares.stream().collect(Collectors.toMap(e -> e.getHealthcareServiceName(), e -> e.getUuid()));
    }

    public Map<String, String> getWardsData() {
        Map<String, String> wards = new HashMap<>();

        String sql = """
                SELECT  id, lower(name) as name FROM healthcare_service hsc where service_class = 'Hospitalization'
                """;
        SqlRowSet set = legJdbcTemplate.queryForRowSet(sql);

        while (set.next()) {
            wards.put(set.getString("name"), set.getString("id"));

        }

        return wards;

    }

    public Map<String, String> getBedsInDb() {

        Map<String, String> beds = new HashMap<>();

        String sql = """
                select lower(name) as name ,room_name from hospitalization_beds hb
                               """;
        SqlRowSet set = legJdbcTemplate.queryForRowSet(sql);

        while (set.next()) {
            beds.put(set.getString("name"), set.getString("room_name"));

        }

        return beds;

    }

    public Map<String, String> getRooms() {
        Map<String, String> rooms = new HashMap<>();

        String sql = """
                select lower(name) as name,uuid from hospitalization_rooms hr
                """;
        SqlRowSet set = legJdbcTemplate.queryForRowSet(sql);

        while (set.next()) {
            rooms.put(set.getString("name"), set.getString("uuid"));
        }
        return rooms;
    }

    public Map<String, String> getRoom() {
        // LOGGER.info("Searching for "+stock.getFullName());
        String url = "https://api.serenity.health/v1/providers/161380e9-22d3-4627-a97f-0f918ce3e4a9/rooms?page_size=100";
        System.err.println(url);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.add("Authorization", "Bearer " + getProdToken().getAccess());
        headers.add("provider-portal-id", "J9DG4WcX+eV<;5xuKtY[yp8g&Sa@~R%wUMnE_6^.jbH{=Lf)>d");
        HttpEntity<String> httpEntity = new HttpEntity<>(headers);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<RoomResponse> response = restTemplate.exchange(url, HttpMethod.GET, httpEntity,
                RoomResponse.class);
        // System.err.println(response.getBody());
        return response.getBody().getResults().stream().collect(Collectors.toMap(e -> e.getName(), e -> e.getUuid()));
    }

    public V1Response getToken() {
        User user = new User();
        user.setEmail("chris@clearspacelabs.com");
        user.setPassword("charlehaschanged");
        Gson g = new Gson();
        String data = g.toJson(user);
        System.err.println(data);
        String url = "https://api.staging.serenity.health/v1/providers/auth/login";
        System.err.println(url);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        headers.add("Authorization", "Bearer " + token);
        headers.add("PROVIDER-PORTAL-ID", "J9DG4WcX+eV<;5xuKtY[yp8g&Sa@~R%wUMnE_6^.jbH{=Lf)>d");
        HttpEntity<String> httpEntity = new HttpEntity<>(data, headers);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<V1Response> response = restTemplate.exchange(url, HttpMethod.POST, httpEntity,
                V1Response.class);
        System.err.println(response.getBody().getAccess());
        return (response.getBody());
    }

    public void getWards() {

        Map<String, String> wards = getWardsData();
        Map<String, String> rooms = getRooms();
        Map<String, String> beds = getBedsInDb();

        System.err.println(wards.size() + " Start from db");

        String sql = "SELECT ward from wards";
        List<String> wardsInvec = vectorJdbcTemplate.queryForList(sql, String.class);
        System.err.println(wardsInvec.size() + " Start");
        List<String> wardsInvecNew = new ArrayList<>();

        for (String war : wardsInvec) {
            if (!wards.keySet().contains(war.toLowerCase())) {

                wardsInvecNew.add(war);

            }
        }
            List<WardRoom> wardRoomss = wardRoomRepo.findAll();

            for (WardRoom wr : wardRoomss) {

                if (!rooms.keySet().contains(wr.getRoom().toLowerCase())) {
                    wr.setWardId(wards.get(wr.getWard().toLowerCase()));
                    wardRoomRepo.save(wr);
                }

            }
List<WardRoom> newRooms = wardRoomss.stream().filter(e -> e.getWardId()!=null).toList();
for (WardRoom f : newRooms){
  
    String json = String.format("""
            {"name":"%s","ward_uuid":"%s"}
            """,f.getRoom(),f.getWardId());


      

     //   addRooms(json);
}

           // rooms = getRooms();

            List<BedDTO> vecBedDTOs = bedRepo.findAll();
            for (BedDTO vedsBedDTO : vecBedDTOs) {
                if (!beds.keySet().contains(vedsBedDTO.getBed().toLowerCase())) {
                    vedsBedDTO.setRoomUuid(rooms.get(vedsBedDTO.getRoom().toLowerCase()));
                    vedsBedDTO.setWardUuid(wards.get(vedsBedDTO.getWard().toLowerCase()));
                    bedRepo.save(vedsBedDTO);
                  System.err.println(vedsBedDTO.getRoomUuid() + "--------uuid room\t" + vedsBedDTO.getRoom());
                }

            }

        }

    
}
