package com.example.actionaidactivista.models;

public class feedmonitor {
    private String fname;
    private String sname;
    private String userid;
    private String profile;
    private String nooffeeds;
    private String last_date;

    public String getLast_date() {
        return last_date;
    }

    public void setLast_date(String last_date) {
        this.last_date = last_date;
    }

    public String getFname() {
        return fname;
    }

    public void setFname(String fname) {
        this.fname = fname;
    }

    public String getSname() {
        return sname;
    }

    public void setSname(String sname) {
        this.sname = sname;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public String getNooffeeds() {
        return nooffeeds;
    }

    public void setNooffeeds(String nooffeeds) {
        this.nooffeeds = nooffeeds;
    }
}
