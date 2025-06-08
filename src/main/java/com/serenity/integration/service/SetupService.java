package com.serenity.integration.service;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
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
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.serenity.integration.models.AvailableTime;
import com.serenity.integration.models.Category;
import com.serenity.integration.models.CustomerGroup;
import com.serenity.integration.models.CustomerGroupResponse;
import com.serenity.integration.models.HealthCareServices;
import com.serenity.integration.models.Healthcare;
import com.serenity.integration.models.HealthcareService;
import com.serenity.integration.models.HealthcareServiceResponse;
import com.serenity.integration.models.PriceTier;
import com.serenity.integration.models.Report;
import com.serenity.integration.models.ServiceData;
import com.serenity.integration.models.ServicePrice;
import com.serenity.integration.models.ServicePriceResponse;
import com.serenity.integration.models.ServicePricing;
import com.serenity.integration.models.ServiceType;
import com.serenity.integration.models.Specialty;
import com.serenity.integration.models.TestCode;
import com.serenity.integration.models.User;
import com.serenity.integration.models.V1Response;
import com.serenity.integration.repository.HealthCareRepository;
import com.serenity.integration.repository.ReportRepo;
import com.serenity.integration.repository.ServiceDataRepo;
import com.serenity.integration.repository.ServicePriceRepo;
import com.serenity.integration.repository.TestCodeRepository;
import com.serenity.integration.setup.Location;

@Service
public class SetupService {
    @Value("${access.token}")
    String token;

    @Autowired
    ServiceDataRepo serviceDataRepo;

    @Autowired
    HealthCareRepository repository;

    @Autowired
    ServicePriceRepo servicePriceRepo;

    @Autowired
    @Qualifier(value = "legJdbcTemplate")
    JdbcTemplate legJdbcTemplate;

    @Autowired
    TestCodeRepository testCodeRepository;

    @Autowired
    @Qualifier("serenityJdbcTemplate")
    JdbcTemplate serenityJdbcTemplate;

    @Autowired
    ReportRepo reportRepo;
    Logger LOGGER = LoggerFactory.getLogger("ME");

