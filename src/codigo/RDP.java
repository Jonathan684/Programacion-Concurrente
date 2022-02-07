package codigo;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;
import log.Log;

public class RDP {

	private String[] Transiciones;
	private String[] Plazas;
	private Matriz VectorMarcadoActual, VectorExtendido, VectorSensibilizado, VectorInhibicion;
	private Matriz Incidencia, Identidad, Inhibicion;
	private Matriz Intervalo;
	private Matriz VectorZ;
	private final int numeroPlazas;
	private final int numeroTransiciones;
	// private final List<Matriz> invariantes;
	private Scanner input;
	private long SensibilizadaConTiempo[];
	private Matriz IEntrada;
	private SensibilizadaConTiempo Temporizadas;
	private Mutex mutex;
	private Log consola;
	private long timeStamp[];
	private Integer[] todavia_espera;
	private int TimeStamp_ahora;
	private static HashMap<String, String> p_invariantes;
	private int timeout ;
////////////////////////////////////////////////////////////////////////////////////////////////////

	public RDP(Mutex mutex, Log consola) {
		p_invariantes = new HashMap<String, String>();
		TimeStamp_ahora = 0;
		this.consola = consola;
		this.mutex = mutex;
		int timeout = 0;
		numeroTransiciones = cargarTransiciones("matrices/M.I.txt"); // Extraccion de la cantidad de transiciones.
		numeroPlazas = cargarPlazas("matrices/M.I.txt"); // Extraccion de la cantidad de plazas.

		// Matrices
		// invariantes = cargarInvariantes("matrices/InvTrans.txt");
		Incidencia = new Matriz(numeroPlazas, numeroTransiciones);
		Inhibicion = new Matriz(numeroPlazas, numeroTransiciones);
		IEntrada = new Matriz(numeroPlazas, numeroTransiciones);

		Identidad = new Matriz(numeroTransiciones, numeroTransiciones);
		Intervalo = new Matriz(2, numeroTransiciones);
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
		Cargar_P_Invariante();

		todavia_espera = new Integer[numeroTransiciones];
		timeStamp = new long[numeroTransiciones];

		for (int i = 0; i < timeStamp.length; i++) {
			timeStamp[i] = 0;
			todavia_espera[i] = 0;
		}
		SensibilizadaConTiempo = new long[numeroTransiciones];
		Temporizadas = new SensibilizadaConTiempo(numeroTransiciones, mutex, consola, Intervalo);
		//sensibilizarVectorZ();

		/**
		 * Metodo que sensibiliza las transiciones y carga el vector extendido
		 */
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

		if (!estaSensibilizada(transicion)) { // no sensibilizada
			// Estoy en el vectorZ(transicion) == 0
			return false;
		}

		else {
			if (Temporizadas.esTemporal(transicion)) {// Es temporal y esta sensibilizada.
				// consola.registrarDisparo("* Salidas posible", 1);
				// Dos cosas pueden pasar:
				//
				if (!test_ventana(transicion)) {
					if (getEsperando(transicion) == false) {
						//flag = true;
						consola.registrarDisparo(
								"*         Salida 2 antes de alfa  TimeStamp :" + timeStamp[transicion], 1);
						return false;
					}
				}
			}
			if (estaSensibilizada(transicion))// Verificamos si sigue sensibilizada por si es temporal
			{
				// consola.registrarDisparo("* Disparo asegurado", 1);
				if (Temporizadas.esTemporal(transicion))
					// Temporizadas.resetEsperando(transicion);
					resetEsperando(transicion);

				calculoDeVectorEstado(transicion);
				// Verificacion de los invariante de plaza
				if (!Test_Invariante()) {
					consola.registrarDisparo("* NO SE CUMPLE EL INVARIANTE DE PLAZA \n", 0);
					throw new RuntimeException("NO SE CUMPLE EL INVARIANTE DE PLAZA");
				}

				// consola.registrarDisparo("* Actualizar", 1);
				sensibilizar();
				actualiceSensibilizadoT();
			}
			return true;
		}

	}
//	public boolean get_flag() {
//		return flag;
//	}
//	public void set_flag(boolean flag) {
//		this.flag=flag;
//	}
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
	
	/**
	 * 
	 */
	public void sensibilizar() {

		sensibilizarVectorE();
		sensibilizarVectorB();
		// sensibilizarVectorZ();
		VectorExtendido = VectorSensibilizado.getAnd(VectorInhibicion);
//		VectorExtendido = VectorExtendido.getAnd(VectorZ.getTranspuesta());
	}


