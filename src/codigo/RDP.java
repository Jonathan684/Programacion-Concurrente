package codigo;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import log.Log;

public class RDP {

	private String[] Transiciones;
	private String[] Plazas;
	private Matriz VectorMarcadoActual, VectorExtendido, VectorSensibilizado, VectorInhibicion;
	private Matriz Incidencia, Identidad, Inhibicion;
	private Matriz Intervalo;
	// private Matriz VectorZ;
	private final int numeroPlazas;
	private final int numeroTransiciones;
	// private final List<Matriz> invariantes;
	private Scanner input;
	private long SensibilizadaConTiempo[];
	private Matriz IEntrada;
	private SensibilizadaConTiempo Temporizadas;
	private Mutex mutex;
	private Log consola;

////////////////////////////////////////////////////////////////////////////////////////////////////

	public RDP(Mutex mutex, Log consola) {

		this.consola = consola;
		this.mutex = mutex;
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
		// VectorZ = new Matriz(1,numeroTransiciones);

		// Carga de datos
		Incidencia.cargarMatriz("matrices/M.I.txt");
		Inhibicion.cargarMatriz("matrices/M.H.txt");
		Intervalo.cargarMatriz("matrices/IZ.txt");
		VectorMarcadoActual.cargarMatriz("matrices/VMI.txt");
		Identidad.cargarIdentidad();
		IEntrada.cargarMatriz("matrices/M.Pre.txt");

		SensibilizadaConTiempo = new long[numeroTransiciones];
		Temporizadas = new SensibilizadaConTiempo(numeroTransiciones, consola);
		// sensibilizarVectorZ();
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
	 * Metodo que sensibiliza las transiciones y carga el vector extendido
	 */
	public void sensibilizar() {

		sensibilizarVectorE();
		sensibilizarVectorB();
		// System.out.println("sensibilizar");
		// VectorExtendido.getTranspuesta().imprimirMatriz();
		VectorExtendido = VectorSensibilizado.getAnd(VectorInhibicion);// .getAnd(VectorZ.getTranspuesta());
		// System.out.println("fin sensibilizar");
	}

	public void setEsperando(int transicion) {

		// VectorZ.setDato(0, transicion, 0);
		Temporizadas.setEsperando(transicion);
	}

	public void resetEsperando(int transicion) {

		Temporizadas.resetEsperando(transicion);
		// VectorZ.setDato(0, transicion, 1);
//		Si el disparo de una transicion temporal desensibilizo a otra transicion temporal deberia resetea el timeStamp
	}

	/*
	 * Este metodo me devuleve si hay alguien esperando por ese token. Ya que
	 * realizo un falso disparo deshabilitando la transicion.
	 */
	public boolean soy_el_primero(int transicion) {
		boolean soy_el_primero = true;
		for (int transicion_prueba = 0; transicion_prueba < VectorExtendido.getNumFilas(); transicion_prueba++) {
			if ((estaSensibilizada(transicion_prueba) == true) && (esTemporal(transicion_prueba) == true)
					&& ((Temporizadas.getEsperando(transicion_prueba) == true) && (transicion != transicion_prueba))) {
				Matriz VectorExtendido_auxiliar = new Matriz(numeroTransiciones, 1);
				VectorExtendido_auxiliar = Falso_Disparo(transicion_prueba);
				if (VectorExtendido_auxiliar.getDato(transicion, 0) == 0) {
					soy_el_primero = false;
					break;
				}
			}
		}
		return soy_el_primero;

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

//    public void sensibilizarVectorZ() {
//    	for(int k = 0 ; k<numeroTransiciones; k++) {VectorZ.setDato(0, k, 1);}
//    	//VectorZ.imprimirMatriz();
//    }

	/*
	 * Metodo donde espera el hilo.
	 * 
	 */
	public void esperar(int transicion) {
		int alfa = Intervalo.getDato(0, transicion);
		long Tiempo_esperar = (Temporizadas.getTimeStamp()[transicion] + alfa) - System.currentTimeMillis();
		// System.out.println("OBSERVACION : "+Tiempo_esperar+ " HILO:
		// "+Thread.currentThread().getName());
		if (Tiempo_esperar > 0) {
			try {
				Thread.sleep(Tiempo_esperar);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

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

		boolean k = true;
		if (!estaSensibilizada(transicion))
			return false;

		if (esTemporal(transicion)) {
			consola.registrarDisparo("\n* Transicion temporal, control de ventana T[" + (transicion + 1) + "]\n"
					+ "* Tiempo: " + System.currentTimeMillis(), 1);
			boolean soy_el_primero = false;
			boolean ventana = false;
			ventana = Temporizadas.testVentana(transicion, Intervalo);
			soy_el_primero = soy_el_primero(transicion); // true si no hay alguien esperando

			consola.registrarDisparo("* Ventana: " + ventana, 1);
			consola.registrarDisparo("* Soy el primero: " + soy_el_primero + " T" + (transicion + 1), 1);

			if (ventana == true && soy_el_primero == true) { // Soy el primero
				k = true;
				consola.registrarDisparo("* Se va a disparar: " + "T" + (transicion + 1), 1);
			} else {// ventana == false
					// pregunto si estoy antes de la ventana
				boolean antes = false;
				antes = Temporizadas.antesDeLaVentana(transicion, Intervalo);

				if (antes == true && soy_el_primero == true) { // soy el primero y estoy antes de la ventana de tiempo
					consola.registrarDisparo("* Se va a esperar: T" + (transicion + 1), 1);
					setEsperando(transicion);
					mutex._release();
					esperar(transicion);
					mutex._acquire();
					consola.registrarDisparo("\n* Desperte tomé el acquire: T" + (transicion + 1), 1);// +"\n* Fin: "
					k = true; // puede estar desensibilizada. Despues de haber esperado por una transicion sensibilizada
					
				}
				/*
				 * Si estoy despues de la ventana y no hay nadie esperando por ella k == true
				 * 
				 */
				else if (antes == false && soy_el_primero == true) {// estoy despues de la ventana de tiempo y soy el
																	// primero
					consola.registrarDisparo("* Antes : " + antes, 1);
					// Estoy despues de la ventana pero soy el primero en llegar
					consola.registrarDisparo(
							"* Estoy despues de la ventana pero soy el primero en llegar: T" + (transicion + 1), 1);
					k = true;
				} else if (antes == false && soy_el_primero == false) {
					consola.registrarDisparo(
							"* Estoy despues de la ventana pero no soy el primero en llegar: T" + (transicion + 1), 1);
					k = false;
				} else if (antes == true && soy_el_primero == false) {
					consola.registrarDisparo(
							"* Estoy antes de la ventana pero no soy el primero en llegar: T" + (transicion + 1), 1);
					k = false;
				}
			}
		}
		////////////////////////////////////////////
		// System.out.println("Valor de k en disparar :"+k +" esta sensibilizada "+
		//////////////////////////////////////////// estaSensibilizada(transicion));
		if (k == true)// && estaSensibilizada(transicion)==true)
		{
			if (esTemporal(transicion))
				resetEsperando(transicion);
			if (!estaSensibilizada(transicion))
				return false; // Verificamos si sigue sensibilizada
			calculoDeVectorEstado(transicion);
			// System.out.println("Verificacion de los invariante de plaza");
			if (Test_Invariante() == false) {
				consola.registrarDisparo("* NO SE CUMPLE EL INVARIANTE DE PLAZA \n", 0);
				throw new RuntimeException("NO SE CUMPLE EL INVARIANTE DE PLAZA");
			}
			sensibilizar();
			actualiceSensibilizadoT();// Comprueba si disparo de esta transicion ha habilitado el contador de
										// trasiciones temporales sucesivas

			return true;
		}
		return false;

	}

	public void calculoDeVectorEstado(int transicion) {

		Matriz aux = Incidencia.getMultiplicacion(Identidad.getColumna(transicion));
		VectorMarcadoActual = VectorMarcadoActual.getSuma(aux);
	}

	/*
	 * Comprueba si se sensibilizaron transiciones temporales colocar el valor
	 * TimeStamp
	 */
	public void actualiceSensibilizadoT() {
		for (int t = 0; t < numeroTransiciones; t++) {
			if (esTemporal(t) && (VectorExtendido.getDato(t, 0) == 1)) {
				Temporizadas.setNuevoTimeStamp(t);
			}
			if ((esTemporal(t) == true) && VectorExtendido.getDato(t, 0) == 0
					&& (Temporizadas.getEsperando(t) == true)) {
				Temporizadas.resetEsperando(t);
				consola.registrarDisparo("* Borrado del TimeStamp de: T" + t, 1);
			}
		}
	}

	public boolean esTemporal(int transicion) {
		if (Intervalo().getDato(0, transicion) - Intervalo().getDato(1, transicion) != 0) {
			return true;
		}
		return false;
	}

	public Matriz Intervalo() {
		return Intervalo;
	}

	public long[] SensibilizadaConTiempo() {
		return SensibilizadaConTiempo;
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
	public boolean Test_Invariante() {
		// VectorMarcadoActual.getTranspuesta().imprimirMatriz();
		CharSequence cort;
		int Inv = 0;
		int Suma = 0;// suma los valores que hay en las plaza
		try {
			input = new Scanner(new File("matrices/P_Invariantes.txt"));
			// System.out.println("Verificacion de p-invariantes");
			while (input.hasNextLine()) {
				String line = input.nextLine();
				// System.out.println("linea "+line);
				for (int k = 0; k < line.length(); k++) {
					char r = line.charAt(k);
					if (r == '(') {
						for (int z = k; z < line.length(); z++) {
							char h = line.charAt(z);
							if (h == ')') {
								cort = line.subSequence(k + 2, z);
								Inv = Integer.parseInt(cort.toString());
								// System.out.println("Lugares donde extraer los valores "+ (Inv-1));
								// VectorMarcadoActual.getTranspuesta().imprimirMatriz();
								Suma = Suma + VectorMarcadoActual.getDato(Inv - 1, 0);
								// System.out.println("Suma :"+Suma);
								k = z;
								break;
							}
						}
					} else if (r == '=') {

						cort = line.subSequence(k + 2, line.length());
						Inv = Integer.parseInt(cort.toString());
						// System.out.println("Resultado :"+Suma);
						if (Inv != Suma) {
							return false;
						}
						Suma = 0;
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return true;
	}

	// Metodos get

	public Matriz getVectorExtendido() {
		return VectorExtendido;
	}

	public Matriz getVectorSensibilizado() {
		return VectorSensibilizado;
	}

	public Matriz getMatrizInhibicion() {
		return VectorInhibicion;
	}

	public Matriz getVectorMA() {
		return VectorMarcadoActual;
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
	public String sensibilidadas(Matriz vector) {
		String Transiciones_sensibilizadas = "";

		for (int n = 0; n < vector.getNumFilas(); n++) {
			Transiciones_sensibilizadas += Transiciones[n] + ":" + vector.getDato(n, 0) + " ";
		}
		return Transiciones_sensibilizadas;
	}

	/*
	 * Retorna el marcado actual
	 */
	public String Marcado(Matriz vector) {
		String Marcado_actual = "";
		for (int n = 0; n < vector.getNumFilas(); n++) {
			Marcado_actual += Plazas[n] + ":" + vector.getDato(n, 0) + " ";
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

	/**
	 * Metodo que calcula el vector Inhibicion
	 */
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

	private void sensibilizarVectorB() {
		Matriz Q = new Matriz(numeroPlazas, 1);
		for (int i = 0; i < Q.getNumFilas(); i++) {

			if (VectorMarcadoActual.getDato(i, 0) != 0)
				Q.setDato(i, 0, 1);
			else
				Q.setDato(i, 0, 0);

		}
		// System.out.println("sensibilizarVectorB ");
		// Inhibicion.imprimirMatriz();
		// Q.imprimirMatriz();
		VectorInhibicion = Inhibicion.getTranspuesta().getMultiplicacion(Q);
		// VectorInhibicion.getTranspuesta().imprimirMatriz();

		for (int i = 0; i < VectorInhibicion.getNumFilas(); i++) {
			if (VectorInhibicion.getDato(i, 0) > 1)
				VectorInhibicion.setDato(i, 0, 1);
		}
		VectorInhibicion = VectorInhibicion.getComplemento();
	}
}
