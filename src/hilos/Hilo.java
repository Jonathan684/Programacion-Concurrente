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
				for(int i=0 ; i<secuencia.length;) {
					if(monitor.dispararTransicion(secuencia[i] - 1)){
						//System.out.println("i :"+i +" secuencia.length :"+secuencia.length+" T"+secuencia[i]+" hilo:"+ Thread.currentThread().getName()+" tiempo:"+System.currentTimeMillis());
						i++;
						
						if(i ==(secuencia.length)) {
							i=0;
						}
					}
				}
			}
			
//			while(continuar){
//			for (int element:secuencia)monitor.dispararTransicion(element - 1);
//		}
	}
	// Fin del hilo 
	public void set_Fin() {
		continuar = false;
	}
}
