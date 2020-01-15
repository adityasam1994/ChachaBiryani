package com.cyfoes.aditya.chachabiryani;

public class place_order {
    String order, payment_method, cost, delivery_charge, latitude, longitude, order_status,
    payment_status, address, date, time;

    public place_order(String order, String payment_method, String cost, String delivery_charge, String latitude, String longitude, String order_status, String payment_status, String address, String date, String time) {
        this.order = order;
        this.payment_method = payment_method;
        this.cost = cost;
        this.delivery_charge = delivery_charge;
        this.latitude = latitude;
        this.longitude = longitude;
        this.order_status = order_status;
        this.payment_status = payment_status;
        this.address = address;
        this.date = date;
        this.time = time;
    }

    public String getOrder() {
        return order;
    }

    public void setOrder(String order) {
        this.order = order;
    }

    public String getPayment_method() {
        return payment_method;
    }

    public void setPayment_method(String payment_method) {
        this.payment_method = payment_method;
    }

    public String getCost() {
        return cost;
    }

    public void setCost(String cost) {
        this.cost = cost;
    }

    public String getDelivery_charge() {
        return delivery_charge;
    }

    public void setDelivery_charge(String delivery_charge) {
        this.delivery_charge = delivery_charge;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getOrder_status() {
        return order_status;
    }

    public void setOrder_status(String order_status) {
        this.order_status = order_status;
    }

    public String getpayment_status() {
        return payment_status;
    }

    public void setpayment_status(String payment_status) {
        this.payment_status = payment_status;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