    public static void main(String[] args) {
        Integer[] data = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 15, 16, 17, 18, 19 };
        List<Integer> das = Arrays.asList(data);
        int as = (das.size() / 3);
        System.err.println(as);
        int rounds = 0;
        for (int a = 0; a <= as; a++) {
            if (rounds < as) {
                System.err.println(true);
                System.err.println(das.subList(a * 3, (a * 3) + 3));
            } else {

                System.err.println(false);

                System.err.println(das.subList(rounds * 3, das.size()));

            }
            rounds++;

        }

    }

    public ResponseEntity<CustomerGroupResponse> migrate(String organisationId) {
        ResponseEntity<CustomerGroupResponse> response = null;
        String[] paymentTypes = {
                "A.G.C. ANGLOGOLD GHANA FOREIGN",
                "A.G.C. ANGLOGOLD GHANA LOCAL",
                "ACACIA ABSA TPA MEDICALS",
                "ACACIA BARCLAYS TPA (Local)",
                "Acacia EY (Ernst & Young) TPA (Foreign)",
                "Acacia EY (Ernst & Young) TPA (Local)",
                "Acacia Health Insurance foreign",
                "Acacia Health Insurance guinness Gh Brew Ltd Local",
                "Acacia Health Insurance local",
                "ACACIA HEALTH INSURANCE(Foreign)",
                "ACACIA HEALTH INSURANCE(Guinness Gh. Brew ltd) Local",
                "ACACIA HEALTH INSURANCE(Local)",
                "Acacia Nestle (Foreign)",
                "Acacia Nestle (Local)",
                "Acacia Nestle Local",
                "Acacia US Embassy (TPA) (Foreign)",
                "Acacia US Embassy (TPA) (Local)",
                "ACE MEDICAL INSURANCE(FOREIGN)",
                "ACE MEDICAL INSURANCE(LOCAL)",
                "AETNA INT FOREIGN",
                "AFRI BAND TELECOMMUNICATION NETWORK LTD.",
                "African Continental Free Trade Area Secretariat AfCFTA",
                "AFRICAN UNDERGROUND MINING SERVICE (LOCAL)",
                "AITEO GHANA LIMITED (FOREIGN)",
                "AKPO JAMES TORGBUI AFEDE XIV LOCAL",
                "AKYEA FAMILY",
                "Alliance International Medical Services Foreign",
                "Allianz Worldwide Care foreign",
                "Allianz Worldwide Care local",
                "Apex Bost Tpa",
                "Apex Mutual Healthcare local",
                "APEX MUTUAL HEALTHCARE(FOREIGN)",
                "APEX MUTUAL HEALTHCARE(Local)",
                "ARTHUR MATUSEVIC FAMILY",
                "Asante Gold Bibiani Ltd Foreign",
                "Asante Gold Bibiani Ltd Local",
                "Asante Gold Chirano Ltd Foreign",
                "Axa Assistance Foreign",
                "Axa Assistance Local",
                "Aya Engineering Limited",
                "AZUMAH RESOURCES",
                "BLUE CROSS BLUE SHIELD FOREIGN",
                "BLUE CROSS BLUE SHIELD LOCAL",
                "Bupa Ins Services foreign",
                "Bupa Ins Services local",
                "Cal bank Limited",
                "Chemquest Ghana Ltd",
                "CHRISTMAN ESTATES",
                "Cigna International foreign",
                "Cigna International local",
                "CISI FOREIGN",
                "CISI LOCAL",
                "CMA CGM GHANA LTD FOREIGN",
                "CMA CGM GHANA LTD LOCAL",
                "Comfort Berkoh",
                "Corporate",
                "Corporate Foreigner",
                "Cosmopolitan Health Insurance local",
                "COSMOPOLITAN HEALTH INSURANCE(FOREIGN)",
                "COSMOPOLITAN HEALTH INSURANCE(Local)",
                "Dashen Opticians",
                "Databank Financial Services Ltd local",
                "DIAGNOSTIC CENTRE LIMITED (Walk-Ins)",
                "Dosh Health Insurance Ltd",
                "Dr Victoria Lokko Family And Friends",
                "Dwamena Family",
                "E process International foreign",
                "E process International local",
                "Early Power Ltd",
                "Ecobank foreign",
                "Ecobank local",
                "ECOBANK(FOREIGN)",
                "ECOBANK(Local)",
                "EDC ECOBANK",
                "ENSIGN COLLEGE OF PUBLIC HEALTH",
                "Equity Health Insurance (FOREIGN)",
                "Equity Health Insurance (LOCAL)",
                "Equity Health Insurance Local",
                "Euro centre Cape Town foreign",
                "Euro centre Cape Town local",
                "European Union ghana local",
                "Foreign cash",
                "Foreign Credit",
                "GAB HEALTH INSURANCE (Local)",
                "Gab Health Insurance Local",
                "Geodrill Ghana foreign",
                "Geodrill Ghana Local",
                "Getfund Foreign",
                "Getfund Local",
                "Ghana Mine Workers Union foreign",
                "Ghana Mine Workers Union local",
                "Ghana National Gas Company foreign",
                "Ghana National Gas Company local",
                "Ghana Oil Company",
                "Glico Health Care local",
                "GLICO HEALTH CARE(Local)",
                "GLICO TPA (FOREIGN)",
                "GLICO TPA (LOCAL)",
                "Glico Tpa Foreign",
                "Glico Tpa Local",
                "GLOBAL GOLD COMPANY",
                "Gmc Services Ltd local",
                "Gold Fields Ghana Tarkwa local",
                "GOLDEN STAR LTD WASSA LOCAL",
                "Goodwill Health Limited",
                "GPMS (PCR)",
                "GRID COMPANY LIMITED",
                "Grid Petroleum Gh Ltd",
                "Gts Drilling",
                "HAGAN FAMILY",
                "Hth Worldwide Ins foreign",
                "Int Sos Assistance Uk Ltd Foreign",
                "Int Sos Assistance Uk Ltd Local",
                "Jfe Engineering Corporation Ltd",
                "JUDITH AKILAKPA SAWYER LOCAL",
                "Kek Insurance Brokers Ltd foreign",
                "Kek Insurance Brokers Ltd local",
                "LABADI BEACH HOTEL FOREIGN",
                "LABADI BEACH HOTEL LOCAL",
                "Lighthouse International Ltd",
                "Local cash",
                "Local Credit",
                "LYCOPODIUM GHANA LIMITED",
                "M & J Travel And Tours Limited",
                "MARBEN FAMILY",
                "Maripoma Mining Services LtD",
                "MAXAM GHANA LTD FOREIGN",
                "MAXAM GHANA LTD LOCAL",
                "MEDILINK INTERNATIONAL LIMITED",
                "Medisite Newmont Gh FOREIGN",
                "Medisite Newmont Gh Local",
                "Metropolitan Health Insurance foreign",
                "Metropolitan Health Insurance local",
                "METROPOLITAN HEALTH INSURANCE(FOREIGN)",
                "METROPOLITAN HEALTH INSURANCE(Local)",
                "METSO GHANA LTD",
                "Mimi Attipoe For Delivery Only",
                "MR. KOJO FYNN",
                "Mrs Janet Tamaklo Family Friends",
                "Msh International foreign",
                "Msh International Local",
                "Mso bupa Foreign",
                "Mso bupa Local",
                "Mso optimum Global Foreign",
                "Mso optimum Global Local",
                "National Homeownership Company Limited",
                "Nationwide cash foreign",
                "Nationwide cash local",
                "NATIONWIDE(CASH)foreign",
                "NATIONWIDE(CASH)local",
                "NMC BOARD MEMBERS",
                "Norpalm Ghana Limited local",
                "NOW HEALTH INT. GULF TPA LLC (FOREIGN)",
                "NOW HEALTH INT. GULF TPA LLC (Local)",
                "NYAHO MEDICAL CENTRE STAFF",
                "NYAHO SPECIALIST CONSULTANTS",
                "NYAKO P.A. JOHN(Local)",
                "OKERE FAMILY",
                "Omaboe E N FOREIGN",
                "Omaboe E N local",
                "Oxfam Gb FOREIGN",
                "Oxfam Gb local",
                "Oxford Business Group",
                "Perseus Mining Ltd FOREIGN",
                "Perseus Mining Ltd Local",
                "PETROLEUM COMMISSION FOREIGN",
                "PETROLEUM COMMISSION LOCAL",
                "PHOENIX HEALTH INSURANCE(FOREIGN)",
                "PHOENIX HEALTH INSURANCE(LOCAL)",
                "Premier Health Insurance Co Ltd Local",
                "PREMIER HEALTH INSURANCE CO. LTD (Local)",
                "PREMIER HEALTH INSURANCE CO. LTD(Foreign)",
                "PRIME INSURANCE COMPANY LTD",
                "Prime Insurance Company Ltd local",
                "PROMASIDOR GHANA LIMITED(Local)",
                "Prudential Life Insurance (LOCAL)",
                "Prudential Life Insurance Ghana Foreign",
                "Pz Cussons Ghana Ltd FOREIGN",
                "Pz Cussons Ghana Ltd Local",
                "Republic Bank FOREIGN",
                "Republic Bank Local",
                "Rocksure International FOREIGN",
                "Rocksure International local",
                "Sahel Health Gh (Patients)",
                "Sahel Health Gh Patients",
                "Sahel Health Gh staff FOREIGN",
                "Sahel Health Gh staff Local",
                "SANDVIK MINING & CONSTRUCTION(FOREIGN)",
                "SANDVIK MINING & CONSTRUCTION(Local)",
                "Securities And Exchange Commission",
                "Sgs Inspection Testing Services FOREIGN",
                "Sgs Inspection Testing Services Local",
                "Spie Oil Gas Services Gh Ltd FOREIGN",
                "Spie Oil Gas Services Gh Ltd Local",
                "Stanbic Bank Ghana Ltd FOREIGN",
                "Stanbic Bank Ghana Ltd local",
                "Star Health Insurance Ghana",
                "STEC",
                "SWISS EMBASSY(FOREIGN)",
                "SWISS EMBASSY(Local)",
                "TEMA TANK FARM LOCAL",
                "THAMES BUSINESS SOLUTIONS GH LTD. (Local)",
                "THE AFRICAN REGENT(FOREIGN)",
                "THE AFRICAN REGENT(Local)",
                "The Ghana Chamber Of Mines FOREIGN",
                "The Ghana Chamber Of Mines Local",
                "TITUS GLOVER FAMILY",
                "Trustee Services Ltd",
                "UMB INVESTMENT HOLDINGS LIMITED (FOREIGN)",
                "UMB INVESTMENT HOLDINGS LIMITED (LOCAL)",
                "Underground Mining Alliance FOREIGN",
                "Underground Mining Alliance Local",
                "UNISURE SES ASSISTANCE PTY LTD",
                "VISA SERVICE DESK",
                "Vitality Mutual Health Foreign",
                "VITALITY MUTUAL HEALTH(Local)",
                "VOLTA RIVER AUTHORITY(FOREIGN)",
                "VOLTA RIVER AUTHORITY(Local)",
                "Wapco Medicals local",
                "West African Gas Pipeline Co foreign",
                "West African Gas Pipeline Co Local",
                "West African Rescue Asso FOREIGN",
                "West African Rescue Asso locaL",
                "ZENITH BANK LIMITED(FOREIGN)",
                "ZENITH BANK LIMITED(Local)"
        };

        List<String> keys = getCustomerGroups(organisationId).stream().map(CustomerGroup::getName).toList();
        HashSet<String> names = new HashSet(keys);
        for (String type : paymentTypes) {

            if (!names.contains(type)) {
                LOGGER.info("fount new " + type);
                CustomerGroup group = new CustomerGroup();
                group.setActive(true);
                group.setCreatedById("8aaf05f8-741e-4e66-86df-a595f981d963");
                group.setCreatedByName("Rejoice Hormeku");
                group.setManagingOrganization("161380e9-22d3-4627-a97f-0f918ce3e4a9");
                group.setName(type);
                group.setUuid(UUID.randomUUID().toString());
                String url = "https://stag.api.cloud.serenity.health/v2/billing/customer-groups";

                HttpHeaders headers = new HttpHeaders();
                headers.set("Content-Type", "application/json");
                headers.add("x-api-key", "efomrddi");
                HttpEntity<CustomerGroup> httpEntity = new HttpEntity<>(group, headers);
                RestTemplate restTemplate = new RestTemplate();
                response = restTemplate.exchange(url, HttpMethod.POST, httpEntity,
                        CustomerGroupResponse.class);
                LOGGER.info(response.getBody().toString());
            }
        }
        return response;
    }

    public List<CustomerGroup> getCustomerGroups(String organisationId) {

        // LOGGER.info("Searching for "+stock.getFullName());
        String url = "https://stag.api.cloud.serenity.health/v2/billing/customer-groups?managing_organization="
                + organisationId;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.add("x-api-key", "efomrddi");
        HttpEntity<CustomerGroupResponse> httpEntity = new HttpEntity<>(headers);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<CustomerGroupResponse> response = restTemplate.exchange(url, HttpMethod.GET, httpEntity,
                CustomerGroupResponse.class);
        LOGGER.info(response.getBody().getData().toString());
        return response.getBody().getData();
    }

    public List<ServicePricing> getServicePrice(String orgId) {
        List<ServicePricing> prices = new ArrayList<>();
        // LOGGER.info("Searching for "+stock.getFullName());
        String url = "https://stag.api.cloud.serenity.health/v2/billing/service-prices?managing_organization=" + orgId;

        HttpHeaders headers = new HttpHeaders();
        headers.set("   Content-Type", "application/json");
        headers.add("x-api-key", "efomrddi");

        HttpEntity<String> httpEntity = new HttpEntity<>(headers);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<ServicePriceResponse> response = restTemplate.exchange(url, HttpMethod.GET, httpEntity,
                ServicePriceResponse.class);

        int total = response.getBody().getTotal();
        int rounds = 100 / 50;
        for (int i = 1; i <= (rounds + 1); i++) {
            System.err.println("goint for round " + i);
            response = restTemplate.exchange(url + "&page=" + i, HttpMethod.GET, httpEntity,
                    ServicePriceResponse.class);
            System.err.println(response.getBody().getData());
            prices.addAll(response.getBody().getData());

        }

        return prices;
    }

    public ServicePricing getServicePrices(int id) {
        // LOGGER.info("Searching for "+stock.getFullName());
        String url = "https://stag.api.cloud.serenity.health/v2/billing/service-prices/" + id;

        HttpHeaders headers = new HttpHeaders();
        headers.set("   Content-Type", "application/json");
        headers.add("x-api-key", "efomrddi");
        HttpEntity<String> httpEntity = new HttpEntity<>(headers);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<ServicePricing> response = restTemplate.exchange(url, HttpMethod.GET, httpEntity,
                ServicePricing.class);
        System.err.println(response.getBody());

        return response.getBody();
    }

    public List<Healthcare> getHealthService(String orgId) {
        // LOGGER.info("Searching for "+stock.getFullName());
        String url = "https://staging.nyaho.serenity.health/v1/providers/" + orgId
                + "/administration/healthcareservices";
        System.err.println(url);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.add("Authorization", "Bearer " + getToken().getAccess());
        headers.add("PROVIDER-PORTAL-ID", "j&4P8F<6+dF7/HASJ^hI92/6a&jdJOj*O\"[pHsh}t{o\"&7]\"}1~wg&SI%--,h{/");
        HttpEntity<String> httpEntity = new HttpEntity<>(headers);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<HealthcareServiceResponse> response = restTemplate.exchange(url, HttpMethod.GET, httpEntity,
                HealthcareServiceResponse.class);
        System.err.println(response.getBody().getData());
        return (response.getBody().getData());
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

    public V1Response getProdToken() {
        User user = new User();
        // user.setEmail("rejoicehormeku@gmail.com");
        // user.setPassword("5CYYkZhr92HwiPq");
        user.setEmail("chris@clearspacelabs.com");
        user.setPassword("charlehaschanged");
        Gson g = new Gson();
        String data = g.toJson(user);
        System.err.println(data);
        String url = "https://dev.api.serenity.health/v1/providers/auth/login";
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

    public String addHealthService(String org, String c) {
        LOGGER.info("Searching for " + c);
        String url = "https://staging.nyaho.serenity.health/v1/providers/161380e9-22d3-4627-a97f-0f918ce3e4a9"
                + "/administration/healthcareservices";
        System.err.println(url);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.add("Authorization", "Bearer " + getToken().getAccess());
        headers.add("PROVIDER-PORTAL-ID", "j&4P8F<6+dF7/HASJ^hI92/6a&jdJOj*O\"[pHsh}t{o\"&7]\"}1~wg&SI%--,h{/");
        HttpEntity<String> httpEntity = new HttpEntity<>(c, headers);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, httpEntity,
                String.class);
        System.err.println(response.getBody());
        return (response.getBody());
    }

    public String addHealthService(String c) {
        LOGGER.info("Searching for " + c);
        String url = "https://api.staging.serenity.health/v1/providers/161380e9-22d3-4627-a97f-0f918ce3e4a9/administration/healthcareservices";
        System.err.println(url);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.add("Authorization", "Bearer " + getToken().getAccess());
        headers.add("PROVIDER-PORTAL-ID", "J9DG4WcX+eV<;5xuKtY[yp8g&Sa@~R%wUMnE_6^.jbH{=Lf)>d");
        HttpEntity<String> httpEntity = new HttpEntity<>(c, headers);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, httpEntity,
                String.class);
        // System.err.println(response.getBody());
        return (response.getBody());
    }

    public String addHealthServiceProd(String c) {
        LOGGER.info("Searching for " + c);
        String url = "https://dev.api.serenity.health/v1/providers/161380e9-22d3-4627-a97f-0f918ce3e4a9/administration/healthcareservices";
        System.err.println(url);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.add("Authorization", "Bearer " + getProdToken().getAccess());
        headers.add("provider-portal-id", "J9DG4WcX+eV<;5xuKtY[yp8g&Sa@~R%wUMnE_6^.jbH{=Lf)>d");
        HttpEntity<String> httpEntity = new HttpEntity<>(c, headers);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, httpEntity,
                String.class);
        System.err.println(response.getBody());
        return (response.getBody());
    }

    public String addHealthServiceProdWithAuth(String c, String auth) {
        LOGGER.info("Searching for " + c);
        String url = "https://api.serenity.health/v1/providers/161380e9-22d3-4627-a97f-0f918ce3e4a9/administration/healthcareservices";
        System.err.println(url);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.add("Authorization", "Bearer " + auth);
        headers.add("provider-portal-id", "J9DG4WcX+eV<;5xuKtY[yp8g&Sa@~R%wUMnE_6^.jbH{=Lf)>d");
        HttpEntity<String> httpEntity = new HttpEntity<>(c, headers);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, httpEntity,
                String.class);
        System.err.println(response.getBody());
        return (response.getBody());
    }

    public void setPricing(String orgId, String v1Provider) {
        int count = 1;
        List<CustomerGroup> groups = getCustomerGroups(orgId);
        Map<String, CustomerGroup> groupMap = groups.stream().collect(Collectors.toMap(e -> e.getName(), e -> e));
        Map<String, ServicePricing> pricings = getServicePrice(orgId).stream()
                .collect(Collectors.toMap(e -> e.getName(), e -> e));

        System.err.println(pricings.keySet().size());
        Map<String, Healthcare> healthMap = getHealthService(v1Provider).stream()
                .collect(Collectors.toMap(e -> e.getHealthcareServiceName(), e -> e));

        String[] firstElements = { "ServiceName", "HealthcareService", "PriceType", "Currency", "ServiceCharge",
                "basePrice", "groupName" };

        try (Reader in = new FileReader(ResourceUtils.getFile("classpath:pricing.csv"))) {
            CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
                    .setHeader(firstElements)
                    .setDelimiter("\t")
                    .build();

            Iterable<CSVRecord> records = csvFormat.parse(in);
            for (CSVRecord record : records) {

                ServicePricing servicePricing = new ServicePricing(record);
                servicePricing.setManagingOrganization(orgId);
                if (groupMap.containsKey(servicePricing.getCustomerGroupName())) {
                    servicePricing
                            .setCustomerGroupId(groupMap.get(servicePricing.getCustomerGroupName()).getUuid());

                }
                if (healthMap.containsKey(servicePricing.getHealthcareServiceName())) {
                    servicePricing
                            .setHealthcareServiceId(healthMap.get(servicePricing.getHealthcareServiceName()).getId());

                }

                if (servicePricing.getHealthcareServiceId() != null) {

                    try {
                        if (!pricings.containsKey(record.get(0))) {
                            System.err.println("saving prices " + (count++));

                            // savePricing(servicePricing);
                        } else {
                            ServicePricing p = pricings.get(record.get(0));
                            p.setAmount(record.get("basePrice"));
                            System.err.println("already exit prices");

                            // LOGGER.info("already exist");
                            updatePricing(servicePricing);
                        }
                    } catch (Exception e) {
                        System.err.println(e.getMessage());
                        Gson k = new Gson();
                        // System.err.println("error" +k.toJson(servicePricing));
                    }
                }

            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();

        }

    }

    public void healthServiceSetup(String org, String file) {
        String[] firstElements = { "ServiceName", "ServiceClass", "BasePrice", "ServiceRequestCategory",
                "DiagnosticOrderCode", "DiagnosticServiceSection", "RevenueTagDisplay", "ServiceSpecialty",
                "ServiceType" };

        HashSet<String> dasta = new HashSet<>(
                getHealthService(org).stream().map(Healthcare::getHealthcareServiceName).collect(Collectors.toSet()));

        List<ServiceData> services = new ArrayList<>();
        try (Reader in = new FileReader(ResourceUtils.getFile("classpath:" + file))) {
            CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
                    .setHeader(firstElements)
                    .setDelimiter("\t")
                    .build();

            Iterable<CSVRecord> records = csvFormat.parse(in);
            for (CSVRecord record : records) {
                if (!dasta.contains(record.get("ServiceName"))) {

                    if (!record.get(1).strip().equalsIgnoreCase("Diagnostic")
                            & !record.get(1).strip().equalsIgnoreCase("Hospitalization")) {
                        HealthcareService service = new HealthcareService();
                        service.setHealthcareServiceName(record.get("ServiceName"));
                        Category category = new Category();
                        category.setCode(record.get(1).strip());
                        List<Category> cats = new ArrayList<>();
                        cats.add(category);
                        service.setHealthcareServiceCategories(cats);

                        Specialty specialty = new Specialty();
                        specialty.setCode(record.get("ServiceSpecialty"));
                        List<Specialty> specs = new ArrayList<>();
                        specs.add(specialty);
                        service.setHealthcareServiceSpecialties(specs);

                        ServiceType serviceType = new ServiceType();
                        serviceType.setCode(record.get("ServiceType"));
                        List<ServiceType> serviceTypes = new ArrayList<>();
                        serviceTypes.add(serviceType);
                        service.setHealthcareServiceTypes(serviceTypes);
                        service.setProvider("161380e9-22d3-4627-a97f-0f918ce3e4a9");

                        service.setSlotDuration("30");
                        service.setVirtual_service(false);
                        service.setComment("Healthcare service for General Consultation at Nyaho Medical Center");
                        service.setHealthcareServiceAppointmentRequired(false);
                        service.setHealthcareServiceServiceProvisionCode("cost");
                        service.setExtraDetails(record.get("BasePrice"));
                        service.setRevenueDisplayTag(record.get("RevenueDisplayTag"));

                        PriceTier free = new PriceTier();
                        free.setCharge("0");
                        free.setDisplay("Free");
                        free.setCurrency("GHS");
                        free.setDescription("Free service for " + service.getHealthcareServiceName());
                        free.setPriority("routine");

                        PriceTier priceTier = new PriceTier();
                        priceTier.setCharge(record.get("BasePrice"));
                        priceTier.setCurrency("GHS");
                        priceTier.setDisplay("Express");
                        priceTier.setPriority("urgent");
                        priceTier.setDisplay("Express");
                        priceTier.setDescription("Express service for " + record.get("ServiceName"));

                        PriceTier standard = new PriceTier();
                        standard.setCharge("50");
                        standard.setDisplay("Standard");
                        standard.setCurrency("GHS");
                        standard.setDescription("Standard service for " + record.get("ServiceName"));
                        standard.setPriority("routine");

                        List<PriceTier> priceTiers = new ArrayList<>();
                        priceTiers.add(priceTier);
                        priceTiers.add(free);
                        priceTiers.add(standard);
                        service.setPriceTiers(priceTiers);
                        ServiceData d = new ServiceData();
                        Gson gson = new Gson();
                        String json = gson.toJson(service);
                        // System.err.println(json);
                        d.setServiceName(service.getHealthcareServiceName());
                        d.setData(json);

                        services.add(d);

                    } else if (record.get(1).strip().equalsIgnoreCase("Hospitalization")) {
                        LOGGER.info(record.get(1));

                        HealthcareService service = new HealthcareService();
                        service.setHealthcareServiceName(record.get("ServiceName"));
                        Category category = new Category();
                        category.setCode(record.get(1).strip());
                        List<Category> cats = new ArrayList<>();
                        cats.add(category);
                        service.setHealthcareServiceCategories(cats);

                        Specialty specialty = new Specialty();
                        specialty.setCode(record.get("ServiceSpecialty"));
                        List<Specialty> specs = new ArrayList<>();
                        specs.add(specialty);
                        service.setHealthcareServiceSpecialties(specs);

                        ServiceType serviceType = new ServiceType();
                        serviceType.setCode(record.get("ServiceType"));
                        serviceType.setDisplay(record.get("ServiceType"));
                        ;
                        List<ServiceType> serviceTypes = new ArrayList<>();
                        serviceTypes.add(serviceType);
                        service.setHealthcareServiceTypes(serviceTypes);
                        service.setProvider("161380e9-22d3-4627-a97f-0f918ce3e4a9");

                        service.setSlotDuration("30");
                        service.setVirtual_service(false);
                        service.setHealthcareServiceAppointmentRequired(true);
                        service.setComment("Healthcare service for General Consultation at Nyaho Medical Center");
                        service.setExtraDetails(record.get("BasePrice"));
                        service.setMaximumCapacity(10);
                        service.setRevenueDisplayTag(record.get("RevenueDisplayTag"));
                        service.setSubscriptionFrequency("Weekly");
                        service.setWardType("ICU");
                        service.setHealthcareServiceServiceProvisionCode("cost");

                        PriceTier free = new PriceTier();
                        free.setCharge("0");
                        free.setDisplay("Free");
                        free.setCurrency("GHS");
                        free.setDescription("Free service for " + service.getHealthcareServiceName());
                        free.setPriority("routine");

                        PriceTier priceTier = new PriceTier();
                        priceTier.setCharge(record.get("BasePrice"));
                        priceTier.setCurrency("GHS");
                        priceTier.setDisplay("Express");
                        priceTier.setPriority("urgent");
                        priceTier.setDisplay("Express");
                        priceTier.setDescription("Express service for " + record.get("ServiceName"));

                        PriceTier standard = new PriceTier();
                        standard.setCharge("50");
                        standard.setDisplay("Standard");
                        standard.setCurrency("GHS");
                        standard.setDescription("Standard service for " + record.get("ServiceName"));
                        standard.setPriority("routine");

                        List<PriceTier> priceTiers = new ArrayList<>();
                        priceTiers.add(priceTier);
                        priceTiers.add(free);
                        priceTiers.add(standard);
                        service.setPriceTiers(priceTiers);
                        ServiceData d = new ServiceData();
                        Gson gson = new Gson();
                        String json = gson.toJson(service);
                        System.err.println(json);
                        d.setServiceName(service.getHealthcareServiceName());
                        d.setData(json);

                        services.add(d);

                    }

                    else {

                        HealthcareService service = new HealthcareService();
                        service.setActive(true);
                        service.setHealthcareServiceAppointmentRequired(true);
                        service.setVirtual_service(true);

                        service.setOrderCode(record.get("DiagnosticOrderCode").equals("") ? null : record.get(4));

                        service.setHealthcareServiceName(record.get("ServiceName"));

                        service.setServiceRequestCategory(
                                record.get("ServiceRequestCategory").equals("") ? null : record.get(3));

                        service.setDiagnosticServiceSection(
                                record.get("DiagnosticServiceSection").equals("") ? null : record.get(5));

                        service.setCharacteristic(null);
                        service.setComment("This is the service description");
                        service.setCommunication("EN");
                        service.setExtraDetails(null);
                        service.setTelecom("0267600606");
                        service.setProgram(null);
                        String[] methods = { "fax", "mail", "phone" };
                        service.setReferralMethod(Arrays.asList(methods));
                        List<AvailableTime> avTime = new ArrayList<>();
                        AvailableTime time = new AvailableTime();
                        time.setAllDay(true);
                        time.setAvailableStartTime("00:00:00Z");
                        time.setAvailableEndTime("23:59:59Z");
                        String[] days = { "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday",
                                "Sunday" };
                        time.setDaysOfWeek(Arrays.asList(days));
                        avTime.add(time);
                        service.setHealthcareServiceAvailableTimes(avTime);
                        service.setSlotDuration("30");
                        service.setHealthcareServiceServiceProvisionCode("Free");

                        if (!record.get(3).isEmpty()) {
                            Category category = new Category();
                            category.setCode(record.get("ServiceClass"));
                            List<com.serenity.integration.models.Category> cats = new ArrayList<>();
                            cats.add(category);
                            service.setHealthcareServiceCategories(cats);
                        }
                        Specialty specialty = new Specialty();
                        specialty.setCode(record.get("ServiceSpecialty"));
                        List<Specialty> specs = new ArrayList<>();
                        specs.add(specialty);
                        service.setHealthcareServiceSpecialties(specs);

                        ServiceType serviceType = new ServiceType();
                        serviceType.setCode(record.get("ServiceType"));
                        List<ServiceType> serviceTypes = new ArrayList<>();
                        serviceTypes.add(serviceType);
                        service.setHealthcareServiceTypes(serviceTypes);
                        service.setProvider("161380e9-22d3-4627-a97f-0f918ce3e4a9");
                        PriceTier priceTier = new PriceTier();
                        priceTier.setCharge(record.get("BasePrice"));
                        priceTier.setCurrency("GHS");
                        priceTier.setPriority("urgent");
                        priceTier.setDisplay("Express");
                        priceTier.setDescription("Express service for " + record.get("ServiceName"));
                        List<PriceTier> priceTiers = new ArrayList<>();
                        priceTiers.add(priceTier);
                        service.setPriceTiers(priceTiers);
                        service.setRevenueDisplayTag(record.get(file));
                        Location location = new Location();
                        location.setId("u101-location-None");
                        List<Location> locations = new ArrayList<>();
                        locations.add(location);
                        service.setHealthcareServiceLocations(locations);
                        ServiceData d = new ServiceData();
                        Gson gson = new Gson();
                        String json = gson.toJson(service);
                        System.err.println(json);
                        d.setServiceName(service.getHealthcareServiceName());
                        d.setData(json);
                        services.add(d);

                    }
                }

            }
        } catch (IOException e) {
            e.printStackTrace();

        }

        System.err.println(services.size());
        List<Report> daps = new ArrayList<>();

        serviceDataRepo.saveAllAndFlush(services);
        List<ServiceData> datas = serviceDataRepo.findAll();
        for (ServiceData a : datas) {
            try {
                addHealthService(org, a.getData());
            } catch (Exception e) {
                Report rep = new Report();
                rep.setServiceName(a.getServiceName());
                rep.setError(e.getMessage());
                daps.add(rep);
                e.printStackTrace();

            }
        }
        reportRepo.saveAllAndFlush(daps);
    }

    public void savePricing(ServicePricing price) {

        String url = "https://stag.api.cloud.serenity.health/v2/billing/service-prices";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.add("x-api-key", "efomrddi");
        HttpEntity<ServicePricing> httpEntity = new HttpEntity<>(price, headers);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<ServicePriceResponse> response = restTemplate.exchange(url, HttpMethod.POST, httpEntity,
                ServicePriceResponse.class);
        LOGGER.info(response.getBody().toString());

    }

    private void updatePricing(ServicePricing price) {

        String url = "https://stag.api.cloud.serenity.health/v2/billing/service-prices/"
                + price.getHealthcareServiceId();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.add("x-api-key", "efomrddi");
        HttpEntity<ServicePricing> httpEntity = new HttpEntity<>(price, headers);
        RestTemplate restTemplate = new RestTemplate();
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();

        restTemplate.setRequestFactory(requestFactory);
        ResponseEntity<ServicePriceResponse> response = restTemplate.exchange(url, HttpMethod.PATCH, httpEntity,
                ServicePriceResponse.class);
        LOGGER.info(response.getBody().toString());

    }

    public String getFromObject(int id) {
        // for(long id : repository.findAllId()){

        HealthCareServices services = repository.findByPk(id);
        String payload = formulatePayload(services);

        try {
            System.err.println(" add ing data");

            return addHealthServiceProd(payload);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(1 + " failed");
            return "error";
        }

    }

    public String loadToProd() {
        V1Response response = getProdToken();
        for (long id : repository.findAllId()) {

            HealthCareServices services = repository.findByPk(id);
            // services.setPriceTiers(servicePriceRepo.findByHealthcareServiceId(id));
            // services.getPriceTiers().size();
            String payload = formulatePayload(services);

            try {
                System.err.println(payload);
                if (id > 6) {
                    // addHealthServiceProdWithAuth(payload, response.getAccess());
                }
            } catch (Exception e) {
                System.err.println(id + " failed");
                e.printStackTrace();

            }

        }

        return "done";

    }

    public String loadToProdTest() {
        V1Response response = getProdToken();

        HealthCareServices services = repository.findByPk(526);
        services.setPriceTiers(servicePriceRepo.findByHealthcareServiceId(services.getId()));
        System.err.println(services.getPriceTiers());
        String payload = formulatePayload(services);

        try {
            System.err.println(payload);

            // addHealthServiceProdWithAuth(payload, response.getAccess());

        } catch (Exception e) {
            System.err.println(526 + " failed");
            e.printStackTrace();

        }

        return "done";

    }

    public String formulatePayload(HealthCareServices healthcareService) {
        String priceTiers = null;
        try {
            priceTiers = new ObjectMapper().writeValueAsString(healthcareService.getPriceTiers());

        } catch (Exception e) {
            e.printStackTrace();

        }
        String payload = String.format(
                """
                                 {
                            "healthcare_service_not_available_times": [],
                            "healthcare_service_available_times": [
                                {
                                    "is_all_day": false,
                                    "availableStartTime": "00:00:00",
                                    "availableEndTime": "20:00:00",
                                    "daysOfWeek": [
                                        "Sunday",
                                        "Monday",
                                        "Tuesday",
                                        "Wednesday",
                                        "Thursday",
                                        "Friday",
                                        "Saturday"
                                    ]
                                }
                            ],
                            "characteristic": null,
                            "comment": "Healthcare service for %s",
                            "communication": null,
                            "extra_details": null,
                            "healthcare_service_appointment_required": true,
                            "healthcare_service_categories": [
                                {
                                    "code": "Diagnostic"
                                }
                            ],
                            "healthcare_service_locations": [
                                {
                                    "id": "29e22113-9d7b-46a6-a857-810ca3567ca7",
                                    "location_hours_of_operation": null,
                                    "location_name": "Airport Main",
                                    "location_alias": null,
                                    "location_description": null,
                                    "location_mode": "instance",
                                    "location_contact_number": "+233307086490",
                                    "street_address": "35 Kofi Annan St, Accra",
                                    "location_physical_status": "building",
                                    "location_availability_exception": null,
                                    "uuid": "2f7d4c40-fe53-491d-877b-c2fee7edc1f2",
                                    "created_at": "2021-12-02T16:54:00.658123Z",
                                    "is_deleted": false,
                                    "modified_at": "2021-12-02T16:54:02.089735Z",
                                    "resource": null,
                                    "resource_type": null,
                                    "status": "active",
                                    "operational_status": null,
                                    "location_type": null,
                                    "postal_code": "00233",
                                    "city": "Accra",
                                    "country": "GH",
                                    "availability_exception": null,
                                    "physical_type": "building",
                                    "is_branch": true,
                                    "longitude": "0.000000",
                                    "latitude": "0.000000",
                                    "altitude": "0.000000",
                                    "managing_organization": "161380e9-22d3-4627-a97f-0f918ce3e4a9",
                                    "part_of": null
                                }
                            ],
                            "healthcare_service_name": "%s",
                            "healthcare_service_service_provision_code": "cost",
                            "healthcare_service_specialties": [
                                {
                                    "code": "Radiology"
                                }
                            ],
                            "healthcare_service_types": [
                                {
                                    "id": 378,
                                    "coding": [
                                        {
                                            "code": "General Care",
                                            "display": "General Care",
                                            "system": null
                                        }
                                    ],
                                    "text": "General Care",
                                    "uuid": "6ee9eff0-9d7d-48e7-8a84-93706328b75d",
                                    "created_at": "2024-02-10T09:39:13.831603Z",
                                    "code": "General Care"
                                }
                            ],
                            "id": "%s",
                            "uuid": "%s",
                            "is_active": true,
                            "is_outsourced": false,
                            "price_tiers": %s
                            ,
                            "program": null,
                            "provider": "161380e9-22d3-4627-a97f-0f918ce3e4a9",
                            "referral_method": null,
                            "slot_duration": "30",
                            "service_class": "Diagnostic",
                            "service_request_category": "Imaging",
                            "diagnostic_service_section": "Radiology",
                            "service_request_specimen": null,
                            "telecom": null,
                            "virtual_service": false,
                            "order_code": "%s",
                            "revenue_tag_display": "X-Ray Fees"
                        }

                        """, healthcareService.getServiceName(), healthcareService.getServiceName(),
                healthcareService.getId(), healthcareService.getUuid(), priceTiers, healthcareService.getOrderCode());

        return payload;

    }

    public String formulatePayloadConsultation(HealthCareServices healthcareService) {

        String payload = String.format(
                """
                                                      {
                            "healthcare_service_categories": [
                                {
                                    "code": "%s"
                                }
                            ],
                            "healthcare_service_specialties": [
                                {
                                    "code": "%s"
                                }
                            ],
                            "healthcare_service_types": [
                                {
                                    "code": "%s",
                                    "display": "%s"
                                }
                            ],
                            "slot_duration": 30,
                            "virtual_service": false,
                            "healthcare_service_appointment_required": false,
                            "comment": "Healthcare service for %s at Nyaho Medical Center",
                            "healthcare_service_service_provision_code": "cost",
                            "price_tiers": [],
                            "extra_details": %s,
                            "revenue_tag_display": "%s",
                            "healthcare_service_name": "%s",
                            "provider": "161380e9-22d3-4627-a97f-0f918ce3e4a9",
                            "healthcare_service_not_available_times": []
                        }
                                                """, healthcareService.getServiceClass(),
                healthcareService.getServiceSpecialty(),
                healthcareService.getServiceType(), healthcareService.getServiceType(),
                healthcareService.getServiceName(),
                healthcareService.getExtraDetails().isEmpty() ? 0 : healthcareService.getExtraDetails(),
                healthcareService.getRevenueTagDisplay(), healthcareService.getServiceName());

        return payload;

    }

    public String formulatePayloadDiagnostics(HealthCareServices healthcareService) {

        String payload = String.format(
                """
                                                      {
                            "healthcare_service_categories": [
                                {
                                    "code": "%s"
                                }
                            ],
                            "healthcare_service_specialties": [
                                {
                                    "code": "%s"
                                }
                            ],
                            "healthcare_service_types": [
                                {
                                    "code": "%s",
                                    "display": "%s"
                                }
                            ],
                            "slot_duration": 30,
                            "virtual_service": false,
                            "healthcare_service_appointment_required": false,
                            "comment": "Healthcare service for %s at Nyaho Medical Center",
                            "healthcare_service_service_provision_code": "cost",
                            "price_tiers": [],
                            "extra_details": %s,
                            "revenue_tag_display": "%s",
                            "service_request_category": "%s",
                            "order_code": "%s",
                            "diagnostic_service_section": "%s",
                            "healthcare_service_name": "%s",
                            "provider": "161380e9-22d3-4627-a97f-0f918ce3e4a9",
                            "healthcare_service_not_available_times": []
                        }
                                                """,
                healthcareService.getServiceClass(),
                healthcareService.getServiceSpecialty(),
                healthcareService.getServiceType(),
                healthcareService.getServiceType(),
                healthcareService.getServiceName(),
                (healthcareService.getExtraDetails().isEmpty() ? 0 : healthcareService.getExtraDetails()),
                healthcareService.getRevenueTagDisplay(),
                healthcareService.getServiceRequestCategory(),
                healthcareService.getOrderCode(),
                healthcareService.getDiagnosticServiceSection(),
                healthcareService.getServiceName());

        return payload;

    }

    public String formulatePayloadHospitalization(HealthCareServices healthcareService) {

        String payload = String.format(
                """
                                {
                            "healthcare_service_categories": [
                                {
                                    "code": "%s"
                                }
                            ],
                            "healthcare_service_specialties": [
                                {
                                    "code": "%s"
                                }
                            ],
                            "healthcare_service_types": [
                                {
                                    "code": "%s",
                                    "display": "%s"
                                }
                            ],
                            "slot_duration": 30,
                            "virtual_service": false,
                            "healthcare_service_appointment_required": true,
                            "comment": "Healthcare service for %s at Nyaho Medical Center",
                            "healthcare_service_service_provision_code": "cost",
                            "price_tiers": [],
                            "maximum_capacity": %s,
                            "extra_details": %s,
                            "healthcare_service_name": "%s",
                            "ward_type": "%s",
                            "subscription_frequency": "%s",
                            "revenue_tag_display": "%s",
                            "provider": "161380e9-22d3-4627-a97f-0f918ce3e4a9",
                            "healthcare_service_not_available_times": []
                        }
                                                """, healthcareService.getServiceClass(),
                healthcareService.getServiceSpecialty(),
                healthcareService.getServiceType(), healthcareService.getServiceType(),
                healthcareService.getServiceName(),
                healthcareService.getMaximumCapacity(),
                (healthcareService.getExtraDetails().isEmpty() ? 0 : healthcareService.getExtraDetails()),
                healthcareService.getServiceName(),
                healthcareService.getWardType(), healthcareService.getSubscriptionFrequency(),
                healthcareService.getRevenueTagDisplay());

        return payload;

    }

    public String formulatePayloadProcedure(HealthCareServices healthcareService) {

        String payload = String.format(
                """
                                                      {
                            "healthcare_service_categories": [
                                {
                                    "code": "%s"
                                }
                            ],
                            "healthcare_service_specialties": [
                                {
                                    "code": "%s"
                                }
                            ],
                            "healthcare_service_types": [
                                {
                                    "code": "%s",
                                    "display": "%s"
                                }
                            ],
                            "slot_duration": 30,
                            "virtual_service": false,
                            "healthcare_service_appointment_required": false,
                            "comment": "Healthcare service for %s at Nyaho Medical Center",
                            "healthcare_service_service_provision_code": "cost",
                            "price_tiers": [],
                            "extra_details": %s,
                            "revenue_tag_display": "%s",
                            "healthcare_service_name": "%s",
                            "service_request_category": "%s",
                            "provider": "161380e9-22d3-4627-a97f-0f918ce3e4a9",
                            "healthcare_service_not_available_times": []
                        }
                                                """, healthcareService.getServiceClass(),
                healthcareService.getServiceSpecialty(),
                healthcareService.getServiceType(), healthcareService.getServiceType(),
                healthcareService.getServiceName(),
                (healthcareService.getExtraDetails().isEmpty() ? 0 : healthcareService.getExtraDetails()),
                healthcareService.getRevenueTagDisplay(), healthcareService.getServiceName(),
                healthcareService.getServiceRequestCategory());

        return payload;

    }

    public String formulatePayloadAdministrative(HealthCareServices healthcareService) {

        String payload = String.format(
                """
                        {
                            "healthcare_service_categories": [
                                {
                                    "code": "%s"
                                }
                            ],
                            "healthcare_service_specialties": [
                                {
                                    "code": "%s"
                                }
                            ],
                            "healthcare_service_types": [
                                {
                                    "code": "%s",
                                    "display": "%s"
                                }
                            ],
                            "slot_duration": 30,
                            "virtual_service": false,
                            "healthcare_service_appointment_required": false,
                            "comment": "Healthcare service for %s at Nyaho Medical Center",
                            "healthcare_service_service_provision_code": "cost",
                            "price_tiers": [],
                            "extra_details": 10,
                            "revenue_tag_display": "%s",
                            "healthcare_service_name": "%s",
                            "provider": "161380e9-22d3-4627-a97f-0f918ce3e4a9",
                            "healthcare_service_not_available_times": []
                        }

                                                """,
                healthcareService.getServiceClass(),
                healthcareService.getServiceSpecialty(),
                healthcareService.getServiceType(),
                healthcareService.getServiceType(),
                healthcareService.getServiceName(),

                healthcareService.getRevenueTagDisplay(), healthcareService.getServiceName()

        );

        return payload;

    }

    public List<HealthCareServices> sethealthcareServicePayload() {
        List<HealthCareServices> updateList = new ArrayList<>();
        List<HealthCareServices> createList = new ArrayList<>();
        List<HealthCareServices> failed = new ArrayList<>();
        Map<String, HealthcareService> dbData = getHealthcareServiceIndDb();
        List<HealthCareServices> services = repository.findAll();
        int count =0;
        services.forEach(e -> {
            if (dbData.keySet().contains(e.getServiceName().strip())) {
                try {
                    e.setId((dbData.get(e.getServiceName().strip()).getId()));
                    e.setUuid(UUID.fromString(dbData.get(e.getServiceName().strip()).getUuid()));
                    
                } catch (Exception ec) {
                    ec.printStackTrace();
                    // TODO: handle exception
                }

                updateList.add(e);
            } else {
                try {
                    addHealthServiceProdWithAuth(convertHealthCareServices(e), getProdToken().getAccess());
                    System.err.println(e.getServiceName());
                } catch (Exception ex) {
                    ex.printStackTrace();
                    System.err.println(e.getServiceName() + "\t" + e.getRevenueTagDisplay());
                    failed.add(e);
                }
                createList.add(e);

            }

        });

        //services.stream().forEach(e ->{ data.add(convertHealthCareServices(e));});

       // addHealthService(convertHealthCareServices(s));

        System.err.println(createList.size() + "---tocreate");
        System.err.println(updateList.size() + "---toupdate");
        System.err.println(failed.size() + "--failed");

        return createList;

    }

    public Set<String> testCodeInDb() {
        List<TestCode> healthcareServices = new ArrayList<>();
        String sql = "SELECT * from diagnostic_test_code";
        SqlRowSet set = legJdbcTemplate.queryForRowSet(sql);
        while (set.next()) {

            TestCode service = new TestCode();
            service.setTestId(set.getString("test_id"));
            service.setTestOrderName(set.getString("order_name"));
            // System.err.println(service);
            healthcareServices.add(service);

        }
        Set<String> dbCodes = healthcareServices.stream().map(TestCode::getTestId).collect(Collectors.toSet());
        List<TestCode> codes = testCodeRepository.findAll();
        System.err.println(codes.size() + " --------------");
        List<TestCode> data = codes.stream().filter(e -> !dbCodes.contains(e.getTestId())).collect(Collectors.toList());

        sql = """
                        INSERT INTO public.diagnostic_test_code
                ("uuid", created_at,modified_at ,is_deleted,id, test_id, loinc_code, loinc_attributes, order_name, method_name, alias, is_available_at_provider, provider_id)
                VALUES(?::uuid, now(), now(),false, nextval('diagnostic_test_code_id_seq'::regclass), ?, ?, ?, ?, ?, ?, true, '161380e9-22d3-4627-a97f-0f918ce3e4a9');
                        """;

        legJdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {

            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {

                TestCode code = data.get(i);
                ps.setString(1, UUID.randomUUID().toString());
                ps.setString(2, code.getTestId());
                ps.setString(3, code.getOrderLoincCode());
                ps.setString(4, code.getLoincAttributes());
                ps.setString(5, code.getTestOrderName());
                ps.setString(6, code.getMethodName());
                ps.setString(7, code.getAlias());
            }

            @Override
            public int getBatchSize() {
                // TODO Auto-generated method stub
                return data.size();
            }

        });
        return dbCodes;

    }

    public Map<String, HealthcareService> getHealthcareServiceIndDb() {
        List<HealthcareService> healthcareServices = new ArrayList<>();
        String sql = "SELECT * from healthcare_service";
        SqlRowSet set = legJdbcTemplate.queryForRowSet(sql);
        while (set.next()) {

            HealthcareService service = new HealthcareService();
            service.setUuid(set.getString("uuid"));
            service.setId(set.getString("id"));
            service.setHealthcareServiceName(set.getString("name").strip());
            // System.err.println(service);
            healthcareServices.add(service);
        }
        return healthcareServices.stream().collect(Collectors.toMap(e -> e.getHealthcareServiceName(), e -> e));

    }

    public Map<String, String> getGroupsIndDb() {
        Map<String, String> groups = new HashMap<>();
        String sql = "SELECT * from customer_groups";
        SqlRowSet set = serenityJdbcTemplate.queryForRowSet(sql);

        while (set.next()) {
            groups.put(set.getString("name"), set.getString("uuid"));
        }
        return groups;
    }

    public Map<String, String> getPricesIndDb() {
        Map<String, String> groups = new HashMap<>();
        String sql = "SELECT * from healthcare_service_price_tiers";
        SqlRowSet set = legJdbcTemplate.queryForRowSet(sql);

        while (set.next()) {
            groups.put(set.getString("display"), set.getString("uuid"));

        }

        return groups;

    }



    public List<ServicePrice> getPrices() {
        List<ServicePrice>groups = new ArrayList<ServicePrice>();
        String sql = "SELECT * from healthcare_service_price_tiers";
        SqlRowSet set = legJdbcTemplate.queryForRowSet(sql);

        while (set.next()) {
            ServicePrice p = new ServicePrice();
            p.setUuid(set.getString("uuid"));
            p.setCharge( set.getBigDecimal("charge"));
            groups.add(p);
        }

        return groups;

    }
    public List<ServicePrice> getServicePricesv1() {
        List<ServicePrice>groups = new ArrayList<ServicePrice>();
        String sql = "SELECT * from healthcare_service_price_tiers";// where modified_at::date =?";
        SqlRowSet set = legJdbcTemplate.queryForRowSet(sql);

        while (set.next()) {
            ServicePrice p = new ServicePrice();
            p.setUuid(set.getString("uuid"));
            p.setCharge( set.getBigDecimal("charge"));
            groups.add(p);
        }

        return groups;

    }

    public String convertHealthCareServices(HealthCareServices hCareServices) {
        String data = null;
        switch (hCareServices.getServiceClass()) {

            case "Medication":
                data = formulatePayloadConsultation(hCareServices);

                break;

            case "Procedure":
                data = formulatePayloadProcedure(hCareServices);

                break;

            case "Hospitalization":
                data = formulatePayloadHospitalization(hCareServices);

                break;
            case "Consultation":
                data = formulatePayloadConsultation(hCareServices);

                break;

            case "Administrative":
                data = formulatePayloadAdministrative(hCareServices);

                break;

            default:
                data = formulatePayloadDiagnostics(hCareServices);
                break;
        }
        return data;

    }

    public void setHealthcareIds() {

        Map<String, HealthcareService> services = getHealthcareServiceIndDb();
        List<HealthCareServices> healthcareServicse = repository.findAll();
        healthcareServicse.forEach(e -> {

            if (services.containsKey(e.getServiceName().strip())) {
                e.setId(services.get(e.getServiceName().strip()).getUuid());

            }
        });

        repository.saveAllAndFlush(healthcareServicse);

    }

    public void setPriceMigrate() {

        Map<String, String> services = getGroupsIndDb();
        Map<String, String> oldPrices = getPricesIndDb();

        List<ServicePrice> healthcareServicse = servicePriceRepo.findAll();
        healthcareServicse.forEach(e -> {

            if (services.containsKey(e.getCustomerGroupName().strip())) {
                e.setCustomerGroupId(services.get(e.getCustomerGroupName().strip()));
                System.err.println("found");

            }
        });

        servicePriceRepo.saveAllAndFlush(healthcareServicse);

        int rows = healthcareServicse.size();
        int totalSize = (int) rows;
        int batches = (totalSize + 100 - 1) / 100;

        for (int i = 0; i < batches; i++) {
            final int batchNumber = i; // For use in lambda

            List<ServicePrice> pricing = servicePriceRepo.findServicePrices((i * 100), 100);

            String sql = """
                    INSERT INTO service_prices (unit_price,uuid,name,amount_type,currency,healthcare_service_id,managing_organization,created_by_id,created_by_name,is_active ,created_at,updated_at,customer_group_id,customer_group_name)
                    VALUES (?,  ?::uuid, ?,  ?,  ?, ?,   '161380e9-22d3-4627-a97f-0f918ce3e4a9',  '8aaf05f8-741e-4e66-86df-a595f981d963', 'Rejoice Hormeku',true,now(),now(),?,?)
                    """;
            serenityJdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {

                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    // TODO Auto-generated method stub
                    ServicePrice price = pricing.get(i);
                    ps.setBigDecimal(1, price.getCharge());
                    ps.setString(2, UUID.randomUUID().toString());
                    ps.setString(3, price.getPriceName());
                    ps.setString(4, price.getPriceType());
                    ps.setString(5, price.getCurrency());
                    ps.setString(6, price.getHealthcareServiceId());
                    ps.setString(7, price.getCustomerGroupId());
                    ps.setString(8, price.getCustomerGroupName());

                }

                @Override
                public int getBatchSize() {
                    // TODO Auto-generated method stub
                    return pricing.size();

                }

            });

        }

    }

    public void setPriceGroupIdMigrate() {
        final int BATCH_SIZE = 5;

        Map<String, String> existingPrices = getPricesIndDb();
        List<ServicePrice> allPrices = servicePriceRepo.findAll();

        // Assign UUIDs
        for (ServicePrice price : allPrices) {
            String existingUuid = existingPrices.get(price.getPriceName());
            if (existingUuid != null) {
                price.setUuid(existingUuid);
            } else {
                price.setUuid(UUID.randomUUID().toString());
            }
            System.err.println("UUID set for " + price.getPriceName() + ": " + price.getUuid());
        }

        int totalSize = allPrices.size();
        int batches = (totalSize + BATCH_SIZE - 1) / BATCH_SIZE;

        String sql = """
                    INSERT INTO service_prices (
                        unit_price, uuid, name, amount_type, currency,
                        healthcare_service_id, managing_organization,
                        created_by_id, created_by_name, is_active,
                        created_at, updated_at, customer_group_id, customer_group_name,healthcare_service_name
                    ) VALUES (
                        ?, ?::uuid, ?, ?, ?,
                        ?, '161380e9-22d3-4627-a97f-0f918ce3e4a9',
                        '8aaf05f8-741e-4e66-86df-a595f981d963', 'Rejoice Hormeku', true,
                        now(), now(), ?, ?,?
                    )
                """;

        for (int i = 0; i < batches; i++) {
            int fromIndex = i * BATCH_SIZE;
            int toIndex = Math.min(fromIndex + BATCH_SIZE, totalSize);
            List<ServicePrice> batch = allPrices.subList(fromIndex, toIndex);
            try {
                serenityJdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        ServicePrice price = batch.get(i);
                        ps.setBigDecimal(1, price.getCharge());
                        ps.setString(2, price.getUuid());
                        ps.setString(3, price.getPriceName());
                        ps.setString(4, price.getPriceType());
                        ps.setString(5, price.getCurrency());
                        ps.setString(6, price.getHealthcareServiceId());
                        ps.setString(7, price.getCustomerGroupId());
                        ps.setString(8, price.getCustomerGroupName());
                        ps.setString(9, price.getHealthcareServiceName());
                    }

                    @Override
                    public int getBatchSize() {
                        return batch.size();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("Finished inserting " + totalSize + " service prices in " + batches + " batches.");
        }

    }

    public void setupPricesMigrate(){
    List<ServicePrice> prices = getPrices();
    String sql = """
            UPDATE service_prices set unit_price=? where uuuid =?
            """;
    serenityJdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {

        @Override
        public void setValues(PreparedStatement ps, int i) throws SQLException {
            // TODO Auto-generated method stub
            ServicePrice price = prices.get(i);
            ps.setBigDecimal(1, price.getCharge());
            ps.setString(2, price.getUuid());
        }

        @Override
        public int getBatchSize() {
            // TODO Auto-generated method stub
          return   prices.size();
        }
        
    });
    System.err.println("Update finished");


    }


    public void setupPricesUpdate(LocalDate date){
        List<ServicePrice> prices = getServicePricesv1();
        String sql = """
                UPDATE service_prices set unit_price=? where uuid =?::uuid
                """;
        serenityJdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
    
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                // TODO Auto-generated method stub
                ServicePrice price = prices.get(i);
                ps.setBigDecimal(1, price.getCharge());
                ps.setString(2, price.getUuid());
            }
    
            @Override
            public int getBatchSize() {
                // TODO Auto-generated method stub
              return   prices.size();
            }
            
        });
        System.err.println("Update finished");
    
    
        }
}