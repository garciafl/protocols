package fgarcia.test.protocols.client.web;

import fgarcia.test.protocols.avro.PeopleList;
import fgarcia.test.protocols.avro.Person;
import fgarcia.test.protocols.client.model.JsonPerson;
import fgarcia.test.protocols.client.services.PerformanceService;
import fgarcia.test.protocols.protobuf.ContentProtos;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Map;

@RestController
public class Controller {
    private static final int HUNDRED = 100;
    private static final String serviceUrl = "http://localhost:8080/";

    private PerformanceService perfService;
    private RestTemplate restTemplate;

    /**
     * Hack to change the filename if compressed with GZIP.
     */
    @Value("${gzip.enabled}")
    private boolean isGzip = false;

    @Autowired
    public Controller(PerformanceService perfService, RestTemplate restTemplate) {
        this.perfService = perfService;
        this.restTemplate = restTemplate;
    }

    @RequestMapping(value = "/jsonTest")
    public void startJsonTest(@RequestParam(required = false) boolean small) throws IOException {
        String url = serviceUrl + "json";
        if (small) {
            url += "?size=10";
        }
        for (int i = 0; i < HUNDRED; i++) {
            perfService.startServerCounter(i);
            ParameterizedTypeReference<Map<String, JsonPerson>> typeRef =
                    new ParameterizedTypeReference<Map<String, JsonPerson>>() {
                    };
            ResponseEntity<Map<String, JsonPerson>> peopleList = restTemplate.exchange(
                    url, HttpMethod.GET, null, typeRef);
            perfService.endServerCounter(i);
            perfService.startClientCounter(i);
            for (Map.Entry<String, JsonPerson> entry : peopleList.getBody().entrySet()) {
                System.out.println("Got result : " + entry.toString());
            }
            perfService.endClientCounter(i);
            perfService.setSize(i, peopleList.getHeaders().getContentLength());
        }
        String resultFileName = "jsonStats";
        if (isGzip) {
            resultFileName += "-Gzip";
        }
        perfService.exportResults(resultFileName);
    }

    @RequestMapping(value = "/protobufTest")
    public void startProtoTest(@RequestParam(required = false) boolean small) throws IOException {
        String url = serviceUrl + "protobuf";
        if (small) {
            url += "?size=10";
        }
        for (int i = 0; i < HUNDRED; i++) {
            perfService.startServerCounter(i);
            ResponseEntity<ContentProtos.PeopleList> peopleList = restTemplate.exchange(
                    url, HttpMethod.GET, null, ContentProtos.PeopleList.class);
            perfService.endServerCounter(i);
            perfService.startClientCounter(i);
            for (ContentProtos.MapEntry entry : peopleList.getBody().getEntryList()) {
                System.out.println("Got result : " + entry.toString());
            }
            perfService.endClientCounter(i);
            perfService.setSize(i, peopleList.getHeaders().getContentLength());
        }
        String resultFileName = "protoStats";
        if (isGzip) {
            resultFileName += "-Gzip";
        }
        perfService.exportResults(resultFileName);
    }

    @RequestMapping(value = "/avroTest")
    public void startAvroTest(@RequestParam(required = false) boolean small) throws IOException {
        String url = serviceUrl + "avro";
        if (small) {
            url += "?size=10";
        }
        for (int i = 0; i < HUNDRED; i++) {
            perfService.startServerCounter(i);
            ResponseEntity<PeopleList> peopleList = restTemplate.exchange(
                    url, HttpMethod.GET, null, PeopleList.class);
            perfService.endServerCounter(i);
            perfService.startClientCounter(i);
            for (Map.Entry<CharSequence, Person> entry : peopleList.getBody().getItems().entrySet()) {
                System.out.println("Got result : " + entry.getValue().toString());
            }
            perfService.endClientCounter(i);
            perfService.setSize(i, peopleList.getHeaders().getContentLength());
        }
        String resultFileName = "avroStats";
        if (isGzip) {
            resultFileName += "-Gzip";
        }
        perfService.exportResults(resultFileName);
    }
}