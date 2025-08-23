package PrimeFinder;

public class Main {

    public static void main(String[] args) {
        final int numThreads = Runtime.getRuntime().availableProcessors();
        final int from = 0;
        final int to = 500_000_000;

        PrimeFinderThread[] threads = new PrimeFinderThread[numThreads];
        int range = to - from + 1;
        int base = range / numThreads;
        int rem = range % numThreads;

        int start = from;
        for (int i = 0; i < numThreads; i++) {
            int chunk = base + (i < rem ? 1 : 0);
            int end = start + chunk - 1;
            if (end > to) end = to;
            threads[i] = new PrimeFinderThread(start, end);
            start = end + 1;
        }

        long t0 = System.currentTimeMillis();
        for (PrimeFinderThread t : threads) t.start();
        for (PrimeFinderThread t : threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        long t1 = System.currentTimeMillis();
        System.out.println("Total time: " + (t1 - t0) + " ms");
        
    }

}
