package hilos;

import codigo.Monitor;

public class Hilo implements Runnable {

	private Monitor monitor;
	private int[] secuencia;
	private static boolean continuar;

	public Hilo(Monitor monitor, int[] secuencia) {
		this.monitor = monitor;
		this.secuencia = secuencia;
		continuar = true;
	}

	@Override
	public void run() {
		while(continuar){
				for(Integer i:secuencia) {monitor.dispararTransicion(i - 1);}
			}
	}
	// Fin del hilo 
	public void set_Fin() {
		continuar = false;
	}
}
