package codigo;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;
import java.util.concurrent.Semaphore;

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
	private Matriz IEntrada;
	private SensibilizadaConTiempo Temporizadas;
	private Semaphore mutex;
	private long timeStamp[];
	private PrintWriter pw;
	private static HashMap<String, String> p_invariantes;
	private long timeout[] ;
	private Matriz VectorExtendidoAux; 
////////////////////////////////////////////////////////////////////////////////////////////////////

	public RDP(Semaphore mutex , PrintWriter pw) {
		this.pw = pw;
		this.mutex = mutex;
		p_invariantes = new HashMap<String, String>();
		numeroTransiciones = cargarTransiciones("matrices/M.I.txt"); // Extraccion de la cantidad de transiciones.
		numeroPlazas = cargarPlazas("matrices/M.I.txt"); // Extraccion de la cantidad de plazas.
		
		// Matrices
		// invariantes = cargarInvariantes("matrices/InvTrans.txt");
		Incidencia = new Matriz(numeroPlazas, numeroTransiciones);
		Inhibicion = new Matriz(numeroPlazas, numeroTransiciones);
		IEntrada = new Matriz(numeroPlazas, numeroTransiciones);
		VectorExtendidoAux = new Matriz(numeroTransiciones, 1);
		Identidad = new Matriz(numeroTransiciones, numeroTransiciones);
		Intervalo = new Matriz(2, numeroTransiciones);
		//M_Inicial = new Matriz(1, numeroPlazas);
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
		
		
		Temporizadas = new SensibilizadaConTiempo(numeroTransiciones, Intervalo,pw);
		timeStamp = new long[numeroTransiciones];
		timeout = new long[numeroTransiciones];
		
		Arrays.fill(timeStamp, 0);
		Arrays.fill(timeout, 0);
		Cargar_P_Invariante();
		sensibilizar();
		Temporizadas.inicio(VectorExtendidoAux);
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
		pw.println("* ----------------------");
		pw.println("* Disparar red T"+(transicion+1)+" hilo:"+Thread.currentThread().getName());
		sensibilizar(); // Se actualiza el Vz 
		pw.println("* Red : "+sensibilidadas()+" sensi="+estaSensibilizada(transicion));
		pw.println("* Marcado : "+Marcado());
		if (!estaSensibilizada(transicion)) { // no sensibilizada
			return false;
		}
		else {
			 
			if(Temporizadas.dentroVentana(transicion)){
				pw.println("* Transición dentro de la ventana T"+(transicion+1));
				if(Temporizadas.alguienEsperando(transicion))
					{
					//pw.println("* Transición alguienEsperando"+(transicion+1));
                    	return false;
					}
				else{
						Temporizadas.resetEsperando(transicion);
						Matriz	transAntesdelDisparo = new Matriz(numeroTransiciones, 1); 
						Matriz	transDespuesdelDisparo = new Matriz(numeroTransiciones, 1); 
						transAntesdelDisparo =	VectorExtendidoSinVZ();
						calculoDeVectorEstado(transicion);
						sensibilizar(); // Se vuelve a sensibiizar para sacar el nuevo vectorExtendido
						transDespuesdelDisparo = VectorExtendidoSinVZ();
						Temporizadas.ActualizarTimeStamp(transAntesdelDisparo,transDespuesdelDisparo,transicion);
						if (!Test_Invariante())throw new RuntimeException("NO SE CUMPLE EL INVARIANTE DE PLAZA");
						return true;
					}
			}
			
			else {
				//pw.println("* Transición temporal T"+(transicion+1));
				if(Temporizadas.antesVentana(transicion)){ // 1
					if(Temporizadas.alguienEsperando(transicion)){ // 2
                        return false;
                    }
					
					long Tiempo = Temporizadas.getTiempoFaltanteParaAlfa(transicion);
					pw.println("* Disparar red T"+(transicion+1)+" Tiempo:"+ Tiempo);	
					Temporizadas.setEsperando(transicion); // 3
					if (Tiempo > 0) {

							try {
								//pw.println("* A dormir T"+(transicion+1)+" T:"+Tiempo);
								
								mutex.release();
								Thread.sleep(Tiempo);

							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							try {
								mutex.acquire();
								//pw.println("*==========>>>>>>>>>> Desperte T"+(transicion+1));
								
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							return  Disparar(transicion);
					}
					return false;
				}
				else return false;
			 }
		}
	}

	private Matriz VectorExtendidoSinVZ() {
		// TODO Auto-generated method stub
			
			sensibilizarVectorE();
			sensibilizarVectorB();
			VectorExtendidoAux = VectorSensibilizado.getAnd(VectorInhibicion);
			return VectorExtendidoAux;
	}
	
	/*
	 *
	 */
	public void sensibilizar() {

		sensibilizarVectorE();
		sensibilizarVectorB();
		//sensibilizarVectorZ();
		//////////////////////////////////////////////////////////////
		VectorExtendido = VectorSensibilizado.getAnd(VectorInhibicion);
		VectorZ = Temporizadas.getVectorZ(VectorExtendidoSinVZ());
		//pw.println("* VectorZ---->> " + VectorZ.imprimir());
		VectorExtendido = VectorExtendido.getAnd(VectorZ.getTranspuesta());
		//pw.println("* Marcado---->> " + Marcado());
		pw.println("* Sin Vz---->> " + VectorExtendidoSinVZ().imprimir());
		pw.println("* Con Vz---->> " + VectorExtendido.imprimir());
	}

	public void calculoDeVectorEstado(int transicion) {
		Matriz aux = Incidencia.getMultiplicacion(Identidad.getColumna(transicion));
		VectorMarcadoActual = VectorMarcadoActual.getSuma(aux);
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

	public Matriz getVectorExtendidosinVz() {
		return VectorExtendidoAux ;
	}
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
		//consola.registrarDisparo("* 0 m: " + VectorMarcadoActual.getDato(0, 0), 1);
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

}
