package codigo;

import java.util.concurrent.Semaphore;

public class Cola {



	private int cantTransiciones;
	private Semaphore[] semaforos;
    private static boolean Transiciones_en_espera[];
    
	/**
	 * Constructor de la clase Cola que inicia un semaforo para cada elemento que se
	 * encuentre en la misma, de esta manera el hilo se queda esperando en la cola.
	 * 
	 * @param cantTransiciones Cantidad de transiciones para armar vector Vc
	 */
	public Cola(int cantTransiciones) {
		
		this.cantTransiciones = cantTransiciones;
        semaforos = new Semaphore[cantTransiciones];
		Transiciones_en_espera = new boolean[cantTransiciones];

		for (int i = 0; i < cantTransiciones; i++) {
			semaforos[i] = new Semaphore(0, true);
			Transiciones_en_espera[i]=false;
		}
    }
	/**
	 * Metodo que debe devolver el vector con los hilos que estan en cola
	 * 
	 * @return Vc matriz con los hilos que esperan
	 */
	public Matriz quienesEstan() {
		Matriz Vc = new Matriz(cantTransiciones, 1);
		for (int transicion=0;transicion < cantTransiciones ;transicion++) {
			if(Transiciones_en_espera[transicion])Vc.setDato(transicion, 0, 1);
		}

		return Vc;
	}

	/**
	 * Metodo que debe poner en cola el hilo en una ubicacion determinada para esa
	 * transicion
	 * 
	 * @param transicion transicion que intento realizar el disparo
	 */

	public void poner_EnCola(int Transicion) {

		if (semaforos[Transicion] != null) {
			Transiciones_en_espera[Transicion] = true;
			try {
				semaforos[Transicion].acquire(); // se queda esperando
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
	}

	public void sacar_de_Cola(int nTransicion) {

		Transiciones_en_espera[nTransicion] = false;
//		if(nTransicion == 6) {
//			while(semaforos[nTransicion] != null) {
//				semaforos[nTransicion].release();
//				if(semaforos[nTransicion] != null)break;
//			}
//		}
		if (semaforos[nTransicion] != null)semaforos[nTransicion].release();
		//semaforos[nTransicion] != null)semaforos[nTransicion].release()
	}
	
	public void imprimir(){
		System.out.print("Cola [");
		for(int i=0;i<cantTransiciones;i++){
			if(Transiciones_en_espera[i])System.out.print("T"+(i+1)+" ");
		}
		System.out.println("]");
	}
//	public String imprimir2(){
//		String cadena="";
//		cadena = cadena+"Cola [";
//		for(int i=0;i<cantTransiciones;i++){
//			if(Transiciones_en_espera[i])cadena = cadena + ("T"+(i+1)+" ");
//		}
//		cadena = cadena + "]";
//		return cadena;
//	}
}
