package leapCode;

import com.leapmotion.leap.*;
import javax.swing.*;
import javax.swing.ImageIcon;

public class PantallaSeleccion extends JPanel {

    ///Datos miembro: paneles de pestañas para albergar
    ///las distintas imagenes
    private JPanel firstPanel;
    private JTextField headerText;
    private JTextField textoIndicacion;
    private JTabbedPane panelImagenes;
    private JLabel hacha, cimitarra;

    //Variables accesibles desde MainWindow
    GestureList ultimosGestos;
    Gesture.Type tipoUltimoGesto;
    String accionARealizar = "Adelante";
    String imagenSeleccionada = "";

    /**
     * Getter de firstPanel
     * @return PanelPrincipal
     */
    public JPanel getFirstPanel() {
        return firstPanel;
    }

    /**
     * Getter del panel de imagenes
     * @return el panel con pestañas correspondiente a las imagenes
     */
    public JTabbedPane getPanelImagenes() {
        return panelImagenes;
    }

    /**
     * Getter de la imagen seleccionada por el usuario
     * @return imagen seleccionada por el usuario
     */
    public String getImagenSeleccionada() {
        return imagenSeleccionada;
    }

    /**
     * Setter de la accion a realizar
     * @param accionARealizar
     */
    public void setAccionARealizar(String accionARealizar) {
        this.accionARealizar = accionARealizar;
    }


    /**
     * Metodo detectarSentidoGiro
     * En funcion de un gesto de tipo CIRCLE, calcula el sentido de giro
     * segun la vista del usuario
     * @param circle Gesto de tipo circulo
     * @return sentido de giro: horario 1, antihorario 2
     */
    static int detectarSentidoGiro(CircleGesture circle){

        //Sentido giro: horario 1, antihorario 2
        int sentidoGiro;

        if (circle.pointable().direction().angleTo(circle.normal()) <= Math.PI/2)
            // El sentido de las agujas del reloj es un angulo menor de 90 grados
            sentidoGiro = 1;
        else
            sentidoGiro = 2;

        return sentidoGiro;

    }

    /**
     * Metodo detectarDireccionSwipe
     * Detectar la direccion del swipe para saber si pasar a la siguiente
     * pagina o volver a la anterior. Se calcula en funcion de si el desplazamiento en el
     * eje X ha sido mayor que en los demás
     * @param swipe Gesto de tipo SWIPE
     * @return sentido del swipe: Izquierda o derecha
     */
    public int detectarDireccionSwipe(SwipeGesture swipe){

        float fAbsX = Math.abs(swipe.direction().getX());
        float fAbsY = Math.abs(swipe.direction().getY());
        float fAbsZ = Math.abs(swipe.direction().getZ());

        //1 indica derecha, 2 izquierda
        int direccionMovimiento = -1;
        // Ha habido mas desplazamiento en el eje X

        if (fAbsX > fAbsY && fAbsX > fAbsZ) {

            if (swipe.direction().getX() > 0)
                direccionMovimiento = 1;
            else direccionMovimiento = 2;
        }


        return direccionMovimiento;
    }

    /**
     * Metodo comprobarGrab
     * Para comprobar el GRAB se puede comprobar que los 5 dedos de la mano
     * estan recogidos o con la fuerza del grab; en este caso por encima de un 90%
     * se considera GRAB.
     * @param manoActual Hand sobre la que se registraran los eventos
     * @return Si se ha detectado un grab o no
     */
    public boolean comprobarGrab(Hand manoActual){

        //Un gesto de grab se ha tenido que producir cuando
        //se ha producido un determinado valor de fuerza de Grab > 0.9
        boolean accionGrab = false;


        if(manoActual.grabStrength() >= 0.99)
            accionGrab = true;

        return accionGrab;

    }


    /**
     * Metodo gestionarPrimeraPantala
     * Funcion de gestion de la primera pantalla en funcion del ultimo frame
     * recibido en la clase principal.
     * @param ultimoFrame ultimo paquete de datos recibido
     * @return String indicando la accion a realizar
     */
    public String gestionarPrimeraPantalla(Frame ultimoFrame) {

        //Miramos los gestos disponibles
        ultimosGestos = ultimoFrame.gestures();
        int indiceUltimoGesto = ultimosGestos.count() -1;
        tipoUltimoGesto = ultimosGestos.get(indiceUltimoGesto).type();

        //El unico gesto NATIVO aceptado en esta pantalla es el SWIPE
        if (tipoUltimoGesto == Gesture.Type.TYPE_SWIPE) {
            int sentidoSwipe = detectarDireccionSwipe(new SwipeGesture(ultimosGestos.get(indiceUltimoGesto)));
            int imagenActual = panelImagenes.getSelectedIndex();

            switch (sentidoSwipe) {
                case 1: //Derecha
                    if (imagenActual > 0) {
                        panelImagenes.setSelectedIndex(imagenActual - 1);
                        //System.out.println("SWIPE DERECHA");
                    }
                    break;

                case 2: //Izquierda
                    //System.out.println("SWIPE IZQUIERDA");
                    if (imagenActual < panelImagenes.getTabCount() - 1)
                        panelImagenes.setSelectedIndex(imagenActual + 1);
                    break;


            }

        }

        //Para la comprobacion del GRAB hace falta que exista al menos una mano disponible
        else if(ultimoFrame.hands().count() >= 1 && ultimoFrame.gestures().isEmpty()){

            //Se comprobará para cada mano disponible si se ha realizado un GRAB
            HandList manosActuales = ultimoFrame.hands();
            for(Hand manoActual : manosActuales){

                if(comprobarGrab(manoActual)){

                    accionARealizar = "Grab";
                    System.out.println(panelImagenes.getSelectedIndex());
                    if(panelImagenes.getSelectedIndex() == 0)
                        imagenSeleccionada = "src/images/cimitarra";

                    else
                        imagenSeleccionada = "src/images/hacha";
                }

            }

        }

        else {
            accionARealizar = "";
            imagenSeleccionada = "";
        }

        //Para el caso de que no se detecte un SWIPE se comprobará si se ha realizado un
        //GRAB: cerrar el puño, esto es, que detecte una mano pero sin dedos

        return accionARealizar;

    }


}
