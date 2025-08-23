package PrimeFinder;
public class Main {

    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        PrimeFinderThread pft=new PrimeFinderThread(0, 50000000);
        pft.start();
        try {
            pft.join(); // esperar a que termine el hilo
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        long end = System.currentTimeMillis();
        System.out.println("Tiempo: "+(end-start)+" ms");
    }
    
}
