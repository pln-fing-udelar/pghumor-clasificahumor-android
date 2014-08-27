package com.clasificahumor.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import de.keyboardsurfer.android.widget.crouton.Configuration;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class MainActivity extends Activity {

    private static final int REQUEST_GOOGLE_PLAY_SERVICES = 0;

    public static final String CONNECTIVITY_CHANGE_INTENT_NAMESPACE = MainActivity.class.getPackage().getName() + ".CONNECTIVITY_CHANGE";

    @InjectView(R.id.chiste)
    TextView mChiste;
    @InjectView(R.id.estrellas)
    RatingBar mEstrellas;
    @InjectView(R.id.calificacion_text)
    TextView mCalificacionText;
    @InjectView(R.id.wrapper_calificacion)
    LinearLayout mWrapperCalificacion;

    private List<Tweet> chistes;
    private int index = 0;

    private IntentFilter mIntentFilter = new IntentFilter(CONNECTIVITY_CHANGE_INTENT_NAMESPACE);

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            verifyConnectionInUi(context);
        }
    };

    public static void verifyConnectionInUi(final Context context) {
        if (context instanceof Activity) {
            Crouton.cancelAllCroutons();

            if (!isConnected(context)) {
                Configuration configuration = new Configuration.Builder()
                        .setDuration(Configuration.DURATION_INFINITE)
                        .build();
                Style style = new Style.Builder()
                        .setBackgroundColorValue(Style.holoRedLight)
                        .setConfiguration(configuration)
                        .build();
                Crouton.makeText((Activity) context, "No hay conexión a Internet", style).show();
            }
        }
    }

    public static boolean isConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Crashlytics.start(this);

        registerReceiver(mReceiver, mIntentFilter);

        final int statusCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (statusCode == ConnectionResult.SUCCESS) {
            GoogleAnalytics.getInstance(this).reportActivityStop(this);
        } else if (statusCode == ConnectionResult.SERVICE_MISSING ||
                statusCode == ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED ||
                statusCode == ConnectionResult.SERVICE_DISABLED) {
            showGooglePlayServicesAvailabilityErrorDialog(statusCode);
        }

        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);

        RestService.getInstance().getService().obtenerTresChistes(new Callback<List<Tweet>>() {
            @Override
            public void success(List<Tweet> tweets, Response response) {
                chistes = tweets;
                mChiste.setText(chistes.get(index).getText_tweet());
            }

            @Override
            public void failure(RetrofitError error) {
                error.printStackTrace();
            }
        });

        mEstrellas.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                if (fromUser && rating > 0) {
                    votar(String.valueOf(Float.valueOf(rating).intValue()));

                    switch (Float.valueOf(rating).intValue()) {
                        case 1:
                            mWrapperCalificacion.setBackgroundResource(R.drawable.background_calificacion_1);
                            mCalificacionText.setText("¡Malísimo!");
                            break;
                        case 2:
                            mWrapperCalificacion.setBackgroundResource(R.drawable.background_calificacion_2);
                            mCalificacionText.setText("Malo");
                            break;
                        case 3:
                            mWrapperCalificacion.setBackgroundResource(R.drawable.background_calificacion_3);
                            mCalificacionText.setText("Ni ni");
                            break;
                        case 4:
                            mWrapperCalificacion.setBackgroundResource(R.drawable.background_calificacion_4);
                            mCalificacionText.setText("Bueno");
                            break;
                        default:
                            mWrapperCalificacion.setBackgroundResource(R.drawable.background_calificacion_5);
                            mCalificacionText.setText("¡Buenísimo!");
                    }

                    new CountDownTimer(700, 700) {
                        @Override
                        public void onTick(long millisUntilFinished) {

                        }

                        public void onFinish() {
                            mCalificacionText.setText("¡Gracias!");
                            new CountDownTimer(800, 800) {
                                @Override
                                public void onTick(long millisUntilFinished) {

                                }

                                public void onFinish() {
                                    mWrapperCalificacion.setBackgroundResource(R.drawable.background_calificacion);
                                    mCalificacionText.setText("¡Votá!");
                                    mEstrellas.setRating(0);
                                }
                            }.start();
                        }
                    }.start();
                }
            }
        });
    }

    @OnClick(R.id.no_es_humor)
    public void noEsHumor() {
        votar("n");
    }

    @OnClick(R.id.ver_otro)
    public void verOtro() {
        votar("x");
    }

    private void votar(String voto) {
        final int indexViejo = index;

        RestService.getInstance().getService().obtenerChisteNuevo(
                chistes.get(0).getId_tweet(),
                chistes.get(1).getId_tweet(),
                chistes.get(2).getId_tweet(),
                new Callback<List<Tweet>>() {
                    @Override
                    public void success(List<Tweet> tweets, Response response) {
                        chistes.get(indexViejo).setId_tweet(tweets.get(0).getId_tweet());
                        chistes.get(indexViejo).setText_tweet(tweets.get(0).getText_tweet());
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        error.printStackTrace();
                    }
                });

        RestService.getInstance().getService().procesarVoto(chistes.get(index).getId_tweet(), voto, new Callback<Void>() {
            @Override
            public void success(Void aVoid, Response response) {

            }

            @Override
            public void failure(RetrofitError error) {
                error.printStackTrace();
            }
        });

        index = (index + 1) % chistes.size();

        mChiste.setText(chistes.get(index).getText_tweet());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.quienes_somos:
                AlertDialog alert = new AlertDialog.Builder(this)
                        .setTitle("¿Quiénes somos?")
                        .setMessage("Somos dos estudiantes (Santiago Castro y Matías Cubero) de Ingeniería en Computación - Universidad de la República, Montevideo, Uruguay que estamos realizando un proyecto de grado que pretende detectar humor en textos en español. Para esto necesitamos obtener una base de datos de chistes en español. Gracias a tu ayuda, vamos a poder obtener una base de datos de buena calidad.\n" +
                                "\n" +
                                "¡¡¡MUCHAS GRACIAS!!!")
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        }).create();
                alert.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        verifyConnectionInUi(this);
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);
        verifyConnectionInUi(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        Crouton.cancelAllCroutons();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //Crouton.cancelAllCroutons(); // It's already in onPause.
        unregisterReceiver(mReceiver);

        final int statusCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (statusCode == ConnectionResult.SUCCESS) {
            GoogleAnalytics.getInstance(this).reportActivityStop(this);
        }
    }

    void showGooglePlayServicesAvailabilityErrorDialog(final int statusCode) {
        runOnUiThread(new Runnable() {
            public void run() {
                Dialog dialog = GooglePlayServicesUtil.getErrorDialog(statusCode, MainActivity.this, REQUEST_GOOGLE_PLAY_SERVICES);
                if (dialog != null) {
                    dialog.show();
                }
            }
        });
    }
}
