package codigo;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class RDP {

/////////Marcado inicial/////////////////////////////////////////////////////////
	private String [] Transiciones;
	private String[] Plazas;
    private Matriz VectorMarcadoActual, VectorExtendido, VectorSensibilizado, VectorInhibicion ; // , VectorExtendido_auxiliar;
	private Matriz Incidencia,Inhibicion,Identidad;
	private Matriz Intervalo;
    private Matriz VectorZ;
    private final int numeroPlazas;
	private final int numeroTransiciones;
    private final List<Matriz> invariantes;
	private Scanner input;
	private long SensibilizadaConTiempo[]; 
	private Matriz IEntrada ;
	private SensibilizadaConTiempo Temporizadas;
	private Mutex mutex;
	private Log consola;
	//private int [] ;
////////////////////////////////////////////////////////////////////////////////////////////////////
//EL VALOR ACTUAL ES IGUAL AL INICIAL
////Constructor
	public RDP(Mutex mutex,Log consola) {
	
	this.consola = consola;
	this.mutex = mutex;
	numeroTransiciones = cargarTransiciones("matrices/M.I.txt");	//Extraccion de la cantidad de transiciones.
	numeroPlazas = cargarPlazas("matrices/M.I.txt");				//Extraccion de la cantidad de plazas.
	//Matrices
	invariantes = cargarInvariantes("matrices/InvTrans.txt");
	Incidencia = new Matriz(numeroPlazas,numeroTransiciones);
	Inhibicion = new Matriz(numeroTransiciones,numeroPlazas);
	Identidad = new Matriz(numeroTransiciones,numeroTransiciones);
	IEntrada = new Matriz(numeroPlazas,numeroTransiciones);

	Intervalo = new Matriz(2,numeroTransiciones);
	//ISalida = new Matriz(numeroPlazas,numeroTransiciones);

	//Vectores
	VectorMarcadoActual = new Matriz(numeroPlazas,1);
	VectorSensibilizado = new Matriz(numeroTransiciones, 1);
	VectorInhibicion = new Matriz(numeroTransiciones, 1);
	VectorExtendido = new Matriz(numeroTransiciones, 1);
    VectorZ = new Matriz(1,numeroTransiciones);
	
    Intervalo.cargarMatriz("matrices/IZ.txt");
	//Carga de datos
	Incidencia.cargarMatriz("matrices/M.I.txt");
	//Inhibicion.cargarMatriz("matrices/M.B.txt");
	VectorMarcadoActual.cargarMatriz("matrices/VMI.txt");
	
	Identidad.cargarIdentidad();
	
	IEntrada.cargarMatriz("matrices/M.Pre.txt");
	SensibilizadaConTiempo  = new long[numeroTransiciones];
	
	
	
	Temporizadas = new SensibilizadaConTiempo(get_numero_Transiciones(), consola);
	sensibilizarVectorZ();
    }
	
    private List<Matriz> cargarInvariantes(String pathI) {
		List<Matriz> invariantes = new ArrayList<>();
		try {
			input = new Scanner(new File(pathI));
			while (input.hasNextLine()) {
				String line = input.nextLine();
				Matriz inv = new Matriz(1, numeroTransiciones);

				for (int columna = 0; columna < line.length (); columna ++) {
					char c = line.charAt (columna);
					if(c == 1) inv.setDato(1, columna, 1);
				}

				invariantes.add(inv);
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		return invariantes;
	}
    //METODOS PRIVADOS
	//-----------------------------------------------------
	/**
	 * Este metodo devuelve la cantidad de transiciones disponibles en la red
	 * @param pathMI la ruta al archivo de texto que contiene la matriz de incidencia
	 * @return transiciones de la red
	 */
	private int cargarTransiciones(String pathMI){
		int nrotrans = 0;
		try {
			input = new Scanner(new File(pathMI));
			//while (input.hasNextLine()) {
			String line = input.nextLine();
			for (int columna = 0; columna < line.length (); columna ++) {
				char c = line.charAt (columna);
				if(c == '1' || c == '0') nrotrans ++ ;
			}
			setStringTranciones(nrotrans);
				//break;
			//}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		return nrotrans;
	 }
	/**
	 * Este metodo devuelve la cantidad de plazas disponible en la red
	 * @param pathMI la ruta al archivo de texto que contiene la matriz de incidencia
	 * @return Plazas de la red
	 */
	private int cargarPlazas(String pathMI) {
		int nroPlazas = 0;
		try {
			input = new Scanner(new File(pathMI));
			while (input.hasNextLine()) {
				 input.nextLine();
				 nroPlazas ++;
			}
			setStringPlazas(nroPlazas);
		}
	  catch (IOException e) {
		  e.printStackTrace();
	  }
		return nroPlazas;
	}


	/**
	 * Completa el string de Transiciones con la cantidad correspondiente
	 * @param nroT: la cantidad de transiciones en la matriz de incidencia.
	 */
	private void setStringTranciones(int nroT) {
		Transiciones = new String[nroT];
//		Transiciones[0]= "T1";
//		Transiciones[1]= "T10";
//		Transiciones[2]= "T2";
//		Transiciones[3]= "T3";
//		Transiciones[4]= "T4";
//		Transiciones[5]= "T5";
//		Transiciones[6]= "T6";
//		Transiciones[7]= "T7";
//		Transiciones[8]= "T8";
//		Transiciones[9]= "T9";
//				//T1 T10 T2 T3 T4 T5 T6 T7 T8 T9
		for(int t = 1; t<nroT+1; t++) {	Transiciones[t-1] = "T"+t; }
	}
	/**
	 * Completa el string de Plazas con la cantidad correspondiente
	 * @param nroP: la cantidad de plazas en la matriz de incidencia.
	 */
	private void setStringPlazas(int nroP) {
		Plazas = new String[nroP];
        Plazas[0] = "P1";
		Plazas[1] = "P10";
		Plazas[2] = "P11";
		Plazas[3] = "P12";
		Plazas[4] = "P13";
		Plazas[5] = "P2";
		Plazas[6] = "P3";
		Plazas[7] = "P4";
		Plazas[8] = "P5";
		Plazas[9] = "P6";
		Plazas[10] = "P7";
		Plazas[11] = "P8";
		Plazas[12] = "P9";
		Plazas[13] = "P14";
		
		
		//for(int p = 1; p<nroP+1; p++) { Plazas[p-1] = "P"+p; }		
		//1 10 11 12 13 2 3 4 5 6 7 8 9 14
	}
	
	/**
	 * Metodo que calcula el vector Inhibicion
	 */
	/*private void sensibilizarVectorB() {
		Matriz Q = new Matriz(numeroPlazas, 1);
		for(int i=0; i < Q.getNumFilas(); i++) {
			
			 if(VectorMarcadoActual.getDato(i, 0) != 0 ) Q.setDato(i, 0, 1);
			else Q.setDato(i, 0, 0);

		}
		VectorInhibicion = Inhibicion.getMultiplicacion(Q);
		for(int i=0; i < VectorInhibicion.getNumFilas(); i++) {
			if(VectorInhibicion.getDato(i, 0)>1)VectorInhibicion.setDato(i, 0, 1);
		}
		VectorInhibicion = VectorInhibicion.getComplemento();
	}*/
	/**
	 * Metodo que calcula el vector sensibilizado
	 */
	private void sensibilizarVectorE() {
		
		//System.out.println("Columnas "+IEntrada.getNumColumnas());
		//System.out.println("CONTROL");
		//IEntrada.imprimirMatriz();
		//VectorMarcadoActual.getTranspuesta().imprimirMatriz();
		for (int i = 0; i < IEntrada.getNumColumnas(); i++) {
			int e = 1;
			for (int j = 0; j < Incidencia.getNumFilas(); j++) {
				if (VectorMarcadoActual.getDato(j, 0) < IEntrada.getDato(j, i)) {
					e = 0;
				}
//				if(i==6) {
//					System.out.println("ERRORRRR "+IEntrada.getDato(j, i)+" j="+j+" i= "+i+" e= "+e+" vmi= "+ VectorMarcadoActual.getDato(j, 0));
//					//VectorMarcadoActual.getDato(j, 0)
//				}
				VectorSensibilizado.setDato(i, 0, e);
			}
		}
	    
		//System.out.println("Posible error en este punto");
		//VectorSensibilizado.getTranspuesta().imprimirMatriz();
	}
	
	//METODOS PUBLICOS
	//-----------------------------------------------------
	/**
	 * Metodo que sensibiliza las transiciones y carga el vector extendido
	 */
	public void sensibilizar() {
		//sensibilizarVectorB();
		//System.out.println("-------------Vector antes-------------");
		//VectorSensibilizado.getTranspuesta().imprimirMatriz();
		//System.out.println("**************Imprimir************");
		sensibilizarVectorE();
		//System.out.println("-------------Vector despues-------------");
		//VectorSensibilizado.getTranspuesta().imprimirMatriz();
		//System.out.println("**************Imprimir************");
		VectorExtendido = VectorSensibilizado;//.getAnd(VectorInhibicion);//.getAnd(VectorZ.getTranspuesta());		
		
		//System.out.println("************** metodo sinsibilizar ************");
	}
	
	///////////////////	TRABAJANDO ACA /////////////////////////////////////////////
	/*
	 * Al estar esperando T3 deberia tambien anularse T2 como si fuera un falso disparo
	 */
	public void setEsperando(int transicion)
	{
		
		VectorZ.setDato(0, transicion, 0);
		Temporizadas.setEsperando(transicion);
	}
	public void resetEsperando(int transicion)
	{
		
		Temporizadas.resetEsperando(transicion);
		VectorZ.setDato(0, transicion, 1);
//		Si el disparo de una transicion temporal desensibilizo
//		a otra transicion temporal deberia resetear si timeStamp
		
		
	}
	public void resetTimeDesensibilizadas() {
		/*for(int transicion_prueba = 0 ; transicion_prueba<VectorExtendido.getNumFilas();transicion_prueba++) {
			if((estaSensibilizada(transicion_prueba) == true) 
					&& (esTemporal(transicion_prueba) == true) 
					&& ((Temporizadas.getEsperando(transicion_prueba) == true)))
			{
				Matriz VectorExtendido_auxiliar = new Matriz(numeroTransiciones, 1);
			    VectorExtendido_auxiliar = Falso_Disparo(transicion_prueba);
				
				if(VectorExtendido_auxiliar.getDato(transicion,0) == 0) {
					observacion =  false;
					break;
				}
				
			}
		}*/
	}
	/*
	 * Este metodo hace un falso disparo con las transicion que estan en el vector Esperando
	 * Si un disparo proboca que la transicion que se envia por parametro cambie a estar desensibilizada
	 * significa que ya hay una transicion que espera por lo mismo que la transicion enviada por parametro.
	 *Return:
	 *		false: Si alguien espera por ese token 	
	 *		true: Si no alguien esperando
	 */
	public boolean  soy_el_primero(int transicion) {
		boolean soy_el_primero = true;
		for(int transicion_prueba = 0 ; transicion_prueba<VectorExtendido.getNumFilas();transicion_prueba++ ) {
			if((estaSensibilizada(transicion_prueba) == true) 
					&& (esTemporal(transicion_prueba) == true) 
					&& ((Temporizadas.getEsperando(transicion_prueba) == true)
				    && (transicion != transicion_prueba))){
				Matriz VectorExtendido_auxiliar = new Matriz(numeroTransiciones, 1);
				VectorExtendido_auxiliar = Falso_Disparo(transicion_prueba);
			    if(VectorExtendido_auxiliar.getDato(transicion,0) == 0) {
					soy_el_primero =  false;
					break;
				}
			}
		}
		return soy_el_primero;
		
	}
	/*
	 * Este metodo me devuleve si hay alguien esperando por ese token.
	 * Ya que realizo un falso disparo deshabilitando la transicion.
	 */
	public Matriz Falso_Disparo(int transicion)
	{
		//Falso disparo de las trasiciones dormirdas
		Matriz VectorExtendido_auxiliar = new Matriz(numeroTransiciones, 1);
		Matriz VectorMarcadoActual2;
		
		consola.registrarDisparo("* 0 m: "+VectorMarcadoActual.getDato(0,0),1);
		//VectorMarcadoActual2 = calculoDeVectorEstado2(transicion);
		Matriz aux = Incidencia.getMultiplicacion(Identidad.getColumna(transicion));
		/*
		consola.registrarDisparo("* ========================= ",1);
		consola.registrarDisparo("*Marcado Inicial y extendido",1);
		consola.registrarDisparo("* 0 m: "+VectorMarcadoActual.getDato(0,0),1);
		consola.registrarDisparo("* 7 m: "+VectorMarcadoActual.getDato(6,0),1);
		consola.registrarDisparo("* 8 m: "+VectorMarcadoActual.getDato(7,0),1);
		consola.registrarDisparo("* 9 m: "+VectorMarcadoActual.getDato(8,0),1);
		consola.registrarDisparo("* 10 m: "+VectorMarcadoActual.getDato(9,0),1);
		consola.registrarDisparo("* 12 m: "+VectorMarcadoActual.getDato(11,0),1);
		consola.registrarDisparo("* 13 m: "+VectorMarcadoActual.getDato(12,0),1);
		*/
		VectorMarcadoActual2 = VectorMarcadoActual.getSuma(aux);
		/*
		consola.registrarDisparo("* ========================= ",1);
		consola.registrarDisparo("*Marcado medio  ",1);
		consola.registrarDisparo("* 0 m: "+VectorMarcadoActual2.getDato(0,0),1);
		consola.registrarDisparo("* 7 m: "+VectorMarcadoActual2.getDato(6,0),1);
		consola.registrarDisparo("* 8 m: "+VectorMarcadoActual2.getDato(7,0),1);
		consola.registrarDisparo("* 9 m: "+VectorMarcadoActual2.getDato(8,0),1);
		consola.registrarDisparo("* 10 m: "+VectorMarcadoActual2.getDato(9,0),1);
		consola.registrarDisparo("* 12 m: "+VectorMarcadoActual2.getDato(11,0),1);
		consola.registrarDisparo("* 13 m: "+VectorMarcadoActual2.getDato(12,0),1);
		consola.registrarDisparo("* ========================= ",1);
		*/
		
		/*		
		consola.registrarDisparo("* ========================= ",1);
		consola.registrarDisparo("*Marcado despues de sensibilizar ver si lo modifico  ",1);
		consola.registrarDisparo("* 0 m: "+VectorMarcadoActual2.getDato(0,0),1);
		consola.registrarDisparo("* 7 m: "+VectorMarcadoActual2.getDato(6,0),1);
		consola.registrarDisparo("* 8 m: "+VectorMarcadoActual2.getDato(7,0),1);
		consola.registrarDisparo("* 9 m: "+VectorMarcadoActual2.getDato(8,0),1);
		consola.registrarDisparo("* 10 m: "+VectorMarcadoActual2.getDato(9,0),1);
		consola.registrarDisparo("* 12 m: "+VectorMarcadoActual2.getDato(11,0),1);
		consola.registrarDisparo("* 13 m: "+VectorMarcadoActual2.getDato(12,0),1);
		consola.registrarDisparo("* ========================= ",1);*/
		
		VectorExtendido_auxiliar = sensibilizarVectorE2(VectorMarcadoActual2).getAnd(sensibilizarVectorB2(VectorMarcadoActual2));//.getAnd(VectorZ.getTranspuesta());		
		/*
				
		consola.registrarDisparo("* ========================= ",1);
		consola.registrarDisparo("*Marcado Final  ",1);
		consola.registrarDisparo("* 0 m: "+VectorMarcadoActual2.getDato(0,0),1);
		consola.registrarDisparo("* 7 m: "+VectorMarcadoActual2.getDato(6,0),1);
		consola.registrarDisparo("* 8 m: "+VectorMarcadoActual2.getDato(7,0),1);
		consola.registrarDisparo("* 9 m: "+VectorMarcadoActual2.getDato(8,0),1);
		consola.registrarDisparo("* 10 m: "+VectorMarcadoActual2.getDato(9,0),1);
		consola.registrarDisparo("* 12 m: "+VectorMarcadoActual2.getDato(11,0),1);
		consola.registrarDisparo("* 13 m: "+VectorMarcadoActual2.getDato(12,0),1);
		consola.registrarDisparo("* ========================= ",1);
		consola.registrarDisparo("* 0 E: "+VectorExtendido.getDato(0,0),1);
		consola.registrarDisparo("* 7 E: "+VectorExtendido.getDato(6,0),1);
		consola.registrarDisparo("* 8 E: "+VectorExtendido.getDato(7,0),1);
		consola.registrarDisparo("* 9 E: "+VectorExtendido.getDato(8,0),1);
		consola.registrarDisparo("* 10 E: "+VectorExtendido.getDato(9,0),1);
		
		consola.registrarDisparo("* 0 EA: "+VectorExtendido_auxiliar.getDato(0,0),1);
		consola.registrarDisparo("* 7 EA: "+VectorExtendido_auxiliar.getDato(6,0),1);
		consola.registrarDisparo("* 8 EA: "+VectorExtendido_auxiliar.getDato(7,0),1);
		consola.registrarDisparo("* 9 EA: "+VectorExtendido_auxiliar.getDato(8,0),1);
		consola.registrarDisparo("* 10 E: "+VectorExtendido_auxiliar.getDato(9,0),1);
		*/
		return VectorExtendido_auxiliar;
	}
		
	/**
	 * Metodo que calcula el vector Inhibicion
	 */
	private Matriz sensibilizarVectorB2(Matriz VectorMarcadoActual2) {
		Matriz VectorInhibicion2;
		Matriz Q2 = new Matriz(numeroPlazas, 1);
		for(int i=0; i < Q2.getNumFilas(); i++) {
			
			 if(VectorMarcadoActual2.getDato(i, 0) != 0 ) Q2.setDato(i, 0, 1);
			else Q2.setDato(i, 0, 0);

		}
		VectorInhibicion2 = Inhibicion.getMultiplicacion(Q2);
		for(int i=0; i < VectorInhibicion.getNumFilas(); i++) {
			if(VectorInhibicion2.getDato(i, 0)>1)VectorInhibicion2.setDato(i, 0, 1);
		}
		VectorInhibicion2 = VectorInhibicion2.getComplemento();
		return VectorInhibicion2;
	}
	
	/**
	 * Metodo que calcula el vector sensibilizado
	 */
	private Matriz sensibilizarVectorE2(Matriz VectorMarcadoActual2) {
		Matriz VectorSensibilizado2;
		VectorSensibilizado2 = new Matriz(numeroTransiciones, 1);
		for (int i = 0; i < IEntrada.getNumColumnas(); i++) {
			int e = 1;
			for (int j = 0; j < Incidencia.getNumFilas(); j++) {
				if (VectorMarcadoActual2.getDato(j, 0) < IEntrada.getDato(j, i)) {
					e = 0;
				}
				//VectorMarcadoActual2
				VectorSensibilizado2.setDato(i, 0, e);
			}
		}
		return VectorSensibilizado2;
	}
	///////////////////////////////////////////////////////////////////////////////
	/*public void resetEsperando(int transicion)
	{
		VectorZ.setDato(0, transicion, 1);
		//VectorZ.imprimirMatriz();
	}*/
	
    public void sensibilizarVectorZ() {
    	for(int k = 0 ; k<numeroTransiciones; k++) {VectorZ.setDato(0, k, 1);}
    	//VectorZ.imprimirMatriz();
    }
        
   /* public boolean instante_T(int transicion) {
        boolean antes = Temporizadas.antesDeLaVentana(transicion,Intervalo); 
    	
        if (antes == true) {	
    		return true;
    	}
    	return false;
    }*/
    /*
     * Metodo donde espera el hilo.
     * 
     */
    public void esperar(int transicion) {
    	int alfa = Intervalo.getDato(0, transicion);
        long Tiempo_esperar =  (Temporizadas.getTimeStamp()[transicion]+alfa)-System.currentTimeMillis();
    	//System.out.println("OBSERVACION : "+Tiempo_esperar+ " HILO: "+Thread.currentThread().getName());
        if(Tiempo_esperar > 0) {
        try {
			Thread.sleep(Tiempo_esperar);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        }
        
    }
    
    /**
	 * Este metodo dispara una transicion de la rdp indicada por parametro, teniendo en cuenta el modo indicado por parametro
	 *@param transicion : numero de transicion.
	 *@return : -0 retorna 0 si el disparo no es exitoso.
	 *          -1 retorna 1 si el disparo es exitoso.
	 */
	public boolean Disparar(int transicion){
//		
		//System.out.println("Cambiando el valor de transicion antes"+transicion);
		//if(transicion != 0) {
			//transicion = buscar_en_posicion(transicion);
				
		//}
		//System.out.println("Cambiando el valor de transicion despues"+transicion);
		
		boolean k = true;
		if(!estaSensibilizada(transicion)){
			return false;
		}
		//if(estaSensibilizada(transicion) && esTemporal(transicion)) 
		///////////////////////////////////////////////////////////////////////////////
		if(esTemporal(transicion)) 
		{ 
			boolean soy_el_primero = false;
			
			
			consola.registrarDisparo("\n* Transicion temporal, control de ventana T["+(transicion+1)+"]\n"+ "* Tiempo: "+ System.currentTimeMillis(),1);
			boolean ventana = false;
			ventana =  Temporizadas.testVentana(transicion,Intervalo);
			soy_el_primero = soy_el_primero(transicion); // true si no hay alguien esperando
			
			consola.registrarDisparo("* Ventana: "+ventana,1);
			consola.registrarDisparo("* Soy el primero: "+soy_el_primero+" T"+(transicion+1),1);
			
			if(ventana == true && soy_el_primero == true){ // Soy el primero
				    k = true;
					consola.registrarDisparo("* Se va a disparar: "+"T"+(transicion+1),1);
				}
			else{//ventana == false
				//pregunto si estoy antes de la ventana
				boolean antes = false;
				antes  = Temporizadas.antesDeLaVentana(transicion,Intervalo); 
				
				if(antes == true && soy_el_primero == true){ // soy el primero y estoy antes de la ventana de tiempo
					consola.registrarDisparo("* Se va a esperar: T"+(transicion+1),1);
					setEsperando(transicion);
					//sensibilizar();
					//int alfa = Intervalo.getDato(0, transicion);
			        //long Tiempo_esperar =  Temporizadas.getTimeStamp()[transicion]+alfa-System.currentTimeMillis();
			    	//consola.registrarDisparo("* Tiempo a esperar: "+ Tiempo_esperar +"ms\n"+"* Inicio: " +System.currentTimeMillis(),1);
					
			    	
			    	mutex._release();
					esperar(transicion);
					mutex._acquire();
					consola.registrarDisparo("\n* Desperte tomé el acquire: T"+(transicion+1),1);//+"\n* Fin: " +System.currentTimeMillis(),1);
					//primero_en_llegar = false;
					k = true; // La politica debera elegir a quien disparar
					//consola.registrarDisparo("\n* Valor de k: "+k,1);
				}
				/*
				 * Si estoy despues de la ventana y no hay nadie esperando por ella k == true
				 * 
				 */
				else if(antes == false && soy_el_primero == true ){// estoy despues de la ventana de tiempo y soy el primero
					consola.registrarDisparo("* Antes : "+antes,1); 
					// Estoy despues de la ventana pero soy el primero en llegar
					consola.registrarDisparo("* Estoy despues de la ventana pero soy el primero en llegar: T"+(transicion+1),1);
					k = true;
				}
				else if(antes == false && soy_el_primero == false ){
				    consola.registrarDisparo("* Estoy despues de la ventana pero no soy el primero en llegar: T"+(transicion+1),1);
					k = false;
				}
				else if(antes == true && soy_el_primero == false ){
				    consola.registrarDisparo("* Estoy antes de la ventana pero no soy el primero en llegar: T"+(transicion+1),1);
					k = false;
				}
			}
		}
		////////////////////////////////////////////
		//System.out.println("Valor de k en disparar :"+k +" esta sensibilizada "+ estaSensibilizada(transicion));
		if(k == true && estaSensibilizada(transicion)==true)
		{
			if(esTemporal(transicion)) {
				resetEsperando(transicion);
			}
			calculoDeVectorEstado(transicion);
			Test_Invariante();
			sensibilizar();
			if(esTemporal(transicion)) {
				resetTimeDesensibilizadas();
			}
			actualiceSensibilizadoT();//Comprueba el disparo de esta transicion ha habilitado el contador de trasiciones temporales sucecivas
			return true;
		}
		else {return false;}
	}
	
	public void calculoDeVectorEstado(int transicion){
	//	System.out.println("TRABAJANDO EN EL CALCULO DEL VECTOR DE ESTADOS\n");
		//transicion = buscar_en_posicion(transicion);
		//VectorMarcadoActual.getTranspuesta().imprimirMatriz();
		Matriz aux = Incidencia.getMultiplicacion(Identidad.getColumna(transicion)); 
		VectorMarcadoActual = VectorMarcadoActual.getSuma(aux);
		//VectorMarcadoActual.getTranspuesta().imprimirMatriz();
	//	System.out.println("------------- FIN -----------------------------\n");
	}
	/*
	 *  Comprueba si se sensibilizaron transiciones temporales colocar el valor TimeStamp
	 */
	public void actualiceSensibilizadoT() {
		for (int t=0; t<numeroTransiciones;t++)
		{
			if(esTemporal(t) && (VectorExtendido.getDato(t, 0) == 1)){
				Temporizadas.setNuevoTimeStamp(t);
				//System.out.println("Entro para la transicion "+(t+1));
			}
			if((esTemporal(t)==true) && VectorExtendido.getDato(t, 0)==0 && (Temporizadas.getEsperando(t)==true)) {
				Temporizadas.resetEsperando(t);
				consola.registrarDisparo("* Borrado del TimeStamp de: T"+t,1);
			}
		}
	}
	
	public boolean esTemporal(int transicion) {
			if(Intervalo().getDato(0, transicion)-Intervalo().getDato(1, transicion) != 0)
			{
				return true;
			}
		return false;
	}
	
    public Matriz Intervalo() {return Intervalo;}
	
    public long [] SensibilizadaConTiempo() {return SensibilizadaConTiempo;}
	
	public Matriz getVectorZ() { return VectorZ; }
	/**
	 * Devuelve true si la transicion esta habilitada de acuerdo al vector extendido
	 * @param transicion : transicion la que se desea saber si está habilitada o no.
	 * @return verdadero estan habilitadas
	 */
	public boolean estaSensibilizada(int transicion) {
		//System.out.println();
		//System.out.println("Se busca en posicion en sensibilizada: "+transicion);
		//int posicion = buscar_en_posicion(transicion);
		//System.out.println("Posicion encontrada: "+posicion);
		//System.out.println("Transicion : "+VectorExtendido.getDato(transicion,0));
		//System.out.println("Transicion esta sensibilizada: "+VectorExtendido.getDato(posicion,0));
		//VectorExtendido.getTranspuesta().imprimirMatriz();
		//System.out.println("=============================================");
		//System.out.println();
		if(VectorExtendido.getDato(transicion,0) == 1) 
			return true; 
		else return false;
	}
	/**
	 * Metodo que devuelve el vector con las transiciones sensibilizadas
	 * @return vector extendido
	 */
	public Matriz getVectorExtendido() {return VectorExtendido; } 
	public Matriz getVectorSensibilizado() {return VectorSensibilizado; } 
	/**
	 * Metodo que devuelve la matriz de inhibicion
	 * @return matriz inhibicion
	 */
	public Matriz getMatrizInhibicion() { return VectorInhibicion; }
	/**
	 * Metedo encargado de verificar si se cumple el los p-Inavariantes.
	 * @param vector vector a imprimir.
	 */
	public boolean Test_Invariante() {
	    //System.out.println("Test de Invariante de plaza");
		CharSequence cort ;
		int Inv=0;
	    int Suma = 0;//suma los valores que hay en las plaza 
	    try { 
			input = new Scanner(new File("matrices/P_Invariantes.txt"));
			while (input.hasNextLine()) {
				String line = input.nextLine();
				for(int k =0 ; k<line.length() ; k++) {
					 char r =line.charAt(k);
					 if(r=='(') {
						for(int z =k ; z<line.length() ; z++) {
							 char h =line.charAt (z);
							 if(h==')') {
								 cort = line.subSequence(k+2 , z);
								 Inv = Integer.parseInt(cort.toString());
								 Suma = Suma + VectorMarcadoActual.getDato(Inv-1,0);
								 k=z;
								 break;
								 }
					                                             }
						}
					 else if(r == '=') {
				
						 	 cort = line.subSequence(k+2,line.length());
						     Inv = Integer.parseInt(cort.toString());
							 if(Inv != Suma) {
								 return false;
							 }
							 Suma = 0;
					 }
				}
			}
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
		return true;
	}
	/**
	 * Este metodo muestra el vector indicado por parametro
	 * @param vector vector a imprimir.
	 * 		  Tipo 0: Transiciones 
	 * 		       1: Plazas
	 */
	public void mostrar(Matriz vector ,int Tipo) {
			//System.out.println("\n");
			if(Tipo == 0) {
				for(int n=0 ; n<vector.getNumFilas() ; n++) System.out.print(Transiciones[n] +":" + vector.getDato(n, 0) +" ");
			}
			else if(Tipo > 0) {
				for(int n=0 ; n<vector.getNumFilas(); n++) System.out.print(Plazas [n] +":" +  vector.getDato(n, 0) +" ");
			}
			System.out.println("\n");
		   
	}
	/**
	 * 
	 * @param vector
	 * @return Transiciones_sensibilizadas tipo String
	 */
	public String sensibilidadas(Matriz vector) {
		//System.out.println("\n");
		String Transiciones_sensibilizadas = "";
		
			for(int n=0 ; n<vector.getNumFilas() ; n++)
				{
				Transiciones_sensibilizadas += Transiciones[n] +":" + vector.getDato(n, 0) +" ";
				}
		return Transiciones_sensibilizadas;
}
	/*
	 *  Retorna el marcado actual
	 */
	public String Marcado(Matriz vector) {
		//System.out.println("\n");
		String Marcado_actual = "";
		for(int n=0 ; n<vector.getNumFilas() ; n++)
				{
				Marcado_actual += Plazas[n] +":" + vector.getDato(n, 0) +" ";
				}
		return Marcado_actual;
    }
	
	public Matriz getVectorMA() { return VectorMarcadoActual; }
	
	public int numero_t() { return numeroTransiciones; }
	
	public List<Matriz> getInvariantes() {
		return invariantes;
	}
    public int get_numero_Transiciones()
    {
    	return numeroTransiciones;
    }
    public int buscar_en_posicion(int transicion) {
    	int ERROR=-1;
    	//System.out.println("Transicion -> "+ transicion);
//    	if(transicion==0) {
//    		return 0;
//    	}
    	transicion = transicion+1;
    	for(int i = 0; i<Transiciones.length;i++) {
    		String[] numero = Transiciones[i].split("T");
    		//System.out.println("cero: "+Integer.parseInt(numero[1]));
    		if(Integer.parseInt(numero[1])== transicion) {
    			return i;
    		}
    		//if(Transiciones[i].split("T"))
    		//System.out.println("->"+numero[1]+"<-");
    		}
    	return ERROR;
    }
}
