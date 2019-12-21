package com.diluv.api.endpoints.v1;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.diluv.api.utils.TestUtil;
import com.diluv.api.utils.error.ErrorResponse;

import static io.restassured.RestAssured.given;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.equalTo;

public class AuthTest {

    private static final String URL = "/v1/auth";

    @BeforeAll
    public static void setup () {

        TestUtil.start();
    }

    @AfterAll
    public static void stop () {

        TestUtil.stop();
    }

    @Test
    public void testRegister () {

        // Valid user
        given()
            .formParam("email", "testing@example.com")
            .formParam("username", "testing")
            .formParam("password", "password")
            .formParam("terms", "true")
            .with().post(URL + "/register").then().assertThat().statusCode(200);

        // Email used
        given()
            .formParam("email", "lclc98@example.com")
            .formParam("username", "testing")
            .formParam("password", "password")
            .formParam("terms", "true")
            .with().post(URL + "/register").then().assertThat().statusCode(400)
            .body(matchesJsonSchemaInClasspath("schema/error-schema.json"))
            .body("message", equalTo(ErrorResponse.USER_TAKEN_EMAIL.getMessage()));

        // Username used
        given()
            .formParam("email", "testing@example.com")
            .formParam("username", "lclc98")
            .formParam("password", "password")
            .formParam("terms", "true")
            .with().post(URL + "/register").then().assertThat().statusCode(400)
            .body(matchesJsonSchemaInClasspath("schema/error-schema.json"))
            .body("message", equalTo(ErrorResponse.USER_TAKEN_USERNAME.getMessage()));

        // Terms false
        given()
            .formParam("email", "testing@example.com")
            .formParam("username", "lclc98")
            .formParam("password", "password")
            .formParam("terms", "false")
            .with().post(URL + "/register").then().assertThat().statusCode(400)
            .body(matchesJsonSchemaInClasspath("schema/error-schema.json"))
            .body("message", equalTo(ErrorResponse.USER_INVALID_TERMS.getMessage()));
    }

    @Test
    public void testLogin () {

        given()
            .formParam("username", "jaredlll08")
            .formParam("password", "password")
            .with().post(URL + "/login").then().assertThat().statusCode(200)
            .body(matchesJsonSchemaInClasspath("schema/login-schema.json"));

        given()
            .formParam("username", "darkhax")
            .formParam("password", "password")
            .with().post(URL + "/login").then().assertThat().statusCode(400)
            .body(matchesJsonSchemaInClasspath("schema/error-schema.json"))
            .body("message", equalTo(ErrorResponse.USER_REQUIRED_MFA.getMessage()));


        given()
            .formParam("username", "lclc98")
            .formParam("password", "password")
            .with().post(URL + "/login").then().assertThat().statusCode(400)
            .body(matchesJsonSchemaInClasspath("schema/error-schema.json"))
            .body("message", equalTo(ErrorResponse.USER_NOT_VERIFIED.getMessage()));

        given()
            .formParam("username", "testing")
            .formParam("password", "password")
            .with().post(URL + "/login").then().assertThat().statusCode(400)
            .body(matchesJsonSchemaInClasspath("schema/error-schema.json"))
            .body("message", equalTo(ErrorResponse.NOT_FOUND_USER.getMessage()));
    }

    @Test
    public void testVerify () {

        given()
            .formParam("email", "lclc98@example.com")
            .formParam("username", "lclc98")
            .formParam("code", "8f32d879-45b3-4b8b-ae44-999e59566125")
            .with().post(URL + "/verify").then().assertThat().statusCode(200);

        given()
            .formParam("email", "darkhax@example.com")
            .formParam("username", "darkhax")
            .formParam("code", "1")
            .with().post(URL + "/verify").then().assertThat().statusCode(400)
            .body(matchesJsonSchemaInClasspath("schema/error-schema.json"))
            .body("message", equalTo(ErrorResponse.NOT_FOUND_USER.getMessage()));

        given()
            .formParam("email", "jaredlll08@example.com")
            .formParam("username", "jaredlll08")
            .formParam("code", "1")
            .with().post(URL + "/verify").then().assertThat().statusCode(400)
            .body(matchesJsonSchemaInClasspath("schema/error-schema.json"))
            .body("message", equalTo(ErrorResponse.NOT_FOUND_USER.getMessage()));

        given()
            .formParam("email", "testing@example.com")
            .formParam("username", "testing")
            .formParam("code", "1")
            .with().post(URL + "/verify").then().assertThat().statusCode(400)
            .body(matchesJsonSchemaInClasspath("schema/error-schema.json"))
            .body("message", equalTo(ErrorResponse.NOT_FOUND_USER.getMessage()));
    }
}