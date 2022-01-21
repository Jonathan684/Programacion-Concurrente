package codigo;

public class kuka_1 implements Runnable {
	private Monitor monitor;
	private int[] secuencia;
	private int siguienteTransicion;
	private boolean continuar = true;

	public kuka_1(Monitor monitor, int[] secuencia) {

		this.monitor = monitor;
		this.secuencia = secuencia;
		siguienteTransicion = secuencia[0];
	}

	public void run() {
		int i = 0;
		while ((continuar == true)) {
			siguienteTransicion = secuencia[i] - 1; // T1 -> T0 = [0,0]
			if (monitor.dispararTransicion(siguienteTransicion)) {
				i++;
			}
			if (i == secuencia.length) {
				i = 0;
				try {
					Thread.sleep(30);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}
	}

	public void set_Fin() {
		continuar = false;
	}
}
//if monitor.dispararTransicion(siguienteTransicion); == true
/*
 * for(char transicion: secuencia) { try {
 * monitor.dispararTransicion(transicion); } catch (InterruptedException e) {
 * e.printStackTrace(); } catch (IOException e) { e.printStackTrace(); }
 */