package hilos;

import codigo.Monitor;

public class kuka_4_5_6 implements Runnable {
	private Monitor monitor;
	private int[] secuencia;
	private boolean continuar = true;

	public kuka_4_5_6(Monitor monitor, int[] secuencia) {
		this.monitor = monitor;
		this.secuencia = secuencia;
	}
	//T7 T8 T9 T10
 @Override
	public void run() {
		while (continuar == true) {
			for (Integer Transicion : secuencia) {
				monitor.dispararTransicion(Transicion - 1);
				try {
					Thread.sleep(10);//SIN TIEMPO 95 // CON TIEMPO 490
				} catch (InterruptedException e) {
					e.printStackTrace();
			}
		}
			try {
				Thread.sleep(200);//SIN TIEMPO 95 // CON TIEMPO 490
			} catch (InterruptedException e) {
				e.printStackTrace();
			}			
		}
	}

	public void set_Fin() {
		continuar = false;
	}
}
