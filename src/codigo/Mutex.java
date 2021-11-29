package codigo;

import java.util.concurrent.Semaphore;

public class Mutex {
	private Semaphore mutex;
	public Mutex() {
		mutex = new Semaphore(1,true);
	}
	public void _acquire(){
		try {
			mutex.acquire();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void _release() {
		mutex.release();
	}
	
}
