package xyz.sentrionic.harmony.api.main

import androidx.lifecycle.LiveData
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*
import xyz.sentrionic.harmony.api.GenericResponse
import xyz.sentrionic.harmony.api.main.responses.*
import xyz.sentrionic.harmony.models.AccountProperties
import xyz.sentrionic.harmony.models.Profile
import xyz.sentrionic.harmony.util.GenericApiResponse

interface HarmonyMainService {

    /**
     * Account API
     */
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

    /**
     * Profile API
     */
    @GET("account/profile_list/")
    fun searchProfiles(
        @Header("Authorization") authorization: String,
        @Query("search") query: String,
        @Query("ordering") ordering: String,
        @Query("page") page: Int
    ): LiveData<GenericApiResponse<ProfileListSearchResponse>>

    @POST("account/{username}/follow")
    fun toggleFollow(
        @Header("Authorization") authorization: String,
        @Path("username") username: String
    ): LiveData<GenericApiResponse<GenericResponse>>

    /**
     * StoryPost API
     */
    @GET("story/list")
    fun searchListStoryPosts(
        @Header("Authorization") authorization: String,
        @Query("search") query: String,
        @Query("ordering") ordering: String,
        @Query("page") page: Int
    ): LiveData<GenericApiResponse<StoryListSearchResponse>>

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

    /**
     * Comment API
     */
    @GET("story/comments")
    fun getStoryPostComments(
        @Header("Authorization") authorization: String,
        @Query("slug") query: String,
        @Query("page") page: Int
    ): LiveData<GenericApiResponse<CommentListResponse>>
}