# UGR-GII-NPI
Proyecto final de la asignatura de Nuevos Paradigmas de Interacción, 4º curso del Grado de Ingeniería Informática de la UGR

## Interfaz Gestual
Utilizando el dispositivo de detección controlada del movimiento de las manos, Leap Motion (https://www.leapmotion.com), se ha creado un proyecto llamado InterfazGestual utilizando la IDE de https://www.jetbrains.com IntelliJ IDEA. En este proyecto se realiza un procesamiento de la información recibida por el dispositivo para interactuar con una GUI que simula una sala de un museo, concretamente una de armas medievales, que a través de gestos permite desplazarnos por las distintas imagenes y seleccionar aquella que queramos ver con más detalle, utilizando movimientos simples de la mano para interactuar con ésta.

## Interfaz Oral
Utilizando el Android SDK 24 (Android 7 Nougat) se ha desarrollado una interfaz oral a través de Android Studio enlazada con el servicio DialogFlow, que permite realizar un procesamiento que transforme lo que dice un usuario en texto plano. De esta forma se ha implementado (siguiendo la idea de un museo interactivo) un agente conversacional que recrea un trabajador o figura importante de una época determinada que responde a la preguntas del usuario de forma natural, explicándole su labor y detallando información importante de la época.

## Aplicacion Museo
Otra vez valiéndonos de Android Studio y con compatibilidad desde SDK 23, se ha desarrollado una app movil que utiliza los sensores de un smartphone para proporcionar una interacción adicional entre el museo y el visitante. En ésta app se han utilizado los siguientes sensores:

### GPS: permite activar la aplicación automáticamente sólo cuando se encuentra en el rango del museo.
### NFC: a través de etiquetas NFC distribuidas por el museo, obtenemos información relevante de objetos, construcciones y demás.
### Giroscopio: permite cambiar con un movimiento de muñeca entre la información y las imagenes que nos muestran las etiquetas NFC.
### Pantalla táctil: dispondremos de un mapa interactivo que podremos consultar en todo momento, ampliando la imagen y desplazándonos por éste.

