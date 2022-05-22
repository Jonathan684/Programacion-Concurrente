package codigo;

import java.io.PrintWriter;
import java.util.concurrent.Semaphore;

import log.Log;

public class Monitor {

	private RDP red;
	private Cola cola;
	private Politica pol;
	private int nTransicion;
	private Semaphore mutex;
	private Matriz m;
	private boolean k;
	private	PrintWriter pw;
	
	public Monitor(PrintWriter pw,RDP red) {

		this.pw=pw;
		this.red = red;
        this.mutex = new Semaphore(1, false);
		cola = new Cola(red.get_numero_Transiciones());
		nTransicion = 0;
		red.sensibilizar();
        pol = new Politica(red, cola);
	}
	
	public boolean dispararTransicion(int T_Disparar) {

		try {
			mutex.acquire();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		k=true;
    	while (k) {
			k = red.Disparar(T_Disparar);// Hilo "+ Thread.currentThread().getName()
			
			if (k) { // k =true
				pw.println("* Se disparo:["+(T_Disparar+1)+"]\n");
				pol.registrarDisparo(T_Disparar);
				m = calcularVsAndVc();
				if (m.esNula()) {
					m.getTranspuesta().getTranspuesta();
					k = false;// No hay hilos con transiciones esperando para disparar y que esten
					mutex.release();
					return true;
				
				} else {
					
					nTransicion = pol.cual(m);
					cola.sacar_de_Cola(nTransicion);
					return true;
				}
			} else { // k =false
				mutex.release();
				cola.poner_EnCola(T_Disparar);
				mutex.release();
				return false;
			}
		}

		mutex.release();
		return true;
	}

	/**
	 * Calcula la operacion AND entre los que estan en la cola y las transiciones
	 * que estan en sensibilidas
	 * 
	 * @return m : Matriz con transiciones en la cola y sensibilizadas
	 */
	public Matriz calcularVsAndVc() {
		Matriz Vs = red.getVectorExtendido();
		Matriz Vc = cola.quienesEstan();
		Matriz m = Vs.getAnd(Vc);
		return m;
	}

	public void vaciarcolas() {
		
			try {
				mutex.acquire();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		    for (int i = 0; i < red.get_numero_Transiciones(); i++)cola.sacar_de_Cola(i);
			mutex.release();
	}
    public void imprimir(Log loga) {
		pol.imprimir(loga);
	}
}