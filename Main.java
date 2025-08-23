public class Main {

	public static void main(String[] args) {
		
		PrimeFinderThread pft=new PrimeFinderThread(0, 500000000);
		
		pft.start();
		
		
	}
	
}
