package codigo;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
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
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
		consola.registrarDisparo(dtf.format(LocalDateTime.now()), 1);
		consola.registrarDisparo("**************************************************", 1);
		consola.registrarDisparo("*        COMIENZO DEL MONITOR                    *", 1);
		consola.registrarDisparo("**************************************************\n", 1);
		consola.registrarDisparo("** Informe de los disparos **", 1);
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
			
			consola.registrarDisparo("* ======================", 1);// +" Hilo:
			consola.registrarDisparo("* Dentro del monitor T" + (T_Disparar + 1), 1);// +" Hilo:
			consola.registrarDisparo("* ¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯", 1);// +" Hilo:
			consola.registrarDisparo(cola.imprimirCola(), 1);
			consola.registrarDisparo("* Tiempo de ingreso :" + System.currentTimeMillis(), 1);
			consola.registrarDisparo("* "+red.Marcado(), 1);
			consola.registrarDisparo("* " + red.sensibilidadas(), 1);
			
			k = red.Disparar(T_Disparar);// Hilo "+ Thread.currentThread().getName()
			if (k) { // k =true
				consola.registrarDisparo("* Valor de k : " + k, 1);
				consola.registrarDisparo("* Se disparo: T" + (T_Disparar + 1), 1);
				consola.registrarDisparo("* " + red.Marcado(), 1);
				consola.registrarDisparo("* " + red.sensibilidadas(), 1);
				consola.registrarDisparo("* k 0:" + k +" "+Thread.currentThread().getName(), 1);
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
					consola.registrarDisparo(
							"* Se saca de la cola: T" + (nTransicion + 1)+" k:"+k + " tiempo:" + System.currentTimeMillis(), 1);
					
					cola.sacar_de_Cola(nTransicion);
					//mutex.release();
					return true;
				}
			} else { // k =false
			
				mutex.release();
				
				////Si es inmediata me voy a dormir
				if(red.getTemporales().getInmediata()[T_Disparar] == 1 || (red.getTemporales().getTimeStamp()[T_Disparar] == -1)) {
					//System.out.println(" Encolando T"+(T_Disparar+1));
					//consola.registrarDisparo("* Encolar T"+(T_Disparar+1), 1);
					//mutex.release();
					cola.poner_EnCola(T_Disparar);
					consola.registrarDisparo("* Desperte T"+(T_Disparar+1)+" hilo:"+Thread.currentThread().getName()+" k:"+k, 1);
				}
				else {
					//consola.registrarDisparo("* TimeStamp -->"+red.getTemporales().getTimeStamp()[T_Disparar],1);
					long timeout=red.getTemporales().getTiempoFaltanteParaAlfa(T_Disparar);
					//consola.registrarDisparo("* Tiempo para dormir-->"+timeout,1);
					try {
						//mutex.release();
						//dormir[T_Disparar].delay((timeout)+2);
						Thread.sleep(timeout);
						mutex.acquire();
						//consola.registrarDisparo("\n* DESPERTE --> hilo :"+Thread.currentThread().getName()+" instante :"+System.currentTimeMillis()+
							//	" T"+(T_Disparar +1 ),1);
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