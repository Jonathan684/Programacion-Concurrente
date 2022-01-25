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
	 @Override
	public void run() {
		while (continuar == true) {
			for (Integer Transicion : secuencia) {
				monitor.dispararTransicion(Transicion - 1);
				try {
					Thread.sleep(20);// SIN TIMEPO 30 // CON TIEMPO 30
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				//System.out.println("T "+(Transicion));
			}
				
		}
	}

	public void set_Fin() {
		continuar = false;
	}
}
