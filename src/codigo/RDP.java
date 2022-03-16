package codigo;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;
import log.Log;

public class RDP {

	private String[] Transiciones;
	private String[] Plazas;
	private Matriz VectorMarcadoActual, VectorExtendido, VectorSensibilizado, VectorInhibicion;
	private Matriz Incidencia, Identidad, Inhibicion;
	private Matriz Intervalo;
	private Matriz VectorZ,M_Inicial;
	private final int numeroPlazas;
	private final int numeroTransiciones;
	// private final List<Matriz> invariantes;
	private Scanner input;
	private Matriz IEntrada;
	private SensibilizadaConTiempo Temporizadas;
	//private Mutex mutex;
	private Log consola;
	private long timeStamp[];
	private int TimeStamp_ahora;
	private static HashMap<String, String> p_invariantes;
	private long timeout[] ;
////////////////////////////////////////////////////////////////////////////////////////////////////

	public RDP(Log consola) {
		p_invariantes = new HashMap<String, String>();
		TimeStamp_ahora = 0;
		this.consola = consola;
		//this.mutex = mutex;
		//int timeout = 0;
		
		numeroTransiciones = cargarTransiciones("matrices/M.I.txt"); // Extraccion de la cantidad de transiciones.
		numeroPlazas = cargarPlazas("matrices/M.I.txt"); // Extraccion de la cantidad de plazas.
		
		// Matrices
		// invariantes = cargarInvariantes("matrices/InvTrans.txt");
		Incidencia = new Matriz(numeroPlazas, numeroTransiciones);
		Inhibicion = new Matriz(numeroPlazas, numeroTransiciones);
		IEntrada = new Matriz(numeroPlazas, numeroTransiciones);

		Identidad = new Matriz(numeroTransiciones, numeroTransiciones);
		Intervalo = new Matriz(2, numeroTransiciones);
		M_Inicial = new Matriz(1, numeroPlazas);
		// ISalida = new Matriz(numeroPlazas,numeroTransiciones);
		// Vectores
		VectorMarcadoActual = new Matriz(numeroPlazas, 1);
		VectorSensibilizado = new Matriz(numeroTransiciones, 1);
		VectorInhibicion = new Matriz(numeroTransiciones, 1);
		VectorExtendido = new Matriz(numeroTransiciones, 1);
		VectorZ = new Matriz(1, numeroTransiciones);
		// Carga de datos
		Incidencia.cargarMatriz("matrices/M.I.txt");
		Inhibicion.cargarMatriz("matrices/M.H.txt");
		Intervalo.cargarMatriz("matrices/IZ.txt");
		VectorMarcadoActual.cargarMatriz("matrices/VMI.txt");
		Identidad.cargarIdentidad();
		IEntrada.cargarMatriz("matrices/M.Pre.txt");
		//M_Inicial.cargarMatriz("matrices/M_Inicial.txt");
		
		
		Cargar_P_Invariante();
		Temporizadas = new SensibilizadaConTiempo(numeroTransiciones, consola, Intervalo);
		timeStamp = new long[numeroTransiciones];
		timeout = new long[numeroTransiciones];
		
		Arrays.fill(timeStamp, 0);
		Arrays.fill(timeout, 0);
	}
	public SensibilizadaConTiempo getTemporales() {
		return Temporizadas;
	}
	/**
	* Este metodo dispara una transicion de la rdp indicada por parametro, teniendo
	 * en cuenta el modo indicado por parametro
	 * 
	 * @param transicion : numero de transicion.
	 * @return : -0 retorna 0 si el disparo no es exitoso. -1 retorna 1 si el
	 *         disparo es exitoso.
	 */
	public boolean Disparar(int transicion) {
		//System.out.println("Sensillibizar apenas entra :");
		//consola.registrarDisparo("* =====Antes======= ", 1);
		sensibilizar(); // Se actualiza el Vz 
		if (!estaSensibilizada(transicion)) { // no sensibilizada
			return false;
		}
		else {
			 
			 Matriz	transAntesdelDisparo = new Matriz(numeroTransiciones, 1); 
			 Matriz	transDespuesdelDisparo = new Matriz(numeroTransiciones, 1); 
			 
			 transAntesdelDisparo =	VectorExtendidoSinVZ();
//			 System.out.println("\n VectorExtendido antes del disparo");
//			 transAntesdelDisparo.getTranspuesta().imprimirMatriz();
//      	 System.out.println("......................");	
			// consola.registrarDisparo("* =====despues======= ", 1);
			 calculoDeVectorEstado(transicion);
			 sensibilizar(); // Se vuelve a sensibiizar para sacar el nuevo vectorExtendido
			 
			 transDespuesdelDisparo = VectorExtendidoSinVZ();
//			 System.out.println("\n VectorExtendido despues del disparo");
//			 transDespuesdelDisparo.getTranspuesta().imprimirMatriz();
			 Temporizadas.ActualizarTimeStamp(transAntesdelDisparo,transDespuesdelDisparo,transicion);
			// System.out.println("fin de disparar en rdp");
			 
			 
			 
			 if (!Test_Invariante()) {
					consola.registrarDisparo("* NO SE CUMPLE EL INVARIANTE DE PLAZA \n", 0);
					throw new RuntimeException("NO SE CUMPLE EL INVARIANTE DE PLAZA");
				}

			return true;
		}

	}

