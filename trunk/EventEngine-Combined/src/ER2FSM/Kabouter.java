package ER2FSM;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashMap;

import ui.FSMViewer;

import ER2FSM.fsm.FSM;

public class Kabouter {

	public static void main(String args[]) {

		/** INIT **/
		try {
			FileWriter file = new FileWriter("ER2FSM.log");

			BufferedWriter bufWriter = new BufferedWriter(file);
			bufWriter.write("nbFragments;startTime;endTime;duration;nbStates" + System.getProperty("line.separator"));

			/** ER2FSM **/
			
//			for (int j=5;j<=25;j=j+5){
				LinkedHashMap<String,String> fragmentERcollection = new LinkedHashMap<String, String>();
				
//				fragmentERcollection.put("-1","[(SignalStartProcess)]");
				int j=11;
				for (int i=0;i<j;i++) {
//					fragmentERcollection.put(Integer.toString(i),"[(Signal" + Integer.toString(i-1) + ")]");
					fragmentERcollection.put(Integer.toString(i),"[(SignalStartProcess)]");
				}

				long startTime = System.currentTimeMillis();
				ER2FSM er2FSM = new ER2FSM(fragmentERcollection);
				FSM fsm = er2FSM.getFSM();
				long endTime = System.currentTimeMillis();

				System.out.println(j + ";" + startTime + ";" + endTime + ";" + (endTime-startTime) 
						+ ";" + fsm.getState().size());
				
//				bufWriter.write(j + ";" + startTime + ";" + endTime + ";" + (endTime-startTime) 
//						+ ";" + fsm.getState().size() + System.getProperty("line.separator"));
//				bufWriter.flush();
				
				FSMViewer viewer = new FSMViewer(fsm);
//			}


			//		bufWriter.write(processInstance + ";" + task + ";" + time + System.getProperty("line.separator"));


			/** END **/

			bufWriter.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}


}
