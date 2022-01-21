package codigo;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import log.Log;

public class Monitor {

	private RDP red;
	private Cola cola;
	private Politica politica;
	private Log log;
	private Log consola;
	private int nTransicion;
	private final String REPORT_FILE_NAME_1 = "Python/log.txt";
	private Mutex mutex;
	private Matriz m;
	private boolean k;

	/**
	 * Constructor de la clase Monitor
	 * 
	 * @param mutex
	 */
	public Monitor(Mutex mutex, RDP red, Politica politica, Log consola) {
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
		consola.registrarDisparo(dtf.format(LocalDateTime.now()), 1);
		consola.registrarDisparo("**************************************************", 1);
		consola.registrarDisparo("*        COMIENZO DEL MONITOR                    *", 1);
		consola.registrarDisparo("**************************************************\n", 1);
		consola.registrarDisparo("** Informe de los disparos **", 1);
		this.mutex = mutex;
		this.red = red;
		this.politica = politica;
		this.log = new Log(REPORT_FILE_NAME_1);
		this.consola = consola;
		cola = new Cola(red.get_numero_Transiciones());
		nTransicion = 0;
		red.sensibilizar();
		k = true;
		consola.registrarDisparo("* Marcado inicial     : " + red.Marcado(red.getVectorMA()), 1);
		consola.registrarDisparo("* Transciones Inicial : " + red.sensibilidadas(red.getVectorExtendido()), 1);// +"Disparo
																												// "+(T_Disparar+1),2);

	}

	/**
	 * 
	 * @param T_Disparar
	 * @return True : cuando el disparo fue exitoso
	 */
	public boolean dispararTransicion(int T_Disparar) {
		mutex._acquire();
		k = true;
		while (k) {

			consola.registrarDisparo("\n* Dentro del monitor T" + (T_Disparar + 1), 1);// +" Hilo:
																						// "+Thread.currentThread().getName(),1);
			consola.registrarDisparo(cola.imprimirCola(), 1);
			k = red.Disparar(T_Disparar);// Hilo "+ Thread.currentThread().getName()

			if (k) {
				consola.registrarDisparo("* Se disparo: T" + (T_Disparar + 1), 1);
				consola.registrarDisparo("* " + red.Marcado(red.getVectorMA()), 1);
				consola.registrarDisparo("* " + red.sensibilidadas(red.getVectorExtendido()), 1);
				politica.registrarDisparo(T_Disparar);

				if ((T_Disparar + 1) == 10)
					log.registrarDisparo("T" + 0, 0);
				else
					log.registrarDisparo("T" + (T_Disparar + 1), 0);

				m = calcularVsAndVc();
				if (m.esNula()) {
					k = false;// No hay hilos con transiciones esperando para disparar y que esten
								// sensibilidas
//			    	consola.registrarDisparo("* m: nula",1);
//					consola.registrarDisparo("* k: "+ k,1);

				} else {
//					consola.registrarDisparo("* m: no es nula",1);
					nTransicion = politica.cual(m);
					consola.registrarDisparo("* Se saca de la cola: T" + (nTransicion + 1), 1);
					cola.sacar_de_Cola(nTransicion);
					return true; // Sale del monitor
				}
			} else {
				consola.registrarDisparo("* Encolar: T" + (T_Disparar + 1), 1);
				mutex._release();
				cola.poner_EnCola(T_Disparar); //
			    //consola.registrarDisparo("* Hilo que sale de la cola :->"+Thread.currentThread().getName()+"<-",1);
			}
		}
		mutex._release();
		return true;
	}

	/**
	 * Calcula la operacion AND entre los que estan en la cola y las transiciones
	 * que estan en sensibilidas
	 * @return m : Matriz con transiciones en la cola y sensibilizadas
	 */
	public Matriz calcularVsAndVc() {
		Matriz Vs = red.getVectorExtendido();
		Matriz Vc = cola.quienesEstan();
		Matriz m = Vs.getAnd(Vc);
		return m;
	}
}
