package codigo;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.concurrent.Semaphore;

import log.Log;

public class Monitor {

	private RDP red;
	private Cola cola;
	private Politica politica;
	private int nTransicion;
	private Semaphore mutex;
	private Matriz m;
	private boolean k;
	private PrintWriter pw, registro_disparo;
	private boolean fin;

	public Monitor(PrintWriter pw, PrintWriter registro_disparo, FileWriter archivo1, FileWriter archivo2) {

		this.pw = pw;
		this.registro_disparo = registro_disparo;
		this.mutex = new Semaphore(1);
		red = new RDP(mutex, pw, archivo1, archivo2);
		cola = new Cola(red.get_numero_Transiciones());
		politica = new Politica(pw, RDP.get_Intervalo(), red.get_numero_Transiciones(), registro_disparo);
		nTransicion = -1;
		fin = false;
		k = true;
	}

	public void dispararTransicion(int T_Disparar) throws InterruptedException {

		mutex.acquire();

		k = true;
		while (k) {
			k = red.Disparar(T_Disparar);
			// pw.println(cola.imprimir());
			// pw.println("* k= "+k);
			if (k) { // k =true
				registrar_log(T_Disparar);
				politica.registrarDisparo(T_Disparar);
				// pw.println("* True : T"+ (T_Disparar+1));
				m = red.getVectorExtendidosinVz().getAnd(cola.quienesEstan());
				if (m.esNula()) {
					k = false;
				} else {
					// pw.println("* Despertar "+ T_Disparar);
					nTransicion = politica.cual(m);
					cola.sacar_de_Cola(nTransicion);
					// pw.println("* Despertar a: T"+ (nTransicion+1)+" k :"+k);
					return;
				}
			} else {// k = false

				mutex.release();
				cola.poner_EnCola(T_Disparar);
				// pw.println("* Desperté : T"+ (T_Disparar+1)+" k:"+k);
			}
		}
		mutex.release();
	}

	/**
	 * Imprime en un log la cantidad de disparo realizados por las transiciones.
	 * 
	 * @param informe
	 */
	public void imprimir(Log informe) {
		politica.imprimir(informe);
	}

	/**
	 * Registro para luego aplicar la expresion regular correspondiente.
	 * 
	 * @param nTransicion
	 */
	private void registrar_log(int nTransicion) {
		if ((nTransicion + 1) == 10)
			registro_disparo.print("T" + 0);
		else
			registro_disparo.print("T" + (nTransicion + 1));
	}
}