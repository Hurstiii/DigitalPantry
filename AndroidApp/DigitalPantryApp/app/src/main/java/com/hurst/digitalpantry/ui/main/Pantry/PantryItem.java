package com.hurst.digitalpantry.ui.main.Pantry;

public class PantryItem {
    private String name;
    private String barcode;
    private int quantity;

    public PantryItem() {

    }

    public PantryItem(String name, String barcode, int quantity) {
        this.name = name;
        this.barcode = barcode;
        this.quantity = quantity;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void decreaseQuantity() {
        if(this.quantity >= 1) {
            this.quantity--;
        }
    }

    public void decreaseQuantity(int amount) {
        if(this.quantity >= 1) {
            this.quantity =- amount;
            if(this.quantity < 1) this.quantity = 1;
        }
    }

    public void increaseQuantity() {
        this.quantity++;
    }

    public void increaseQuantity(int amount) {
        this.quantity =+ amount;
    }
}
