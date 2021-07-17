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
    private Matriz VectorMarcadoActual, VectorExtendido, VectorSensibilizado, VectorInhibicion;
	private Matriz Incidencia,Inhibicion,Identidad;
	private Matriz Intervalo;
    private Matriz VectorZ;
    private final int numeroPlazas;
	private final int numeroTransiciones;
    private final List<Matriz> invariantes;
	private Scanner input;
	private long SensibilizadaConTiempo[]; 
	private Matriz IEntrada ;
	
////////////////////////////////////////////////////////////////////////////////////////////////////
//EL VALOR ACTUAL ES IGUAL AL INICIAL
////Constructor
	public RDP() {
	
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
	
	//Carga de datos
	Incidencia.cargarMatriz("matrices/M.I.txt");
	Inhibicion.cargarMatriz("matrices/M.B.txt");
	VectorMarcadoActual.cargarMatriz("matrices/VMI.txt");
	Intervalo.cargarMatriz("matrices/IZ.txt");
	Identidad.cargarIdentidad();
	IEntrada.cargarMatriz("matrices/M.Pre.txt");
	SensibilizadaConTiempo  = new long[numeroTransiciones];
	
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
		for(int t = 1; t<nroT+1; t++) {	Transiciones[t-1] = "T"+t; }
	}
	/**
	 * Completa el string de Plazas con la cantidad correspondiente
	 * @param nroP: la cantidad de plazas en la matriz de incidencia.
	 */
	private void setStringPlazas(int nroP) {
		Plazas = new String[nroP];
		for(int p = 1; p<nroP+1; p++) { Plazas[p-1] = "P"+p; }
	}
	
	/**
	 * Metodo que calcula el vector Inhibicion
	 */
	private void sensibilizarVectorB() {
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
	}
	/**
	 * Metodo que calcula el vector sensibilizado
	 */
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
	
	//METODOS PUBLICOS
	//-----------------------------------------------------
	/**
	 * Metodo que sensibiliza las transiciones y carga el vector extendido
	 */
	public void sensibilizar() {
		sensibilizarVectorB();
		sensibilizarVectorE();
		
		VectorExtendido = VectorSensibilizado.getAnd(VectorInhibicion).getAnd(VectorZ.getTranspuesta());		
		
	}
	public void setEsperando(int transicion)
	{
		VectorZ.setDato(0, transicion, 0);
		//VectorZ.imprimirMatriz();
	}
	public void resetEsperando(int transicion)
	{
		VectorZ.setDato(0, transicion, 1);
		//VectorZ.imprimirMatriz();
	}
	
    public void sensibilizarVectorZ() {
    	for(int k = 0 ; k<numeroTransiciones; k++) {VectorZ.setDato(0, k, 1);}
    	//VectorZ.imprimirMatriz();
    }
    
	/**
	 * Este metodo dispara una transicion de la rdp indicada por parametro, teniendo en cuenta el modo indicado por parametro
	 *@param transicion : numero de transicion.
	 *@return : -0 retorna 0 si el disparo no es exitoso.
	 *          -1 retorna 1 si el disparo es exitoso.
	 */
	public boolean Disparar(int transicion){
		if(!estaSensibilizada(transicion))
		{
			return false;
		}
        Matriz aux = Incidencia.getMultiplicacion(Identidad.getColumna(transicion)); //Probleamas con el numero de la transicion
		VectorMarcadoActual = VectorMarcadoActual.getSuma(aux);
		Test_Invariante();
		sensibilizar();
		return true;
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
		if(VectorExtendido.getDato(transicion,0) == 1) 
			return true; 
		else return false;
	}
	/**
	 * Metodo que devuelve el vector con las transiciones sensibilizadas
	 * @return vector extendido
	 */
	public Matriz getSensibilizadas() { return VectorExtendido; }
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
	public String sensibilidadas(Matriz vector) {
		//System.out.println("\n");
		String Transiciones_sensibilizadas = "";
		
			for(int n=0 ; n<vector.getNumFilas() ; n++)
				{
				Transiciones_sensibilizadas += Transiciones[n] +":" + vector.getDato(n, 0) +" ";
				}
		return Transiciones_sensibilizadas;
}
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
}
