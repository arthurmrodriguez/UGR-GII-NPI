package app.interfazoralapp.example.com.interfazoralapp;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import ai.api.AIListener;
import ai.api.android.AIConfiguration;
import ai.api.android.AIService;
import ai.api.model.AIError;
import ai.api.model.AIResponse;
import ai.api.model.Result;
import java.util.Locale;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

//Clase principal que hereda de la clase Activity e implementa el AiListener para el procesamiento de lenguaje natural.
public class MainActivity extends Activity implements AIListener {


    // Imagen del personaje con el que estamos dialogando
    private ImageButton imagenPersonaje;

    // Imagen de la bandera con el idioma actual
    private ImageButton imagenBandera;

    // Campo de texto de respuesta del agente
    private TextView resultTextView;

    //Instancia del servicio que hará las solicitudes de consulta
    private AIService aiService;

    //Instancia del motor TextToSpeech que usaremos para sintetizar el texto obtenido del agente de DialogFlow
    private TextToSpeech mytts = null;

    // Atributo que identifica el intento de comprobar si el sintetizador está instalado
    private int TTS_DATA_CHECK = 12;

    // Atributo que identifica la petición de los permisos de grabación de audio
    private final int MY_PERMISSIONS_REQUEST_RECORD_AUDIO = 22;

    // Identificador del servicio que utilizamos para añadir las salidas de los logs
    private static final String LOGTAG = "API.AI";

    // Idioma activo tanto de sintentización como de reconocimiento de audio
    private String idiomaActivo = "ES";

    // Token de acceso al agente de dialog flow español
    private String dialogEN = "eb63588f8aee4e5bb07549b3c5ef1aba";

    // Token de acceso al agente de dialog flow inglés
    private String dialogES = "51ac2ae97a5049beba6c5fc6568d27fb";


    // Booleano que nos servirá para que la primera vez que inicialicemos la aplicación no nos lea el campo de texto.
    private boolean comprobarPermisos = true;


    // Método donde asignaremos a cada variable su correspondencia con los objetos de la vista e
    // inicializaremos el servicio de reconocimiento de voz y de sintetización.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imagenBandera = (ImageButton) findViewById(R.id.imageButton2);
        imagenPersonaje = (ImageButton) findViewById(R.id.imageCooperativista);
        resultTextView = (TextView) findViewById(R.id.resultTextView);

        //Desactivaremos los botones de la vista hasta que se haya comprobado que esté disponible el motor de TTS
        disableSpeakButton();

        //Creamos una instancia de AIConfiguration para indicar que agente se usará para el procesamiento del lenguaje.
        //Indicaremos el token de acceso del cliente, el lenguaje que soporta y le indicaremos que usará el motor de reconocimiento del sistema.
        final AIConfiguration config = new AIConfiguration(dialogES,
                AIConfiguration.SupportedLanguages.Spanish,
                AIConfiguration.RecognitionEngine.System);

        Toast.makeText(MainActivity.this, "Spanish", Toast.LENGTH_LONG).show();

        // Utilizamos la instancia de AIConfiguration creada anteriormente para obtener un objeto que haga referencia al AIService,
        // que será el que haga las solicitudes de consulta
        aiService = AIService.getService(this, config);

        // Establecemos la instancia de AIListener para la instancia de AIService
        aiService.setListener(this);

