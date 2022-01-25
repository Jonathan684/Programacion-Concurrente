package hilos;

import codigo.Monitor;

public class kuka_1 implements Runnable {
	private Monitor monitor;
	private int[] secuencia;
	private boolean continuar = true;

	public kuka_1(Monitor monitor, int[] secuencia) {
		this.monitor = monitor;
		this.secuencia = secuencia;
	}
    // T2 T4
	 @Override
	public void run() {
		while (continuar == true) {
			for (Integer Transicion : secuencia) {
				
				monitor.dispararTransicion(Transicion - 1);
				try {
					Thread.sleep(60);//SIN TIEMPO 45  // CON TIEMPO 75
				//50
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