	private Matriz VectorExtendidoSinVZ() {
		// TODO Auto-generated method stub
			Matriz VectorExtendidoAux = new Matriz(numeroTransiciones, 1);
			sensibilizarVectorE();
			sensibilizarVectorB();
			VectorExtendidoAux = VectorSensibilizado.getAnd(VectorInhibicion);
			return VectorExtendidoAux;
	}
	public boolean Test_Invariante() {
		int Suma_Tokens_Plaza = 0;
		String valor = "";
		String[] corte = null;

		for (String plazas : p_invariantes.keySet()) {
			valor = p_invariantes.get(plazas);
			corte = plazas.split(" ");
			for (String elemt : corte) {
				int token = Integer.parseInt(elemt);
				Suma_Tokens_Plaza += VectorMarcadoActual.getDato((token - 1), 0);
			}
			if (Suma_Tokens_Plaza != Integer.parseInt(valor))
				return false;
			Suma_Tokens_Plaza = 0;
		}
		return true;
	}
	/*
	 *
	 */
	public void sensibilizar() {

		sensibilizarVectorE();
		sensibilizarVectorB();
		//sensibilizarVectorZ();
		//////////////////////////////////////////////////////////////
		
//		System.out.println("..Marcado Inicial..");
//		M_Inicial.imprimirMatriz();
//		System.out.println("...................");
		
		//////////////////////////////////////////////////////////////
		VectorExtendido = VectorSensibilizado.getAnd(VectorInhibicion);
		VectorZ = Temporizadas.getVectorZ(VectorExtendidoSinVZ());
		VectorExtendido = VectorExtendido.getAnd(VectorZ.getTranspuesta());
	//	consola.registrarDisparo("* con Vz---->> " + sensibilidadas(), 1);
	//	consola.registrarDisparo("* sin Vz---->> " + sensibilidadas2(), 1);
//		System.out.println("VectorZ :");
//		VectorZ.imprimirMatriz();
	}
	public void sensibilizarVectorZ() {
		for (int k = 0; k < numeroTransiciones; k++) {

			if (timeStamp[k] != 0) {
				VectorZ.setDato(0, k, 0);
			} else {
				VectorZ.setDato(0, k, 1);
			}
		}
    }
//	public boolean test_ventana(int transicion) {
//		//Marca_actual(transicion);
//		consola.registrarDisparo("* Test ventana :"+System.currentTimeMillis()+" T"+(transicion+1) +" timeStamp :"+ timeStamp[transicion],1);
//		long TimeStamp_ahora = (System.currentTimeMillis() - (timeStamp[transicion]));
//		if ((TimeStamp_ahora >= (Intervalo.getDato(0, transicion)))&&(TimeStamp_ahora <= (Intervalo.getDato(1, transicion)))) {
//			return true;
//		} else {
//			if(TimeStamp_ahora < Intervalo.getDato(0, transicion)) {
//				timeout[transicion] =  ((((timeStamp[transicion]) + (Intervalo.getDato(0, transicion)))
//						- System.currentTimeMillis()) + 2);
//				consola.registrarDisparo("* Tiempo a dormir "+timeout[transicion],1);
//				return false;
//			}
//			else {
//				throw new RuntimeException("Beta demasiado chico, elegir un beta mas grande : T"+ (transicion+1)+" tiempo:"+System.currentTimeMillis()+ " timeStamp :"+timeStamp[transicion]);
//			}
//			
//		}
//			
//	}
	public long gettimeout(int t) {
		return timeout[t];
	}
	
//	public void actualiceSensibilizadoT() {
//
//		for (int t = 0; t < numeroTransiciones; t++) {
//			if (Temporizadas.esTemporal(t) && (VectorExtendido.getDato(t, 0) == 1))setNuevoTimeStamp(t);
//			if ((Temporizadas.esTemporal(t) == true) && (VectorExtendido.getDato(t, 0) == 0))
//				resetEsperando(t);
//		}
//	}
	
//	public void Marca_actual(int transicion) {
//		TimeStamp_ahora = (int) (System.currentTimeMillis() - (timeStamp[transicion]));
//	}

//	public boolean Analisis_Temporal(int transicion) {
//
//		// consola.registrarDisparo("* Tiempo 1 :" + System.currentTimeMillis()+"
//		// TimeStamp :"+timeStamp[transicion] , 1);
//		Marca_actual(transicion);
//
//		if (test_ventana(transicion)) {
//			return true; // <<-- Esta en la ventana de tiempo
//		}
//		consola.registrarDisparo("* Tiempo en analisis temporal :"+System.currentTimeMillis()+" T"+(transicion+1),1);
//		// No estoy en la venta de tiempo.
//		if (TimeStamp_ahora < (Intervalo.getDato(0, transicion))) {// <-- Esta antes del alfa
//			// consola.registrarDisparo("* Tiempo 2 :" + System.currentTimeMillis(), 1);
//
//			int Tiempo_esperar = (int) ((((timeStamp[transicion]) + (Intervalo.getDato(0, transicion)))
//					- System.currentTimeMillis()) + 2); // +2
//
//			if (Tiempo_esperar < 0) { // Tiempo negativo signidica que estoy dentro de la ventana.
//				// consola.registrarDisparo("* Tiempo_esperar negativo esta dentro de la ventana
//				// ahora :" + Tiempo_esperar, 1);
//				return true;
//			} 
//			//timeout = Tiempo_esperar;
//			return false; // esperar alfa
//			
//		} 
//		
//		throw new RuntimeException("Beta demasiado chico, elegir un beta mas grande : T"+ (transicion+1)+" tiempo:"+System.currentTimeMillis());
//		
//		//return true;
//		
////		else { // <<-- Esta despues de beta
////					// consola.registrarDisparo("* Saliendo pasando beta :" +
////					// Thread.currentThread().getName(), 1);
////			return;
////		}
//	}
//	
	public void esperar(int transicion) {
		//Temporizadas.setEsperando(transicion);
		//consola.registrarDisparo("* Tiempo a esperar :"+ timeout, 1);
		//mutex._release();
		int timeout = (int) ((((timeStamp[transicion]) + (Intervalo.getDato(0, transicion)))
				- System.currentTimeMillis()) + 2);
		try {
			Thread.sleep(timeout);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void calculoDeVectorEstado(int transicion) {

		Matriz aux = Incidencia.getMultiplicacion(Identidad.getColumna(transicion));
		VectorMarcadoActual = VectorMarcadoActual.getSuma(aux);
	}

//	public void resetEsperando(int transicion) {
//		consola.registrarDisparo("transicion que resetea mal :"+transicion,1);
//		timeStamp[transicion] = 0;
//		timeout[transicion]=0;
//		Temporizadas.resetEsperando(transicion);
//	}

	public void setNuevoTimeStamp(int transicion) {

		if (timeStamp[transicion] == 0) {
			timeStamp[transicion] = System.currentTimeMillis();
			consola.registrarDisparo("* Nuevo Timestamp a T" + (transicion + 1) + ": " + timeStamp[transicion], 1);
		}
	}

	public boolean esInmediata(int transicion) {
		if (Temporizadas.esTemporal(transicion)) {
			return false;
		} else
			return true;
	}

	public boolean estaSensibilizada(int transicion) {
		if (VectorExtendido.getDato(transicion, 0) == 1)
			return true;
		else
			return false;
	}

	public void Cargar_P_Invariante() {

		CharSequence cort;
		try {
			input = new Scanner(new File("matrices/P_Invariantes.txt"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		while (input.hasNextLine()) {
			String line = input.nextLine();
			String cort2 = "";
			String valor = "";
			// System.out.println("linea "+line);
			for (int k = 0; k < line.length(); k++) {
				char r = line.charAt(k);
				if (r == '(') {
					for (int z = k; z < line.length(); z++) {
						char h = line.charAt(z);
						if (h == ')') {
							cort = line.subSequence(k + 2, z);
							cort2 += cort + " ";
							k = z;
							break;
						}
					}
				} else if (r == '=') {

					cort = line.subSequence(k + 2, k + 3);
					// System.out.println("CORT :"+cort);
					valor = (String) cort;
					p_invariantes.put(cort2, valor);
					cort2 = "";
					valor = "";
				}
			}
		}

	}

	// Metodos get

	public Matriz getVectorExtendido() {
		return VectorExtendido;
	}

	public int get_numero_Transiciones() {
		return numeroTransiciones;
	}

	public String sensibilidadas() {
		String Transiciones_sensibilizadas = "";
		
		for (int n = 0; n < VectorExtendido.getNumFilas(); n++) {
			Transiciones_sensibilizadas += Transiciones[n] + ":" + VectorExtendido.getDato(n, 0) + " ";
		}
		return Transiciones_sensibilizadas;
	}
	public String sensibilidadas2() {
		String Transiciones_sensibilizadas = "";
		Matriz VectorExtendidoAux = new Matriz(numeroTransiciones, 1);
		VectorExtendidoAux = VectorExtendidoSinVZ();
		for (int n = 0; n < VectorExtendidoAux.getNumFilas(); n++) {
			Transiciones_sensibilizadas += Transiciones[n] + ":" + VectorExtendidoAux.getDato(n, 0) + " ";
		}
		return Transiciones_sensibilizadas;
	}
	public String Marcado() {
		String Marcado_actual = "";
		for (int n = 0; n < VectorMarcadoActual.getNumFilas(); n++) {
			Marcado_actual += Plazas[n] + ":" + VectorMarcadoActual.getDato(n, 0) + " ";
		}
		return Marcado_actual;
	}

	public void mostrar(Matriz vector, int Tipo) {
		if (Tipo == 0) {
			for (int n = 0; n < vector.getNumFilas(); n++)
				System.out.print(Transiciones[n] + ":" + vector.getDato(n, 0) + " ");
		} else if (Tipo > 0) {
			for (int n = 0; n < vector.getNumFilas(); n++)
				System.out.print(Plazas[n] + ":" + vector.getDato(n, 0) + " ");
		}
		System.out.println("\n");
	}

	private void sensibilizarVectorE() {

		for (int i = 0; i < IEntrada.getNumColumnas(); i++) {
			int e = 1;
			for (int j = 0; j < Incidencia.getNumFilas(); j++) {
				if (VectorMarcadoActual.getDato(j, 0) < IEntrada.getDato(j, i)) {
					e = 0;
				}
				VectorSensibilizado.setDato(i, 0, e);
			}
		}
	}

	private void sensibilizarVectorB() {
		Matriz Q = new Matriz(numeroPlazas, 1);
		for (int i = 0; i < Q.getNumFilas(); i++) {

			if (VectorMarcadoActual.getDato(i, 0) != 0)
				Q.setDato(i, 0, 1);
			else
				Q.setDato(i, 0, 0);

		}
		VectorInhibicion = Inhibicion.getTranspuesta().getMultiplicacion(Q);
		for (int i = 0; i < VectorInhibicion.getNumFilas(); i++) {
			if (VectorInhibicion.getDato(i, 0) > 1)
				VectorInhibicion.setDato(i, 0, 1);
		}
		VectorInhibicion = VectorInhibicion.getComplemento();
	}

	private int cargarTransiciones(String pathMI) {
		int nrotrans = 0;
		try {
			input = new Scanner(new File(pathMI));
			String line = input.nextLine();
			for (int columna = 0; columna < line.length(); columna++) {
				char c = line.charAt(columna);
				if (c == '1' || c == '0')
					nrotrans++;
			}
			setStringTranciones(nrotrans);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return nrotrans;
	}

	private int cargarPlazas(String pathMI) {
		int nroPlazas = 0;
		try {
			input = new Scanner(new File(pathMI));
			while (input.hasNextLine()) {
				input.nextLine();
				nroPlazas++;
			}
			setStringPlazas(nroPlazas);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return nroPlazas;
	}

	private void setStringTranciones(int nroT) {
		Transiciones = new String[nroT];
		for (int t = 1; t < nroT + 1; t++) {
			Transiciones[t - 1] = "T" + t;
		}
	}

	private void setStringPlazas(int nroP) {
		Plazas = new String[nroP];
		for (int p = 1; p < nroP + 1; p++) {
			Plazas[p - 1] = "P" + p;
		}
	}

	public boolean noduerme(int transicion) {
		// TODO Auto-generated method stub
		if(timeStamp[transicion] != 0) {
			return false;
		}
		return true;
	}
	/*
	 * Este metodo hace un falso disparo con las transicion que estan en el vector
	 * Esperando Si un disparo proboca que la transicion que se envia por parametro
	 * cambie a estar desensibilizada significa que ya hay una transicion que espera
	 * por lo mismo que la transicion enviada por parametro. Return: false: Si
	 * alguien espera por ese token true: Si no alguien esperando
	 */
	public Matriz Falso_Disparo(int transicion) {
		// Falso disparo de las trasiciones dormidas
		Matriz VectorExtendido_auxiliar = new Matriz(numeroTransiciones, 1);
		Matriz VectorMarcadoActual2;
		consola.registrarDisparo("* 0 m: " + VectorMarcadoActual.getDato(0, 0), 1);
		Matriz aux = Incidencia.getMultiplicacion(Identidad.getColumna(transicion));
		VectorMarcadoActual2 = VectorMarcadoActual.getSuma(aux);
		VectorExtendido_auxiliar = sensibilizarVectorE2(VectorMarcadoActual2)
				.getAnd(sensibilizarVectorB2(VectorMarcadoActual2));// .getAnd(VectorZ.getTranspuesta());
		return VectorExtendido_auxiliar;
	}
	private Matriz sensibilizarVectorB2(Matriz VectorMarcadoActual2) {
		Matriz VectorInhibicion2;
		Matriz Q2 = new Matriz(numeroPlazas, 1);
		for (int i = 0; i < Q2.getNumFilas(); i++) {

			if (VectorMarcadoActual2.getDato(i, 0) != 0)
				Q2.setDato(i, 0, 1);
			else
				Q2.setDato(i, 0, 0);

		}
		VectorInhibicion2 = Inhibicion.getTranspuesta().getMultiplicacion(Q2);
		for (int i = 0; i < VectorInhibicion.getNumFilas(); i++) {
			if (VectorInhibicion2.getDato(i, 0) > 1)
				VectorInhibicion2.setDato(i, 0, 1);
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
				// VectorMarcadoActual2
				VectorSensibilizado2.setDato(i, 0, e);
			}
		}
		return VectorSensibilizado2;
	}

}
