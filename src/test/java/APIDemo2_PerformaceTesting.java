import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import org.json.simple.JSONObject;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.lessThan;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class APIDemo2_PerformaceTesting {
    private static final String BASE_URI = "https://520f348fa65a.ngrok.io";

    private static final String TASK_NAME = "Task 1";
    private static final String TASK_CATEGORY = "R&D";
    private static final String TASK_STATUS = "Completed";

    private static final String UPDATED_TASK_NAME = "Task 2";
    private static final String UPDATED_TASK_CATEGORY = "API";
    private static final String UPDATED_TASK_STATUS = "Ongoing";

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
                .log().all()
                .and()
                .time(lessThan(300L));

        assertEquals(response.extract().statusCode(), 201);
        assertEquals(response.extract().body().jsonPath().get("name"), TASK_NAME);
        assertEquals(response.extract().body().jsonPath().get("category"), TASK_CATEGORY);
        assertEquals(response.extract().body().jsonPath().get("status[0]"), "Completed");
        assertNotNull(response.extract().body().jsonPath().get("_id"));

        TASK_ID = response.extract().body().jsonPath().get("_id");
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
