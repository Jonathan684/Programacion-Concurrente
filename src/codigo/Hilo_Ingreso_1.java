package codigo;

public class Hilo_Ingreso_1 implements Runnable{

	private Monitor monitor;
	private int[] secuencia;
	private int siguienteTransicion;
	private boolean continuar = true;
	
	public Hilo_Ingreso_1(Monitor monitor,int[] secuencia) {
		
		this.monitor = monitor;
		this.secuencia = secuencia;
		siguienteTransicion = secuencia[0];
		System.out.println("Secuencia dentro del hilo"+siguienteTransicion);
	}

	public void run() {
		// TODO Auto-generated method stub
		int i=0;
		while((continuar == true)) {
			    siguienteTransicion = secuencia[i]-1; //T1 -> T0 =  [0,0]
				if(monitor.dispararTransicion(siguienteTransicion))
				 {
					 i++;
				 }
				try {
					Thread.sleep(15);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				 if(i==secuencia.length) {
					 i = 0;
				 }
		}
	}
	public void set_Fin()
	{
		continuar = false;
	}

}
