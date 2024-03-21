package model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class ShoppingCartResponse {
    private List<Product> cart;
    @JsonProperty("total_price") // Указывает имя поля в JSON
    private double totalPrice;

    @JsonProperty("total_discount") // Указывает имя поля в JSON
    private double totalDiscount;
    private double total_price;
    private double total_discount;

    // Конструктор по умолчанию
    public ShoppingCartResponse() {}

    // Геттеры и сеттеры
    public List<Product> getCart() {
        return cart;
    }

    public void setCart(List<Product> cart) {
        this.cart = cart;
    }

    public double getTotalPrice() {
        return total_price;
    }

    public void setTotalPrice(double total_price) {
        this.total_price = total_price;
    }

    public double getTotalDiscount() {
        return total_discount;
    }

    public void setTotalDiscount(double total_discount) {
        this.total_discount = total_discount;
    }
}
