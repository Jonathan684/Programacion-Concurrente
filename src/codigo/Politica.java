package codigo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import log.Log;

public class Politica {
	// Campos

	private int[][] invariantes;
	private List<Integer> vecesPorInvariante;
	private List<Integer> disparos;
	private List<Integer> Indices;
	private RDP rdp;
	private Log consola;
	private static HashMap<String, Integer> t_invariantes;
	private Info[] Transiciones;
	private List<Info> disp = new ArrayList<Info>();

	public Politica(String[][] T_Invariantes, int[][] invariantes, RDP rdp, Log consola) {
		this.consola = consola;
		this.invariantes = invariantes;
		vecesPorInvariante = new ArrayList<>();
		disparos = new ArrayList<Integer>(Collections.nCopies(10, 0));
		this.rdp = rdp;
		Transiciones = new Info[rdp.get_numero_Transiciones()];
		for (int i = 0; i < rdp.get_numero_Transiciones(); i++) {
			Transiciones[i] = new Info(i, 0);
			disp.add(Transiciones[i]);
		}
//		 System.out.println("-------================------------------");	  
//			for (Info i : disp) {
//	            System.out.println(" Transicion " +i.getCant_disparos());
//	            
//	        }
//		System.out.println("---------------=====================-------");

		for (int i = 0; i < T_Invariantes.length; i++) {
			vecesPorInvariante.add(0);
		}
		/**
		 * Carga de invariantes en la politica HashMap con invariantes y cantidad de
		 * disparos. Inico cantidad de disparos 0.
		 */
		t_invariantes = new HashMap<String, Integer>();
		for (String[] inv : T_Invariantes) {
			t_invariantes.put(inv[0], 0);
		}
//		for (String Inv : t_invariantes.keySet()) {
//			Integer Cantidad_de_disparos = t_invariantes.get(Inv);
//			System.out.println("Clave: " + Inv + ", key: " + Cantidad_de_disparos);
//		}
//		System.out.println("Primer valor :"+t_invariantes.keySet().size());
//		System.out.println("Primer valor :"+t_invariantes.values());
//		System.out.println("Primer valor --> :"+ T_Invariantes[0][0]);
//		String[] letras = (T_Invariantes[2][0]).split(" ");
//		for (String letra : letras) {
//			// Integer.parseInt(cort.toString());
//			int t = Integer.parseInt(letra.replace("T", ""));
//			// System.out.println("letra -->"+ t);
//		}
		// t_invariantes.values().toArray()[0];

	}

	public void registrarDisparo(int nTransicion) { // No considera a T1 ni a T6

		// T1
		if (nTransicion == 3) {// T4
			// System.out.println("Se disparo primer invariante");
			vecesPorInvariante.set(0, (vecesPorInvariante.get(0) + 1));
		}
		if (nTransicion == 4) { // T5
			// System.out.println("Se disparo segundo invariante");
			vecesPorInvariante.set(1, (vecesPorInvariante.get(1) + 1));
		}
		if (nTransicion == 9) {// T10
			// System.out.println("Se disparo tercer invariante");
			vecesPorInvariante.set(2, (vecesPorInvariante.get(2) + 1));
		}

		Info aux = disp.get(nTransicion);
//		System.out.println("Transicion que se disparo->"+nTransicion);
		aux.update(nTransicion, (aux.getCant_disparos()) + 1);

		disp.set(nTransicion, aux);
		// System.out.println("Transicion : "+nTransicion+" Disparos
		// :"+disp.get(0).getCant_disparos());
		disparos.set(nTransicion, (disparos.get(nTransicion) + 1));
	}

	/**
	 * Metodo que devuelve una transicion
	 * 
	 * @param m matriz que contiene el resultado de Vc and Vs
	 * @return Transicion
	 */

	public int cual(Matriz m) {
		int tansicion_a_disparar = -1;
		for (int transicion = 0; transicion < rdp.get_numero_Transiciones(); transicion++) {
			if ((m.getDato(transicion, 0) == 1) && (rdp.esInmediata(transicion))) {
				return transicion;
			}
		}
		System.out.println("\n//////////////////////inicio cual//////////////////////\n");
		
		Ordenar_x_disparos();
		//imprimir();
		for (Info i : disp) {
			if (m.getDato(i.get_transicion(), 0) == 1) {
				tansicion_a_disparar = i.get_transicion();
				break;
			}
		}
		Ordenar_x_transicion();
		System.out.println("Duermen");
		int despertar = -1;
		for(int k = 0;k<rdp.get_vector_Esperando().length;k++){
			System.out.print("T"+k+":"+rdp.get_vector_Esperando()[k]+" ");
			if(rdp.get_vector_Esperando()[k] == 1) {
				despertar = k;
				break;
			}
		}
		
		System.out.println("\n"+System.currentTimeMillis());
		if(despertar != -1) {
			System.out.println("Va despertar : "+ (despertar+1));
			Matriz aux = rdp.Falso_Disparo(despertar);
			aux.getTranspuesta().imprimirMatriz();
		}
		
		System.out.println("Transicion_a_disparar : "+(tansicion_a_disparar+1));
		m.getTranspuesta().imprimirMatriz();
		System.out.println("*******************fin cual*************************");
		return tansicion_a_disparar;
	}

//	public void imprimir_disp() {
//		for (Info i : disp) {
//			System.out.println("Trans " + i.get_transicion() + " " + i.getCant_disparos());
//		}
//	}

	public void Ordenar_x_disparos() {
		Collections.sort(disp, new Comparator<Info>() {
			public int compare(Info c1, Info c2) {
				if (c1.getCant_disparos() < c2.getCant_disparos())
					return -1;
				if (c1.getCant_disparos() < c2.getCant_disparos())
					return 1;
				return 0;
			}
		});
	}

	public void Ordenar_x_transicion() {
		Collections.sort(disp, new Comparator<Info>() {
			public int compare(Info c1, Info c2) {
				if (c1.get_transicion() < c2.get_transicion())
					return -1;
				if (c1.get_transicion() < c2.get_transicion())
					return 1;
				return 0;
			}
		});
	}

	public void imprimir(Log log) {
		// System.out.println("=================================");

		int j = 1;
		log.registrarDisparo("=====================================", 1);
		for (int veces : vecesPorInvariante) {
			if (j == 1) {
				// System.out.println("Invariante " + j + ": " + veces + " veces [T1 T2 T4 T6] "
				// );
				log.registrarDisparo("Invariante " + j + ": " + veces + " veces  [T1 T2 T4 T6] ", 1);
			}
			if (j == 2) {
				// System.out.println("Invariante " + j + ": " + veces + " veces [T1 T3 T5 T7]"
				// );
				log.registrarDisparo("Invariante " + j + ": " + veces + " veces  [T1 T3 T5 T6]", 1);
			}
			if (j == 3) {
				// System.out.println("Invariante " + j + ": " + veces + " veces [T7 T8 T9 T10]"
				// );
				log.registrarDisparo("Invariante " + j + ": " + veces + " veces  [T7 T8 T9 T10]", 1);
			}

			j++;
		}
		log.registrarDisparo("=====================================", 1);
		for (int i = 0; i < disparos.size(); i++) {
			// System.out.println("Transicion: "+(i+1)+ " disparos: "+disparos.get(i));
			if (i == 6)
				log.registrarDisparo("Transicion: " + (i + 1) + " disparos: " + (disparos.get(i)), 1);
			else
				log.registrarDisparo("Transicion: " + (i + 1) + " disparos: " + disparos.get(i), 1);
		}
		// System.out.println("Mayor " + Collections.max(disparos)) ;
	}

}
