package codigo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import log.Log;

public class Politica {
	// Campos

	// private int[][] invariantes;
	private List<Integer> vecesPorInvariante;
	private List<Integer> disparos;
	private RDP rdp;

	private static HashMap<String, Integer> t_invariantes;
	private Info[] Transiciones;
	private List<Info> disp = new ArrayList<Info>();

	private static String[][] T_invariantes = { { "T1 T2 T4 T6" }, // Invariante 1
			{ "T1 T3 T5 T6" }, // Invariante 2
			{ "T7 T8 T9 T10" } };// Invariane 3

	public Politica(RDP rdp, Cola cola) {
		// this.invariantes = invariantes;
		vecesPorInvariante = new ArrayList<>();
		disparos = new ArrayList<Integer>(Collections.nCopies(10, 0));
		this.rdp = rdp;
		Transiciones = new Info[rdp.get_numero_Transiciones()];

		for (int i = 0; i < rdp.get_numero_Transiciones(); i++) {
			Transiciones[i] = new Info(i, 0);
			disp.add(Transiciones[i]);
		}

		for (int i = 0; i < T_invariantes.length; i++) {
			vecesPorInvariante.add(0);
		}
		/**
		 * Carga de invariantes en la politica HashMap con invariantes y cantidad de
		 * disparos. Inico cantidad de disparos 0.
		 */
		t_invariantes = new HashMap<String, Integer>();
		for (String[] inv : T_invariantes) {
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
//		if(nTransicion == 6) {
//			aux.update(nTransicion, (aux.getCant_disparos()) + 2);	
//		}
//		else {
		aux.update(nTransicion, (aux.getCant_disparos()) + 1);
//		}

		disp.set(nTransicion, aux);
		// System.out.println("Transicion : "+nTransicion+" Disparos
		// :"+disp.get(0).getCant_disparos());
		disparos.set(nTransicion, (disparos.get(nTransicion) + 1));
	}

//------------------------------------------------------------------------------------
	/**
	 * Metodo que devuelve una transicion
	 * 
	 * @param m matriz que contiene el resultado de Vc and Vs
	 * @return Transicion
	 */

	public int cual(Matriz m) {
		
		//consola.registrarDisparo("* Vector m", 1);
		//String _m = "";
		int cantidad = 0;
		
		for (int i = 0; i < rdp.get_numero_Transiciones(); i++) {
			//_m += " " + m.getDato(i, 0);
			if (m.getDato(i, 0) == 1)cantidad++;
		}
		//consola.registrarDisparo("*" + _m, 1);
		if (cantidad > 1) {
			int transicion_a_disparar = -1;
			transicion_a_disparar = decision_entre_T1_T7(m);
			if (transicion_a_disparar != -1)
				return transicion_a_disparar;

			transicion_a_disparar = decisiones(m);
			if (transicion_a_disparar != -1)
				return transicion_a_disparar;

			transicion_a_disparar = Aleatorio(cantidad, m);
			if (transicion_a_disparar != -1)
				return transicion_a_disparar;

			return transicion_a_disparar;
		}
		return Unica_opcion(m);
	}

	public int decisiones(Matriz m) {
		if ((m.getDato(1, 0) == 1) && (m.getDato(2, 0) == 1) && (m.getDato(5, 0) == 0) ) {
			int transicion_a_disparar = -1;
			List<Info> aux = new ArrayList<Info>(disp);
			Ordenar_x_disparos(aux);
			for (Info i : aux) {
				if (m.getDato(i.get_transicion(), 0) == 1) {
					transicion_a_disparar = i.get_transicion();
					break;
				}
			}
			return transicion_a_disparar;
		}
		return -1;
	}

	public int decision_entre_T1_T7(Matriz m ) {
		if ((m.getDato(0, 0) == 1) && (m.getDato(6, 0) == 1)) {

			// si T7 es mayor a T2
			if (disp.get(1).getCant_disparos() < disp.get(6).getCant_disparos()) {
				return 0; // T1
			}
			// Si T7 es mayor a T3
			else if (disp.get(2).getCant_disparos() < disp.get(6).getCant_disparos()) {
				return 0; // T1
			}
			else if (disp.get(2).getCant_disparos() == disp.get(6).getCant_disparos()) {
				m.setDato(2, 0, 0);
				return Aleatorio(2, m);
				
			}
			else if (disp.get(1).getCant_disparos() == disp.get(6).getCant_disparos()) {
				m.setDato(1, 0, 0);
				return Aleatorio(2, m);
				
			} 
			else return 6;// T7
		}
		return -1;
	}

	public int Unica_opcion(Matriz m) {
		for (int i = 0; i < rdp.get_numero_Transiciones(); i++) {
			if (m.getDato(i, 0) == 1) {
				return i;
			}
		}
		return -1;
	}

	public int Aleatorio(int cantidad, Matriz m) {
		int j = 0;
		List<Info> auxiliar = new ArrayList<Info>();
		Info[] T;
		T = new Info[cantidad];
		for (int i = 0; i < rdp.get_numero_Transiciones(); i++) {
			if (m.getDato(i, 0) == 1) {
				// ystem.out.println(" Se agrega :"+ i);
				T[j] = new Info(i, 0);
				auxiliar.add(T[j]);
				// System.out.println(" -> "+auxiliar.get(j).get_transicion());
				j++;
			}
		}
		int number = (int) (Math.random() * auxiliar.size());
		return auxiliar.get(number).get_transicion();
		// return 0;
	}

	// ------------------------------------------------------------------------------------
//	public void imprimir_disp() {
//		for (Info i : disp) {
//			System.out.println("Trans " + i.get_transicion() + " " + i.getCant_disparos());
//		}
//	}

	public void Ordenar_x_disparos(List<Info> aux) {
		Collections.sort(aux, new Comparator<Info>() {
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
