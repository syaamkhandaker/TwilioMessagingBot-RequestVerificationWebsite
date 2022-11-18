package com.example.simplewebsite.gigsurf.Entities;

import java.util.Objects;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class Request{

@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private int id;


public String name;
public String phoneNumber;
public String request;
public String price;
public String type;
public String timeline;

    public Request() {
    }

    public Request(String name, String phoneNumber, String request, String price, String type, String timeline) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.request = request;
        this.price = price;
        this.type = type;
        this.timeline = timeline;
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return this.phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getRequest() {
        return this.request;
    }

    public void setRequest(String request) {
        this.request = request;
    }

    public String getPrice() {
        return this.price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTimeline() {
        return this.timeline;
    }

    public void setTimeline(String timeline) {
        this.timeline = timeline;
    }

    public Request id(int id) {
        setId(id);
        return this;
    }

    public Request name(String name) {
        setName(name);
        return this;
    }

    public Request phoneNumber(String phoneNumber) {
        setPhoneNumber(phoneNumber);
        return this;
    }

    public Request request(String request) {
        setRequest(request);
        return this;
    }

    public Request price(String price) {
        setPrice(price);
        return this;
    }

    public Request type(String type) {
        setType(type);
        return this;
    }

    public Request timeline(String timeline) {
        setTimeline(timeline);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Request)) {
            return false;
        }
        Request request = (Request) o;
        return id == request.id && Objects.equals(name, request.name) && Objects.equals(phoneNumber, request.phoneNumber) && Objects.equals(request, request.request) && Objects.equals(price, request.price) && Objects.equals(type, request.type) && Objects.equals(timeline, request.timeline);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, phoneNumber, request, price, type, timeline);
    }

    @Override
    public String toString() {
        return "{" +
            " id='" + getId() + "'" +
            ", name='" + getName() + "'" +
            ", phoneNumber='" + getPhoneNumber() + "'" +
            ", request='" + getRequest() + "'" +
            ", price='" + getPrice() + "'" +
            ", type='" + getType() + "'" +
            ", timeline='" + getTimeline() + "'" +
            "}";
    }

}