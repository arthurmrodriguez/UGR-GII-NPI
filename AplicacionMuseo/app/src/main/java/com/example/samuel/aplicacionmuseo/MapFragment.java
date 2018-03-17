package com.example.samuel.aplicacionmuseo;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;


/**
 * Clase principal con el contenido del fragmento (vista)
 * del subapartado del mapa.
 */
public class MapFragment extends Fragment  {

    //Datos miembro de la clase
    private ScaleGestureDetector detectorEscalado;///Detector de gestos de escalado
    private GestureDetector detectorGestos;///Detector de gestos generales
    private float factorEscala = 1.0f;///Factor de escalado inicial
    private float escalaXMAX = 3.0f;///Maxima escala en el eje X
    private float escalaYMAX = 3.0f;///Maxima escala en el eje Y
    private View vistaMapa;

    /**
     * Constructor vacio de un objeto de tipo MapFragment
     */
    public MapFragment() {
    }

    /**
     * Metodo onCreateView: permite crear la vista sobre el fragmento
     * @param inflater Layout sobre el que se crea la vista
     * @param container
     * @param savedInstanceState
     * @return Objeto View con la vista del fragmento para su posterior modificacion
     */
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //Se crea la vista del fragmento y se asocia para futuras interacciones con
        //objetos que escuchan eventos de tipo táctil o gestos generales
        vistaMapa = inflater.inflate(R.layout.fragment_map, container, false);
        detectorEscalado = new ScaleGestureDetector(vistaMapa.getContext(),new ScaleListener());
        detectorGestos = new GestureDetector(vistaMapa.getContext(),new MyGestureListener());

        //La vista del fragmento tendrá asociado un listener de eventos de tipo tactil que
        //discriminará el tipo de Evento segun el numero de dedos que incidan en la pantalla
        vistaMapa.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {

                //Restringimos el uso de dos dedos unicamente para realizar el zoom
                if(event.getPointerCount()>1){
                    detectorEscalado.onTouchEvent(event);
                }

                //Con un dedo se registran los demas eventos
                else
                    detectorGestos.onTouchEvent(event);

                return true;
            }

        });

        return vistaMapa;
    }

    /**
     * Clase ScaleListener
     * Escucha eventos de tipo escalado, esto es, cuando se realiza un gesto
     * de "Pinch" o "Fling" sobre una vista.
     */
    private class ScaleListener
            extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        /**
         *
         * @param detector Gesto de escalado detectado
         * @return Si se ha detectado un gesto valido o no. Devuelve
         * true siempre porque no se discrimina.
         */
        @Override
        public boolean onScale(ScaleGestureDetector detector) {

            //Se calcula el factor de escala
            factorEscala *= detector.getScaleFactor();
            factorEscala = Math.max(0.1f, Math.min(factorEscala, 5.0f));

            //Se comprueba la escala actual de la vista
            float escalaX, escalaY;
            escalaX = vistaMapa.getScaleX();
            escalaY = vistaMapa.getScaleY();
            boolean escalar = false;

            //Si la escala de la vista es menor que una cota superior y el factor de escalado es mayor que uno
            //se podrá escalar, asi como cuando el factor de escala es menor que 1 pero la escala
            //se mantiene dentro de unos margenes
            if(factorEscala > 1.0 && escalaX < escalaXMAX && escalaY < escalaYMAX)
                escalar = true;

            else
            if(escalaX > 1.0 && escalaY > 1.0 && escalaX < escalaXMAX && escalaY < escalaYMAX )
                escalar = true;

            //Funcion de escalado
            if(escalar)
                escalarVista(detector.getFocusX(), detector.getFocusY(),factorEscala,false);

            return true;
        }

    }

    /**
     *
     * @param pivoteX Valor del eje X del punto que actuará de pivote para el escalado
     * @param pivoteY Valor del eje Y del punto que actuará de pivote para el escalado
     * @param escala Factor de escala que se aplicará a la vista
     * @param reinicio booeleano que reinicia la vista a escala 1/1
     */
    private void escalarVista(float pivoteX, float pivoteY, float escala, boolean reinicio){

        vistaMapa.setPivotX(pivoteX);
        vistaMapa.setPivotY(pivoteY);

        if(reinicio){
            vistaMapa.setScaleX(1.0f);
            vistaMapa.setScaleY(1.0f);
        }

        else {
            vistaMapa.setScaleX(vistaMapa.getScaleX() * escala);
            vistaMapa.setScaleY(vistaMapa.getScaleY() * escala);
        }

    }

    /**
     * Clase MyGestureListener
     * Ejemplo obtenido de StackOverflow (ver memoria)
     * Clase que escucha eventos de tipo gestos generales
     */
    class MyGestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDown(MotionEvent event) {
            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            return true;
        }

        /**
         * Metodo que detecta el gesto LongPress
         * @param e Evento de movimiento de tipo LongPress
         */
        @Override
        public void onLongPress(MotionEvent e) {
            //Reinicia la vista a una proporcion 1/1
            escalarVista(vistaMapa.getWidth()/2, vistaMapa.getHeight()/2, 1.0f,true);
        }

        /**
         * Metodo que detecta el gesto DoubleTap
         * @param e Evento de movimiento de tipo DoubleTap
         * @return si ha ocurrido un Doble Tap o no
         */
        @Override
        public boolean onDoubleTap(MotionEvent e) {

            //Permite realizar Zoom con un gesto de Double Tap
            //La vista se aumenta un 100%
            float escalaX, escalaY;
            escalaX = vistaMapa.getScaleX();
            escalaY = vistaMapa.getScaleY();

            if(escalaX >=1.0 && escalaY >= 1.0 && escalaX < escalaXMAX && escalaY < escalaYMAX ){
                escalarVista(e.getX(),e.getY(),2.0f,false);

            }
            return true;
        }

        /**
         * Metodo que detecta eventos de tipo Scroll
         * @param e1 Evento Scroll inicial
         * @param e2 Event Scroll final
         * @param distanceX distancia recorrida en el eje X
         * @param distanceY distancia recorrida en el eje Y
         * @return
         */
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2,
                                float distanceX, float distanceY) {

            //Realiza un desplazamiento de la vista en la direccion de movimient
            //del gesto Scroll
            vistaMapa.scrollBy((int) distanceX, (int)distanceY);
            return true;
        }

        @Override
        public boolean onFling(MotionEvent event1, MotionEvent event2,
                               float velocityX, float velocityY) {
            return true;
        }
    }

}
