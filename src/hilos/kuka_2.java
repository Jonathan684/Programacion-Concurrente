package hilos;

import codigo.Monitor;

public class kuka_2 implements Runnable {
	private Monitor monitor;
	private int[] secuencia;
	private boolean continuar = true;

	public kuka_2(Monitor monitor, int[] secuencia) {

		this.monitor = monitor;
		this.secuencia = secuencia;
	}
   //T3 T5
	public void run() {
		while (continuar == true) {
			for (Integer Transicion : secuencia) {
				monitor.dispararTransicion(Transicion - 1);
				//System.out.println("T "+(Transicion));
			}
				try {
					Thread.sleep(30);// min 15 max 25
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
		}
	}

	public void set_Fin() {
		continuar = false;
	}
}
