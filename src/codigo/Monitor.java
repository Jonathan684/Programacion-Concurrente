package codigo;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Semaphore;

import log.Log;

public class Monitor {

	private RDP red;
	private Cola cola;
	private Politica pol;
	private Log log;
	private Log consola;
	private int nTransicion;
	private final String REPORT_FILE_NAME_1 = "Python/log.txt";
	// private Mutex mutex;
	private Semaphore mutex;
	private Matriz m;
//	private boolean k;
	private static volatile boolean fin;
	private boolean k;
	private Tempo[] dormir; 
	// private Politica politica;
	/**
	 * Constructor de la clase Monitor
	 * 
	 * @param mutex
	 */
	public Monitor(Mutex mutex, RDP red, Log log2) {
		this.consola = log2;
//		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
//		consola.registrarDisparo(dtf.format(LocalDateTime.now()), 1);
		consola.registrarDisparo("**************************************************", 1);
		consola.registrarDisparo("*        COMIENZO DEL MONITOR                    *", 1);
		consola.registrarDisparo("**************************************************\n", 1);
//		consola.registrarDisparo("** Informe de los disparos **", 1);
		this.red = red;
		this.log = new Log(REPORT_FILE_NAME_1);
		this.mutex = new Semaphore(1, true);
		cola = new Cola(red.get_numero_Transiciones());
		nTransicion = 0;
		red.sensibilizar();
		
		//boolean k = true;
		fin = false;
		pol = new Politica(red, log2, cola);
		
		
		dormir = new Tempo[red.get_numero_Transiciones()];
		//Arrays.fill(dormir, 0);
		for(int i = 0;i<red.get_numero_Transiciones() ; i++) {
			dormir[i]=new Tempo();
		}
		consola.registrarDisparo("* Marcado inicial     : " + red.Marcado(), 1);
		consola.registrarDisparo("* Transciones Inicial : " + red.sensibilidadas(), 1);// +"Disparo
		
	}

	/**
	 * 
	 * @param T_Disparar
	 * @return True : cuando el disparo fue exitoso
	 */
	public boolean dispararTransicion(int T_Disparar) {

		try {
			mutex.acquire();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		k=true;
		while (k) {
//			consola.registrarDisparo("* ======================", 1);// +" Hilo:
//			consola.registrarDisparo("* Dentro del monitor T" + (T_Disparar + 1), 1);// +" Hilo:
//			consola.registrarDisparo("* ¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯", 1);// +" Hilo:
//			consola.registrarDisparo(cola.imprimirCola(), 1);
//			consola.registrarDisparo("* Tiempo de ingreso :" + System.currentTimeMillis(), 1);
//			consola.registrarDisparo("* "+red.Marcado(), 1);
//			consola.registrarDisparo("* " + red.sensibilidadas(), 1);

//			consola.registrarDisparo(cola.imprimirCola(), 1);
//			consola.registrarDisparo("* Tiempo de ingreso :" + System.currentTimeMillis(), 1);
			
			k = red.Disparar(T_Disparar);// Hilo "+ Thread.currentThread().getName()
			if (k) { // k =true
				
				consola.registrarDisparo("* Se disparo: T" + (T_Disparar + 1), 1);
				consola.registrarDisparo("* " + red.Marcado(), 1);
				consola.registrarDisparo("* " + red.sensibilidadas(), 1);
				if ((T_Disparar + 1) == 10)
					log.registrarDisparo("T" + 0, 0);
				else
					log.registrarDisparo("T" + (T_Disparar + 1), 0);
				
				pol.registrarDisparo(T_Disparar);
				m = calcularVsAndVc();
				if (m.esNula()) {
					
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
				
				////Si es inmediata me voy a dormir
				if(red.getTemporales().getInmediata()[T_Disparar] == 1 || (red.getTemporales().getTimeStamp()[T_Disparar] == -1)) {
					cola.poner_EnCola(T_Disparar);
					mutex.release();
					return false;
				}
				else {
					
					long timeout=red.getTemporales().getTiempoFaltanteParaAlfa(T_Disparar);
					try {
						Thread.sleep(timeout);
						mutex.acquire();
						k=true;
						
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} //+2 Por problemas de redondeo.
				}
				
				if (fin)
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
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			fin = true;
			for (int i = 0; i < red.get_numero_Transiciones(); i++)cola.sacar_de_Cola(i);
			mutex.release();
			
		
		// TODO Auto-generated method stub
	}

	public void imprimir(Log loga) {
		pol.imprimir(loga);
	}
}

