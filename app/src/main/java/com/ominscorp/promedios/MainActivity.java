package com.ominscorp.promedios;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {
    private int hint = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(@NonNull InitializationStatus initializationStatus) {
            }
        });

        LinearLayout layout = findViewById(R.id.scroll_notas);
        crea_fragment(layout);
        crea_fragment(layout);

        FrameLayout adContainerView = findViewById(R.id.adView);
        // Step 1 - Create an AdView and set the ad unit ID on it.
        AdView mAdView = new AdView(this);
        mAdView.setAdUnitId("ca-app-pub-4235180355584066/7978558256");
        adContainerView.addView(mAdView);
        loadBanner(mAdView);

        AdView mAdView1 = findViewById(R.id.adView1);
        AdRequest adRequest1 = new AdRequest.Builder().build();
        mAdView1.loadAd(adRequest1);

    }

    private void loadBanner(AdView mAdView) {
        AdRequest adRequest = new AdRequest.Builder().build();
        AdSize adSize = getAdSize();
        mAdView.setAdSize(adSize);
        mAdView.loadAd(adRequest);
    }

    private AdSize getAdSize() {
        Display display = getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);

        float widthPixels = outMetrics.widthPixels;
        float density = outMetrics.density;

        int adWidth = (int) (widthPixels / density);

        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, adWidth);
    }

    public void crea_fragment(LinearLayout layout){
        LinearLayout fragment = (LinearLayout) this.getLayoutInflater().inflate(R.layout.card_notas, null);
        int maxLength = 10;
        if (hint>maxLength){
            return;
        }
        fragment.setId(hint);
        EditText edittext = fragment.findViewById(R.id.card_text);
        edittext.setHint(getString(R.string.nota_numero)+" "+hint);
        InputFilter[] fArray = new InputFilter[1];
        fArray[0] = new InputFilter.LengthFilter(maxLength);
        edittext.setFilters(fArray);
        layout.addView(fragment);
        hint++;
        //String msj="creado fragmento id: "+fragment.getId()+" texto: "+edittext.getText().toString();
    }

    public void menos_notas(View view) {
        LinearLayout layout = findViewById(R.id.scroll_notas);
        int largo=layout.getChildCount();
        if(largo<=2){
            String msj=getResources().getString(R.string.msj_error_largo_notas);
            Toast toast = Toast.makeText(this, msj, Toast.LENGTH_SHORT);
            toast.show();
        }
        else{
            layout.removeViewAt(largo-1);
            hint=hint-1;
        }
    }

    public void mas_notas(View view) {
        LinearLayout layout = findViewById(R.id.scroll_notas);
        crea_fragment(layout);
    }

    public void calcular(View view) {
        LinearLayout layout = findViewById(R.id.scroll_notas);
        int cantidad=layout.getChildCount();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        float promedio=0;
        float suma_porcentajes=0;
        String msj;
        float nota_aprovacion = Float.parseFloat(sharedPreferences.getString("nota aprovacion", "40"));
        for(int i =1; i<=cantidad; i++){
            LinearLayout layout1 = layout.findViewById(i);
            //System.out.println(layout1.getId());
            EditText nota_txt = layout1.findViewById(R.id.card_text);
            EditText porcentaje_txt = layout1.findViewById(R.id.porcentaje_txt);
            String validar = validar_datos(nota_txt.getText().toString(),porcentaje_txt.getText().toString());
            if(!validar.equals("1")){
                //System.out.println(validar);
                Toast toast = Toast.makeText(this, validar, Toast.LENGTH_SHORT);
                toast.show();
                return;
            }
            else {
                float nota = Float.parseFloat(nota_txt.getText().toString());
                float porcentaje = Float.parseFloat(porcentaje_txt.getText().toString());
                System.out.println(nota);
                System.out.println(porcentaje);
                suma_porcentajes+=porcentaje;
                promedio += ((porcentaje / 100) * nota);
            }
        }
        if(suma_porcentajes!=100){
            msj = (getResources().getString(R.string.msj_error_porcentaje_part_1)+suma_porcentajes+getResources().getString(R.string.msj_error_porcentaje_part_2));
            Toast toast = Toast.makeText(this, msj, Toast.LENGTH_SHORT);
            toast.show();
        }else{
            TextView nota_label = findViewById(R.id.texto_nota);
            nota_label.setText(Float.toString(promedio));
            if(nota_aprovacion>promedio){
                nota_label.setTextColor(Color.RED);
            }
            else{
                nota_label.setTextColor(Color.GREEN);
            }
            //System.out.println(promedio);
        }
    }

    public String validar_datos(String nota_txt, String porcentaje_txt){
        String msj;
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        //System.out.println("nota max: "+sharedPreferences.getString("nota maxima","70"));
        float nota_max = Float.parseFloat(sharedPreferences.getString("nota maxima","70"));
        float nota_min = Float.parseFloat(sharedPreferences.getString("nota minima", "10"));
        //System.out.println("nota max: "+nota_max+" nota min: "+nota_min);
        try {
            float nota = Float.parseFloat(nota_txt);
            float porcentaje = Float.parseFloat(porcentaje_txt);
            if(nota>nota_max || nota<nota_min){//hacer archivo config para nota maxima y minima
                msj=getResources().getString(R.string.msj_error_nota_fuera_rango);
            }else if(porcentaje>100 || porcentaje<0){
                msj=getResources().getString(R.string.msj_error_rango_porcentaje);
            }else{
                msj= "1";
            }
            return msj;
        }
        catch (Exception e){
            msj=getResources().getString(R.string.msj_error_datos_incorrectos);
            return msj;
        }
    }

    public void go_to_settings(View view) {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    public void limpiar(View view) {
        LinearLayout layout = findViewById(R.id.scroll_notas);
        int cantidad=layout.getChildCount();
        for(int i =1; i<=cantidad; i++) {
            LinearLayout layout1 = layout.findViewById(i);
            EditText nota_txt = layout1.findViewById(R.id.card_text);
            EditText porcentaje_txt = layout1.findViewById(R.id.porcentaje_txt);
            nota_txt.setText("");
            porcentaje_txt.setText("");
        }
        TextView nota_label = findViewById(R.id.texto_nota);
        nota_label.setText("");
    }
}
