package codigo;



public class Hilo implements Runnable {
	private Monitor monitor;
	private int[] secuencia;
	private int siguienteTransicion;
	private boolean continuar = true;
	
	public Hilo(Monitor monitor,int[] secuencia) {
		
		this.monitor = monitor;
		this.secuencia = secuencia;
		siguienteTransicion = secuencia[0];
	}
	public void run() {
		while(continuar) {
			for(int i=0;i<secuencia.length;i++) {
				siguienteTransicion = secuencia[i]-1;
				 monitor.dispararTransicion(siguienteTransicion);
			}
		}
	}

	public void set_Fin()
	{
		continuar = false;
	}
}
