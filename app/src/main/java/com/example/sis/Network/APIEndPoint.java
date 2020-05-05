package com.example.sis.Network;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface APIEndPoint {

    @FormUrlEncoded
    @POST("Login.aspx")
    Call<ResponseBody> login(
            @Header("accept") String type,
            @Field("Username") String Username,
            @Field("Password") String Password);
    // new
    @FormUrlEncoded
    @POST("ForgotPassword.aspx")
    Call<ResponseBody> forgetPassword(
            @Header("accept") String type,
            @Field("Username") String Username,
            @Field("CivilID") String CivilID);
    // new
    @FormUrlEncoded
    @POST("ChangePassword.aspx")
    Call<ResponseBody> changePassword(
            @Header("accept") String type,
            @Field("Token") String Token,
            @Field("OldPassword") String oldPassword,
            @Field("NewPassword") String NewPassword
    );

    // new
    @FormUrlEncoded
    @POST("GetStudentCourses.aspx")
    Call<ResponseBody> GetStudentCourses(
            @Header("accept") String type,
            @Field("Token") String Token
    );


    @FormUrlEncoded
    @POST("GetStudentSemesters.aspx")
    Call<ResponseBody> GetStudentSemesters(
            @Header("accept") String type,
            @Field("Token") String Token
    );
    @FormUrlEncoded
    @POST("GetStudentGrades.aspx")
    Call<ResponseBody> Grades(
            @Header("accept") String type,
            @Field("Token") String Token,
            @Field("SemesterID") String year
    );
    @FormUrlEncoded
    @POST("GetStudentFinancial.aspx")
    Call<ResponseBody> GetFees(
            @Header("accept") String type,
            @Field("Token") String Token
    );
    @FormUrlEncoded
    @POST("GetStudentPlanGroups.aspx")
    Call<ResponseBody> studentPlanGroup(
            @Header("accept") String type,
            @Field("Token") String Token
    );
    @FormUrlEncoded
    @POST("GetStudentPlanCourses.aspx")
    Call<ResponseBody> studentPlanCourse(
            @Header("accept") String type,
            @Field("Token") String Token
    );

    @FormUrlEncoded
    @POST("GetStudentComplaints.aspx")
    Call<ResponseBody> GetStudentComplaints(
            @Header("accept") String type,
            @Field("Token") String Token
    );
    @FormUrlEncoded
    @POST("GetStudentComplaintStatuses.aspx")
    Call<ResponseBody> GetStudentComplaintStatuses(
            @Header("accept") String type,
            @Field("Token") String Token,
            @Field("ComplaintID") String ComplaintID
    );
    @FormUrlEncoded
    @POST("GetStudentNewComplaintStatuses.aspx")
    Call<ResponseBody> GetStudentNewComplaintStatuses(
            @Header("accept") String type,
            @Field("Token") String Token
    );

    @FormUrlEncoded
    @POST("InsertStudentComplaint.aspx")
    Call<ResponseBody> InsertStudentComplaint(
            @Header("accept") String type,
            @Field("Token") String Token,
            @Field("ComplaintStatusID") String ComplaintStatusID,
            @Field("ComplaintStatusName") String ComplaintStatusName,
            @Field("StudentComments") String StudentComments
    );






        /**============================================== PHP ==================================*/
    @FormUrlEncoded
    @POST("Editeprofile.php")
    Call<ResponseBody> Editeprofile(
            @Header("accept") String type,
            @Field("id") String StdID,
            @Field("email") String email,
            @Field("phone") String phone,
            @Field("adresse") String adresse
    );

    @FormUrlEncoded
    @POST("Plan.php")
    Call<ResponseBody> Plan(
            @Header("accept") String type,
            @Field("id") String StdID
    );

    @FormUrlEncoded
    @POST("Plan_details.php")
    Call<ResponseBody> Plan_details(
            @Header("accept") String type,
            @Field("id") String StdID,
            @Field("school") String school,
            @Field("type") String label
    );

    @FormUrlEncoded
    @POST("Timetable.php")
    Call<ResponseBody> Timetable(
            @Header("accept") String type,
            @Field("id") String StdID,
            @Field("center") String center,
            @Field("code") String code
    );

    @FormUrlEncoded
    @POST("Registration.php")
    Call<ResponseBody> Registration(
            @Header("accept") String type,
            @Field("id") String StdID,
            @Field("cours_id") int program_id,
            @Field("year") String year,
            @Field("semester") String semester,
            @Field("filiere_id") int filiere_id,
            @Field("role") String role
            //,@Field("ids") String ids
            );

    @FormUrlEncoded
    @POST("Results.php")
    Call<ResponseBody> Results(@Header("accept") String type, @Field("id") String StdID, @Field("year") String Year);


    @FormUrlEncoded
    @POST("Fees.php")
    Call<ResponseBody> Fees(@Header("accept") String type, @Field("id") String StdID);


}