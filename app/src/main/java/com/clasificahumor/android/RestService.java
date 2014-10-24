package com.clasificahumor.android;

import com.squareup.okhttp.OkHttpClient;

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.List;

import retrofit.Callback;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.client.Client;
import retrofit.client.OkClient;
import retrofit.http.GET;
import retrofit.http.Query;

/**
 * Created by Santiago on 25/08/2014.
**/
public class RestService {

    private final Service service;

    private static final RestService instance = new RestService();

    private RestService() {
        CookieManager cookieManager = new CookieManager(new PersistentCookieStore(MyApplication.getContext()), CookiePolicy.ACCEPT_ALL);
        OkHttpClient okHttpClient = new OkHttpClient();
        okHttpClient.setCookieHandler(cookieManager);
        Client client = new OkClient(okHttpClient);

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setClient(client)
                .setEndpoint("http://clasificahumor.com")
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .build();

        service = restAdapter.create(Service.class);
    }

    public static RestService getInstance() {
        return instance;
    }

    public Service getService() {
        return service;
    }

    interface Service {
        @GET("/obtenerTresChistes.php")
        void obtenerTresChistes(Callback<List<Tweet>> callback);

        @GET("/obtenerChisteNuevo.php")
        void obtenerChisteNuevo(@Query("id1") String id1, @Query("id2") String id2, @Query("id3") String id3, Callback<List<Tweet>> callback);

        @GET("/procesarVoto.php")
        void procesarVoto(@Query("id") String id, @Query("voto") String voto, Callback<Void> callback);
    }
}