        // Iniciamos el motor de síntesis TTS, con las comprobaciones necesarias
        initTTS();

    }

    /**
     * Inicia la comprobación de saber si el motor de síntesis de voz está o no instalado.
     * Cuando termina llama al método onActivityResult automáticamente.
     */

    private void initTTS() {

        Intent checkIntent = new Intent(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkIntent, TTS_DATA_CHECK);
    }


    /**
     * Función que es llamada cuando se comprueba si el motor de síntesis está instalado
     * Si está instalado, creamos una instancia de TextToSpeech para realizar síntesis de audio.
     * Si no está instalado, creamos un intento para instalar el motor de TextToSpeech.
     */
    @Override
    protected void
    onActivityResult(int requestCode, int resultCode, Intent data) {

        // Comprobamos que el requestCode sea el código del intento de comprobar que el sintetizador está instalado
        if (requestCode == TTS_DATA_CHECK) {

            // Si el resultCode coincide con el atributo CHECK_VOICE_DATA_PASS entonces sabremos que el motor TTS está disponible
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                // Creamos la instancia de TexToSpeech con el idioma, inicialmente Español
                mytts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
                    public void onInit(int status) {
                        if (status == TextToSpeech.SUCCESS) {
                            //Establecer idioma español
                            Locale l = new Locale("ES", "es");
                            if (mytts.isLanguageAvailable(l) >= 0)
                                mytts.setLanguage(l);
                        }
                        enableSpeakButton();
                    }
                });

            } else {
                // Si el motor TTS no esta instalado intentamos instalarlo
                Intent installIntent = new Intent();
                installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);

                PackageManager pm = getPackageManager();
                ResolveInfo resolveInfo = pm.resolveActivity(installIntent, PackageManager.MATCH_DEFAULT_ONLY);

                //Si no podemos instalarlo automáticamente le pedimos al usuario que lo instale él mismo
                if (resolveInfo != null) {
                    startActivity(installIntent);
                } else {
                    Toast.makeText(MainActivity.this, "There is no TTS installed, please download it from Google Play", Toast.LENGTH_LONG).show();
                }
            }
        }
    }


    /**
     * Función que es llamada cuando pulsamos sobre el personaje para iniciar la interacción con él.
     * Comprobará los permisos para la grabación de audio y el AIService empezará a escuchar.
     */
    public void listenButtonOnClick(final View view) {

        //Sólo escuchamos si estamos conectados a internet
        if(estaConectado(getApplicationContext())){
            // Detenemos la síntesis de voz si pulsamos el botón para escuchar otra vez.

            if(mytts.isSpeaking()) {
                mytts.stop();
            }

            disableSpeakButton();
            //Comprobamos los permisos antes de empezar a escuchar
            checkPermission();

            aiService.startListening();
        }
        else{
            Toast.makeText(MainActivity.this, "No está conectado a internet", Toast.LENGTH_LONG).show();
        }

    }


    /**
     * Comprobamos si el usuario garantiza a la aplicación el uso del micrófono.
     * El resultado de la consulta es procesado en en la función onRequestPermissionsResult.
     */
    public void checkPermission() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO))
                Toast.makeText(getApplicationContext(), "SimpleASR must access the microphone in order to perform speech recognition", Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO},
                    MY_PERMISSIONS_REQUEST_RECORD_AUDIO); //Callback in "onRequestPermissionResult"
        }
    }

    /**
     * Función que procesa el resultado de la petición de permisos para la grabación de audio.
     * */
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if(requestCode == MY_PERMISSIONS_REQUEST_RECORD_AUDIO) {
            // Si la petición es cancelada, limpiamos el vector de resultados.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.i(LOGTAG, "Los permisos para la grabación de audio están activos");
            }
            else {
                Log.i(LOGTAG, "Record audio permission denied");
                Toast.makeText(getApplicationContext(), "Sorry, SimpleASR cannot work without accessing the microphone", Toast.LENGTH_SHORT).show();
            }
            comprobarPermisos = true;
        }
    }


    /**
     * Función que es llamada cuando pulsamos la bandera del idioma. Al pulsar cambiaremos el idioma en el que escucha y sintetiza la aplicación.
     */
    public void idiomaButtonOnClick(final View view) {

        // Detenemos la síntesis de voz si pulsamos el botón para cambiar el idioma.
        if(mytts.isSpeaking()) {
            mytts.stop();
        }

        disableSpeakButton();

        // Si el idioma activo es el Español, cambiamos la bandera a la inglesa, reiniciamos el servicio AIService con el idioma inglés
        // y cambiamos el idioma del sintetizador a inglés
        if (idiomaActivo == "ES"){
            idiomaActivo = "EN";
            AIConfiguration config = new AIConfiguration(dialogEN,
                    AIConfiguration.SupportedLanguages.English,
                    AIConfiguration.RecognitionEngine.System);
            imagenBandera.setImageResource(R.mipmap.england);
            Toast.makeText(MainActivity.this, "English", Toast.LENGTH_LONG).show();
            aiService = AIService.getService(this, config);
            aiService.setListener(this);

            resultTextView.setText("Ask me!");

            if (mytts.isLanguageAvailable(Locale.US) >= 0)
                mytts.setLanguage(Locale.US);

        }
        else if (idiomaActivo == "EN"){
            // Si el idioma activo es el Inglés, cambiamos la bandera a la española, reiniciamos el servicio AIService con el idioma español
            // y cambiamos el idioma del sintetizador a español
            idiomaActivo = "ES";
            AIConfiguration config = new AIConfiguration(dialogES,
                    AIConfiguration.SupportedLanguages.Spanish,
                    AIConfiguration.RecognitionEngine.System);
            imagenBandera.setImageResource(R.mipmap.spain);
            Toast.makeText(MainActivity.this, "Spanish", Toast.LENGTH_LONG).show();
            aiService = AIService.getService(this, config);
            aiService.setListener(this);

            Locale l = new Locale("ES", "es");
            if (mytts.isLanguageAvailable(l) >= 0)
                mytts.setLanguage(l);
        }
        enableSpeakButton();

    }

    /**
     * Desactiva los botones del cambio de idioma y del botón de escucha.
     */
    private void disableSpeakButton() {
        imagenPersonaje.setEnabled(false);
        imagenBandera.setEnabled(false);
    }

    /**
     * Activa los botones del cambio de idioma y del botón de escucha.
     */
    private void enableSpeakButton() {
        imagenPersonaje.setEnabled(true);
        imagenBandera.setEnabled(true);
    }

    /**
     * Función llamada cuando obtenemos la respuesta del agente mediante el AIService
     */
    public void onResult(final AIResponse response) {

        // Obtenemos el resultado de la solicitud
        Result result = response.getResult();
        String speech = result.getFulfillment().getSpeech();

        // Mostramos la respuesta en el campo de texto.
        resultTextView.setText(speech);

        //Sintetizamos la respuesta usando la instancia de TextToSpeech
        Synthesized();

    }

    /**
     * Función utilizada para sintetizar el texto que tenemos en el campo de texto.
     */
    private void Synthesized(){
        String text = resultTextView.getText().toString();

        // Si tenemos un texto no nulo en el campo de texto, lo sintetizamos.
        if (text != null && text.length() > 0) {

            //Para versiones de la SDK superiores a 21 llamamos de una forma distinta al speak que con versiones inferiores
            if (Build.VERSION.SDK_INT >= 21) {
                //Con el QUEUE_FLUSH eliminamos todos los elementos de la cola de reproducción e introducimos los nuevos.
                mytts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "msg");
            } else {
                mytts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
            }

        }
        enableSpeakButton();
    }


    // Tratamiento de error al escuchar cuando nos conectamos con el servidor
    @Override
    public void onError(final AIError error) {

        if(comprobarPermisos == true){
            comprobarPermisos = false;
            //resultTextView.setText(error.toString());
            if(idiomaActivo == "EN"){
                resultTextView.setText("Ask me!");
            }
            else if(idiomaActivo == "ES"){
                resultTextView.setText("¡Hazme una pregunta!");
            }
            enableSpeakButton();
        }
        else {
            if (idiomaActivo == "EN") {
                resultTextView.setText("I haven't listened anything or there are no answers for your question.");

            } else if (idiomaActivo == "ES") {
                resultTextView.setText("No he escuchado nada o no hay resultados para su pregunta.");

            }
            Synthesized();
        }
    }

    // Métodos necesarios para implementar la clase abstracta de AIListener

    /**
     * Apagar el motor TTS cuando terminemos
     */
    @Override
    public void onDestroy() {
        if (mytts != null) {
            mytts.stop();
            mytts.shutdown();
        }

        super.onDestroy();
    }


    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onStop() {
        super.onStop();

    }


    @Override
    public  void onListeningStarted () {

    }

    @Override
    public  void onListeningCanceled () {

    }

    @Override
    public  void onListeningFinished () {


    }

    @Override
    public  void onAudioLevel ( final  float level) {}

    /**
     * Función que comprueba si el dispositivo está conectado a internet. Habría que comprobar además el caso en que estuvieramos conectados a redes privadas.
     * En este caso habría que hacer ping para comprobar que de verdad estamos teniendo acceso a internet y no sólo conectados a la red.
     * Referencia: https://es.stackoverflow.com/questions/41864/cómo-comprobar-la-conexión-a-internet
     */
    public static boolean estaConectado(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isAvailable() && networkInfo.isConnected();
    }

}

