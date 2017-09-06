package com.andhikasrimadeva.myapp;

/**
 * Created by Andhika on 07/09/2017.
 */

public class Requests {

    public String getRequest_type() {
        return request_type;
    }

    public void setRequest_type(String request_type) {
        this.request_type = request_type;
    }

    public Requests(String request_type) {
        this.request_type = request_type;
    }

    public Requests(){}

    private String request_type;


}
