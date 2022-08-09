package codigo;

import java.io.PrintWriter;
import java.util.Date;

import log.Log;

public class SensibilizadaConTiempo {

	private long timeStamp[];
	private Matriz Intervalo;
	private int cantidad_transiciones;
	private Matriz VectorZ;
	private int [] T_Inmediata;
	private	PrintWriter pw;
	public SensibilizadaConTiempo(int cantidad_transiciones , Matriz Intervalo,PrintWriter pw) {
		this.pw = pw;
		timeStamp = new long[cantidad_transiciones];
		T_Inmediata = new int[cantidad_transiciones];
		VectorZ = new Matriz(1, cantidad_transiciones);
		this.Intervalo = Intervalo;
		this.cantidad_transiciones = cantidad_transiciones;
		for (int i = 0; i < cantidad_transiciones; i++) {
			timeStamp[i] = -1;
			T_Inmediata[i] = 0;
		}
		Inmediatas();
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
	/*
	 * Este metodo verifica si se habilitaron transiciones temporales
	 */			
	// Metodos Set
//	public void setEsperando(int transicion) {
//		SetEsperando[transicion] = 1;
//	}
//	public void resetEsperando(int transicion) {
//		//timeStamp[transicion] = 0;
//		SetEsperando[transicion] = 0;
//	}

	// Metodos Get
	public long[] getTimeStamp() {
		return timeStamp;
	}

//	public boolean getEsperando(int transicion) {
//
//		if (SetEsperando[transicion] == 1)
//			return true;
//
//		return false;
//	}
//	public int[] getvectorEsperando() {
//
//		return SetEsperando;
//	}
	// TODO Auto-generated method stub
			/*
			 * De lo contrario, si la red tiene un marcado diferente al inicial (se disparo alguna transicion)
			 * se realiza lo siguiente:
			 */
			
	public void ActualizarTimeStamp(Matriz transAntesdelDisparo, Matriz transDespuesdelDisparo, int transicion_a_disparar) {
		
//		System.out.println("ActualizarTimeStamp T"+(transicion_a_disparar+1));
//        for(int elem : T_Inmediata)System.out.print(" "+elem);
//		System.out.println();
//		System.out.println("\n vector antes del disparo");
//		transAntesdelDisparo.getTranspuesta().imprimirMatriz();
//		System.out.println("\n vector despues del disparo");
//		transDespuesdelDisparo.getTranspuesta().imprimirMatriz();
		
		for (int transicion = 0; transicion < cantidad_transiciones; transicion++) { //Se recorren todas las transiciones
			
			if(transAntesdelDisparo.getDato(transicion, 0)==1 && transDespuesdelDisparo.getDato(transicion, 0)==1) { //Si la transicion se encontraba sensibilizada antes del disparo y sigue sensibilizada despues de disparar, no se resetea el contador de la misma 
				if(transicion==transicion_a_disparar) { //A MENOS QUE justamente sea esa la transicion disparada (tiene que reinizializar su cronometro)
					//System.out.println("Continua la cuenta");
					//consola.registrarDisparo("* Continua la cuenta para T"+(transicion+1),1);
					timeStamp[transicion] = System.currentTimeMillis(); //continua la cuenta
					//System.out.println("transicion :"+transicion+" :"+timeStamp[transicion]);
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
				//consola.registrarDisparo("* Inicia la cuenta a T"+(transicion+1),1);
				
				timeStamp[transicion] = System.currentTimeMillis()+2;
				pw.println("*	Inicio del contador para T"+(transicion+1)+"  ->"+timeStamp[transicion]);	
				//System.out.println("Inicio del contador para T"+(transicion+1)+"  ->"+timeStamp[transicion]);
			}
			
			//Si no estaba sensibilizada y sigue sin estarlo despues del disparo, no se empieza la cuenta del cronometro alfa.
			else if(transAntesdelDisparo.getDato(transicion, 0)==0 && transDespuesdelDisparo.getDato(transicion, 0)==0){
				//consola.registrarDisparo("* Reset "+(transicion+1),1);
				
				timeStamp[transicion] = -1;
				//pw.println("*   Salida 3. T"+(transicion+1));	
				//System.out.println("Contador en -1 T"+(transicion+1));
			}
		
		}
		//consola.registrarDisparo("* updateVectorZ", 1);
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
		//long TimeStamp_ahora = new Date().getTime();
		//System.out.println("Tiempo :"+TimeStamp_ahora);
		//pw.println("* updateVectorZ");
		for (int T= 0; T < cantidad_transiciones; T++) { 
			if(test_ventana(T) & q.getDato(T, 0)==1) { 
				VectorZ.setDato(0, T, 1);
//				if(T == 5) {
//					pw.println("* pone 1");	
//				}
			}
			else {
//				if(T == 5) {
//					pw.println("* pone 0");	
//				}
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
	public boolean test_ventana(int transicion) {
//		if(timeStamp[transicion] == -1) {
//			return false;
//		}
		//Marca_actual(transicion);
		long l =  new Date().getTime();
		//long TimeStamp_ahora = (System.currentTimeMillis() - (timeStamp[transicion]));
		long TimeStamp_ahora = l - (timeStamp[transicion]);
		//System.out.println("TimeStamp_ahora "+timeStamp[transicion]+" para T"+(transicion+1)+"                        Time :"+TimeStamp_ahora+" l->"+l);
		// Esta en la ventana de tiempo
		// Es inmediata
		// O no timeStamp = -1
		
		// EN LA VENTANA DE TIEMPO
//		if(transicion == 4) {
//			pw.println("* ANALISIS PARA T"+(transicion+1));
//			pw.println("* Intervalo.getDato(0, transicion) :" + Intervalo.getDato(0, transicion));
//			pw.println("* TimeStamp_ahora :" + TimeStamp_ahora);
//			pw.println("* timeStamp[transicion] : T"+(transicion+1)+" = " + timeStamp[transicion] +" l="+l);
//		}
		if (((TimeStamp_ahora >= (Intervalo.getDato(0, transicion)))
			&& (TimeStamp_ahora <= (Intervalo.getDato(1, transicion))))//ESTA EN LA VENTA DE TIEMPO
				|| (T_Inmediata[transicion] == 1) 						//ES INMEDIATA
				|| (timeStamp[transicion] == -1)) 						//TODAVIA NO INICIO EL TIEMPO
		{
			//pw.println("* timeStamp[transicion]" + timeStamp[transicion]);
			//pw.println("* Retorna true");
			return true;//en la ventana de tiempo
		} 
		// ANTES DEL ALFA o Despues del beta
		else {
			
			//pw.println("* Retorna false");
			return false;
		}
			//else(TimeStamp_ahora < Intervalo.getDato(0, transicion)) {
		//	return false;	//antes del alfa
		//}
		//System.out.println("TimeStamp_ahora :"+TimeStamp_ahora);
        //return false; // despues del beta
		//throw new RuntimeException("\n Beta demasiado chico, elegir un beta mas grande : \n T"+ (transicion+1)+"\n Tiempo :"+System.currentTimeMillis()+ "\n TimeStamp :"+timeStamp[transicion]);
	}
	public int[] getInmediata() {
		return T_Inmediata;
	}
	public long[] timeStamp() {
		return timeStamp;
	}

	public long getTiempoFaltanteParaAlfa(int transicion) {
		// TODO Auto-generated method stub
		boolean comparacion1= timeStamp[transicion]>=(long)Intervalo.getDato(0, transicion); //Si el time estan es mayor a alfa
		boolean comparacion2=timeStamp[transicion]<=(long)Intervalo.getDato(1, transicion); //Si el time esta antes del beta
		//System.out.println("TimeStamp "+ timeStamp[transicion]);
		if(comparacion1 && comparacion2) {
			pw.println("* Salida 1");
			return (long)0;
		}
		
		else {
			int Tiempo_esperar = (int) ((((timeStamp[transicion]) + (Intervalo.getDato(0, transicion)))
					- System.currentTimeMillis()));
			if(Tiempo_esperar <= 0) {
				pw.println("* Salida 2"); 
				return (long)0;
			     }
			else if(timeStamp[transicion] <= 0) {
				pw.println("* Salida 2.5"); 
				return (long)0;
			     }
			else {
				pw.println("* Salida 3 :"+Tiempo_esperar+"  timeStamp[transicion] = "+timeStamp[transicion]);
				return ((long)Tiempo_esperar);
			}
		}
	}
}

