package codigo;

import log.Log;

public class SensibilizadaConTiempo {

	private long timeStamp[];
	private int SetEsperando[];
	private Log consola;
	private Matriz Intervalo;
	//private Mutex mutex;
	
	public SensibilizadaConTiempo(int cantidad_transiciones,Mutex mutex ,Log consola2, Matriz Intervalo) {
		this.consola = consola2;
		timeStamp = new long[cantidad_transiciones];
		SetEsperando = new int[cantidad_transiciones];
		this.Intervalo = Intervalo;
		//this.mutex = mutex;
		
		
		for (int i = 0; i < timeStamp.length; i++) {
			timeStamp[i] = 0;
			SetEsperando[i] = 0;
		}
	}
	 public boolean esTemporal(int transicion) {
			if (Intervalo.getDato(0, transicion) - Intervalo.getDato(1, transicion) != 0) {
				return true;
			}
			return false;
		}
	/*
	 * Este metodo verifica si se habilitaron transiciones temporales
	 */			
	// Metodos Set
	public void setEsperando(int transicion) {
		SetEsperando[transicion] = 1;
	}
	public void resetEsperando(int transicion) {
		//timeStamp[transicion] = 0;
		SetEsperando[transicion] = 0;
	}

	// Metodos Get
	public long[] getTimeStamp() {
		return timeStamp;
	}

	public boolean getEsperando(int transicion) {

		if (SetEsperando[transicion] == 1)
			return true;

		return false;
	}
	public int[] getvectorEsperando() {

		return SetEsperando;
	}

}

