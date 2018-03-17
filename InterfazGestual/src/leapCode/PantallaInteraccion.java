package leapCode;

import com.leapmotion.leap.*;
import com.leapmotion.leap.Frame;

import javax.swing.*;
import java.awt.Image;

public class PantallaInteraccion extends JPanel{

    ///Paneles de interaccion
    private JPanel panelInteraccion;
    private JLabel imagenSeleccionada;
    private JTextField informacionImagen;
    private ImageIcon imagenActual;
    private int widthImgActual= 1000, heightImgActual= 700;
    private String nombreImagen;

    /**
     * Getter del panel principal de esta pantalla
     * @return panel de interaccion
     */
    public JPanel getPanelInteraccion() {
        return panelInteraccion;
    }

    /**
     * Metodo setImagenLabelImage
     * Actualiza la imagen del modelo 3D y guarda su tamaño
     * @param URL
     */
    public void setImagenLabelImage(String URL){

        if(!URL.contains(".jpg")) {
            imagenActual = new ImageIcon(URL+"1.jpg");
            imagenSeleccionada.setIcon(imagenActual);
        }
        else{
            imagenActual = new ImageIcon(URL);
            imagenSeleccionada.setIcon(imagenActual);
        }


        //Guardamos el nombre de la imagen
        if(URL.contains("src/images/cimitarra"))
            nombreImagen = "src/images/cimitarra";
        else
            nombreImagen = "src/images/hacha";


        widthImgActual = imagenActual.getIconWidth();
        heightImgActual = imagenActual.getIconHeight();

        if(widthImgActual == 0)
            widthImgActual = 1000;

        if(heightImgActual == 0)
            heightImgActual = 700;


    }


    /**
     * Metodo zoomImagen
     * El zoom se realiza obteniendo una instancia escalada de la imagen
     * en cuestion y actualizando la vista
     * @param proporcion que se quiere aplicar a la imagen escalada
     */
    public void zoomImagen(double proporcion){

        //Cambiamos la escala de la imagen en funcion de la proporcion deseada
        double nuevoWidth = Math.abs(widthImgActual*proporcion);
        double nuevoHeight = Math.abs(heightImgActual*proporcion);

        //Se crea una nueva instancia de la imagen original escalada
        Image nuevaImg = imagenActual.getImage().getScaledInstance((int)nuevoWidth,(int)nuevoHeight,java.awt.Image.SCALE_SMOOTH);
        imagenActual = new ImageIcon(nuevaImg);
        imagenSeleccionada.setIcon(imagenActual);

    }

    /**
     * Metodo getionarPantallaInteraccion:
     * La gestion de la pantalla de interaccion se encarga de
     * modificar la vista del modelo 3D. En este caso se trata de una
     * serie de imagenes que iremos modificando y conforme se realicen
     * los desplazamientos de la mano siguiendo la posicion relativa a la que
     * se encuentra del dispositivo
     * @param penultimoFrame
     * @param ultimoFrame
     * @return String con la accion a realizar en funcion de los gestos detectados
     */
    public String gestionarPantallaInteraccion(Frame penultimoFrame, Frame ultimoFrame){

        String accion = "";
        //Empezamos por el zoom de la imagen actual
        //El frame que rige es el penultimo, por tanto se harán calculos
        //sobre las manos que habian en el penultimo frame
        int numManosPenultimoFrame = penultimoFrame.hands().count()-1;
        Hand penultimaMano = penultimoFrame.hands().get(numManosPenultimoFrame);
        Hand ultimaMano = ultimoFrame.hands().get(numManosPenultimoFrame);

        //Incrementos de posicion en cada posible eje
        double incrementoX, incrementoY, incrementoZ;
        incrementoX = Math.abs(penultimaMano.palmPosition().getX() - ultimaMano.palmPosition().getX());
        incrementoY = Math.abs(penultimaMano.palmPosition().getY() - ultimaMano.palmPosition().getY());
        incrementoZ = Math.abs(penultimaMano.palmPosition().getZ() - ultimaMano.palmPosition().getZ());

        //El desplazamiento en el eje X me permitirá girar el modelo en el eje Y
        if(incrementoX > incrementoY && incrementoX > incrementoZ){

            //Para esta demostracion utilizaremos cotas para cargar una u otra imagen
            double posicionRelativa = ultimaMano.palmPosition().getX();

            if(posicionRelativa > 0){

                if(posicionRelativa > 50 && posicionRelativa < 100)
                    setImagenLabelImage(nombreImagen + "2.jpg");

                else if(posicionRelativa >100){
                    if(nombreImagen == "src/images/hacha")
                        setImagenLabelImage(nombreImagen+"3.jpg");
                    else
                        setImagenLabelImage(nombreImagen+"2.jpg");
                }
                else
                    setImagenLabelImage(nombreImagen + "1.jpg");

            }

            else{
                if(posicionRelativa < -50 && posicionRelativa > -100)
                    setImagenLabelImage(nombreImagen + "2.jpg");
                else if(posicionRelativa < -100){
                    if(nombreImagen == "src/images/hacha")
                        setImagenLabelImage(nombreImagen+"3.jpg");
                    else
                        setImagenLabelImage(nombreImagen+"2.jpg");
                }
                else
                    setImagenLabelImage(nombreImagen + "1.jpg");

            }

        }

        //El desplazamiento en el eje Z permitirá hacer ZOOM en funcion de
        //la distancia relativa de la mano con el centro del eje de coordenadas del LEAP
        else if(incrementoZ > incrementoX && incrementoZ > incrementoY) {
            //Se escalará en funcion de que tan lejos esté la mano
            //del centro de coordenadas
            double proporcion = 1;
            double deduccion = ultimaMano.palmPosition().getZ() / 100;
            proporcion = proporcion - deduccion;

            if (proporcion < 0.1)
                proporcion = 0.1;

            zoomImagen(proporcion);
        }

        //Permite volver a la pantalla de seleccion de armas con el gesto
        //del CIRCLE en sentido antihorario
        if(!ultimoFrame.gestures().isEmpty()){

            int indiceUltimoGesto = ultimoFrame.gestures().count() -1;
            Gesture ultimoGesto = ultimoFrame.gestures().get(indiceUltimoGesto);

            if(ultimoGesto.type() == Gesture.Type.TYPE_CIRCLE){
                int sentidoGiro = PantallaSeleccion.detectarSentidoGiro(new CircleGesture(ultimoGesto));
                //Se ha detectado el giro hacia atras
                if(sentidoGiro == 2)
                    accion = "Atras";

            }

        }

        return accion;
    }
}
