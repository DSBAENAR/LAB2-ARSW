package PrimeFinder;
import java.util.LinkedList;
import java.util.List;

public class PrimeFinderThread extends Thread{

	
	int a,b;
	int numThreads;
	private final PauseControl pauseControl;
	
	private List<Integer> primes=new LinkedList<Integer>();
	
	public PrimeFinderThread(int a, int b, PauseControl pauseControl) {
		super();
		this.a = a;
		this.b = b;
		this.pauseControl = pauseControl;
	}
	
	public synchronized void run(){
		
		for (int i=a;i<=b;i++){
			try {
                pauseControl.checkPaused();
            } catch (InterruptedException e) {
                break;
            }
			if (isPrime(i)){
				primes.add(i);
				System.out.println(i);
			}
		}
		
		
	}

	
	
	boolean isPrime(int n) {
	    if (n%2==0) return false;
	    for(int i=3;i*i<=n;i+=2) {
	        if(n%i==0)
	            return false;
	    }
	    return true;
	}

	public List<Integer> getPrimes() {
		return primes;
	}
	
}