package util;

public class Tuple<A,B> {

	private A a;
	private B b;
	
	public void addFirst(A a) {
		this.a = a;
	}
	
	public void addSecond(B b) {
		this.b = b;
	}
	
	public A getFirst() {
		return a;
	}
	
	public B getSecond() {
		return b;
	}
	
}
