package hilos;

import codigo.Monitor;

public class kuka_3 implements Runnable {
	private Monitor monitor;
	private int[] secuencia;
	private boolean continuar = true;

	public kuka_3(Monitor monitor, int[] secuencia) {
		this.monitor = monitor;
		this.secuencia = secuencia;
	}
	//T6
	 @Override
	public void run() {
		while (continuar == true) {
			for (Integer Transicion : secuencia) {
				monitor.dispararTransicion(Transicion - 1);
				try {
				Thread.sleep(5);//SIN TIEMPO 15 // CON TIEMPO 20 
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			}
			
		}
	}

	public void set_Fin() {
		continuar = false;
	}

}
