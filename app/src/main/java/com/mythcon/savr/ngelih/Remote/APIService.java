package com.mythcon.savr.ngelih.Remote;

import com.mythcon.savr.ngelih.Model.MyResponse;
import com.mythcon.savr.ngelih.Model.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

/**
 * Created by SAVR on 26/03/2018.
 */

public interface APIService {

    @Headers(
            {
                    "Content-type:application/json",
                    "Authorization:key=AAAAxE_WpGE:APA91bH82Kb4q9Le2IMdWyR8mKtm88EHurLhtxcajGXiaHQIeIXEnJDsLROIRb_OSONlThCDDa8Lj8SKHNWERrr6M9cUfUy9I2NyMCGtKujeemJ7lAX0IwrdYXE3IpuInoiTGjNoWI6P"
            }
    )

    @POST("fcm/send")
    Call<MyResponse> sendNotification (@Body Sender body);

}
