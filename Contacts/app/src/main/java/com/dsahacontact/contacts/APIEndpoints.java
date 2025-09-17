package com.dsahacontact.contacts;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface APIEndpoints {
    @POST("/contacts") // /contacts
    Call<Void> postContacts(@Body List<Contact> contacts);;
}