package com.example.actionaidactivista.models;

import com.google.gson.annotations.SerializedName;

public class sample {
    @SerializedName("landlordid")
    private String id;
    @SerializedName("name")
    private String name;
    @SerializedName("surname")
    private String surname;

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSurname() {
        return surname;
    }
}
