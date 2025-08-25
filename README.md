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
