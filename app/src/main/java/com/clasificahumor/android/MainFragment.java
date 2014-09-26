package com.clasificahumor.android;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

import java.io.Serializable;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by santiago on 16/09/14.
**/
public class MainFragment extends Fragment {

    private static final String CHISTES_KEY = "chistes";
    private static final String INDEX_KEY = "index";

    private static final String MENSAJE_ERROR_COMUNICACION = "Ups... ocurrió un error al intentar comunicarse";

    @InjectView(R.id.chiste)
    TextView mChiste;
    @InjectView(R.id.estrellas)
    RatingBar mEstrellas;
    @InjectView(R.id.calificacion_text)
    TextView mCalificacionText;
    @InjectView(R.id.wrapper_calificacion)
    LinearLayout mWrapperCalificacion;
    @InjectView(R.id.progressBar)
    ProgressBar mProgressBar;

    private List<Tweet> chistes;
    private int index = 0;

    private boolean obteniendoPrimerosChistes = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_main, container, false);
        ButterKnife.inject(this, view);

        if (savedInstanceState != null) {
            chistes = (List<Tweet>) savedInstanceState.getSerializable(CHISTES_KEY);
            index = savedInstanceState.getInt(INDEX_KEY);
            ponerChisteEnUI();
        }

        onConnected();

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

        return view;
    }

    public void onConnected() {
        if (chistes == null && !obteniendoPrimerosChistes) {
            obteniendoPrimerosChistes = true;
            RestService.getInstance().getService().obtenerTresChistes(new Callback<List<Tweet>>() {
                @Override
                public void success(List<Tweet> tweets, Response response) {
                    obteniendoPrimerosChistes = false;
                    chistes = tweets;
                    ponerChisteEnUI();
                }

                @Override
                public void failure(RetrofitError error) {
                    obteniendoPrimerosChistes = false;
                    Crouton.makeText(getActivity(), MENSAJE_ERROR_COMUNICACION, Style.ALERT).show();
                    error.printStackTrace();
                }
            });
        }

    }

    private void ponerChisteEnUI() {
        if (chistes != null) {
            String chiste = chistes.get(index).getText_tweet();
            if (chiste != null) {
                chiste = Html.fromHtml(chiste).toString();
                mChiste.setText(chiste);
            }
            mChiste.setVisibility(View.VISIBLE);
            mProgressBar.setVisibility(View.INVISIBLE);
        }
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
        if (chistes != null) {
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
                            Crouton.makeText(getActivity(), MENSAJE_ERROR_COMUNICACION, Style.ALERT).show();
                            error.printStackTrace();
                        }
                    });

            RestService.getInstance().getService().procesarVoto(chistes.get(index).getId_tweet(), voto, new Callback<Void>() {
                @Override
                public void success(Void aVoid, Response response) {

                }

                @Override
                public void failure(RetrofitError error) {
                    Crouton.makeText(getActivity(), MENSAJE_ERROR_COMUNICACION, Style.ALERT).show();
                    error.printStackTrace();
                }
            });

            index = (index + 1) % chistes.size();

            ponerChisteEnUI();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        menuInflater.inflate(R.menu.menu, menu);
        super.onCreateOptionsMenu(menu, menuInflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.quienes_somos:
                AlertDialog alert = new AlertDialog.Builder(getActivity())
                        .setTitle("¿Quiénes somos?")
                        .setMessage("Somos dos estudiantes (Santiago Castro y Matías Cubero) de Ingeniería en Computación - Universidad de la República, Uruguay, apoyados por la ANII, que estamos realizando un proyecto de grado que pretende detectar humor en textos en español. Para esto necesitamos obtener una base de datos de chistes en español. Gracias a tu ayuda, vamos a poder obtener una base de datos de buena calidad.\n" +
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
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putSerializable(CHISTES_KEY, (Serializable) chistes);
        savedInstanceState.putInt(INDEX_KEY, index);
    }
}
