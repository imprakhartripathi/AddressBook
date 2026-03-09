package com.addressbook.integration;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.addressbook.dto.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnabledIfEnvironmentVariable(named = "JSON_SERVER_URL", matches = ".+")
class JsonServerRestAssuredTests {
    @LocalServerPort
    private int port;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private String jsonServerUrl;

    @BeforeEach
    void setup() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
        jsonServerUrl = System.getenv("JSON_SERVER_URL");
    }

    @Test
    void jsonServerCrudAndSync() throws Exception {
        Map<String, Object> payload = Map.of(
            "firstName", "Alan",
            "lastName", "Turing",
            "address", "23 Code",
            "city", "London",
            "state", "LDN",
            "zip", "EC1A",
            "phone", "7777777777",
            "email", "alan@example.com"
        );

        int id = given()
            .baseUri(jsonServerUrl)
            .contentType(ContentType.JSON)
            .body(payload)
            .post()
            .then()
            .statusCode(201)
            .extract()
            .path("id");

        given()
            .baseUri(jsonServerUrl)
            .get("/{id}", id)
            .then()
            .statusCode(200)
            .body("email", equalTo("alan@example.com"));

        given()
            .baseUri(jsonServerUrl)
            .contentType(ContentType.JSON)
            .body(Map.of("city", "Manchester"))
            .patch("/{id}", id)
            .then()
            .statusCode(200)
            .body("city", equalTo("Manchester"));

        given()
            .post("/api/json-server/pull")
            .then()
            .statusCode(200)
            .body("success", equalTo(true));

        ApiResponse response = objectMapper.readValue(
            given().get("/api/contacts").then().statusCode(200).extract().asString(),
            ApiResponse.class
        );

        List contacts = (List) response.getData();
        assertTrue(contacts.size() >= 1, "Expected contacts to be synced from JSON server");

        given()
            .baseUri(jsonServerUrl)
            .delete("/{id}", id)
            .then()
            .statusCode(200);

        given()
            .post("/api/json-server/pull")
            .then()
            .statusCode(200);

        given()
            .get("/api/contacts")
            .then()
            .statusCode(200)
            .body("data.size()", greaterThanOrEqualTo(0));
    }
}
