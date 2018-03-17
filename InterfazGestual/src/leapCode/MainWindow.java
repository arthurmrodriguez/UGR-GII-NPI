package leapCode;

import com.leapmotion.leap.*;
import com.leapmotion.leap.Frame;
import com.leapmotion.leap.Gesture.*;
import javax.swing.*;
import java.awt.*;

public class MainWindow {

    ///Paneles de cada pantalla
    private JTextField estadoText;///< Estado de conexion del Leap
    private JPanel panelPrincipal;
    private JTextField introText;
    private JFrame mainFrame;
    private JTabbedPane panelTabs; ///< Paneles estilo pestaña
    private PantallaSeleccion panelPrimeraPantalla; ///< Primera pestaña
    private PantallaInteraccion panelInteraccion; ///< Segunda pestaña

    ///Gestion de datos del Leap
    private LeapListener leapListener; ///< Objeto que escucha los eventos del Leap
    private Thread leapThread; ///< Hebra que ejecutará las labores del LeapListener
    private Controller leapController; ///< Objeto controlador del sensor en si
    private Frame ultimoFrame; ///< Ultimo frame recibido del Leap
    private Frame penultimoFrame; ///< Penultimo frame recibido del Leap
    private int manosDisponibles, vecesManoNoDetectada; ///< Contadores auxiliares
    private boolean manoPrimeraVez; ///< Si es la primera vez que detecta una mano


    /**
     *  Constructor de la ventana principal, que es la propia aplicacion
     *  Se crea un objeto de la clase LeapListener y se traslada su funcionamiento
     *  a una hebra para liberar de carga a la app principal
     */
    public MainWindow(){
        vecesManoNoDetectada = 0;
        leapListener = new LeapListener();
        leapThread = new Thread(leapListener);
        leapThread.start();
        leapController = new Controller();
        leapController.addListener(leapListener);
        panelTabs = new JTabbedPane();
        manoPrimeraVez = false;
    }

    /**
     * Metodo addComponentToPane:
     * Permite añadir los componentes que tendrá el JFrame desde donde
     * colgará la GUI: añade las distintas pantallas a cada Tab.
     * @param pane Panel donde se añadiran los componentes
     */
    public void addComponentToPane(Container pane) {

        panelPrimeraPantalla = new PantallaSeleccion();
        panelInteraccion = new PantallaInteraccion();
        panelTabs.addTab("Pantalla Inicio",panelPrincipal);
        panelTabs.setFont(new Font("Courier New",1,20));
        panelTabs.addTab("Museo Interactivo",panelPrimeraPantalla.getFirstPanel());
        panelTabs.addTab("Arma Seleccionada", panelInteraccion.getPanelInteraccion());
        pane.add(panelTabs, BorderLayout.CENTER);
    }


