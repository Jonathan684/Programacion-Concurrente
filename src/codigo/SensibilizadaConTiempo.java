package codigo;

import java.io.PrintWriter;
import java.util.Date;

public class SensibilizadaConTiempo {

	private long timeStamp[];
	private Matriz Intervalo;
	private int cantidad_transiciones;
	private Matriz VectorZ;
	private int [] T_Inmediata;
	private Thread [] N_hilo;
	private	PrintWriter pw;
	private boolean [] T_Esperando;
	public SensibilizadaConTiempo(int cantidad_transiciones , Matriz Intervalo,PrintWriter pw) {
		this.pw = pw;
		timeStamp = new long[cantidad_transiciones];
		T_Inmediata = new int[cantidad_transiciones];
		N_hilo = new Thread[cantidad_transiciones];
		T_Esperando = new boolean[cantidad_transiciones];
		VectorZ = new Matriz(1, cantidad_transiciones);
		this.Intervalo = Intervalo;
		this.cantidad_transiciones = cantidad_transiciones;
		for (int i = 0; i < cantidad_transiciones; i++) {
			timeStamp[i] = -1;
			T_Inmediata[i] = 0;
			T_Esperando[i] = false;
			N_hilo[i]= null;
		}
		Inmediatas();
	}
	public void setEsperando(int transicion) {
		T_Esperando[transicion]= true;
		N_hilo[transicion]= Thread.currentThread();
	}
	public boolean alguienEsperando(int transicion) {
		if(T_Esperando[transicion]==true &&  (N_hilo[transicion]!= Thread.currentThread())) {
			return true;
		}
		return false;
	}
	public boolean dentroVentana(int transicion) {
		long now =  new Date().getTime();
		long TimeStamp_ahora = now - (timeStamp[transicion]);
		if ((TimeStamp_ahora >= (Intervalo.getDato(0, transicion)))
				&& (TimeStamp_ahora <= (Intervalo.getDato(1, transicion))))
				{
				return true;
				}
			return false;
	}
	public boolean antesVentana(int transicion) {
		long now =  new Date().getTime();
		long TimeStamp_ahora = now - (timeStamp[transicion]);
		
		if ((TimeStamp_ahora < (Intervalo.getDato(0, transicion))))
			{
			return true;
			}
		return false;
	}
	public void resetEsperando(int transicion) {
		T_Esperando[transicion] = false;
	}
	

	
	
	
	
	 private void Inmediatas() {
		// TODO Auto-generated method stub
		 for(int i=0 ; i< cantidad_transiciones ; i++) {
			 
		 // 1 si es inmediata
		 if ((Intervalo.getDato(0, i)==0) && (Intervalo.getDato(1, i)==0)) {
			 T_Inmediata[i] = 1; // Inmedianta
			 //System.out.println("Transiciones Inmedia T"+i);
			}
		 else  T_Inmediata[i] = 0;
		 }	
	}
	public boolean esTemporal(int transicion) {
		 if (Intervalo.getDato(0, transicion) - Intervalo.getDato(1, transicion) != 0) {
				return true;
			}
			return false;
		}

