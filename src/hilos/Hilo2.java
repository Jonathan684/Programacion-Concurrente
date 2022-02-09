package hilos;

import codigo.Monitor;

public class Hilo2 implements Runnable {
	private Monitor monitor;
	private int[] secuencia;
	private static boolean continuar;

	public Hilo2(Monitor monitor, int[] secuencia) {
		this.monitor = monitor;
		this.secuencia = secuencia;
		continuar = true;
	}
	@Override
	public void run() {
		while(continuar){
			for(int i=0 ; i<secuencia.length;) {
				if(monitor.dispararTransicion(secuencia[i] - 1)){
					i++;
					if(i==(secuencia.length-1)) {
						i=0;
					}
				}
			}
		}
	}
	public void set_Fin() {
		continuar = false;
	}
}
