import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import org.json.simple.JSONObject;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class APIDemo
{
    private static final String BASE_URI ="https://520f348fa65a.ngrok.io";

    private static final String TASK_NAME ="Task 1";
    private static final String TASK_CATEGORY ="R&D";
    private static final String TASK_STATUS ="Completed";

    private static final String UPDATED_TASK_NAME ="Task 2";
    private static final String UPDATED_TASK_CATEGORY ="API";
    private static final String UPDATED_TASK_STATUS ="Ongoing";

    private String TASK_ID = null;

    private RequestSpecification requestSpecification;
    private ResponseSpecification responseSpecification;

    @BeforeClass        // give the health route
    public void CheckHealthRoute(){
        requestSpecification = new RequestSpecBuilder()
                .setBaseUri(BASE_URI)
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .build();

        responseSpecification = new ResponseSpecBuilder()
                .expectContentType(ContentType.JSON)
                .build();

        ValidatableResponse response = given()  // ValidatableResponse are the libs needed to validate ur response
                        .spec(requestSpecification)
                        .when()
                        .get("/health")
                        .then()
                        .spec(responseSpecification)
                        .log().all();
                        assertEquals(response.extract().statusCode(),200);
                        assertEquals(response.extract().body().jsonPath().get("message"),"ExpressJS web service is up and running");
    }


    @Test
    public void createTaskTest() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name", TASK_NAME);
        jsonObject.put("category", TASK_CATEGORY);
        jsonObject.put("status", TASK_STATUS);

        ValidatableResponse response = given()
                .spec(requestSpecification)
                .body(jsonObject.toJSONString())
                .when()
                .post("/tasks")
                .then()
                .spec(responseSpecification)
                .log().all();

                assertEquals(response.extract().statusCode(),201);
                assertEquals(response.extract().body().jsonPath().get("name"),TASK_NAME);
                assertEquals(response.extract().body().jsonPath().get("category"),TASK_CATEGORY);
                assertEquals(response.extract().body().jsonPath().get("status[0]"),"Completed");
                assertNotNull(response.extract().body().jsonPath().get("_id"));

                TASK_ID = response.extract().body().jsonPath().get("_id");
    }




    @Test
    public void getTaskTest() {
        createTaskTest();
        ValidatableResponse response = given()
                .spec(requestSpecification)
                .when()
                .get("/tasks/"+TASK_ID)
                .then()
                .spec(responseSpecification)
                .log().all();

        assertEquals(response.extract().statusCode(),200);
        assertEquals(response.extract().body().jsonPath().get("name"),TASK_NAME);
        assertEquals(response.extract().body().jsonPath().get("category"),TASK_CATEGORY);
        assertEquals(response.extract().body().jsonPath().get("status[0]"),TASK_STATUS);
    }


    @Test
    public void updateTaskTest() {
        createTaskTest();

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name", UPDATED_TASK_NAME);
        jsonObject.put("category", UPDATED_TASK_CATEGORY);
        jsonObject.put("status", UPDATED_TASK_STATUS);

        ValidatableResponse response = given()
                .spec(requestSpecification)
                .body(jsonObject.toJSONString())
                .when()
                .put("/tasks/"+TASK_ID)
                .then()
                .spec(responseSpecification)
                .log().all();

        assertEquals(response.extract().statusCode(),200);
        assertEquals(response.extract().body().jsonPath().get("name"),UPDATED_TASK_NAME);
        assertEquals(response.extract().body().jsonPath().get("category"),UPDATED_TASK_CATEGORY);
        assertEquals(response.extract().body().jsonPath().get("status[0]"),UPDATED_TASK_STATUS);
    }



    @AfterMethod
    public void deleteTaskTest() {
        try {
            ValidatableResponse response = given()
                    .spec(requestSpecification)
                    .when()
                    .delete("/tasks/" + TASK_ID)
                    .then()
                    .log().all();

            assertEquals(response.extract().statusCode(), 204);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

