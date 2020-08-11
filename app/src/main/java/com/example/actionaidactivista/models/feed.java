package com.example.actionaidactivista.models;

public class feed {

    private String mId;
    private String mDescription;//short activity description
    private String mDate;//activity date
    private String mFileType;//
    private String mIntType;//video(2),text(0) or image(1)
    private String mMimeType;
    private String mPath;//non text
    private String mUrl;//non text
    private String mContent;//this is for text feed only - int type 0
    private String mStatus;//whether approved or not
    private String mLocation;//location is string
    private String mGeoLocation;//location in coordinates
    private String mUploaderId;//user id of the person who uploaded the activity
    private String noOfLikes;//number of likes on feed
    private String uploaderName;//first name of person who uploaded the feed
    private String uploaderSurname;//surname of person who uploaded the feed
    private String uploaderProfile;//profile pic of person who uploaded the feed

    public String getNoOfLikes() {
        return noOfLikes;
    }

    public void setNoOfLikes(String noOfLikes) {
        this.noOfLikes = noOfLikes;
    }

    public String getUploaderName() {
        return uploaderName;
    }

    public void setUploaderName(String uploaderName) {
        this.uploaderName = uploaderName;
    }

    public String getUploaderSurname() {
        return uploaderSurname;
    }

    public void setUploaderSurname(String uploaderSurname) {
        this.uploaderSurname = uploaderSurname;
    }

    public String getUploaderProfile() {
        return uploaderProfile;
    }

    public void setUploaderProfile(String uploaderProfile) {
        this.uploaderProfile = uploaderProfile;
    }

    public String getmUploaderId() {
        return mUploaderId;
    }

    public void setmUploaderId(String mUploaderId) {
        this.mUploaderId = mUploaderId;
    }

    public String getmLocation() {
        return mLocation;
    }

    public void setmLocation(String mLocation) {
        this.mLocation = mLocation;
    }

    public String getmGeoLocation() {
        return mGeoLocation;
    }

    public void setmGeoLocation(String mGeoLocation) {
        this.mGeoLocation = mGeoLocation;
    }


    public String getmStatus() {
        return mStatus;
    }

    public void setmStatus(String mStatus) {
        this.mStatus = mStatus;
    }

    public String getmId() {
        return mId;
    }

    public void setmId(String mId) {
        this.mId = mId;
    }

    public String getmDescription() {
        return mDescription;
    }

    public void setmDescription(String mDescription) {
        this.mDescription = mDescription;
    }

    public String getmDate() {
        return mDate;
    }

    public void setmDate(String mDate) {
        this.mDate = mDate;
    }

    public String getmFileType() {
        return mFileType;
    }

    public void setmFileType(String nFileType) {
        this.mFileType = nFileType;
    }

    public String getmIntType() {
        return mIntType;
    }

    public void setmIntType(String mIntType) {
        this.mIntType = mIntType;
    }

    public String getmMimeType() {
        return mMimeType;
    }

    public void setmMimeType(String mMimeType) {
        this.mMimeType = mMimeType;
    }

    public String getmPath() {
        return mPath;
    }

    public void setmPath(String mPath) {
        this.mPath = mPath;
    }

    public String getmUrl() {
        return mUrl;
    }

    public void setmUrl(String mUrl) {
        this.mUrl = mUrl;
    }

    public String getmContent() {
        return mContent;
    }

    public void setmContent(String mContent) {
        this.mContent = mContent;
    }

}
