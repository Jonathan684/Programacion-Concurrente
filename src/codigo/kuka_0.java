package codigo;

public class kuka_0 implements Runnable {

	private Monitor monitor;
	private int[] secuencia;
	private int siguienteTransicion;
	private boolean continuar = true;

	public kuka_0(Monitor monitor, int[] secuencia) {

		this.monitor = monitor;
		this.secuencia = secuencia;
		siguienteTransicion = secuencia[0];

	}

	public void run() {
		// TODO Auto-generated method stub
		int i = 0;
		while ((continuar == true)) {
			siguienteTransicion = secuencia[i] - 1; // T1 -> T0 = [0,0]
			if (monitor.dispararTransicion(siguienteTransicion)) {
				i++;
			}

			if (i == secuencia.length) {
				try {
					Thread.sleep(20);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				i = 0;
			}
		}
	}

	public void set_Fin() {
		continuar = false;
	}

}
