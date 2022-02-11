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
		
		k = true;
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
		k = true;
		while (k) {

			consola.registrarDisparo("* \n======================", 1);// +" Hilo:
			consola.registrarDisparo("* Dentro del monitor T" + (T_Disparar + 1), 1);// +" Hilo:
			consola.registrarDisparo(cola.imprimirCola(), 1);
			consola.registrarDisparo("* Tiempo de ingreso : " + System.currentTimeMillis() + " Hilo :"
					+ Thread.currentThread().getName(), 1);
			k = red.Disparar(T_Disparar);// Hilo "+ Thread.currentThread().getName()
			if (k) { // k =true
				consola.registrarDisparo("* " + red.sensibilidadas(), 1);
				consola.registrarDisparo("* Se disparo: T" + (T_Disparar + 1), 1);
				pol.registrarDisparo(T_Disparar);
				m = calcularVsAndVc();
				if (m.esNula()) {

					k = false;// No hay hilos con transiciones esperando para disparar y que esten
					consola.registrarDisparo("* Saliendo true", 1);// +" Hilo:
//					mutex.release();
//					return true;
				} else {
					nTransicion = pol.cual(m);
					consola.registrarDisparo("* Se saca de la cola: T" + (nTransicion + 1) + " tiempo:"
							+ System.currentTimeMillis() + " valor de k :" + k, 1);
					cola.sacar_de_Cola(nTransicion);
					return true;
				}
			} else { // k =false
				consola.registrarDisparo("* Tiempo para cola o sleep : " + System.currentTimeMillis() + " Hilo :"
						+ Thread.currentThread().getName(), 1);
				mutex.release();
				if (!red.estaSensibilizada(T_Disparar) && (red.gettimeout(T_Disparar) == 0)) {
					cola.poner_EnCola(T_Disparar);
					consola.registrarDisparo("* Sale de la cola " + k+ " T"+(T_Disparar+1)+" Hilo :" + Thread.currentThread().getName(), 1);
				}
				// Transiciones Temporales
				else {
					try {
						//Thread.sleep(red.gettimeout(T_Disparar));
						dormir[T_Disparar].delay(red.gettimeout(T_Disparar));
						mutex.acquire();
						consola.registrarDisparo("* Saliendo "+ Thread.currentThread().getName()+" hilo:"+Thread.currentThread().getName() +" ->"+System.currentTimeMillis() , 1);
						k = true;
						//mutex.release();
						//return false;
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
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