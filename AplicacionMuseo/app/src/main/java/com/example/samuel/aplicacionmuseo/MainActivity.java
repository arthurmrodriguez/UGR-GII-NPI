package com.example.samuel.aplicacionmuseo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.location.LocationManager;
import android.location.LocationListener;
import android.location.Location;
import android.content.Context;
import android.support.v4.content.*;
import android.support.v7.app.AlertDialog;
import android.content.DialogInterface;
import java.util.ArrayList;
import android.nfc.Tag;

import android.nfc.tech.MifareUltralight;


// Creamos la actividad principal MainActivity
public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    // Definimos una variables estática necesaria para los permisos de localización
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    TextView textIntro;
    // adquirimos una referencia al gestor de localización
    LocationManager locationManager;
    // Definimos un listener que responde a las actualizaciones de localización
    LocationListener locationListener;

    // Definimos los rangos de latitud y longitud válidos
    private ArrayList<Double> latitude_range;
    private ArrayList<Double> longitude_range;

    //Para cambiar entre diferentes fragmentos de nuestra aplicación:
    FragmentTransaction fragment_transaction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // creamos los vectores que restringen la latitud y longitud
        latitude_range = new ArrayList<Double>();
        longitude_range = new ArrayList<Double>();
        latitude_range.add(35.0);
        latitude_range.add(40.0);
        longitude_range.add(-4.0);
        longitude_range.add(-2.0);

        // obtenemos el servicio de gestión localización
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        // creamose un objeto locationListener y definimos la acción a realizar cuando la
        // localización varíe (onLocationChanged).
        locationListener = new LocationListener() {

            public void onLocationChanged(Location location) {
                // Called when a new location is found by the network location provider.
                checkLocation(location);
            }

            public void onStatusChanged(String provider, int status, Bundle extras) { }

            public void onProviderEnabled(String provider) { }

            public void onProviderDisabled(String provider) { }
        };


        // antes de hacer uso de los servicios de localización comprobamos (y pedimos en caso de no
        // tenerlos) los permisos de localización del dispositivo
        checkLocationPermission();

        // cada vez que se produce una actualización de localización por parte del NETWORK_PROVIDER
        // o bien por parte del GPS_PROVIDER, esta será tratada por el locationListener antes definido.
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

        // inicialmente mostramos el menú de inicio
        setContentView(R.layout.activity_main);

        // creamos la barra de herramientas
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Botón de ayuda del museo
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Bienvenido a la ayuda del museo", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        // añadimos el menú a la barra superior de la aplicación para abrir la barra de herramientas
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();


        // Inicialmente bloqueamos el acceso a los distintos elementos del menú de herramientas
        // hasta que no se esté en la localización definida dentro del rango (dentro del museo)
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        Menu menuNav=navigationView.getMenu();
        menuNav.findItem(R.id.home_bar).setEnabled(false);
        menuNav.findItem(R.id.map_bar).setEnabled(false);
        menuNav.findItem(R.id.nfc_bar).setEnabled(false);
        menuNav.findItem(R.id.voice_bar).setEnabled(false);

        textIntro = (TextView)(findViewById(R.id.textIntro));
        textIntro.setTextSize(25);
        textIntro.setText("Accediendo al museo...");

        //Aniadimos el fragmento de Home:
        fragment_transaction = getSupportFragmentManager().beginTransaction();
        fragment_transaction.add(R.id.main_container, new HomeFragment());
        navigationView.setNavigationItemSelectedListener(this);
        getSupportActionBar().setTitle("Home");
        fragment_transaction.commit();
    }

    // función para la comprobación de la localización
    protected void checkLocation(Location location) {

        textIntro = (TextView)(findViewById(R.id.textIntro));

        //Accedemos a la barra de navegación
        NavigationView navigationView= findViewById(R.id.nav_view);
        Menu menuNav=navigationView.getMenu();


        // Si no nos encontramos dentro del museo impedimos que se
        // utilice la aplicación:2
        Log.d("Latitud, longitud", "("+Double.toString(location.getLatitude())+ ", "+
                Double.toString(location.getLongitude()));

        Log.d("limites;", "("+Double.toString(location.getLatitude())+ ", "+
                Double.toString(location.getLongitude()));

        if(location.getLatitude() > latitude_range.get(0) &&
                location.getLatitude() < latitude_range.get(1) &&
                location.getLongitude() > longitude_range.get(0) &&
                location.getLongitude() < longitude_range.get(1) ){

            menuNav.findItem(R.id.home_bar).setEnabled(true);
            menuNav.findItem(R.id.map_bar).setEnabled(true);
            menuNav.findItem(R.id.nfc_bar).setEnabled(true);
            menuNav.findItem(R.id.voice_bar).setEnabled(true);

            textIntro.setText("BIENVENIDO AL MUSEO");


        }
        else { // si nos encontramos dentro del museo permitimos el acceso a las funciones de la App



            menuNav.findItem(R.id.home_bar).setEnabled(false);
            menuNav.findItem(R.id.map_bar).setEnabled(false);
            menuNav.findItem(R.id.nfc_bar).setEnabled(false);
            menuNav.findItem(R.id.voice_bar).setEnabled(false);

            textIntro.setText("NO SE ENCUENTRA DENTRO DEL MUSEO");
        }
    }

    // funcion para comprobar los permisos de localización del dispositivo
    public boolean checkLocationPermission() {

        // en caso de que el permiso aún no esté concedido...
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Para mostrar la explicación
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // mostramos de forma asíncrona una explicación al usuario (para no bloquear la
                // hebra esperando a que el usuario responda), cuando el usuario lea el mensaje
                // se intenta de nuevo la petición.
                new AlertDialog.Builder(this)
                        .setTitle(R.string.title_location_permission)
                        .setMessage(R.string.text_location_permission)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();


            } else { // en caso de que ya haya permisos de localización...
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }


    // Función que se encarga del tratamiento de los permisos de uso de la localización
    // en caso de que se haya dado permiso se hace una cosa, en caso contrario otra.
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // los permisos han sido concecidos, procedemos a hacer lo que deseemos
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                    {
                        Log.d("Ejecutando", "Pedimos localizacion");

                        //Actualizamos las peticiones de localización:
                        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
                    }

                } else {

                    // permisos denegados
                    Log.d("Ejecutando", "NO PUEDE ACCEDER AL MUSEO SI NO PROPORCIONA PERMISOS DE UBICACIÓN");

                }
                return;
            }

        }
    }

    // tratamiento del botón Back del dispositivo que estamos utilizando para la aplicación
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    // creación del botón de opciones de la aplicación (el icono que se encuentra arriba a la
    // izquierda)
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    // tratamiento de la herramienta presionada en la barra de herramientas de la aplicación
    // devuelve el botón que hayamos presionado en dicha barra de herramientas (Main, Museum Map,
    // Tag Reader o Voice Assistant)
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        // obtenemos la opción de la barra de herramientas sobre la que se a pulsado
        int id = item.getItemId();

        // Eliminamos el texto de bienvenida al museo existente en la pantalla
        textIntro.setVisibility(View.INVISIBLE);

        // En función del elemento seleccionado en la barra de herramientas mostramos
        // un fragment u otro de la pantalla de la aplicación
        if (id == R.id.voice_bar) {
            fragment_transaction = getSupportFragmentManager().beginTransaction();
            fragment_transaction.replace(R.id.main_container, new VoiceFragment());
            fragment_transaction.commit();
            getSupportActionBar().setTitle("Voice Fragment");

        } else if (id == R.id.home_bar) {
            fragment_transaction = getSupportFragmentManager().beginTransaction();
            fragment_transaction.replace(R.id.main_container, new HomeFragment());
            fragment_transaction.commit();
            getSupportActionBar().setTitle("Home Fragment");

        } else if (id == R.id.map_bar) {
            fragment_transaction = getSupportFragmentManager().beginTransaction();
            fragment_transaction.replace(R.id.main_container, new MapFragment());
            fragment_transaction.commit();
            getSupportActionBar().setTitle("Map Fragment");
        } else if (id == R.id.nfc_bar) {

            fragment_transaction = getSupportFragmentManager().beginTransaction();
            fragment_transaction.replace(R.id.main_container, new NFCFragment());
            fragment_transaction.commit();
            getSupportActionBar().setTitle("NFC Fragment");

        }

        // al mismo tiempo que mostramos en pantalla el fragment seleccionado, cerramos la
        // barra de herramientas de forma automática.
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


}
