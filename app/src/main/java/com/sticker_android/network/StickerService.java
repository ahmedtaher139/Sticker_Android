package com.sticker_android.network;


import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

/**
 * Created by monika on 19/4/17.
 */
public interface StickerService {

  @FormUrlEncoded
  @POST(ApiConstant.API_LOGIN_URL)
  public Call<ApiResponse> userLogin(@Field("email") String email_id, @Field("password") String password, @Field("device_type") String deviceType
                                           , @Field("device_token") String deviceToken,
                                            @Field("device_udid") String deviceId,@Field("user_type") String accountType);
  @FormUrlEncoded
  @POST(ApiConstant.API_REGISTER)
  public Call<ApiResponse> userRegistration(@Field("language_id")int languageId,@Field("email") String email_id,@Field("password") String password,@Field("first_name") String firstName, @Field("last_name") String lastName
          , @Field("user_type") String accountType,@Field("device_type") String deviceType,
                                             @Field("device_token") String deviceToken,
                                            @Field("device_udid") String deviceId);
  @FormUrlEncoded
  @POST(ApiConstant.API_FORGOT_PASSWORD)
  public Call<ApiResponse> forgotPassword(@Field("email") String email_id);
  @FormUrlEncoded
  @POST(ApiConstant.API_CHANGE_PASSWORD)
  public Call<ApiResponse> changePassword(@Field("id") String userId,@Field("password")String password,@Field("authrized_key")String authKey);

  @FormUrlEncoded
  @POST(ApiConstant.API_PROFILE)
  public Call<ApiResponse> updateProfile(@Field("id") String userId,@Field("company_name")String companyName,@Field("authrized_key")String authKey
  ,@Field("company_address")String companyAddress,@Field("first_name")String firstName,
  @Field("last_name")String lastName,@Field("email")String email,@Field("user_type")String userType);

  @Multipart
  @POST(ApiConstant.API_PROFILE_IMAGE)
  public Call<ApiResponse> profileImage(@Part("id") RequestBody requestBody,
                                         @Part("language_id") RequestBody requestLanguageId,@Part("authrized_key")RequestBody requestAuthKey,
                                         @Part MultipartBody.Part part);
  @FormUrlEncoded
  @POST(ApiConstant.API_CHANGE_LANGUAGE)
  public Call<ApiResponse> changeLanguage(@Field("id") String userId,@Field("language_id")int languageId,@Field("authrized_key")String authKey);

  @FormUrlEncoded
  @POST(ApiConstant.API_GET_CONTENT)
  public Call<ApiResponse> apiGetContent(@Field("id") String userId,@Field("authrized_key")String authKey);

  @FormUrlEncoded
  @POST(ApiConstant.API_ADD_PRODUCT)
  public Call<ApiResponse> apiAddProduct(@Field("language_id") String languageId,@Field("authrized_key")String authKey,
                                         @Field("user_id")String userId,@Field("product_name")String productname,
                                         @Field("type")String type,@Field("description")String description,
                                         @Field("expiry_date")String expireDate
  ,@Field("image_path")String imagePath,@Field("product_id")String productId);
  @FormUrlEncoded
  @POST(ApiConstant.API_GET_PRODUCT_LIST)
  public Call<ApiResponse> apiGetProductList(@Field("language_id") String languageId,@Field("authrized_key")String authKey,
                                             @Field("user_id")String userId,@Field("index")int index,
                                             @Field("limit")int limit,@Field("type")String type,@Field("key_name")String name);


  @FormUrlEncoded
  @POST(ApiConstant.API_DELETE_PRODUCT)
  public Call<ApiResponse> apiDeleteProduct(@Field("language_id") String languageId,@Field("authrized_key")String authKey,
                                             @Field("user_id")String userId,@Field("product_id")String productId);

  @FormUrlEncoded
  @POST(ApiConstant.API_SEARCH_PRODUCT)
  public Call<ApiResponse> apiSearchProduct(@Field("language_id") String languageId,@Field("authrized_key")String authKey,
                                            @Field("user_id")String userId,@Field("index")int index,
                                            @Field("limit")int limit,@Field("type")String type,@Field("search")String search,@Field("key_name")String name);


}
