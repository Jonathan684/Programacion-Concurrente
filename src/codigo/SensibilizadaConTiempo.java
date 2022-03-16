package codigo;

import log.Log;

public class SensibilizadaConTiempo {

	private long timeStamp[];
	//private int SetEsperando[];
	private Log consola;
	private Matriz Intervalo;
	private int cantidad_transiciones;
	private Matriz VectorZ;
	private int [] T_Inmediata;
	//private long timeout[] ;
	//private Mutex mutex;
	
	public SensibilizadaConTiempo(int cantidad_transiciones ,Log consola2, Matriz Intervalo) {
		this.consola = consola2;
		timeStamp = new long[cantidad_transiciones];
		//SetEsperando = new int[cantidad_transiciones];
		T_Inmediata = new int[cantidad_transiciones];
		VectorZ = new Matriz(1, cantidad_transiciones);
		this.Intervalo = Intervalo;
		//this.mutex = mutex;
		
		this.cantidad_transiciones = cantidad_transiciones;
		for (int i = 0; i < cantidad_transiciones; i++) {
			timeStamp[i] = -1;
			//SetEsperando[i] = 0;
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
		
		
		
		//System.out.println("ActualizarTimeStamp T"+(transicion_a_disparar+1));
		//for(int elem : T_Inmediata)System.out.print(" "+elem);
//		System.out.println();
//		System.out.println("\n vector antes del disparo");
//		transAntesdelDisparo.getTranspuesta().imprimirMatriz();
//		System.out.println("\n vector despues del disparo");
//		transDespuesdelDisparo.getTranspuesta().imprimirMatriz();
		
		for (int transicion = 0; transicion < cantidad_transiciones; transicion++) { //Se recorren todas las transiciones
			
			if(transAntesdelDisparo.getDato(transicion, 0)==1 && transDespuesdelDisparo.getDato(transicion, 0)==1) { //Si la transicion se encontraba sensibilizada antes del disparo y sigue sensibilizada despues de disparar, no se resetea el contador de la misma 
				if(!(transicion==transicion_a_disparar)) { //A MENOS QUE justamente sea esa la transicion disparada (tiene que reinizializar su cronometro)
					//System.out.println("Continua la cuenta");
					//consola.registrarDisparo("* Continua la cuenta para T"+(transicion+1),1);
					timeStamp[transicion] = System.currentTimeMillis(); //continua la cuenta
				}
			}
			
			//Si la transicion estaba sensibilizada antes de disparar, y despues del disparo no se encuentra sensibilizada, el contador de la misma se pone a 0
			// (tiene que esperar sensibilizarse nuevamente para iniciar la cuenta).
			else if(transAntesdelDisparo.getDato(transicion, 0)==1 && transDespuesdelDisparo.getDato(transicion, 0)==0) {
				//consola.registrarDisparo("* Reset a T"+(transicion+1),1);
				timeStamp[transicion] = -1;
				//System.out.println("Contador en cero para T"+(transicion+1));
			}
			
			//Si no estaba sensibilizada y despues de disparar si lo esta, se inicia la cuenta del cronometro alfa con setNuevoTimeStamp.
			else if((transAntesdelDisparo.getDato(transicion, 0)==0) && (transDespuesdelDisparo.getDato(transicion, 0)==1)  && (T_Inmediata[transicion] == 0 )) {
				//consola.registrarDisparo("* Inicia la cuenta a T"+(transicion+1),1);
				timeStamp[transicion] = System.currentTimeMillis();
				//System.out.println("Inicio del contador para T"+(transicion+1));
			}
			
			//Si no estaba sensibilizada y sigue sin estarlo despues del disparo, no se empieza la cuenta del cronometro alfa.
			else if(transAntesdelDisparo.getDato(transicion, 0)==0 && transDespuesdelDisparo.getDato(transicion, 0)==0){
				//consola.registrarDisparo("* Reset "+(transicion+1),1);
				timeStamp[transicion] = -1;
				//System.out.println("Contador en -1 T"+(transicion+1));
			}
		
		}
		consola.registrarDisparo("* updateVectorZ", 1);
		updateVectorZ(transDespuesdelDisparo); // (E AND B AND L AND C)
		
	}
	public void updateVectorZ(Matriz q) {
		//System.out.println("_________updateVectorZ___________");
//		System.out.println("vectorExtendido");
//		q.getTranspuesta().imprimirMatriz();
//		System.out.println("¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯");
		for (int T= 0; T < cantidad_transiciones; T++) { //recorro todas las transiciones y hago and entre las sensibilizadas por E AND B y su ventana de tiempo
			if(test_ventana(T) & q.getDato(T, 0)==1) { 
				VectorZ.setDato(0, T, 1);
			}
			else {
				VectorZ.setDato(0, T, 0);
			}
		}
//		System.out.println("vectorZ");
//		VectorZ.imprimirMatriz();
//		System.out.println("_________________________________");
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
		//Marca_actual(transicion);
		long TimeStamp_ahora = (System.currentTimeMillis() - (timeStamp[transicion]));
		//System.out.println("TimeStamp_ahora "+timeStamp[transicion]+" para T"+(transicion+1)+"                        Time :"+TimeStamp_ahora);
		// Esta en la ventana de tiempo
		// Es inmediata
		// O no timeStamp = -1
		if (((TimeStamp_ahora >= (Intervalo.getDato(0, transicion)))
				&& (TimeStamp_ahora <= (Intervalo.getDato(1, transicion))))
				|| (T_Inmediata[transicion] == 1)
				|| (timeStamp[transicion] == -1)) {
			return true;//en la ventana de tiempo
		} 
		
		if(TimeStamp_ahora < Intervalo.getDato(0, transicion)) {
			return false;	//antes del alfa
		}
		//System.out.println("TimeStamp_ahora :"+TimeStamp_ahora);
//		return false; // despues del beta
		throw new RuntimeException("\n Beta demasiado chico, elegir un beta mas grande : \n T"+ (transicion+1)+"\n Tiempo :"+System.currentTimeMillis()+ "\n TimeStamp :"+timeStamp[transicion]);
		
			
		
			
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
			//System.out.println("Salida 1");
			return 0;
		}
		
		else {
			int Tiempo_esperar = (int) ((((timeStamp[transicion]) + (Intervalo.getDato(0, transicion)))
					- System.currentTimeMillis()) + 2);
			if(Tiempo_esperar <= 0) {
				//System.out.println("Salida 2");
				return (long)0;
			}
			else {
				//System.out.println("Salida 3");
				return ((long)Tiempo_esperar);
			}
		}
	}
}

