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
            @Part("occupation") RequestBody occupation,
            @Part("phoneno") RequestBody phoneno,
            @Part("email") RequestBody email,
            @Part("biography") RequestBody biography,
            @Part("dob_status") RequestBody dob_status
    );

    //endpoint for retrieving activista list (approved)
    @POST("Activista/GetActivistas")
    Call<ResponseBody> getActivistas();

    //endpoint for retrieving alumni list
    @POST("Activista/GetAlumni")
    Call<ResponseBody> getAlumni();

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
    @Multipart
    @POST("Feed/GetApprovedFeeds")
    Call<ResponseBody> getApprovedFeeds(
            @Part("row_num") RequestBody row_num,
            @Part("page_num") RequestBody page_num
    );

    //endpoint for retrieving opportunities
    @POST("Feed/GetAllFeeds")
    Call<ResponseBody> getAllFeeds();

    //endpoint for retrieving activistas list for approval
    @POST("Activista/GetActivistasForApproval")
    Call<ResponseBody> getActivistasForApproval();

    //endpoint for retrieving activistas list for approval
    @POST("Activista/GetActivistasForApproval")
    Call<ResponseBody> getUsers();

    //LOOK UP DATA ENDPOINTS
    //endpoint for retrieving provinces
    @GET("GetLookUpData/GetProvinces")
    Call<ResponseBody> getProvinces();

    //endpoint for retrieving districts
    @GET("GetLookUpData/GetDistricts")
    Call<ResponseBody> getDistricts();

    //endpoint for getting
    @GET("GetLookUpData/GetActivityTypes")
    Call<ResponseBody> getActivityTypes();

    //endpoint for getting
    @GET("AdminOperations/GetReportedComments")
    Call<ResponseBody> getReportedComments();

    //endpoint for user sig nup
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

    //endpoint for deleting feed
    @Multipart
    @POST("AdminOperations/DeleteFeed")
    Call<ResponseBody> DeleteFeed(
            @Part("id") RequestBody id,
            @Part("path") RequestBody path,
            @Part("typ") RequestBody typ
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
            @Part("location") RequestBody location,
            @Part("docs_link") RequestBody docs_link
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
            @Part("inttype") RequestBody inttype,
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
            @Part("geolocation") RequestBody geolocation,
            @Part("tags") RequestBody tags,
            @Part("activity_type") RequestBody activity_type
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
            @Part("geolocation") RequestBody geolocation,
            @Part("tags") RequestBody tags,
            @Part("activity_type") RequestBody activity_type
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

    //endpoint for posting library material
    @Multipart
    @POST("Activista/DeleteFeed")
    Call<ResponseBody> DeleteMyFeed(
            @Part("feed_id") RequestBody postid,
            @Part("user_id") RequestBody userid
    );

    //endpoint for liking a feed
    @Multipart
    @POST("ActivityMonitoring/LikeFeed")
    Call<ResponseBody> LikeFeed(
            @Part("likerid") RequestBody likerid,
            @Part("likertype") RequestBody likertype,
            @Part("dateliked") RequestBody dateliked,
            @Part("uid") RequestBody uid,
            @Part("feedid") RequestBody feedid,
            @Part("ownerid") RequestBody ownerid
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
            @Part("comment") RequestBody comm,
            @Part("ownerid") RequestBody ownerid
    );

    //endpoint for getting feed comments number
    @Multipart
    @POST("Feed/FeedCommentsNumber")
    Call<ResponseBody> GetNoOfComments(
            @Part("feedid") RequestBody feedid
    );

    //FEEDS MONITORING ENDPOINTS
    //endpoint for commenting feed
    @Multipart
    @POST("FeedMonitoring/GetTopActivistas")
    Call<ResponseBody> GetTopUsers(
            @Part("rows") RequestBody rows,
            @Part("listtype") RequestBody listtype
    );

    //endpoint for un liking feed
    @Multipart
    @POST("ActivityMonitoring/UnlikeFeed")
    Call<ResponseBody> UnlikeFeed(
            @Part("feedid") RequestBody feedid,
            @Part("likerid") RequestBody likerid,
            @Part("usertype") RequestBody usertype
    );

    //endpoint for un liking feed
    @Multipart
    @POST("ActivityMonitoring/CheckIfLikedFeed")
    Call<ResponseBody> CheckLikeFeed(
            @Part("likerid") RequestBody likerid,
            @Part("feedid") RequestBody feedid,
            @Part("usertype") RequestBody usertype
    );

    //endpoint for getting feed comments
    @Multipart
    @POST("Feed/FeedComments")
    Call<ResponseBody> GetFeedComments(
            @Part("feed_id") RequestBody feed_id,
            @Part("row_num") RequestBody row_num,
            @Part("page_num") RequestBody page_num
    );

    //endpoint for deleting a comment
    @Multipart
    @POST("ActivityMonitoring/DeleteComment")
    Call<ResponseBody> DeleteComment(
            @Part("comment_id") RequestBody comm_id
    );

    //PROFILE ENDPOINT
    //endpoint to upload/update profile
    @Multipart
    @POST("FileUploads/SetProfilePic")
    Call<ResponseBody> UpdateProfile(
            @Part("userid") RequestBody userid,
            @Part MultipartBody.Part file
    );

    //endpoint to remove profile
    @Multipart
    @POST("Activista/RemoveProfilePic")
    Call<ResponseBody> RemoveProfile(
            @Part("user_id") RequestBody user_id,
            @Part("url") RequestBody url,
            @Part("path") RequestBody path
    );

    //endpoint to update profile info
    @Multipart
    @POST("Activista/UpdateDetails")
    Call<ResponseBody> UpdateProfileInfo(
            @Part("name") RequestBody name,
            @Part("surname") RequestBody surname,
            @Part("dob_public") RequestBody dob_public,
            @Part("gender") RequestBody gender,
            @Part("occupation") RequestBody occupation,
            @Part("phone") RequestBody phone,
            @Part("email") RequestBody email,
            @Part("user_id") RequestBody user_id,
            @Part("acc_no") RequestBody acc_no
    );

    //endpoint to update either biography or dob
    @Multipart
    @POST("Activista/UpdateDOBBiography")
    Call<ResponseBody> UpdateDOBBioInfo(
            @Part("which_value") RequestBody which_value,
            @Part("value") RequestBody value,
            @Part("user_id") RequestBody user_id,
            @Part("acc_no") RequestBody acc_no
    );

    //endpoint to update either biography or dob
    @Multipart
    @POST("Activista/UpdateProvDis")
    Call<ResponseBody> UpdateProvDis(
            @Part("province_id") RequestBody province_id,
            @Part("district_id") RequestBody district_id,
            @Part("user_id") RequestBody user_id,
            @Part("acc_no") RequestBody acc_no
    );

    //endpoint to update profile info
    @Multipart
    @POST("Activista/GetUserDetails")
    Call<ResponseBody> GetMyDetails(
            @Part("user_id") RequestBody user_id,
            @Part("user_acc") RequestBody user_acc
    );

    //OPPORTUNITIES
    //endpoint for getting number of applications on an opportunity
    @POST("Opportunities/GetOpportunityApplications")
    Call<ResponseBody> GetOpportunityApplications();

    //endpoint for Applications for a particular opportunity
    @Multipart
    @POST("Opportunities/GetApplications")
    Call<ResponseBody> GetAplications(
            @Part("oppid") RequestBody id
    );

    //SEARCH OPERATIONS
    //endpoint for searching
    @Multipart
    @POST("Search/SearchUser")
    Call<ResponseBody> Search(
            @Part("searchstring") RequestBody string
    );

    //ACCOUNTS
    //endpoint for checking if account is approved
    @Multipart
    @POST("Activista/CheckAccountApproval")
    Call<ResponseBody> CheckAcc(
            @Part("email") RequestBody phone
    );

    //LOGOUT
    //log out user
    @Multipart
    @POST("Activista/LogoutUser")
    Call<ResponseBody> LogoutUser(
            @Part("user_id") RequestBody user_id
    );
    //log out admin
    @Multipart
    @POST("AuthAdmin/LogoutAdmin")
    Call<ResponseBody> LogoutAdmin(
            @Part("user_id") RequestBody user_id
    );

    //endpoint for reporting foul comments
    @Multipart
    @POST("Activista/ReportComment")
    Call<ResponseBody> ReportComment(
            @Part("reporter_id") RequestBody reporter_id,
            @Part("reported_id") RequestBody reported_id,
            @Part("comment_id") RequestBody comment_id,
            @Part("reporter_type") RequestBody reporter_type,
            @Part("reason") RequestBody reason,
            @Part("comment") RequestBody comment
    );

    //endpoint for deleting feed
    @Multipart
    @POST("AdminOperations/DeleteComment")
    Call<ResponseBody> DeleteReportedComment(
            @Part("comment_id") RequestBody comment_id
    );
}
