package leapCode;

import java.io.IOException;
import java.lang.Math;
import com.leapmotion.leap.*;
import com.leapmotion.leap.Gesture.State;


/**
 * Clase LeapListener
 * Clase principal que gestiona los eventos relacionados
 * con el sensor LeapMotion. Esta clase contiene todos los metodos
 * necesarios para gestionar todos los eventos del sensor.
 * Implementa la interfaz Runnable para ejecutarse en una hebra secundaria
 * (Hija de la hebra principal) de forma que no ralentice la ejecucion de la
 * aplicacion principal
 */
class LeapListener extends Listener implements Runnable {

    ///Datos miembros de la clase
    private Frame ultimoFrame = null;///< Ultimo paquete de datos emitido por el sensor
    private Frame penultimoFrame = null;///< Penultimo paquete de datos emitido por el sensor
    private Gesture ultimoGesto = null;///< Ultimo gesto detectado por el dispositivo
    private Gesture.Type tipoUltimoGesto = Gesture.Type.TYPE_INVALID; ///< Tipo del ultimo gesto detectado
    private String sentidoGiro;
    private boolean conectado = false; ///< Controla la conexion del sensor Leap


    /**
     * Getter del dato miembro conectado
     * @return conectado
     */
    public boolean isConectado(){
        return conectado;
    }

    /**
     * Metodo onInit
     * Notifica que el LeapMotion se ha iniciado satisfactoriamente
     * @param controller Objeto Contoller que representa el dispositivo en sí
     */
    public void onInit(Controller controller) {
        System.out.println("Iniciando...");
    }

    /**
     * Metodo onConnect
     * Notifica que el dispositivo esta conectado y listo para utilizarse
     * @param controller Objeto Contoller que representa el dispositivo en sí
     */
    public void onConnect(Controller controller) {
        System.out.println("Conectado");
        conectado = true;

    }

    /**
     * Metodo onDisconnect
     * Notifica que el dispositivo se ha desconectado
     * @param controller Objeto Contoller que representa el dispositivo en sí
     */
    public void onDisconnect(Controller controller) {
        //Note: not dispatched when running in a debugger.
        System.out.println("Desconectado...");
        conectado = false;
    }

    /**
     * Metodo onExit
     * Notifica que el dispositivo ya no esta disponible
     * @param controller Objeto Contoller que representa el dispositivo en sí
     */
    public void onExit(Controller controller) {
        System.out.println("Saliendo... ");
        conectado = false;
    }

    /**
     * Metodo onFrame
     * Gestiona la recepcion de los paquetes de datos del Leap.
     * Actualiza los dos ultimos frames recibidos y el ultimo gesto (y su tipo)
     * @param controller Objeto Contoller que representa el dispositivo en sí
     */
    public void onFrame(Controller controller){

        ///Obtiene los frames mas recientes en caso de que el ultimo no sea nulo
        if(controller.frame() != null) {
            penultimoFrame = controller.frame(1);
            ultimoFrame = controller.frame();

            ///Obtiene el ultimo gesto y el tipo del mismo
            for (Gesture gesto : ultimoFrame.gestures()){
                ultimoGesto = gesto;
                tipoUltimoGesto = ultimoGesto.type();
            }
        }

        else
            ultimoFrame = null;

    }

    /**
     * Getter del dato miembro ultimoFrame
     * @return ultimoFrame
     * @throws NullPointerException
     */
    public Frame getUltimoFrame()throws NullPointerException{
        if(ultimoFrame == null)
            throw new NullPointerException();
        return ultimoFrame;
    }

    /**
     * Getter del dato miembro penultimoFrame
     * @return penultimoFrame
     * @throws NullPointerException
     */
    public Frame getPenultimoFrame() throws NullPointerException{
        if(penultimoFrame == null)
            throw new NullPointerException();
        return penultimoFrame;
    }

    /**
     * Getter del dato miembro ultimoGesto
     * @return ultimoGesto
     */
    public Gesture getUltimoGesto() {
        return ultimoGesto;
    }

    /**
     * Getter del dato miembro tipoUltimoGesto
     * @return tipoUltimoGesto
     */
    public Gesture.Type getTipoUltimoGesto() {
        return tipoUltimoGesto;
    }

    /**
     * Setter del dato miembro ultimoGesto
     * @param ultimoGesto
     */
    public void setUltimoGesto(Gesture ultimoGesto) {
        this.ultimoGesto = ultimoGesto;
    }

    /**
     *Metodo run que permite a la hebra ejecutarse
     * y capturar los eventos que relacionados con el Leap
     */
    public void run() {

    }
}

