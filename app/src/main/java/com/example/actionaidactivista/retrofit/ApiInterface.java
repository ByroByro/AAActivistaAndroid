package com.example.actionaidactivista.retrofit;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

public interface ApiInterface {

    //endpoint for user registration
    @Multipart
    @POST("UserRegistration/Register")
    Call<ResponseBody> RegisterUser(
            @Part("fname") RequestBody fname,
            @Part("sname") RequestBody sname,
            @Part("dob") RequestBody dob,
            @Part("provinceid") RequestBody provinceid,
            @Part("districtid") RequestBody districtid,
            @Part("gender") RequestBody gender,
            @Part("occupation") RequestBody occupation
    );

    //endpoint for retrieving activista list (approved)
    @POST("Activista/GetActivistas")
    Call<ResponseBody> getActivistas();

    //endpoint for retrieving activista list (approved)
    @POST("Activista/GetLibraryMaterial")
    Call<ResponseBody> getLibrary();

    //download library article
    @Streaming
    @GET
    Call<ResponseBody> downloadArticle(@Url String url);

    //endpoint for retrieving opportunities
    @POST("Activista/GetOpportunities")
    Call<ResponseBody> getOpportunities();

    //endpoint for retrieving opportunities
    @POST("Feed/GetApprovedFeeds")
    Call<ResponseBody> getApprovedFeeds();

    //endpoint for retrieving opportunities
    @POST("Feed/GetAllFeeds")
    Call<ResponseBody> getAllFeeds();

    //endpoint for retrieving activistas list for approval
    @POST("Activista/GetActivistasForApproval")
    Call<ResponseBody> getActivistasForApproval();

    //endpoint for retrieving activistas list for approval
    @POST("Activista/GetActivistasForApproval")
    Call<ResponseBody> getUsers();

    //endpoint for retrieving provinces
    @GET("GetLookUpData/GetProvinces")
    Call<ResponseBody> getProvinces();

    //endpoint for retrieving districts
    @GET("GetLookUpData/GetDistricts")
    Call<ResponseBody> getDistricts();

    //endpoint for user signup
    @Multipart
    @POST("UserRegistration/SignUp")
    Call<ResponseBody> UserSignUp(
            @Part("accountno") RequestBody accountno,
            @Part("password") RequestBody password
    );

    //endpoint for user login
    @Multipart
    @POST("Activista/login")
    Call<ResponseBody> UserLogin(
            @Part("accountno") RequestBody accountno,
            @Part("password") RequestBody password
    );

    //endpoint for user registration
    @Multipart
    @POST("AuthAdmin/login")
    Call<ResponseBody> AdminLogin(
            @Part("username") RequestBody username,
            @Part("password") RequestBody password
    );

    //endpoint for actions on activista application i.e approve,disapprove,delete
    @Multipart
    @POST("Activista/ApplicationActions")
    Call<ResponseBody> ApplicationActions(
            @Part("id") RequestBody id,
            @Part("action") RequestBody action
    );

    //endpoint for actions on users i.e activate,deactivate
    @Multipart
    @POST("Activista/UsersActions")
    Call<ResponseBody> UserActions(
            @Part("id") RequestBody id,
            @Part("action") RequestBody action
    );

    //endpoint for approving a feed
    @Multipart
    @POST("Feed/FeedActions")
    Call<ResponseBody> ApproveFeed(
            @Part("id") RequestBody id,
            @Part("action") RequestBody action
    );

    //endpoint for deleting an opportunity
    @Multipart
    @POST("AdminOperations/DeleteOpportunity")
    Call<ResponseBody> DeleteOpportunity(
            @Part("id") RequestBody id
    );

    //endpoint for posting an opportunity
    @Multipart
    @POST("AdminOperations/opportunity")
    Call<ResponseBody> PostOpportunity(
            @Part("title") RequestBody title,
            @Part("description") RequestBody description,
            @Part("dateposted") RequestBody dateposted,
            @Part("closingdate") RequestBody closingdate,
            @Part("location") RequestBody location
    );

    //endpoint for posting library material
    @Multipart
    @POST("AdminOperations/postmaterial")
    Call<ResponseBody> PostLibraryMaterial(
            @Part("title") RequestBody title,
            @Part("author") RequestBody author,
            @Part("dateposted") RequestBody dateposted,
            @Part("filetype") RequestBody filetype,
            @Part("mimetype") RequestBody mimetype,
            @Part MultipartBody.Part file
    );

    //endpoint for posting feed of type text
    @Multipart
    @POST("Feed/PostFeed")
    Call<ResponseBody> PostTextFeed(
            @Part("description") RequestBody description,
            @Part("date") RequestBody date,
            @Part("filetype") RequestBody filetype,
            @Part("mimetype") RequestBody mimetype,
            @Part("inttype") RequestBody inttype,
            @Part("content") RequestBody content,
            @Part("userid") RequestBody userid,
            @Part("accno") RequestBody accno,
            @Part("location") RequestBody location,
            @Part("geolocation") RequestBody geolocation
    );

    //endpoint for posting feed of type text
    @Multipart
    @POST("Feed/PostFeed")
    Call<ResponseBody> PostMediaFeed(
            @Part("description") RequestBody description,
            @Part("date") RequestBody date,
            @Part("filetype") RequestBody filetype,
            @Part("mimetype") RequestBody mimetype,
            @Part("inttype") RequestBody inttype,
            @Part("userid") RequestBody userid,
            @Part("accno") RequestBody accno,
            @Part MultipartBody.Part file,
            @Part("location") RequestBody location,
            @Part("geolocation") RequestBody geolocation
    );

    //endpoint for posting library material
    @Multipart
    @POST("Activista/ApplyOpportunity")
    Call<ResponseBody> ApplyOpportunity(
            @Part("postid") RequestBody postid,
            @Part("userid") RequestBody userid,
            @Part("doctype") RequestBody doctype,
            @Part MultipartBody.Part file
    );

    //endpoint for liking a feed
    @Multipart
    @POST("ActivityMonitoring/LikeFeed")
    Call<ResponseBody> LikeFeed(
            @Part("likerid") RequestBody likerid,
            @Part("likertype") RequestBody likertype,
            @Part("dateliked") RequestBody dateliked,
            @Part("uid") RequestBody uid,
            @Part("feedid") RequestBody feedid
    );

    //endpoint for commenting feed
    @Multipart
    @POST("ActivityMonitoring/CommentFeed")
    Call<ResponseBody> CommentFeed(
            @Part("commentorid") RequestBody commentorid,
            @Part("commtype") RequestBody commtype,
            @Part("date") RequestBody date,
            @Part("uid") RequestBody uid,
            @Part("feedid") RequestBody feedid,
            @Part("comment") RequestBody comm
    );
}