    /**
     * Metodo gestionDeAplicacion:
     * Funcion principal de gestion: redirije el procesamiento
     * a la pagina sobre la que nos encontremos.
     */
    public void gestionDeAplicacion() {

        //Obtener los ultimos datos
        ultimoFrame = leapListener.getUltimoFrame();
        penultimoFrame = leapListener.getPenultimoFrame();
        manosDisponibles = ultimoFrame.hands().count();
        Type tipoGesto = leapListener.getTipoUltimoGesto();

        //Primera vez que colocamos la mano sobre el sensor para comenzar:
        //permite activar el mecanismo de paso a la primera pagina: CIRCLE
        if(manosDisponibles > 0 && !manoPrimeraVez) {
            leapController.enableGesture(Type.TYPE_CIRCLE,true);
            manoPrimeraVez = true;
            if(manoPrimeraVez)
                introText.setText("Realice un giro en sentido horario con un dedo para avanzar");
        }

        //Ya hemos colocado una vez la mano y cuando pase 1 segundo volverá
        // a la pantalla inicial si no detecta la mano
        else if(manosDisponibles == 0 && vecesManoNoDetectada <5){
            vecesManoNoDetectada++;
        }

        //Cuando vuelve a la pantalla inicial se reinician todos los componentes
        else if(vecesManoNoDetectada >=5){

            //Se vuelve a la primera pantalla y se reestablecen los objetos que se cargan
            //de forma dinamica
            panelTabs.setSelectedIndex(0);
            panelPrimeraPantalla.getPanelImagenes().setSelectedIndex(0);
            panelInteraccion.setImagenLabelImage("");

            //System.out.println("VOLVIENDO PAGINA 0");
            introText.setText("Coloca una mano sobre el sensor para comenzar");
            vecesManoNoDetectada = 0;
            manoPrimeraVez = false;
            leapListener.setUltimoGesto(null);
        }

        else
            vecesManoNoDetectada=0;


        //Mientras haya manos disponibles y exista algun gesto distinto del nulo
        if(manosDisponibles > 0 && manoPrimeraVez && leapListener.getUltimoGesto() != null ) {

            //En funcion de la pagina donde nos encontremos cargaremos un contenido u otro
            switch (panelTabs.getSelectedIndex()) {

                //Pagina de inicio que detecta dos gestos: colocar la mano sobre el dispositivo
                //y el CIRCLE para pasar de pagina
                case 0:
                    //Haciendo el gesto de circulo en la primera pantalla permite pasar de la misma
                    if (tipoGesto == Type.TYPE_CIRCLE) {

                        //Giro con valor 1 indica sentido horario
                        if (panelPrimeraPantalla.detectarSentidoGiro(
                                new CircleGesture(leapListener.getUltimoGesto())) == 1) {

                            panelTabs.setSelectedIndex(1);
                            leapController.enableGesture(Type.TYPE_SWIPE, true);
                            leapController.enableGesture(Type.TYPE_CIRCLE,false);
                            panelPrimeraPantalla.setAccionARealizar("");

                        }
                    }
                    break;

                //Pagina de seleccion de arma: detecta el gesto de GRAB, que se produce
                //cuando la mano está cerrada un 90% o mas
                case 1:
                    String accion = panelPrimeraPantalla.gestionarPrimeraPantalla(ultimoFrame);

                    if(accion == "Grab"){
                        leapController.enableGesture(Type.TYPE_SWIPE, false);
                        leapController.enableGesture(Type.TYPE_CIRCLE, true);
                        panelInteraccion.setImagenLabelImage(panelPrimeraPantalla.getImagenSeleccionada());
                        panelTabs.setSelectedIndex(2);
                        panelPrimeraPantalla.setAccionARealizar("");

                    }

                    break;

                //Pagina de interaccion con el arma: permite hacer zoom y desplazarnos por las
                //distintas vistas del objeto en funcion de la posicion relativa de la mano en los ejes X y Z
                case 2:
                    String accionInteraccion = panelInteraccion.gestionarPantallaInteraccion(penultimoFrame,ultimoFrame);
                    if(accionInteraccion == "Atras"){
                        panelTabs.setSelectedIndex(1);
                        leapController.enableGesture(Type.TYPE_SWIPE, true);
                        //En la pagina 1 desactivamos el gesto del circulo
                        leapController.enableGesture(Type.TYPE_CIRCLE,false);
                        panelPrimeraPantalla.setAccionARealizar("");
                    }

                    break;

            }
        }

    }

    /**
     * Main de la aplicacion.
     * Crea el frame principal y se inicializa el contenido del mismo
     * @param args
     * @throws InterruptedException
     */
    public static void main(String[] args) throws InterruptedException {

        //Se crea el frame principal sobre el que colgara la aplicacion
        JFrame frame = new JFrame("Museo Memoria Historica");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //Se crea/inicializa el contenido del frame
        MainWindow demo = new MainWindow();
        demo.addComponentToPane(frame.getContentPane());
        frame.pack();
        frame.setVisible(true);

        if(demo.leapListener.isConectado()){
            demo.estadoText.setText("LEAP MOTION ON");
        }


        //Siempre que este conectado el dispositivo, cada 200ms
        //se gestiona la aplicacion. De esta forma el controlador del
        //Leap se mantiene como hijo de la mainthread y por tanto en primer plano
        while(demo.leapListener.isConectado()){
            demo.gestionDeAplicacion();
            Thread.sleep(200);
        }

        demo.estadoText.setText("LEAP MOTION OFF");
    }
}
