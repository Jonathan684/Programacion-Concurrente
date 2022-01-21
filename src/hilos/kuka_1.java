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

	public void run() {
		while (continuar == true) {
			for (Integer Transicion : secuencia) {
				monitor.dispararTransicion(Transicion - 1);
			}
			try {
				Thread.sleep(30);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public void set_Fin() {
		continuar = false;
	}
}