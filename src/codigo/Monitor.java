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
	//private Mutex mutex;
	private Semaphore mutex;
	private Matriz m;
//	private boolean k;
	 private static volatile boolean fin; 

	// private Politica politica;
	/**
	 * Constructor de la clase Monitor
	 * 
	 * @param mutex
	 */
	public Monitor(Mutex mutex, RDP red, Log log2) {
		this.consola = log2;
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
		consola.registrarDisparo(dtf.format(LocalDateTime.now()), 1);
		consola.registrarDisparo("**************************************************", 1);
		consola.registrarDisparo("*        COMIENZO DEL MONITOR                    *", 1);
		consola.registrarDisparo("**************************************************\n", 1);
		consola.registrarDisparo("** Informe de los disparos **", 1);
		//this.mutex = mutex;
		this.red = red;
		// this.politica = politica;
		this.log = new Log(REPORT_FILE_NAME_1);
		this.mutex = new Semaphore(1,true);
		cola = new Cola(red.get_numero_Transiciones());
		nTransicion = 0;
		red.sensibilizar();
//		k = true;
		fin = true;
		consola.registrarDisparo("* Marcado inicial     : " + red.Marcado(), 1);
		consola.registrarDisparo("* Transciones Inicial : " + red.sensibilidadas(), 1);// +"Disparo
		pol = new Politica(red, log2, cola);
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
		consola.registrarDisparo("* 1======================= T"+(T_Disparar+1) , 1);
		consola.registrarDisparo(cola.imprimirCola(), 1);
		while (!red.Disparar(T_Disparar)) {
			consola.registrarDisparo("* cola o sleep:"+(red.esInmediata(T_Disparar) || red.noduerme(T_Disparar)) , 1);
				mutex.release();
				if( red.esInmediata(T_Disparar) || red.noduerme(T_Disparar) ) {
					cola.poner_EnCola(T_Disparar);
				}
				else {
					try {
						Thread.sleep(red.gettimeout(T_Disparar));
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				try {
					mutex.acquire();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				consola.registrarDisparo("* 2======================= T"+(T_Disparar+1)+ " tiempo :"+System.currentTimeMillis()  , 1);
				consola.registrarDisparo(cola.imprimirCola(), 1);
		}
		
		consola.registrarDisparo("* Se disparo: T" + (T_Disparar + 1), 1);
		consola.registrarDisparo("* " + red.Marcado(), 1);
		consola.registrarDisparo("* " + red.sensibilidadas(), 1);
		pol.registrarDisparo(T_Disparar);
		m = calcularVsAndVc();
		if (!(m.esNula()))	
		{
				nTransicion = pol.cual(m);
				consola.registrarDisparo("* Se saca de la cola: T" + (nTransicion + 1) + " tiempo:" + System.currentTimeMillis(), 1);
				cola.sacar_de_Cola(nTransicion);
		}
		consola.registrarDisparo("* Saliendo true Hilo :"+ Thread.currentThread().getName(), 1);// +" Hilo:
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
		for (int i = 0; i < red.get_numero_Transiciones(); i++) {
			try {
				mutex.acquire();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			fin = false;
			cola.sacar_de_Cola(i);
			mutex.release();
			
		}
		// TODO Auto-generated method stub
	}

	public void imprimir(Log loga) {
		pol.imprimir(loga);
	}
}

////consola.registrarDisparo("* Hilo que sale de la cola :->"+Thread.currentThread().getName()+"<-",1);
//consola.registrarDisparo("* Valor en monitor de VectorZ :"+red.T_en_VectorZ(T_Disparar),1);
//if(red.T_en_VectorZ(T_Disparar)) {
//	//System.out.println("Salida espectacular del hilo que se fue a dormir :"+(T_Disparar+1));
//	consola.registrarDisparo("* Saliendo del monitor habiendo tomado el acquire",1);
//	mutex._release();
//	return false;
//}
//if(red.test_ventana(T_Disparar))
//	{
//		consola.registrarDisparo("* -->> Me disparo por que estoy en la ventana Hilo :"+Thread.currentThread().getName()+"<<--",1);
//		k=true;
//		//NO LARGO EL MUTEX
//	}
