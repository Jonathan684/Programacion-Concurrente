package codigo;

public class Tempo {
	public Tempo(){
		
	}
	public synchronized void delay(long timeout) throws InterruptedException{
		 wait(timeout);
	}
}
