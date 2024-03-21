
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.restassured.response.Response;

import model.*;
import org.testng.annotations.Test;

import java.time.Instant;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.testng.Assert.*;


public class ApiTests extends BaseTest {


    /**
     * Тестирование процесса регистрации пользователя.
     * Ожидается успешная регистрация с получением статуса ответа 201.
     */
    @Test(priority = 1)
    public void UserRegistrationTest() {
        // Генерируем уникальное имя пользователя
        registeredUsername = "user" + Instant.now().getEpochSecond();
        registeredPassword = "password";

        // Создаем объект пользователя
        user = new User(registeredUsername, registeredPassword);

        // Создаем экземпляр ObjectMapper для сериализации объекта в JSON
        ObjectMapper objectMapper = new ObjectMapper();
        String requestBody = null;
        try {
            // Сериализуем объект newUser в JSON строку
            requestBody = objectMapper.writeValueAsString(user);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Используем сериализованную JSON строку в качестве тела запроса
        performRequestAndVerify("POST", "/register", requestBody, null, 201);
    }

    @Test(priority = 2)

    public void authenticateUser() throws JsonProcessingException {


        ObjectMapper objectMapper = new ObjectMapper();
        String requestBody = objectMapper.writeValueAsString(user);

        // Отправляем POST запрос для аутентификации пользователя и получаем токен доступа
        Response response = performRequestAndVerify("POST", "/login", requestBody, null, 200);

        // Проверяем, что токен действительно получен и сохраняем его
        accessToken = response.path("access_token");
        assertNotNull(accessToken, "Access token should not be null");
    }


    /**
     * Тестирует получение списка продуктов и извлекает идентификатор первого продукта из списка.
     * Метод выполняет GET запрос к эндпоинту "/products", проверяет успешный ответ от сервера (200 OK),
     * убеждается в наличии непустого списка продуктов в теле ответа, и проверяет корректность структуры данных первого продукта в списке,
     * включая его идентификатор (id), имя (name), категорию (category), цену (price) и скидку (discount).
     * Идентификатор первого продукта в списке сохраняется для использования в последующих тестах, что позволяет
     * тестировать операции, требующие наличия существующего идентификатора продукта.
     *
     * @throws AssertionError если ответ от сервера не соответствует ожидаемому (например, если тело ответа пустое,
     *                        список продуктов отсутствует или не содержит ни одного продукта, или если структура данных продукта
     *                        не соответствует ожидаемой), или если извлеченный идентификатор продукта не является положительным числом.
     */

    @Test(priority = 3)
    public void GetListOfProductsTestAndExtractProductId() {
        // Выполняем GET запрос для получения списка продуктов
        Response response = performRequestAndVerify("GET", "/products", null, null, 200);

        // Убеждаемся, что тело ответа не пустое и содержит список продуктов
        assertNotNull(response.getBody(), "Response body should not be null");

        // Десериализуем ответ в список объектов Product
        List<Product> products = response.jsonPath().getList("", Product.class);

        // Убеждаемся, что список продуктов не пуст
        assertFalse(products.isEmpty(), "Products list should not be empty");

        // Проверяем, что у первого продукта в списке положительный ID
        Product firstProduct = products.get(0);
        assertTrue(firstProduct.getId() > 0, "Product ID should be positive");

        // Сохраняем productId из первого продукта в списке для использования в последующих тестах
        productId = firstProduct.getId();
    }


    /**
     * Тест добавления нового продукта через API.
     * Этот тест формирует JSON объект для тела запроса и отправляет POST-запрос на эндпоинт "/products".
     * Тест проверяет, что запрос на добавление нового продукта обрабатывается корректно,
     * и в ответ сервер возвращает статус код 201, что означает успешное создание ресурса.
     */


    @Test(priority = 4)
    public void AddNewProductTest() throws Exception {

        newProduct = new NewProduct("New Product", "Electronics", 12.99, 5);


        ObjectMapper mapper = new ObjectMapper();
        String requestBody = mapper.writeValueAsString(newProduct);
        performRequestAndVerify("POST", "/products", requestBody, accessToken, 201);
    }


    /**
     * Тестирование попытки добавления нового продукта с методом, который не разрешен API.
     * Тест формирует JSON объект для тела запроса и пытается отправить POST-запрос на эндпоинт "/products",
     * но без предоставления токена доступа. Это имитирует ситуацию, когда использование метода недопустимо.
     * * Ожидается, что API вернет статус код 405, указывающий на то, что метод запроса не разрешен.
     */
    @Test(priority = 5)
    public void AddNewProductWithNotAllowedMethodTest() throws Exception {

        ObjectMapper mapper = new ObjectMapper();
        String requestBody = mapper.writeValueAsString(newProduct);

        performRequestAndVerify("POST", "/products", requestBody, null, 405);
    }


    /**
     * Тестирование получения информации о продукте по его ID.
     * Метод выполняет GET запрос к API для получения данных о конкретном продукте,
     * используя сохраненный ранее productId. Проверяется, что ответ содержит корректные
     * данные о продукте, включая его ID, имя, категорию, цену и скидку.
     * Все эти поля должны соответствовать ожидаемым типам данных.
     * Ожидается, что сервер вернет статусный код 200 (OK) и корректную информацию о продукте.
     */
    @Test(priority = 6)
    public void GetProductInformationTest() {
        // Предполагается, что productId уже был инициализирован
        String path = "/products/" + productId;

        // Использование метода из BaseTest для выполнения запроса и проверки статусного кода
        Response response = performRequestAndVerify("GET", path, null, null, 200);

        // Десериализация JSON ответа в список объектов Product
        List<Product> products = response.jsonPath().getList("", Product.class);

        // Проверяем, что список продуктов не пустой
        assertThat(products, is(not(empty())));

        Product product = products.get(0);

        assertThat(product.getId(), is(equalTo(productId)));
        assertThat(product.getName(), is(not(emptyOrNullString())));
        assertThat(product.getCategory(), is(not(emptyOrNullString())));
        assertThat(product.getPrice(), is(greaterThan(0.0)));
        assertThat(product.getDiscount(), is(greaterThanOrEqualTo(0.0)));
    }

    /**
     * Тестирование ответа API на запрос информации о несуществующем продукте.
     * Этот тест пытается получить информацию о продукте, используя ID, который
     * гарантированно не существует в базе данных. Проверяется, что API корректно
     * обрабатывает такой запрос, возвращая статусный код 404 (Not Found), указывая на то,
     * что запрашиваемый ресурс не был найден.
     */
    @Test(priority = 7)

    public void GetNonexistentProductInformationTest() {
        int nonexistentProductId = 99999; // значение, гарантированно отсутствующее в базе
        String path = "/products/" + nonexistentProductId;
        performRequestAndVerify("GET", path, null, null, 404); // Проверяем, что сервер возвращает статус 404 Not Found
    }

    /**
     * Тестирование обновления информации о продукте.
     * Метод отправляет PUT запрос с обновленными данными продукта на эндпоинт "/products/{productId}".
     * Ожидается, что API успешно обработает запрос и вернет статусный код 200 (OK),
     * подтверждая успешное обновление информации о продукте. Данные для обновления
     * включают новое имя, категорию, цену и скидку продукта.
     *
     * @throws AssertionError если статусный код ответа отличается от ожидаемого или
     *                        если обновление информации о продукте не происходит.
     */

    @Test(priority = 8)
    public void UpdateProductInformationTest() throws Exception {

        updatedProduct = new UpdatedProduct("Updated Product Name", "Electronics", 15.99, 8);

        // Сериализация объекта в JSON строку
        ObjectMapper objectMapper = new ObjectMapper();
        String requestBody = objectMapper.writeValueAsString(updatedProduct);

        // Вызов метода из BaseTest для выполнения PUT запроса и проверки ответа
        performRequestAndVerify("PUT", "/products/" + productId, requestBody, accessToken, 200);
    }


    /**
     * Тестирование попытки обновления информации о продукте с использованием неразрешенного метода.
     * Метод отправляет PUT запрос для обновления продукта без необходимого токена аутентификации,
     * что не разрешено API. API должно ответить с кодом статуса 405 (Method Not Allowed),
     * указывая на то, что хотя метод (PUT) может быть допустим в целом, его использование
     * не разрешено без аутентификации или для конкретного запрашиваемого ресурса.
     *
     * @throws AssertionError если статусный код ответа отличается от ожидаемого 405
     */
    @Test(priority = 9)
    public void UpdateProductWithNotAllowedMethodTest() throws JsonProcessingException {

        ObjectMapper objectMapper = new ObjectMapper();
        String requestBody = objectMapper.writeValueAsString(updatedProduct);

        // Так как здесь не используется accessToken, метод должен вернуть ошибку 405
        performRequestAndVerify("PUT", "/products/" + productId, requestBody, null, 405);
    }

    /**
     * Тестирование удаления продукта через API.
     * Метод отправляет DELETE запрос на эндпоинт "/products/{productId}" с использованием
     * аутентификационного токена для удаления продукта, указанного переменной productId.
     * Ожидается, что API успешно обработает запрос, удалит продукт и вернет
     * статусный код 200 (OK), подтверждая успешное выполнение операции удаления.
     *
     * @throws AssertionError если статусный код ответа отличается от ожидаемого 200
     */
    @Test(priority = 10)

    public void DeleteProductTest() {
        performRequestAndVerify("DELETE", "/products/" + productId, null, accessToken, 200);
    }

    /**
     * Тестирование попытки удаления продукта с использованием неразрешённого метода.
     * Этот тест выполняет DELETE запрос на эндпоинт "/products/{productId}" без аутентификационного токена,
     * что предполагается быть недопустимым в данном контексте API. Ожидается, что сервер отклонит запрос,
     * возвращая статусный код 405 (Method Not Allowed), указывая на то, что выполнение
     * операции удаления не разрешено без аутентификации или данное действие в целом недопустимо.
     *
     * @throws AssertionError если статусный код ответа отличается от ожидаемого 405,
     *                        что указывало бы на некорректную обработку запроса со стороны API.
     */
    @Test(priority = 11)

    public void DeleteProductWithNotAllowedMethodTest() {
        performRequestAndVerify("DELETE", "/products/" + productId, null, null, 405);
    }

    /**
     * Тестирование получения информации о корзине покупок.
     * Метод выполняет аутентификацию пользователя и отправляет GET запрос на эндпоинт "/cart",
     * ожидая получить информацию о содержимом корзины пользователя включая общую стоимость и скидку.
     * Проверяется, что ответ содержит корректные поля total_price и total_discount с числовыми значениями,
     * а также проверяется структура и типы данных каждого продукта в корзине, подтверждая наличие товаров,
     * их идентификаторы, названия, категории, цены и скидки.
     *
     * @throws AssertionError если ответ API не содержит ожидаемую структуру данных или типы данных
     *                        не соответствуют ожидаемым.
     */
    @Test(priority = 14)
    public void GetShoppingCartTest() throws JsonProcessingException {
        authenticateUser();
        Response response = performRequestAndVerify("GET", "/cart", null, accessToken, 200);

        // Десериализация JSON-ответа в объект ShoppingCartResponse
        ShoppingCartResponse shoppingCartResponse = response.as(ShoppingCartResponse.class);

        // Проверяем, что total_price и total_discount являются числами
        assertThat(shoppingCartResponse.getTotalPrice(), instanceOf(Number.class));
        assertThat(shoppingCartResponse.getTotalDiscount(), instanceOf(Number.class));


        assertThat(shoppingCartResponse.getCart(), is(not(empty())));

        // Для каждого продукта в корзине проверяем типы данных
        for (Product product : shoppingCartResponse.getCart()) {
            assertThat(product.getId(), instanceOf(Integer.class)); // Проверяем, что id является числом
            assertThat(product.getName(), instanceOf(String.class)); // Проверяем, что name является строкой
            assertThat(product.getCategory(), instanceOf(String.class)); // Проверяем, что category является строкой
            assertThat(product.getPrice(), instanceOf(Double.class)); // Проверяем, что price является числом
            assertThat(product.getDiscount(), instanceOf(Double.class)); // Проверяем, что discount является числом
            assertThat(product.getQuantity(), instanceOf(Integer.class)); // Проверяем, что quantity является числом
        }
    }


    /**
     * Тестирование попытки получения информации о корзине покупок без аутентификации.
     * Метод отправляет GET запрос на эндпоинт "/cart" без аутентификационного токена,
     * ожидая, что доступ к содержимому корзины будет ограничен для неавторизованных пользователей.
     * Ожидается, что API вернет статусный код 401 (Unauthorized), указывая на то, что
     * доступ к запрашиваемой информации требует предварительной аутентификации пользователя.
     */
    @Test(priority = 15)

    public void GetShoppingCartWithoutAuthorizationTest() {
        performRequestAndVerify("GET", "/cart", null, null, 401);
    }

    /**
     * Тестирование добавления продукта в корзину покупок.
     * Метод сначала аутентифицирует пользователя, затем создает JSON тело запроса,
     * содержащее идентификатор продукта и количество добавляемых единиц этого продукта.
     * После этого отправляется POST запрос на эндпоинт "/cart" с указанным телом запроса и аутентификационным токеном.
     * Ожидается, что API успешно обработает запрос, добавит указанный продукт в корзину пользователя
     * и вернет статусный код 201 (Created), подтверждая успешное добавление продукта в корзину.
     *
     * @throws AssertionError если статусный код ответа отличается от ожидаемого 201,
     *                        что указывало бы на некорректную обработку запроса добавления продукта в корзину.
     */
    @Test(priority = 12)
    public void AddProductToCartTest() throws JsonProcessingException {

        addToCartRequest = new AddToCartRequest(productId, 2);

        // Сериализуем объект запроса в JSON
        ObjectMapper objectMapper = new ObjectMapper();
        String requestBody = objectMapper.writeValueAsString(addToCartRequest);

        authenticateUser();
        performRequestAndVerify("POST", "/cart", requestBody, accessToken, 201);
    }

    /**
     * Тестирование попытки добавления продукта в корзину без аутентификации.
     * Этот метод формирует JSON тело запроса для добавления определенного продукта (указанного через переменную productId)
     * в корзину, но отправляет POST запрос на эндпоинт "/cart" без аутентификационного токена.
     * Такой запрос должен быть отклонен API с возвращением статусного кода 401 (Unauthorized),
     * указывая на необходимость аутентификации пользователя для выполнения данной операции.
     *
     * @throws AssertionError если статусный код ответа отличается от ожидаемого 401,
     *                        что указывало бы на ошибку в механизме контроля доступа API.
     */
    @Test(priority = 13)

    public void AddProductToCartWithoutAuthorizationTest() throws JsonProcessingException {

        ObjectMapper objectMapper = new ObjectMapper();
        String requestBody = objectMapper.writeValueAsString(addToCartRequest);
        performRequestAndVerify("POST", "/cart", requestBody, null, 401);

    }

    /**
     * Тестирование удаления продукта из корзины покупок.
     * Метод сначала аутентифицирует пользователя, а затем отправляет DELETE запрос на эндпоинт "/cart/{productId}",
     * используя аутентификационный токен, для удаления конкретного продукта из корзины покупок.
     * Ожидается, что API успешно обработает запрос и вернет статусный код 200 (OK),
     * подтверждая успешное удаление продукта из корзины.
     *
     * @throws AssertionError если статусный код ответа отличается от ожидаемого 200,
     *                        что указывало бы на некорректную обработку запроса удаления продукта из корзины.
     */
    @Test(priority = 16)

    public void RemoveProductFromCartTest() throws JsonProcessingException {
        authenticateUser();
        performRequestAndVerify("DELETE", "/cart/" + productId, null, accessToken, 200);
    }

    /**
     * Тестирование попытки удаления продукта из корзины без аутентификации.
     * Этот метод отправляет DELETE запрос на эндпоинт "/cart/{productId}" без использования аутентификационного токена.
     * Такой запрос должен быть отклонен API с возвращением статусного кода 401 (Unauthorized),
     * указывая на то, что операция удаления продукта из корзины требует предварительной аутентификации пользователя.
     *
     * @throws AssertionError если статусный код ответа отличается от ожидаемого 401,
     *                        что может указывать на нарушение политики безопасности API по контролю доступа.
     */
    @Test(priority = 17)

    public void RemoveProductFromCartWithoutAuthorizationTest() {
        performRequestAndVerify("DELETE", "/cart/" + productId, null, null, 401);
    }

}