	public boolean test_ventana(int transicion) {
		Marca_actual(transicion);
		if ((TimeStamp_ahora >= (Intervalo.getDato(0, transicion)))) {
			return true;
		} else
			return false;
	}

	/*
	 * Comprueba si se sensibilizaron transiciones temporales colocar el valor
	 * TimeStamp
	 */
	public void actualiceSensibilizadoT() {

		for (int t = 0; t < numeroTransiciones; t++) {
			if (Temporizadas.esTemporal(t) && (VectorExtendido.getDato(t, 0) == 1)) {
				setNuevoTimeStamp(t);
				// VectorZ.setDato(0, t, 0);
			}
			if ((Temporizadas.esTemporal(t) == true) && (VectorExtendido.getDato(t, 0) == 0)) {
				// && (!Temporizadas.getEsperando(t))) {
				resetEsperando(t);
				// VectorZ.setDato(0, t, 1);
			}
		}
		// VectorZ.imprimirMatriz();
		// sensibilizar();
	}
	public void Marca_actual(int transicion) {
		TimeStamp_ahora = (int) (System.currentTimeMillis() - (timeStamp[transicion]));
	}

	/**
	 * alfa = Intervalo.getDato(0, transicion) beta = Intervalo.getDato(1,
	 * transicion)
	 * 
	 * @param transicion
	 * @return
	 */
	public boolean Analisis_Temporal(int transicion) {

		// consola.registrarDisparo("* Tiempo 1 :" + System.currentTimeMillis()+"
		// TimeStamp :"+timeStamp[transicion] , 1);
		Marca_actual(transicion);

		if (test_ventana(transicion)) {
			return true; // <<-- Esta en la ventana de tiempo
		}
		// No estoy en la venta de tiempo.
		if (TimeStamp_ahora < (Intervalo.getDato(0, transicion))) {// <-- Esta antes del alfa
			// consola.registrarDisparo("* Tiempo 2 :" + System.currentTimeMillis(), 1);

			int Tiempo_esperar = (int) ((((timeStamp[transicion]) + (Intervalo.getDato(0, transicion)))
					- System.currentTimeMillis()) + 2); // +2

			if (Tiempo_esperar < 0) { // Tiempo negativo signidica que estoy dentro de la ventana.
				// consola.registrarDisparo("* Tiempo_esperar negativo esta dentro de la ventana
				// ahora :" + Tiempo_esperar, 1);
				return true;
			} 
			timeout = Tiempo_esperar;
			return false; // esperar alfa
			
		} 
		return true;
//		else { // <<-- Esta despues de beta
//					// consola.registrarDisparo("* Saliendo pasando beta :" +
//					// Thread.currentThread().getName(), 1);
//			return;
//		}
	}
	public void esperar(int transicion) {
		// consola.registrarDisparo("* Estoy antes del alfa. Esperar :" +
		// Tiempo_esperar, 1);
		setEsperando(transicion);
		//System.out.println("Tiempo_esperar :"+ Tiempo_esperar);
		// todavia_espera[transicion] = 1;
		 mutex._release();
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

	public void resetEsperando(int transicion) {
		timeStamp[transicion] = 0;
		Temporizadas.resetEsperando(transicion);
	}

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

	/**
	 * Devuelve true si la transicion esta habilitada de acuerdo al vector extendido
	 * 
	 * @param transicion : transicion la que se desea saber si está habilitada o no.
	 * @return verdadero estan habilitadas
	 */
	public boolean estaSensibilizada(int transicion) {
		if (VectorExtendido.getDato(transicion, 0) == 1)
			return true;
		else
			return false;
	}

	/**
	 * Metedo encargado de verificar si se cumple el los p-Inavariantes.
	 * 
	 * @param vector vector a imprimir.
	 */
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

//	public Matriz getVectorZ() { return VectorZ; }

	public int get_numero_Transiciones() {
		return numeroTransiciones;
	}

	// Metodos de formato para el LOG.txt
	/**
	 * 
	 * @param vector
	 * @return Transiciones_sensibilizadas tipo String
	 */
	public String sensibilidadas() {
		String Transiciones_sensibilizadas = "";

		for (int n = 0; n < VectorExtendido.getNumFilas(); n++) {
			Transiciones_sensibilizadas += Transiciones[n] + ":" + VectorExtendido.getDato(n, 0) + " ";
		}
		return Transiciones_sensibilizadas;
	}

	/*
	 * Retorna el marcado actual
	 */
	public String Marcado() {
		String Marcado_actual = "";
		for (int n = 0; n < VectorMarcadoActual.getNumFilas(); n++) {
			Marcado_actual += Plazas[n] + ":" + VectorMarcadoActual.getDato(n, 0) + " ";
		}
		return Marcado_actual;
	}

	/**
	 * Este metodo muestra el vector indicado por parametro
	 * 
	 * @param vector vector a imprimir. Tipo 0: Transiciones 1: Plazas
	 */
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
	/**
	 * Metodos que calcula el vector Inhibicion
	 */
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
	 * Este metodo devuelve la cantidad de transiciones disponibles en la red
	 * 
	 * @param pathMI la ruta al archivo de texto que contiene la matriz de
	 *               incidencia
	 * @return transiciones de la red
	 */
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

	/**
	 * Este metodo devuelve la cantidad de plazas disponible en la red
	 * 
	 * @param pathMI la ruta al archivo de texto que contiene la matriz de
	 *               incidencia
	 * @return Plazas de la red
	 */
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

	/**
	 * Completa el string de Transiciones con la cantidad correspondiente
	 * 
	 * @param nroT: la cantidad de transiciones en la matriz de incidencia.
	 */
	private void setStringTranciones(int nroT) {
		Transiciones = new String[nroT];
		for (int t = 1; t < nroT + 1; t++) {
			Transiciones[t - 1] = "T" + t;
		}
	}

	/**
	 * Completa el string de Plazas con la cantidad correspondiente
	 * 
	 * @param nroP: la cantidad de plazas en la matriz de incidencia.
	 */
	private void setStringPlazas(int nroP) {
		Plazas = new String[nroP];
		for (int p = 1; p < nroP + 1; p++) {
			Plazas[p - 1] = "P" + p;
		}
	}



	public void setEsperando(int transicion) {

		 synchronized(this) {
		      // codigo del metodo aca
			 Temporizadas.setEsperando(transicion);
		   }
		// VectorZ.setDato(0, transicion, 0);
		
	}

	public boolean getEsperando(int transicion) {

		// VectorZ.setDato(0, transicion, 0);
		if (Temporizadas.getEsperando(transicion)) {
			return true;
		}
		return false;

	}
	public int[] get_vector_Esperando() {
		return Temporizadas.getvectorEsperando();
	}


	/*
	 * Este metodo me devuleve si hay alguien esperando por ese token. Ya que
	 * realizo un falso disparo deshabilitando la transicion.
	 */
//	public boolean soy_el_primero(int transicion) {
//		boolean soy_el_primero = true;
//		for (int transicion_prueba = 0; transicion_prueba < VectorExtendido.getNumFilas(); transicion_prueba++) {
//			if ((estaSensibilizada(transicion_prueba) == true) && (Temporizadas.esTemporal(transicion_prueba) == true)
//					&& ((Temporizadas.getEsperando(transicion_prueba) == true) && (transicion != transicion_prueba))) {
//				Matriz VectorExtendido_auxiliar = new Matriz(numeroTransiciones, 1);
//				VectorExtendido_auxiliar = Falso_Disparo(transicion_prueba);
//				if (VectorExtendido_auxiliar.getDato(transicion, 0) == 0) {
//					soy_el_primero = false;
//					break;
//				}
//			}
//		}
//		return soy_el_primero;
//
//	}

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
	///////////////////////////////////////////////////////////////////////////////

//	public boolean T_en_VectorZ(int Transicion) {
//		if (VectorZ.getDato(0, Transicion) == 0) {
//			return true;
//		}
//		return false;
//	}

//	public boolean getVectorZ(int transicion) {
//
//		if (VectorZ.getDato(0, transicion) == 1) {
//			return true;
//		}
//
//		return false;
//	}

//	public void sensibilizarVectorZ() {
//		// consola.registrarDisparo("* Sensibilizar el vector Z" , 1);
//		for (int k = 0; k < numeroTransiciones; k++) {
//
//			if (timeStamp[k] != 0) {
//				VectorZ.setDato(0, k, 0);
//			} else {
//				VectorZ.setDato(0, k, 1);
//			}
//		}
//		System.out.println("Sensibilizado del vector Z");
//		VectorZ.imprimirMatriz();
//		System.out.println("====================");
	//}

	/*
	 * Metodo donde espera el hilo.
	 * 
	 */

//		else {
//			throw new RuntimeException("TIEMPO PARA DORMIR NEGATIVO");
//		}
//	public void actualiceSensibilizadoT_politica(int transicion) {
//	resetEsperando(transicion);
//}
}
