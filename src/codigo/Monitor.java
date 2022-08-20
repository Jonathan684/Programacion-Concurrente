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
	private	PrintWriter pw,registro_disparo;
	private boolean fin;
	//private static RDP red;
	public Monitor(PrintWriter pw,PrintWriter registro_disparo,FileWriter archivo1,FileWriter archivo2) {

		this.pw=pw;
		this.registro_disparo=registro_disparo;
		this.mutex = new Semaphore(1);
        red = new RDP(mutex,pw,archivo1,archivo2);
        cola = new Cola(red.get_numero_Transiciones());
		politica = new Politica(pw,red,registro_disparo);
        nTransicion = -1;
        fin = false;
	}

	public void dispararTransicion(int T_Disparar) {

		try {
			mutex.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		//k=true;
    	//while (k) {
		
			k = red.Disparar(T_Disparar);// Hilo "+ Thread.currentThread().getName()
			
			if (k) { // k =true
//				pw.println("*************************");
//				pw.println("* Se disparo:[T"+(T_Disparar+1)+"]");
				//pw.println("*    "+red.getVectorExtendido().imprimir());
				registrar_para_expresion_regular(T_Disparar);
				politica.registrarDisparo(T_Disparar);
				m = calcularVsAndVc();
//				pw.println("* m: "+m.imprimir());
//				pw.println("* "+cola.imprimir2());
				
				if (m.esNula()) {
					//k = false;// No hay hilos con transiciones esperando para disparar y que esten
//					pw.println("*************************");
//					pw.println("\n");
					mutex.release();
				    return;
				
				} else {
					
					nTransicion = politica.cual(m);
					cola.sacar_de_Cola(nTransicion);
//					pw.println("* Se saca T"+(nTransicion+1)+ " [hilo :"+Thread.currentThread().getName()+"]");
//					
//					pw.println("*************************");
//					pw.println("\n");
					mutex.release();
					return;
				}
			} 
			
			// k =false
//				pw.println("* Se va a la cola T"+(T_Disparar+1)+ " [hilo :"+Thread.currentThread().getName()+"]");
//				pw.println("* Estan en la "+cola.imprimir2());
				mutex.release();
				cola.poner_EnCola(T_Disparar);
				if(fin == true)	return;
				dispararTransicion(T_Disparar);
				//return;
			
	}

	/**
	 * Calcula la operacion AND entre los que estan en la cola y las transiciones
	 * que estan en sensibilidas
	 * 
	 * @return m : Matriz con transiciones en la cola y sensibilizadas
	 */
	public Matriz calcularVsAndVc() {
		Matriz Vs = red.getVectorExtendidosinVz(); // sin vz la politica decidira a quien sacar
		Matriz Vc = cola.quienesEstan();
		Matriz m = Vs.getAnd(Vc);
		return m;
	}

	public void vaciarcolas() {

			try {
				mutex.acquire();
				fin = true;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		    for (int i = 0; i < red.get_numero_Transiciones(); i++)cola.sacar_de_Cola(i);
			mutex.release();
	}
    public void imprimir(Log loga) {
		politica.imprimir(loga);
	}
    private void registrar_para_expresion_regular(int nTransicion) {
    	if ((nTransicion + 1) == 10)
			registro_disparo.print("T" + 0);
		else
			registro_disparo.print("T" + (nTransicion + 1));
    	}
}