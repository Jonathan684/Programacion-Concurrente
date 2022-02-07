package codigo;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import log.Log;

public class Monitor {

	private RDP red;
	private Cola cola;
	private Politica pol;
	private Log log;
	private Log consola;
	private int nTransicion;
	private final String REPORT_FILE_NAME_1 = "Python/log.txt";
	private Mutex mutex;
	private Matriz m;
	private boolean k;
	private boolean salir;
	//private Politica politica; 
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
		this.mutex = mutex;
		this.red = red;
		//this.politica = politica;
		this.log = new Log(REPORT_FILE_NAME_1);
		
		cola = new Cola(red.get_numero_Transiciones());
		nTransicion = 0;
		red.sensibilizar();
		k = true;
		salir=false;
		consola.registrarDisparo("* Marcado inicial     : " + red.Marcado(), 1);
		consola.registrarDisparo("* Transciones Inicial : " + red.sensibilidadas(), 1);// +"Disparo
		pol = new Politica(red,log2,cola);
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
			consola.registrarDisparo("*________________________", 1);
			consola.registrarDisparo("* Dentro del monitor T" + (T_Disparar + 1), 1);// +" Hilo:
			consola.registrarDisparo("*¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯", 1);// +" Hilo:
			consola.registrarDisparo(cola.imprimirCola(), 1);
			consola.registrarDisparo("* Tiempo de ingreso :" + System.currentTimeMillis()+" Hilo :"+Thread.currentThread().getName(), 1);
			//consola.registrarDisparo("* " + red.sensibilidadas(), 1);
			k = red.Disparar(T_Disparar);// Hilo "+ Thread.currentThread().getName()

			if (k) { // k =true
				//consola.registrarDisparo("* Valor de k : " + k, 1);
				consola.registrarDisparo("* Se disparo: T" + (T_Disparar + 1), 1);
				consola.registrarDisparo("* " + red.Marcado(), 1);
				consola.registrarDisparo("* " + red.sensibilidadas(), 1);
				pol.registrarDisparo(T_Disparar);
				//consola.registrarDisparo("* k 0:" + k +" "+Thread.currentThread().getName(), 1);
				
				if ((T_Disparar + 1) == 10)
					log.registrarDisparo("T" + 0, 0);
				else
					log.registrarDisparo("T" + (T_Disparar + 1), 0);
				
				//consola.registrarDisparo("* k 1:" + k +" "+Thread.currentThread().getName(), 1);
				
				m = calcularVsAndVc();
				//consola.registrarDisparo("* k 2:" + k +" "+Thread.currentThread().getName(), 1);
				
				if (m.esNula()) {
					k = false;// No hay hilos con transiciones esperando para disparar y que esten
								// sensibilidas
//					consola.registrarDisparo("* m: nula", 1);
//					consola.registrarDisparo("* k: " + k +" "+Thread.currentThread().getName(), 1);

				} else {
//					consola.registrarDisparo("* m: no es nula",1);

					//consola.registrarDisparo("* k 3 :" + k +" "+Thread.currentThread().getName(), 1);
					nTransicion = pol.cual(m);
					//consola.registrarDisparo("* k 4:" + k +" "+Thread.currentThread().getName(), 1);
					// esta en el cola ?
					// si esta
					consola.registrarDisparo("* Se saca de la cola: T" + (nTransicion + 1), 1);
					//consola.registrarDisparo("* k 5:" + k +" "+Thread.currentThread().getName(), 1);

					cola.sacar_de_Cola(nTransicion);
					// si no
					return true; // Sale del monitor
				}
			} else { //k =false
				
				
				//consola.registrarDisparo("* Encolar : T" + (T_Disparar + 1) + " Temporal Hilo :"+Thread.currentThread().getName(), 1);
				//consola.registrarDisparo("* K 6:" + k+" Antes de encolar", 1);
				
				
				// Transiciones Inmediatas
				if (red.esInmediata(T_Disparar) == true) {
					mutex._release();
					cola.poner_EnCola(T_Disparar);
				}
				// Transiciones Temporales
				else {
					
					// Puede ser que no este sensibilizada o que este desensibilizada
					if (!red.estaSensibilizada(T_Disparar)) { // <-- Si no esta sensibilizada.
						mutex._release();
						cola.poner_EnCola(T_Disparar);
					 }
					else { // k=false 
						
						if(!red.Analisis_Temporal(T_Disparar)) {
							
								red.esperar(red.get_timeout(), T_Disparar);
								mutex._acquire();
								consola.registrarDisparo("* -.Saliendo de un sleep. Hilo" + Thread.currentThread().getName(), 1);
								k=true;
						} // <-- Si esta sensibilizada y está lista para dormir.
						else {
							//mutex._release();
							consola.registrarDisparo("* -.Estoy en la venta. Hilo" + Thread.currentThread().getName(), 1);
							k=true;
						}
						
						
						
					}
				  }
			}
			if(salir == true) {
				break;
			}
		}
		consola.registrarDisparo("* Saliendo de Monitor Hilo:" + Thread.currentThread().getName(), 1);
		mutex._release();
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
		for(int i=0; i<red.get_numero_Transiciones();i++)
		{   
			mutex._acquire();
			salir=true;
			mutex._release();
			cola.sacar_de_Cola(i);
		}
		// TODO Auto-generated method stub
	}
	public void imprimir(Log loga)
	{
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
