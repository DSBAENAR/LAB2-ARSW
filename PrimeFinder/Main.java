package PrimeFinder;

import java.util.Scanner;

public class Main {
    
    /*
     * This program finds all prime numbers in the range from 0 to 50,000,000
     * using 3 threads. Each thread is responsible for a subrange of numbers.
     * The ranges are divided as evenly as possible among the threads.
     * The time taken to complete the computation is printed at the end.
     */
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
        
    }

}
