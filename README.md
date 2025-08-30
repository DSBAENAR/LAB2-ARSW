# Parte I – Antes de terminar la clase.

Creación, puesta en marcha y coordinación de hilos.

1. Revise el programa “primos concurrentes” (en la carpeta parte1), dispuesto en el paquete edu.eci.arsw.primefinder. Este es un programa que calcula los números primos entre dos intervalos, distribuyendo la búsqueda de los mismos entre hilos independientes. Por ahora, tiene un único hilo de ejecución que busca los primos entre 0 y 30.000.000. Ejecútelo, abra el administrador de procesos del sistema operativo, y verifique cuantos núcleos son usados por el mismo.

```java
public class Main {

	public static void main(String[] args) {
		
		PrimeFinderThread pft=new PrimeFinderThread(0, 500000000);
		
		pft.start();
		
		
	}
	
}
```

Con 500 millones de números, en mi caso, consume por máximo el 38% de CPU que son 3 núcleos aproximadamente, se tarda casi 5 minutos.
<img width="1423" height="848" alt="image" src="https://github.com/user-attachments/assets/b4a0840b-f0ba-41ea-9fa5-b9ac881cada7" />

2. Modifique el programa para que, en lugar de resolver el problema con un solo hilo, lo haga con tres, donde cada uno de éstos hará la tarcera parte del problema original. Verifique nuevamente el funcionamiento, y nuevamente revise el uso de los núcleos del equipo.

```java
public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        PrimeFinderThread pft1 = new PrimeFinderThread(0, 500000000);
        PrimeFinderThread pft2 = new PrimeFinderThread(166666667, 333333332);
        PrimeFinderThread pft3 = new PrimeFinderThread(333333333, 500000000);


        try {
            pft1.start();
            pft2.start();
            pft3.start();
            pft1.join();
            pft2.join();
            pft3.join();
            long endTime = System.currentTimeMillis();
            System.out.println("Time taken: " + (endTime - startTime) + "ms ");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }
```

Con 500 millones de números,con 3 hilos, en mi caso, consume aproximadamente el 50% de CPU que son 4 núcleos aproximadamente, Pero ahora tarda casi 2 minutos.
<img width="1470" height="851" alt="image" src="https://github.com/user-attachments/assets/8f347e2c-86ca-4ee4-b765-421fc50a29b8" />

3. Lo que se le ha pedido es: debe modificar la aplicación de manera que cuando hayan transcurrido 5 segundos desde que se inició la ejecución, se detengan todos los hilos y se muestre el número de primos encontrados hasta el momento. Luego, se debe esperar a que el usuario presione ENTER para reanudar la ejecución de los mismo.
   
	## Diseño Solución
   ### 1. Transcurrido 5 segundos desde que se inició la ejecución, se detengan todos los hilos
   Los hilos en Java no tienen un método “pause/resume” seguro (los viejos suspend()/resume() están prohibidos porque podían dejar bloqueos muertos). Si no se puede “parar” un hilo desde fuera, se debe dar a los hilos un punto de control en su bucle:
   - Cada cierto tiempo deben preguntar: “¿me tengo que detener?”
   - Si la respuesta es sí, deben bloquearse hasta que alguien diga “reanuda”.<br>
Eso implica un objeto compartido entre los hilos y el main.

Elegí [Lock/Condition](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/locks/Condition.html 'Lock Interface')  como método de sincronización ya que:

* Bloquea al hilo sin gastar CPU.

* El main puede llamar a pause() y resume() de forma clara.

* Los hilos llaman checkPaused() donde hace falta.

### Diseño de la clase PauseControl
pause() → pone la bandera en true.

resume() → pone la bandera en false y hace signalAll() para despertar a los hilos.

checkPaused() → lo llaman los hilos en cada iteración; si está en pausa, el hilo entra en await() hasta que alguien lo despierte.

```java
package PrimeFinder;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class PauseControl {
    private boolean paused = false;
    private final Lock lock = new ReentrantLock();
    private final Condition unpaused = lock.newCondition();

    
    public void pause() {
        lock.lock();
        try {
            paused = true;
        } finally {
            lock.unlock();
        }
    }

    
    public void resume() {
        lock.lock();
        try {
            paused = false;
            unpaused.signalAll();
        } finally {
            lock.unlock();
        }
    }

    public void checkPaused() throws InterruptedException {
        lock.lock();
        try {
            while (paused) {
                unpaused.await();
            }
        } finally {
            lock.unlock();
        }
    }

}

```

