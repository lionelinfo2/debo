package eventengine;

import java.util.ArrayList;

public class DummyTask {

	public static void main (String args[]){
		calculatePrime(10);
	}
	
	public static void calculatePrime(int number) {

//		ArrayList<Integer> primes = new ArrayList<Integer>();
		
		final int UPPER_LIMIT = number;
//        number = UPPER_LIMIT;
        int i = 0;

        while (++i <= UPPER_LIMIT) {
     	
            int i1 = (int) Math.ceil(Math.sqrt(i));
            
            boolean isPrimeNumber = false;

            while (i1 > 1) {
                if ((i != i1) && (i % i1 == 0)) {
                    isPrimeNumber = false;
                    break;
                } else if (!isPrimeNumber) {
                    isPrimeNumber = true;
                }
                --i1;
            }
//            if (isPrimeNumber) {
//            	primes.add(i);
//            }
        }
        
//        System.out.println(primes);
    }

}