	// TODO Auto-generated method stub
			/*
			 * De lo contrario, si la red tiene un marcado diferente al inicial (se disparo alguna transicion)
			 * se realiza lo siguiente:
			 */
	public void inicio(Matriz transiciones_inicio) {
		   for (int transicion = 0; transicion < cantidad_transiciones; transicion++) {
				if(transiciones_inicio.getDato(transicion, 0)==1) {
					timeStamp[transicion]= System.currentTimeMillis();
					//pw.println("* Inicio el contador para : T"+(transicion+1));
				}

			}
	}
	public void ActualizarTimeStamp(Matriz transAntesdelDisparo, Matriz transDespuesdelDisparo, int transicion_a_disparar) {
		for (int transicion = 0; transicion < cantidad_transiciones; transicion++) { //Se recorren todas las transiciones
			
			if(transAntesdelDisparo.getDato(transicion, 0)==1 && transDespuesdelDisparo.getDato(transicion, 0)==1) { //Si la transicion se encontraba sensibilizada antes del disparo y sigue sensibilizada despues de disparar, no se resetea el contador de la misma 
				if(transicion==transicion_a_disparar) { //A MENOS QUE justamente sea esa la transicion disparada (tiene que reinizializar su cronometro)
					timeStamp[transicion] = System.currentTimeMillis(); //continua la cuenta
					//pw.println("* POSIBLE ERROR ACA T"+(transicion+1)+" transicion_a_disparar:"+transicion_a_disparar);
				}
			}
			
			//Si la transicion estaba sensibilizada antes de disparar, y despues del disparo no se encuentra sensibilizada, el contador de la misma se pone a 0
			// Tiene que esperar sensibilizarse nuevamente para iniciar la cuenta.
			else if(transAntesdelDisparo.getDato(transicion, 0)==1 && transDespuesdelDisparo.getDato(transicion, 0)==0) {
				//consola.registrarDisparo("* Reset a T"+(transicion+1),1);
				timeStamp[transicion] = -1;
				//pw.println("*  Contador en cero para T"+(transicion+1));
			}
			//Si no estaba sensibilizada y despues de disparar si lo esta, se inicia la cuenta del cronometro alfa con setNuevoTimeStamp.
			else if((transAntesdelDisparo.getDato(transicion, 0)==0) && (transDespuesdelDisparo.getDato(transicion, 0)==1)  && (T_Inmediata[transicion] == 0 )) {
				timeStamp[transicion] = System.currentTimeMillis()+2;
				//pw.println("*	Inicio del contador para T"+(transicion+1)+"  ->"+timeStamp[transicion]);	
			}
			//Si no estaba sensibilizada y sigue sin estarlo despues del disparo, no se empieza la cuenta del cronometro alfa.
			else if(transAntesdelDisparo.getDato(transicion, 0)==0 && transDespuesdelDisparo.getDato(transicion, 0)==0){
				timeStamp[transicion] = -1;
				//pw.println("*   Salida 3. T"+(transicion+1));	
			}
		
		}
		updateVectorZ(transDespuesdelDisparo); // (E AND B AND L AND C)
	}
	//
	/**
	 * Recorro todas las transiciones y hago and entre las sensibilizadas por E AND B y su ventana de tiempo.
	 * test_ventana(T) : verifica si la transicion esta en la ventana de tiempo.
	 * Si esta en la venta y esta en q entonces VectorZ = 1
	 * @param q: Transiciones despues del disparo. 
	 */
	public void updateVectorZ(Matriz q) {
		//pw.println("* updateVectorZ");
		for (int T= 0; T < cantidad_transiciones; T++) { 
			if((timeStamp[T] != 0) & q.getDato(T, 0)==1) { 
				VectorZ.setDato(0, T, 1);
			}
			else {
				VectorZ.setDato(0, T, 0);
			}
		}
	}
	/**
	 * 
	 * @param q = vector extendido sin o con Vz?
	 * @return
	 */
	public Matriz getVectorZ(Matriz q ) {
		updateVectorZ(q);
		return VectorZ;
	}
	public Matriz VectorZ() {
		
		return VectorZ;
	}
   public long getTiempoFaltanteParaAlfa(int transicion) {
		// TODO Auto-generated method stub
		boolean comparacion1= timeStamp[transicion]>=(long)Intervalo.getDato(0, transicion); //Si el time estan es mayor a alfa
		boolean comparacion2=timeStamp[transicion]<=(long)Intervalo.getDato(1, transicion); //Si el time esta antes del beta
		//System.out.println("TimeStamp "+ timeStamp[transicion]);
		if(comparacion1 && comparacion2) {
			//pw.println("* Salida 1");
			return (long)0;
		}
		
		else {
			long now =  new Date().getTime();
			int Tiempo_esperar = (int) ((((timeStamp[transicion]) + (Intervalo.getDato(0, transicion)))
					- (now)));
			if(Tiempo_esperar <= 0) {
				//pw.println("* Salida 2"); 
				return (long)0;
			     }
			else if(timeStamp[transicion] <= 0) {
				//pw.println("* Salida 2.5"); 
				return (long)0;
			     }
			else {
				//pw.println("* Salida 3 :"+Tiempo_esperar+"  timeStamp[transicion] = "+timeStamp[transicion]+ " now :"+ (now)+ " int :"+Intervalo.getDato(0, transicion));
				return ((long)Tiempo_esperar);
			}
		}
	}
}

