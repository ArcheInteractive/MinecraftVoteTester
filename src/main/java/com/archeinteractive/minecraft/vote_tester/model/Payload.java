package com.archeinteractive.minecraft.vote_tester.model;

import org.json.JSONObject;

public class Payload {

    private String serviceName;

    private String username;

    private String address;

    private final long timestamp;

    private String challenge;

    public Payload(String serviceName, String username, String address, long localTimestamp, String challenge) {
        this.serviceName = serviceName;
        this.username = username;
        this.address = address;
        this.timestamp = localTimestamp;
        this.challenge = challenge;
    }

    public JSONObject serialize() {
        JSONObject json = new JSONObject();
        json.put("serviceName", serviceName);
        json.put("username", username);
        json.put("address", address);
        json.put("timestamp", timestamp);
        json.put("challenge", challenge);
        return json;
    }

}
