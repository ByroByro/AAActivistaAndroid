package com.example.actionaidactivista.models;

public class opportunity {
    private String mID;
    private String mTitle;
    private String mDescription;
    private String mDateposted;
    private String mClosingdate;
    private String mLocation;
    private String mDocsLink;

    public String getmDocsLink() {
        return mDocsLink;
    }

    public void setmDocsLink(String mDocsLink) {
        this.mDocsLink = mDocsLink;
    }

    public String getmDateposted() {
        return mDateposted;
    }

    public void setmDateposted(String mDateposted) {
        this.mDateposted = mDateposted;
    }

    public String getmClosingdate() {
        return mClosingdate;
    }

    public void setmClosingdate(String mClosingdate) {
        this.mClosingdate = mClosingdate;
    }

    public String getmLocation() {
        return mLocation;
    }

    public void setmLocation(String mLocation) {
        this.mLocation = mLocation;
    }

    public String getmID() {
        return mID;
    }

    public void setmID(String mOpportnityID) {
        this.mID = mOpportnityID;
    }

    public String getmTitle() {
        return mTitle;
    }

    public void setmTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    public String getmDescription() {
        return mDescription;
    }

    public void setmDescription(String mDescription) {
        this.mDescription = mDescription;
    }
}
