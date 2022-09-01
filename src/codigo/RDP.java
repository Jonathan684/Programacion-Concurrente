package codigo;

import java.io.File;
import java.io.FileWriter;
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
	private static Matriz Intervalo;
	private Matriz VectorZ;
	private final int numeroPlazas;
	private final int numeroTransiciones;
	// private final List<Matriz> invariantes;
	private Scanner input;
	private Matriz IEntrada;
	private SensibilizadaConTiempo Temporizadas;
	private Semaphore mutex;
	//private long timeStamp[];
	private PrintWriter pw;
	private static HashMap<String, String> p_invariantes;
	//private long timeout[] ;
	private Matriz VectorExtendidoAux;
	private FileWriter archivo1,archivo2;;
////////////////////////////////////////////////////////////////////////////////////////////////////

	public RDP(Semaphore mutex , PrintWriter pw,FileWriter archivo1,FileWriter archivo2) {
		this.archivo1 = archivo1;
		this.archivo2 = archivo2;
		this.pw = pw;
		this.mutex = mutex;
		p_invariantes = new HashMap<String, String>();
		numeroTransiciones = cargarTransiciones("Matrices/M.I.txt"); // Extraccion de la cantidad de transiciones.
		numeroPlazas = cargarPlazas("Matrices/M.I.txt"); // Extraccion de la cantidad de plazas.
		
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
		Incidencia.cargarMatriz("Matrices/M.I.txt");
		Inhibicion.cargarMatriz("Matrices/M.H.txt");
		Intervalo.cargarMatriz("Matrices/IZ.txt");
		VectorMarcadoActual.cargarMatriz("Matrices/VMI.txt");
		Identidad.cargarIdentidad();
		IEntrada.cargarMatriz("Matrices/M.Pre.txt");
		//M_Inicial.cargarMatriz("matrices/M_Inicial.txt");
		
		Temporizadas = new SensibilizadaConTiempo(numeroTransiciones, Intervalo,pw);
		Cargar_P_Invariante();
		sensibilizar();
		Temporizadas.inicio(VectorExtendidoAux);
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
		//pw.println("* ----------------------");
		//pw.println("* Disparar red T"+(transicion+1));//+" hilo:"+Thread.currentThread().getName()+" t :"+System.currentTimeMillis());
		if (!estaSensibilizada(transicion)) { // no sensibilizada
			//pw.println(getVectorExtendido().imprimir());
			//pw.println("* Disparo no exitoso T"+(transicion+1));
			Temporizadas.resetEsperando(transicion);
			return false;
		}
		else {
			 
			if(Temporizadas.dentroVentana(transicion)){
				//pw.println("* Transición dentro de la ventana T"+(transicion+1));
				if(Temporizadas.alguienEsperando(transicion))
					{
					//pw.println("* Transición alguienEsperando1"+(transicion+1));
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
						if (!Test_Invariante()) {
							
							pw.println("* NO SE CUMPLE EL INVARIANTE DE PLAZA");
							try {
								pw.println("* Fin Con Error*\n");
								archivo1.close();
								archivo2.close();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							throw new RuntimeException("NO SE CUMPLE EL INVARIANTE DE PLAZA");
							
						}
						return true;
					}
			}
			
			else {
				pw.println("* Transición temporal T"+(transicion+1));
				if(Temporizadas.antesVentana(transicion)){ // 1
					if(Temporizadas.alguienEsperando(transicion)){ // 2
						//pw.println("* Transición alguienEsperando2 : T"+(transicion+1));
                        return false;
                    }
					
					long Tiempo = Temporizadas.getTiempoFaltanteParaAlfa(transicion);
					//pw.println("* Disparar red T"+(transicion+1)+" Tiempo:"+ Tiempo);	
					Temporizadas.setEsperando(transicion); // 3
					if (Tiempo > 0) {

							try {
								pw.println("* A dormir T"+(transicion+1)+" T:"+Tiempo);
								
								mutex.release();
								Thread.sleep(Tiempo);

							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							try {
								mutex.acquire();
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							return  Disparar(transicion);
					}
					//pw.println("* tiempo <0"+(transicion+1));
					return false;
				}
				else {
					//pw.println("* despues de la ventana desensibilizada"+(transicion+1));
					return false;
				}
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
	private void sensibilizar() {

		VectorExtendido = VectorExtendidoSinVZ(); // VectorSensibilizado.getAnd(VectorInhibicion);
		VectorZ = Temporizadas.getVectorZ(VectorExtendido);
		VectorExtendido = VectorExtendido.getAnd(VectorZ.getTranspuesta());
	}

	private void calculoDeVectorEstado(int transicion) {
		Matriz aux = Incidencia.getMultiplicacion(Identidad.getColumna(transicion));
		VectorMarcadoActual = VectorMarcadoActual.getSuma(aux);
	}


	public boolean estaSensibilizada(int transicion) {
		if (VectorExtendido.getDato(transicion, 0) == 1)
			return true;
		else
			return false;
	}

	private void Cargar_P_Invariante() {

		CharSequence cort;
		try {
			input = new Scanner(new File("Matrices/P_Invariantes.txt"));
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
			if (VectorInhibicion.getDato(i, 0) > 1)VectorInhibicion.setDato(i, 0, 1);
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

	private boolean Test_Invariante() {
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
	public static Matriz get_Intervalo() {
	return Intervalo;
	}

}
