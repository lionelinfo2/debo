package tests;

import java.util.Arrays;
import java.util.HashSet;

public class StringTest {

	public static void main(String args[]) {
		
		String command = "java -Xss100k -classpath \"jars/eventengine.jar;jars/siena.jar;libs/xmi.jar;libs/ecore.jar;libs/common.jar;libs/bpmn2.jar;libs/wsClient.jar\" " +
			"eventengine.StartEngine PizzaDelivery-dist.bpmn udp:10.10.10.10:2347 udp 0 100";
		Process pc = null;
		
		try {
			pc = Runtime.getRuntime().exec(command);	
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		pc.destroy();
		
		
		
//		System.out.println(Arrays.asList("[]".substring(1,"[]".length()-1).split(", ")));
//		
//		Double sleepTime = (Math.random()*10000);
//		System.out.println(sleepTime.intValue());
		
//		HashSet<Integer> testy = new HashSet<Integer>();
//		
//		testy.add(1);
//		testy.add(5);
//		testy.add(3);
//		
//		System.out.println(testy.toString());
//		
//		
//		String subString = testy.toString().substring(1,testy.toString().length()-1);
//		System.out.println(subString);
//		
//		for (String i: subString.split(", ")) {
//			System.out.println(i);
//		}
	}
}