```java
public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        PauseControl pauseControl = new PauseControl();
        PrimeFinderThread pft1 = new PrimeFinderThread(0, 166666666, pauseControl);
        PrimeFinderThread pft2 = new PrimeFinderThread(166666667, 333333332, pauseControl);
        PrimeFinderThread pft3 = new PrimeFinderThread(333333333, 500000000, pauseControl);

        pft1.start();
        pft2.start();
        pft3.start();
        try {
            Thread.sleep(5000);
            pauseControl.pause();
```
   ### 2. Muestre el número de primos encontrados hasta el momento. Luego, se debe esperar a que el usuario presione ENTER para reanudar la ejecución de los mismo.

```java
try {
            Thread.sleep(5000);
            pauseControl.pause();
            
            int totalPrimes = pft1.getPrimes().size() + pft2.getPrimes().size() + pft3.getPrimes().size();
            System.out.println("Primes found in 5s: " + totalPrimes);

            Scanner scanner = new Scanner(System.in);
            System.out.println("Presione ENTER para continuar...");
            scanner.nextLine();
            scanner.close();

            pauseControl.resume();
            pft1.join();
            pft2.join();
            pft3.join();

            long endTime = System.currentTimeMillis();
            System.out.println("Time taken: " + (endTime - startTime) + "ms ");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
```
## PARTE 2 Galgodrómo
### 1. Corrija la aplicación para que el aviso de resultados se muestre sólo cuando la ejecución de todos los hilos ‘galgo’ haya finalizado. Para esto tenga en cuenta:

a. La acción de iniciar la carrera y mostrar los resultados se realiza a partir de la línea 38 de MainCanodromo.

b. Puede utilizarse el método join() de la clase Thread para sincronizar el hilo que inicia la carrera, con la finalización de los hilos de los galgos.

```java
public void actionPerformed(final ActionEvent e) {
                        ((JButton) e.getSource()).setEnabled(false);
                        new Thread() {
                            public void run() {
                                for (int i = 0; i < can.getNumCarriles(); i++) {

                                    galgos[i] = new Galgo(can.getCarril(i), "" + i, reg, control);
                                    galgos[i].start();


                                }

                                //wait for all threads to finish
                                for (int i = 0; i < can.getNumCarriles(); i++) {
                                    try {
                                        galgos[i].join();
                                    } catch (InterruptedException exception) {
                                        exception.printStackTrace();
                                    }
                                }

				                can.winnerDialog(reg.getGanador(),reg.getUltimaPosicionAlcanzada() - 1); 
                                System.out.println("El ganador fue:" + reg.getGanador());
                                ((JButton) e.getSource()).setEnabled(true);
                            }
                        }.start();
```
### 2. Una vez corregido el problema inicial, corra la aplicación varias veces, e identifique las inconsistencias en los resultados de las mismas viendo el ‘ranking’ mostrado en consola (algunas veces podrían salir resultados válidos, pero en otros se pueden presentar dichas inconsistencias). A partir de esto, identifique las regiones críticas () del programa.
Al ejecutar el programa varias veces, tenía resultados correctos, pero a veces tenía un resultado parecido a este:
```sh
El galgo 4 llego en la posicion 1
El galgo 10 llego en la posicion 2
El galgo 16 llego en la posicion 1
El galgo 0 llego en la posicion 3
El galgo 3 llego en la posicion 4
El galgo 12 llego en la posicion 5
El galgo 2 llego en la posicion 6
El galgo 9 llego en la posicion 7
El galgo 11 llego en la posicion 9
El galgo 5 llego en la posicion 8
El galgo 8 llego en la posicion 10
El galgo 1 llego en la posicion 11
El galgo 14 llego en la posicion 12
El galgo 6 llego en la posicion 13
El galgo 7 llego en la posicion 14
El galgo 13 llego en la posicion 15
El galgo 15 llego en la posicion 16
El ganador fue:16
```
Se tiene  una condición de carrera al registrar llegadas. Dos hilos (galgos) están leyendo y escribiendo ultimaPosicionAlcanzada y ganador sin sincronización, así que a veces dos leen “1” al mismo tiempo y ambos se adjudican la posición 1. Luego, como ganador también se escribe sin control, el ganador final puede quedar con el que escribió de último (por eso se ve “El galgo 4 llegó en la posición 1” y también “El galgo 16 llegó en la posición 1”, pero el ganador mostrado es 16)

### 3. Utilice un mecanismo de sincronización para garantizar que a dichas regiones críticas sólo acceda un hilo a la vez. Verifique los resultados.



