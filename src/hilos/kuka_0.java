package hilos;

import codigo.Monitor;

public class kuka_0 implements Runnable {

	private Monitor monitor;
	private int[] secuencia;
	private boolean continuar = true;

	public kuka_0(Monitor monitor, int[] secuencia) {
		this.monitor = monitor;
		this.secuencia = secuencia;
	}
	@Override
	public void run() {
		while (continuar == true) {
			for (Integer Transicion : secuencia) {
				monitor.dispararTransicion(Transicion - 1);
			}
			try {
				Thread.sleep(5); // SIN TIEMPO 10 // CON TIEMPO 10
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public void set_Fin() {
		continuar = false;
	}
}
