import io.qameta.allure.Step;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.time.Instant;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;

public class ApiTests {

    private String accessToken;
    private String registeredUsername;
    private String registeredPassword;

    @BeforeClass
    public static void setup() {
        RestAssured.baseURI = "http://9b142cdd34e.vps.myjino.ru:49268";
    }

    @Test(priority = 1)
    @Step("UserRegistrationTest")
    public void UserRegistrationTest() {
        registeredUsername = "user" + Instant.now().getEpochSecond();
        registeredPassword = "password";
        // Формируем JSON объект для тела запроса
        String requestBody = String.format("{\"username\":\"%s\",\"password\":\"%s\"}", registeredUsername, registeredPassword);
        // Отправляем POST запрос для регистрации пользователя с логированием запроса и ответа
        given()
                .header("Content-Type", "application/json")
                .body(requestBody)
                .log().all()  // Логируем запрос
                .when()
                .post("/register")
                .then()
                .log().all()  // Логируем ответ
                .statusCode(201) // Проверяем, что статус код 201
                .body("message", equalTo("User registered successfully")); // Проверяем тело ответа, если API предоставляет такую информацию
    }

    @Test(priority = 2)
    @Step("Authenticate user")
    public void authenticateUser() {
        // Формируем JSON объект для тела запроса аутентификации
        String requestBody = String.format("{\"username\":\"%s\",\"password\":\"%s\"}", registeredUsername, registeredPassword);

        // Отправляем POST запрос для аутентификации пользователя и получаем токен доступа
        Response response = given()
                .header("Content-Type", "application/json")
                .body(requestBody)
                .when()
                .post("/login")
                .then()
                .statusCode(200) // Убедимся, что статус код 200
                .body("access_token", notNullValue()) // Проверим, что токен действительно получен
                .extract()
                .response();
        accessToken = response.path("access_token");
    }

    @Test(priority = 3)
    @Step("GetListOfProductsTest")
    public void GetListOfProductsTest() {
        // Отправляем GET запрос для получения списка продуктов с логированием запроса и ответа
        given()
                .header("Content-Type", "application/json")
                .log().all()  // Логируем запрос
                .when()
                .get("/products")
                .then()
                .log().all()  // Логируем ответ
                .statusCode(200) // Проверяем, что статус код 200
                .body(notNullValue()); // Проверяем, что тело ответа не пустое

    }

    @Test(priority = 4)
    @Step("AddNewProductTest")
    public void AddNewProductTest() {

        // Формируем JSON объект для тела запроса на добавление нового продукта
        String requestBody = "{\"name\":\"New Product\",\"category\":\"Electronics\",\"price\":12.99,\"discount\":5}";

        // Отправляем POST запрос для добавления нового продукта с логированием запроса и ответа
        given()
                .header("Content-Type", "application/json")
                .body(requestBody)
                .log().all()  // Логируем запрос
                .when()
                .post("/products") // Убедитесь, что путь к эндпоинту правильный
                .then()
                .log().all()  // Логируем ответ
                .statusCode(201) // Проверяем, что статус код 201
                .body("message", equalTo("Product added successfully")); // Проверяем тело ответа, если API предоставляет такую информацию
    }

    @Test(priority = 5)
    @Step("GetProductInformationTest")
    public void GetProductInformationTest() {
        int productId = 1;
        given()
                .header("Content-Type", "application/json")
                .log().all()
                .pathParam("product_id", productId)
                .when()
                .get("/products/{product_id}")
                .then()
                .log().all()
                .statusCode(200)
                .body("[0].id", equalTo(productId))
                .body("[0].name", notNullValue())
                .body("[0].category", equalTo("Electronics"))
                .body("[0].price", equalTo(10.99f))
                .body("[0].discount", equalTo(10.0f));
    }

    @Test(priority = 6)
    @Step("UpdateProductInformationTest")
    public void UpdateProductInformationTest() {
        int productId = 1;
        String requestBody = "{\"name\":\"Updated Product Name\",\"category\":\"Electronics\",\"price\":15.99,\"discount\":8}";
        given()
                .header("Content-Type", "application/json")
                .log().all()
                .pathParam("product_id", productId)
                .body(requestBody)
                .when()
                .put("/products/{product_id}")
                .then()
                .log().all()
                .statusCode(200); // Verify that the status code is 200
    }

    @Test(priority = 7)
    @Step("DeleteProductTest")
    public void DeleteProductTest() {
        int productIdToDelete = 1;
        given()
                .header("Content-Type", "application/json")
                .log().all()
                .pathParam("product_id", productIdToDelete)
                .when()
                .delete("/products/{product_id}")
                .then()
                .log().all()
                .statusCode(200);
    }

    @Test(priority = 9)
    @Step("GetShoppingCartTest")
    public void GetShoppingCartTest() {
        authenticateUser();
        given()
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + accessToken)
                .log().all()
                .when()
                .get("/cart")
                .then()
                .log().all()
                .statusCode(200);
    }

    @Test(priority = 8)
    @Step("AddProductToCartTest")
    public void AddProductToCartTest() {
        String requestBody = "{\"product_id\": 1, \"quantity\": 2}";
        authenticateUser();
        given()
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + accessToken)
                .body(requestBody)
                .log().all()
                .when()
                .post("/cart")
                .then()
                .log().all()
                .statusCode(201);
    }

    @Test(priority = 10)
    @Step("RemoveProductFromCartTest")
    public void RemoveProductFromCartTest() {
        int productIdToRemove = 1;
        authenticateUser();
        given()
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + accessToken)
                .pathParam("product_id", productIdToRemove)
                .log().all()
                .when()
                .delete("/cart/{product_id}")
                .then()
                .log().all()
                .statusCode(200);
    }
}