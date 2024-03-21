package model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AddToCartRequest {
    @JsonProperty("product_id")
    private int product_id;
    private int quantity;

    // Конструктор с параметрами
    public AddToCartRequest(int product_id, int quantity) {
        this.product_id = product_id;
        this.quantity = quantity;
    }

    // Геттеры и сеттеры
    public int getProductId() {
        return product_id;
    }

    public void setProductId(int product_id) {
        this.product_id = product_id;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
