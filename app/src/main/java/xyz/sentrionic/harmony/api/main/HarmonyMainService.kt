package xyz.sentrionic.harmony.api.main

import androidx.lifecycle.LiveData
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*
import xyz.sentrionic.harmony.api.GenericResponse
import xyz.sentrionic.harmony.api.main.responses.AccountUpdateResponse
import xyz.sentrionic.harmony.api.main.responses.ProfileListSearchResponse
import xyz.sentrionic.harmony.api.main.responses.StoryCreateUpdateResponse
import xyz.sentrionic.harmony.api.main.responses.StoryListSearchResponse
import xyz.sentrionic.harmony.models.AccountProperties
import xyz.sentrionic.harmony.models.Profile
import xyz.sentrionic.harmony.util.GenericApiResponse

interface HarmonyMainService {

    @GET("account/properties")
    fun getAccountProperties(
        @Header("Authorization") authorization: String
    ): LiveData<GenericApiResponse<AccountProperties>>

    @GET("account/{username}")
    fun getProfile(
        @Header("Authorization") authorization: String,
        @Path("username") username: String
    ): LiveData<GenericApiResponse<Profile>>

    @PUT("account/properties/update")
    @Multipart
    fun saveAccountProperties(
        @Header("Authorization") authorization: String,
        @Part("email") email: RequestBody,
        @Part("username") username: RequestBody,
        @Part("display_name") display_name: RequestBody?,
        @Part("description") description: RequestBody?,
        @Part("website") website: RequestBody?,
        @Part image: MultipartBody.Part?
    ): LiveData<GenericApiResponse<AccountUpdateResponse>>

    @PUT("account/change_password/")
    @FormUrlEncoded
    fun updatePassword(
        @Header("Authorization") authorization: String,
        @Field("old_password") currentPassword: String,
        @Field("new_password") newPassword: String,
        @Field("confirm_new_password") confirmNewPassword: String
    ): LiveData<GenericApiResponse<GenericResponse>>

    @GET("story/list")
    fun searchListStoryPosts(
        @Header("Authorization") authorization: String,
        @Query("search") query: String,
        @Query("ordering") ordering: String,
        @Query("page") page: Int
    ): LiveData<GenericApiResponse<StoryListSearchResponse>>

    @GET("account/profile_list/")
    fun searchProfiles(
        @Header("Authorization") authorization: String,
        @Query("search") query: String,
        @Query("ordering") ordering: String,
        @Query("page") page: Int
    ): LiveData<GenericApiResponse<ProfileListSearchResponse>>

    @GET("story/followed")
    fun searchFollowedListStoryPosts(
        @Header("Authorization") authorization: String,
        @Query("search") query: String,
        @Query("page") page: Int
    ): LiveData<GenericApiResponse<StoryListSearchResponse>>

    @GET("story/{slug}/is_author")
    fun isAuthorOfStoryPost(
        @Header("Authorization") authorization: String,
        @Path("slug") slug: String
    ): LiveData<GenericApiResponse<GenericResponse>>

    @DELETE("story/{slug}/delete")
    fun deleteStoryPost(
        @Header("Authorization") authorization: String,
        @Path("slug") slug: String
    ): LiveData<GenericApiResponse<GenericResponse>>

    @Multipart
    @PUT("story/{slug}/update")
    fun updateStory(
        @Header("Authorization") authorization: String,
        @Path("slug") slug: String,
        @Part("caption") caption: RequestBody,
        @Part("tags") tags: RequestBody,
        @Part image: MultipartBody.Part?
    ): LiveData<GenericApiResponse<StoryCreateUpdateResponse>>

    @Multipart
    @POST("story/create")
    fun createStory(
        @Header("Authorization") authorization: String,
        @Part("caption") caption: RequestBody,
        @Part image: MultipartBody.Part?
    ): LiveData<GenericApiResponse<StoryCreateUpdateResponse>>

    @POST("story/{slug}/like")
    fun toggleLike(
        @Header("Authorization") authorization: String,
        @Path("slug") slug: String
    ): LiveData<GenericApiResponse<GenericResponse>>

    @POST("account/{username}/follow")
    fun toggleFollow(
        @Header("Authorization") authorization: String,
        @Path("username") username: String
    ): LiveData<GenericApiResponse<GenericResponse>>
}