```java
package DogTrack;

public class RegistroLlegada {

	private int ultimaPosicionAlcanzada=1;

	private String ganador=null;

	public synchronized int marcarLlegada(String nombreGalgo) {
        if (ganador == null) {
            ganador = nombreGalgo;
        }
        return ultimaPosicionAlcanzada++;
    }

	
	public synchronized String getGanador() {
		return ganador;
	}

	public void setGanador(String ganador) {
		this.ganador = ganador;
	}

	public synchronized int getUltimaPosicionAlcanzada() {
		return ultimaPosicionAlcanzada;
	}

	public void setUltimaPosicionAlcanzada(int ultimaPosicionAlcanzada) {
		this.ultimaPosicionAlcanzada = ultimaPosicionAlcanzada;
	}
```
	
}

Y modificando la clase 'Galgo'

```java
public void corra() throws InterruptedException {
		while (paso < carril.size()) {		
			Thread.sleep(100);
			carril.setPasoOn(paso++);
			carril.displayPasos(paso);
			
			if (paso == carril.size()) {						
				carril.finish();
				int ubicacion=regl.marcarLlegada(getName());
				regl.setUltimaPosicionAlcanzada(ubicacion+1);
				System.out.println("El galgo "+this.getName()+" llego en la posicion "+ubicacion);
				if (ubicacion==1){
					regl.setGanador(this.getName());
				}
				
			}
		}
	}
```
### 4. Implemente las funcionalidades de pausa y continuar. Con estas, cuando se haga clic en ‘Stop’, todos los hilos de los galgos deberían dormirse, y cuando se haga clic en ‘Continue’ los mismos deberían despertarse y continuar con la carrera. Diseñe una solución que permita hacer esto utilizando los mecanismos de sincronización con las primitivas de los Locks provistos por el lenguaje (wait y notifyAll).

Para esto creé una clase llamada ControlLock que usa el Lock de java
```java
package DogTrack;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ControlLock {
    private boolean paused = false;
    private final Lock lock = new ReentrantLock();
    private final Condition unpaused = lock.newCondition();


    /*
     * Pauses the thread by setting the paused flag to true.
     */

    public void pause() {
       lock.lock();
        try {
            paused = true;
        } finally {
            lock.unlock();
        }
        
    }

    /*
     * Resumes the thread by setting the paused flag to false and signaling all waiting threads.
     */
    public void resume() {
        lock.lock();
        try {
            paused = false;
            unpaused.signalAll();
        } finally {
            lock.unlock();
        }
    }

    /*
     * Checks if the thread is paused and waits if it is.
     */
    public void checkPaused() throws InterruptedException {
        lock.lock();
        try {
            while (paused) {
                unpaused.await();
            }
        } finally {
            lock.unlock();
        }
    }
}
```

Luego implementé este controlLock en el Galgo dentro de su contructor, para así poder dormir y despertat el hilo cuando sea necesario
```java
ublic Galgo(Carril carril, String name, RegistroLlegada reg, ControlLock control ) {
		super(name);
		this.carril = carril;
		paso = 0;
		this.regl=reg;
        this.control=control;
	}

	public void corra() throws InterruptedException {
		while (paso < carril.size()) {		
            control.checkPaused();
			Thread.sleep(100);
			carril.setPasoOn(paso++);
			carril.displayPasos(paso);
			
			if (paso == carril.size()) {						
				carril.finish();
				int ubicacion=regl.marcarLlegada(getName());
				regl.setUltimaPosicionAlcanzada(ubicacion+1);
				System.out.println("El galgo "+this.getName()+" llego en la posicion "+ubicacion);
				if (ubicacion==1){
					regl.setGanador(this.getName());
				}
				
			}
		}
	}
```

Luego implementaría los botones de stop y continue 

```java
can.setStopAction(
                new ActionListener() {
                    /**
                     * Handles the action event triggered by the associated component.
                     * This implementation pauses the race by invoking the control.pause() method
                     * and prints a message to the console indicating that the race has been paused.
                     *
                     * @param e the ActionEvent that occurred
                     */
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        control.pause();
                        System.out.println("Carrera pausada!");

                    }
                }
        );

        can.setContinueAction(
                new ActionListener() {
                    /**
                     * Handles the action event triggered when the associated component is activated.
                     * Resumes the race by calling the control's resume method and prints a message
                     * indicating that the race has been resumed.
                     *
                     * @param e the ActionEvent that occurred
                     */
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        control.resume();
                        System.out.println("Carrera reanudada!");
                    }
                }
        );
```
