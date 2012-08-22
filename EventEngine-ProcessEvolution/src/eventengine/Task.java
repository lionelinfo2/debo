package eventengine;

import java.util.Random;

import be.kuleuven.wsClient.Client;

public class Task {
	
	private String _name;
	private long _startTime;
	private long _endTime;
	public int _taskTime = 0;
	
	private org.eclipse.bpmn2.Task _bpmn2Task;
	
	public Task(Task action) {
		_name = action.getName();
		_bpmn2Task = action._bpmn2Task;
		_taskTime = action._taskTime;
	}
	
	public Task() {
		
	}
	
	public static void main(String args[]){
		
		
		
//		ThreadMXBean newBean = ManagementFactory.getThreadMXBean();
//
//		if (!newBean.isCurrentThreadCpuTimeSupported()) {
//			System.out.println("CPU Usage monitoring is not avaliable!");
//		} else {
//			newBean.setThreadCpuTimeEnabled(true);
//		}
//
//		// The order of these two calls matters
//		long start = System.nanoTime();
//		long base_cpu = newBean.getCurrentThreadUserTime();
//		
		System.out.println(System.currentTimeMillis());
		DummyTask.calculatePrime(10000000);
		
		System.out.println(System.currentTimeMillis());
						
//		
//		long cpu = newBean.getCurrentThreadUserTime();
//		long time = System.nanoTime();
//		System.out.println(" load: " +
//					(cpu - base_cpu) * 100.0 / (time - start));
//		
//		OperatingSystemMXBean opBean = ManagementFactory.getOperatingSystemMXBean();
//		System.out.println("average Load:" + opBean.getSystemLoadAverage());
//		
//		System.out.println(System.currentTimeMillis());
//		DummyTask.calculatePrime(100000);
//		System.out.println(System.currentTimeMillis());
//		System.nanoTime();
	}
	
	public void doTask() {
//		System.out.println("DOING TASK " + this._name);
		_startTime = System.currentTimeMillis();
		
		// Doing task (invoking a service with SOAP over HTTP)
		
//		Client newClient = new Client();
//		newClient.run();
//		DummyTask.calculatePrime(100000);
		
		try {
//			Double sleepTime = (Math.random()*10000);
//			Thread.sleep(sleepTime.intValue());
//			Thread.sleep(5000);
			
			if (_taskTime == 0) {
				// do random
				Double sleepTime = (Math.random()*10000);
				Thread.sleep(sleepTime.intValue());
			} else {
				Thread.sleep(_taskTime);
			}
			

			
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		_endTime = System.currentTimeMillis();
		
		Debug.debug("Task " + "[" + this._name + "]" + " done!");
		
//		Random generator = new Random();
		
		// Doing dummy task		
//		DummyTask.calculatePrime(100000);

				
		
		
		
		
	}
	
	public void setTaskTime(int time) {
		_taskTime = time;
	}
	
	public String getName() {
		return _name;
	}
	
	public void setBpmn2Task(org.eclipse.bpmn2.Task task) {
		_bpmn2Task = task;
	}
	
	public org.eclipse.bpmn2.Task getBpmn2Task() {
		return _bpmn2Task;
	}
	
	public String toString() {
		return "Task: " + getName();
	}

	public long getStartTime() {
		return _startTime;
	}

	public long getEndTime() {
		return _endTime;
	}

	public void setName(String name) {
		_name = name;
	}
}
