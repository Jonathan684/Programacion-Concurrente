package codigo;

import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Semaphore;

public class Cola {
	// Campos
	private Set<Integer> transicionesEsperando;
	//private int[] hilos_esperando;
	private int cantTransiciones;
	private Semaphore[] semaforos;
	private int prioridad = -1;

	/**
	 * Constructor de la clase Cola que inicia un semaforo para cada elemento que se
	 * encuentre en la misma, de esta manera el hilo se queda esperando en la cola.
	 * 
	 * @param cantTransiciones Cantidad de transiciones para armar vector Vc
	 */
	public Cola(int cantTransiciones) {
		this.cantTransiciones = cantTransiciones;
		transicionesEsperando = new TreeSet<>();
		//hilos_esperando = new int[10];
		semaforos = new Semaphore[cantTransiciones];

		for (int i = 0; i < cantTransiciones; i++) {
			semaforos[i] = new Semaphore(0, true);
		}

	}

	public void prioridad(int transicion) {
		synchronized (this) {
			// codigo del metodo aca
			prioridad = transicion;
		}
	}

	public int leer_prioridad() {
		synchronized (this) {
			// codigo del metodo aca
			return prioridad;
		}
	}

	/**
	 * Metodo que debe devolver el vector con los hilos que estan en cola
	 * 
	 * @return Vc matriz con los hilos que esperan
	 */
	public Matriz quienesEstan() {
		Matriz Vc = new Matriz(cantTransiciones, 1);
		for (Integer transicion : transicionesEsperando) {
			Vc.setDato(transicion, 0, 1);
		}

		return Vc;
	}

	/**
	 * Metodo que debe poner en cola el hilo en una ubicacion determinada para esa
	 * transicion
	 * 
	 * @param transicion transicion que intento realizar el disparo
	 */

	public boolean agregar(Integer transicion) {
		boolean agregado = false;
		agregado = transicionesEsperando.add(transicion);
		return agregado;
	}

	public String imprimirCola() {
		String esperando = "Esperando en la cola: [";

		for (Integer transicion : transicionesEsperando) {
			esperando += " T" + (transicion + 1);
		}
		esperando = "* " + esperando + " ]        (hilo que hizo la consulta: " + Thread.currentThread().getName()
				+ ")";
		return esperando;
	}

	public int Tamanio() {
		return transicionesEsperando.size();
	}

	public boolean isVacia() {
		return transicionesEsperando.isEmpty();
	}

	public void poner_EnCola(int Transicion) {

		if (semaforos[Transicion] != null) {
			agregar(Transicion);
			try {
				semaforos[Transicion].acquire(); // se queda esperando
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
			if (semaforos[Transicion].hasQueuedThreads()) { // Actualiza transicionesEsperando si hay alguien esperando en acquire()
				agregar(Transicion);
			}
		}
	}

	public void sacar_de_Cola(int nTransicion) {
		transicionesEsperando.remove(nTransicion);
		if (semaforos[nTransicion] != null) {
			semaforos[nTransicion].release();
			
		}

	}
}
