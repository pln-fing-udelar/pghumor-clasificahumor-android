package com.clasificahumor.android;

import java.util.List;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.http.GET;
import retrofit.http.Query;

/**
 * Created by Santiago on 25/08/2014.
**/
public class RestService {

    private Service service;

    private static RestService instance = new RestService();

    private RestService() {
        RestAdapter restAdapter = new RestAdapter.Builder()
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
