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
	//private Matriz VectorZ;
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
		// ISalida = new Matriz(numeroPlazas,numeroTransiciones);
		// Vectores
		VectorMarcadoActual = new Matriz(numeroPlazas, 1);
		VectorSensibilizado = new Matriz(numeroTransiciones, 1);
		VectorInhibicion = new Matriz(numeroTransiciones, 1);
		VectorExtendido = new Matriz(numeroTransiciones, 1);
		//VectorZ = new Matriz(1, numeroTransiciones);
		// Carga de datos
		Incidencia.cargarMatriz("matrices/M.I.txt");
		Inhibicion.cargarMatriz("matrices/M.H.txt");
		Intervalo.cargarMatriz("matrices/IZ.txt");
		VectorMarcadoActual.cargarMatriz("matrices/VMI.txt");
		Identidad.cargarIdentidad();
		IEntrada.cargarMatriz("matrices/M.Pre.txt");
		
		Cargar_P_Invariante();
		Temporizadas = new SensibilizadaConTiempo(numeroTransiciones, consola, Intervalo);
		timeStamp = new long[numeroTransiciones];
		timeout = new long[numeroTransiciones];
		Arrays.fill(timeStamp, 0);
		Arrays.fill(timeout, 0);
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
					if (Temporizadas.getEsperando(transicion) == false) {
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
	
	public void sensibilizar() {

		sensibilizarVectorE();
		sensibilizarVectorB();
		// sensibilizarVectorZ();
		VectorExtendido = VectorSensibilizado.getAnd(VectorInhibicion);
//		VectorExtendido = VectorExtendido.getAnd(VectorZ.getTranspuesta());
	}

	public boolean test_ventana(int transicion) {
		//Marca_actual(transicion);
		consola.registrarDisparo("* Test ventana :"+System.currentTimeMillis()+" T:"+(transicion+1),1);
		int TimeStamp_ahora = (int) (System.currentTimeMillis() - (timeStamp[transicion]));
		if ((TimeStamp_ahora >= (Intervalo.getDato(0, transicion)))&&(TimeStamp_ahora <= (Intervalo.getDato(1, transicion)))) {
			return true;
		} else {
			if(TimeStamp_ahora < Intervalo.getDato(0, transicion)) {
				timeout[transicion] =  ((((timeStamp[transicion]) + (Intervalo.getDato(0, transicion)))
						- System.currentTimeMillis()) + 2);
				consola.registrarDisparo("* Tiempo a dormir "+timeout[transicion],1);
				return false;
			}
			else throw new RuntimeException("Beta demasiado chico, elegir un beta mas grande : T"+ (transicion+1)+" tiempo:"+System.currentTimeMillis());
			
		}
			
	}
	public long gettimeout(int t) {
		return timeout[t];
	}
	
	public void actualiceSensibilizadoT() {

		for (int t = 0; t < numeroTransiciones; t++) {
			if (Temporizadas.esTemporal(t) && (VectorExtendido.getDato(t, 0) == 1))setNuevoTimeStamp(t);
			if ((Temporizadas.esTemporal(t) == true) && (VectorExtendido.getDato(t, 0) == 0))resetEsperando(t);
		}
	}
	
	public void Marca_actual(int transicion) {
		TimeStamp_ahora = (int) (System.currentTimeMillis() - (timeStamp[transicion]));
	}

	public boolean Analisis_Temporal(int transicion) {

		// consola.registrarDisparo("* Tiempo 1 :" + System.currentTimeMillis()+"
		// TimeStamp :"+timeStamp[transicion] , 1);
		Marca_actual(transicion);

		if (test_ventana(transicion)) {
			return true; // <<-- Esta en la ventana de tiempo
		}
		consola.registrarDisparo("* Tiempo en analisis temporal :"+System.currentTimeMillis()+" T"+(transicion+1),1);
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
			//timeout = Tiempo_esperar;
			return false; // esperar alfa
			
		} 
		throw new RuntimeException("Beta demasiado chico, elegir un beta mas grande : T"+ (transicion+1)+" tiempo:"+System.currentTimeMillis());
		//return true;
		
//		else { // <<-- Esta despues de beta
//					// consola.registrarDisparo("* Saliendo pasando beta :" +
//					// Thread.currentThread().getName(), 1);
//			return;
//		}
	}
	
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

}
