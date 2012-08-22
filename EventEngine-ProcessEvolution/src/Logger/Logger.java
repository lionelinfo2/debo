package Logger;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import siena.Filter;
import siena.HierarchicalDispatcher;
import siena.Notification;
import siena.NotificationBuffer;

public class Logger {

	FileWriter _file;
	BufferedWriter _bufWriter;
	String _dir = ".\\logs\\";
	
	public void notify(Notification [] s) { }

	public Logger(String fileName) {
		try {
			_file = new FileWriter(_dir + fileName);
			_bufWriter = new BufferedWriter(_file);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public synchronized void write(Notification e) {
		String processInstance = e.getAttribute("processInstanceId").stringValue();
		String task = e.getAttribute("id").stringValue();
		String time = e.getAttribute("time").stringValue();

		try {
			_bufWriter.write(processInstance + ";" + task + ";" + time + System.getProperty("line.separator"));
			_bufWriter.flush();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		if(args.length != 1) {
			System.err.println("Usage: Logger <server-address>");
			System.exit(1);
		}

		// Declarations
		HierarchicalDispatcher siena;
		FileWriter file;
		BufferedWriter bufWriter;
		
		try {
			file = new FileWriter("log.txt");
			bufWriter = new BufferedWriter(file);
				
			siena = new HierarchicalDispatcher();
			siena.setMaster(args[0]);

			NotificationBuffer _buf = new NotificationBuffer();

//			siena.subscribe(f, _buf);
						
			Notification e;
			boolean loop = true;
			while(loop) {

				e = _buf.getNotification(-1);

				if (e.getAttribute("id").stringValue().equals("stopLogging")) {
					System.out.println("Stop Logging");
					loop = false;
				} else {
					System.out.println("Received: ");
					System.out.println(e.toString());

					String processInstance = e.getAttribute("processInstanceId").stringValue();
					String task = e.getAttribute("id").stringValue();
					String time = e.getAttribute("time").stringValue();

					bufWriter.write(processInstance + ";" + task + ";" + time + System.getProperty("line.separator"));
					bufWriter.flush();
				}

			}

			bufWriter.close();
			siena.shutdown();
		}catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();

		}
	}
}
