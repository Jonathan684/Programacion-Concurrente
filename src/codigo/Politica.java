package codigo;

import java.io.PrintWriter;
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
	private int ultima;
	private int inv1;
	private int inv2;
	private int inv3;
	private int[] T_inv;
	private static HashMap<String, Integer> t_invariantes;
	private Info[] Transiciones;
	private List<Info> disp = new ArrayList<Info>();
	private PrintWriter pw;
	private static String[][] T_invariantes = { { "T1 T2 T4 T6" }, // Invariante 1
			{ "T1 T3 T5 T6" }, // Invariante 2
			{ "T7 T8 T9 T10" } };// Invariane 3

	private static int[][] invariantes = { { 1, 2, 4, 6 }, // Invariante 1
			{ 1, 3, 5, 6 }, // Invariante 2
			{ 7, 8, 9, 10 } };// Invariane 3

	public Politica(PrintWriter pw, RDP rdp, Cola cola) {

		ultima = 0;
		inv1 = 0;
		inv2 = 0;
		inv3 = 0;
		T_inv = new int[3];
		vecesPorInvariante = new ArrayList<>();
		disparos = new ArrayList<Integer>(Collections.nCopies(10, 0));
		this.rdp = rdp;
		Transiciones = new Info[rdp.get_numero_Transiciones()];
		this.pw = pw;
		for (int i = 0; i < rdp.get_numero_Transiciones(); i++) {
			Transiciones[i] = new Info(i, 0, pertence(i));
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
	}

	public void registrarDisparo(int nTransicion) { // No considera a T1 ni a T6

		// T1
		if (nTransicion == 0) {// T4
			if (inv1 > inv2) {
				Transiciones[0].setInvariante(1);
			} else
				Transiciones[0].setInvariante(0);
		}

		if (nTransicion == 3) {// T4
			// System.out.println("Se disparo primer invariante");
			vecesPorInvariante.set(0, (vecesPorInvariante.get(0) + 1));
			ultima = 3;
		}
		if (nTransicion == 4) { // T5
			// System.out.println("Se disparo segundo invariante");
			ultima = 4;
			vecesPorInvariante.set(1, (vecesPorInvariante.get(1) + 1));
		}
		if (nTransicion == 9) {// T10
			// System.out.println("Se disparo tercer invariante");
			vecesPorInvariante.set(2, (vecesPorInvariante.get(2) + 1));
		}
		if (nTransicion == 5) {// T6
			if (ultima == 3) { // T4 --> T6
				inv1 = inv1 + 1;
				T_inv[0] = T_inv[0] + 1;
				Transiciones[5].setInvariante(0);
			}
			if (ultima == 4) { // T4 --> T6
				inv2 = inv2 + 1;
				T_inv[1] = T_inv[1] + 1;
				Transiciones[5].setInvariante(1);
			}
			// System.out.println("Se disparo tercer invariante");
			vecesPorInvariante.set(2, (vecesPorInvariante.get(2) + 1));
		}
		if (nTransicion == 9) {// T6
			inv3 = inv3 + 1;
			T_inv[2] = T_inv[2] + 1;
			// System.out.println("Se disparo tercer invariante");
			vecesPorInvariante.set(2, (vecesPorInvariante.get(2) + 1));
		}
		if (Transiciones[nTransicion].getInvariante() == 0) {
			Transiciones[nTransicion].setcantInvariante(inv1);
		}
		if (Transiciones[nTransicion].getInvariante() == 1) {
			Transiciones[nTransicion].setcantInvariante(inv2);
		}
		if (Transiciones[nTransicion].getInvariante() == 2) {
			Transiciones[nTransicion].setcantInvariante(inv3);
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
		pw.println("* inv1: " + inv1);
		pw.println("* inv2: " + inv2);
		pw.println("* inv3: " + inv3);
		pw.println("* T_inv1: " + T_inv[0]);
		pw.println("* T_inv2: " + T_inv[1]);
		pw.println("* T_inv3: " + T_inv[2]);
	}

//------------------------------------------------------------------------------------
	/**
	 * Metodo que devuelve una transicion
	 * 
	 * @param m matriz que contiene el resultado de Vc and Vs
	 * @return Transicion
	 */

	public int cual(Matriz m) {

		int cantidad = 0;
		int transicion_aux = -1;
		for (int i = 0; i < rdp.get_numero_Transiciones(); i++) {
			// _m += " " + m.getDato(i, 0);
			if (m.getDato(i, 0) == 1) {
				pw.println("* T: " + (i + 1) + " disp:" + disp.get(i).getCant_disparos() + " cant:"
						+ Transiciones[i].getcantInvariante());
				cantidad++;
				transicion_aux=i;
			}
		}
		//
//		// pw.println("* Cantidad: "+cantidad);
//		if (m.getDato(5, 0) == 1) { // Prioridad a la salidad
//			return 5;
//		}
//		if (m.getDato(3, 0) == 1) { // Prioridad a la salidad
//			return 3;
//		}
//		if (m.getDato(4, 0) == 1) { // Prioridad a la salidad
//			return 4;
//		}
//		if (m.getDato(9, 0) == 1) { //T10
//			return 9;
//		}
//		if (m.getDato(8, 0) == 1) {
//			return 8;
//		}
		
		if (cantidad > 1) {
			int transicion_a_disparar = -1;
			int transicion_temporal = 0;
			int disparos_de_invariantes = 0;
			for (int i = 0; i < rdp.get_numero_Transiciones(); i++) {

				if (m.getDato(i, 0) == 1)
				{
					//PRIMERA VEZ QUE ENCUENTRE UNA TRANSICION
					if (transicion_temporal == 0) 
					{
						transicion_temporal = i;
						disparos_de_invariantes = Transiciones[i].getcantInvariante();
						transicion_a_disparar = i;
						transicion_temporal++;
					} 
					//CANTIDAD DE VECES QUE SE DISPARO EL INVARIANTE ES MAYOR QUE EL ACTUAL
					else if (disparos_de_invariantes > Transiciones[i].getcantInvariante()) 
					{
//						//SI PERTENECEN AL MISMO INVARIANTE
//						if (Transiciones[i].getInvariante() == Transiciones[transicion_a_disparar].getInvariante())
//						{
//							if (i > transicion_a_disparar) 
//							{
//								transicion_a_disparar = i;
//								disparos_de_invariantes = Transiciones[i].getcantInvariante();
//							}
//						} 
//						//SI NO SON DEL MISMO INVARIANTE
//						else 
//						{
							disparos_de_invariantes = Transiciones[i].getcantInvariante();
							transicion_a_disparar = i;
//						}
					}
				}
			}
			return transicion_a_disparar;
			}
			return 	transicion_aux;
		}
			
//					else if (disparos_de_invariantes > Transiciones[i].getcantInvariante()) {
//						// pw.println("* Invariante menor: "+Transiciones[i].getcantInvariante()+"
//						// Invariante mayor :"+disparos_de_invariantes);
//						disparos_de_invariantes = Transiciones[i].getcantInvariante();
//						transicion_a_disparar = i;
//					} else if (disparos_de_invariantes == Transiciones[i].getcantInvariante()) {
//						pw.println("* Invariante  menor: " + Transiciones[i].getcantInvariante() + " Invariante mayor :"
//								+ disparos_de_invariantes);
//						disparos_de_invariantes = Transiciones[i].getcantInvariante();
//
//						if (i > transicion_a_disparar)
//							transicion_a_disparar = i;
////						int numero = (int)(Math.random()*2+1);
////						if(numero%2==0) {
////							transicion_a_disparar = i;
////						}
//					}
					
					
					

	

	public int decisiones(Matriz m) {
		// Transiciones
		// if ((m.getDato(1, 0) == 1) && (m.getDato(2, 0) == 1) && (m.getDato(5, 0) ==
		// 0) ) {
		int transicion_a_disparar = -1;
		int temp = 0;
		int pertenece = 0;
		int disparos_de_invariantes = 0;
		for (int i = 0; i < rdp.get_numero_Transiciones(); i++) {
//				System.out.println("Ingreso aca0 " +m.getDato(i, 0) );
			if (m.getDato(i, 0) == 1) {
//					System.out.println("Ingreso aca1");
				if (temp == 0) {
//						System.out.println("Ingreso aca2");
					temp = i;
					pertenece = Transiciones[i].getInvariante();// inv1 - inv2 -inv3 pero en la proxima se puede
																// actualizar
					disparos_de_invariantes = Transiciones[i].getcantInvariante();

					transicion_a_disparar = i;
					temp++;
				} else if (disparos_de_invariantes > Transiciones[i].getcantInvariante()) {
					pw.println("* Invariante  menor: " + Transiciones[i].getcantInvariante() + " Invariante mayor :"
							+ disparos_de_invariantes);
					disparos_de_invariantes = Transiciones[i].getcantInvariante();
					transicion_a_disparar = i;
				} else if (disparos_de_invariantes == Transiciones[i].getcantInvariante()) {
					pw.println("* Invariante  menor: " + Transiciones[i].getcantInvariante() + " Invariante mayor :"
							+ disparos_de_invariantes);
					disparos_de_invariantes = Transiciones[i].getcantInvariante();
					transicion_a_disparar = i;
					int numero = (int) (Math.random() * 2 + 1);
					if (numero % 2 == 0) {
						transicion_a_disparar = i;
					}
				}
			}
		}
		System.out.println(m.getTranspuesta().imprimir());
		System.out.println("LLEGO HASTA ACA :" + transicion_a_disparar);
		System.out.println("--->" + Transiciones[transicion_a_disparar].getcantInvariante());
		// List<Info> aux = new ArrayList<Info>(disp);
//			Ordenar_x_disparos(aux);
//			
//			for (Info i : aux) {
//				pw.print("->"+i.get_transicion() +" ");
//				if (m.getDato(i.get_transicion(), 0) == 1) {
//					transicion_a_disparar = i.get_transicion();
//					break;
//				}
//			}
		pw.println("*");
		pw.print("* " + transicion_a_disparar);
		return transicion_a_disparar;
		// }
		// return -1;
	}

	public int pertence(int ingreso) {

//    for(int i = 0;i<invariantes.length;i++) {
//			
//			for(int j = 0;j<((invariantes[i].length));j++) {
//				System.out.println(" valor :"+ invariantes[i][j]+" -->"+i+" "+j);
//				
//			}
//		}
//		System.out.println("Tamño :"+ invariantes.length);
//		System.out.println("Tamño2 :"+ invariantes[0].length);
		// System.out.println("Ingreso T:"+ (ingreso+1));
		int retorna = -1;
		for (int i = 0; i < invariantes.length; i++) {

			for (int j = 0; j < (invariantes[i].length); j++) {
				// System.out.println(" -->"+i+" "+j);
				// System.out.println("["+ invariantes[i][j]+" -->"+i+" "+j+" ]");
				if (invariantes[i][j] == (ingreso + 1)) {
					// System.out.println("invariante al que pertenece :"+i);
					return i;
				}
			}
		}
		// System.out.println("Retorna :"+retorna);
		return retorna;

	}

	public int decision_entre_T1_T7(Matriz m) {
		if ((m.getDato(0, 0) == 1) && (m.getDato(6, 0) == 1)) {

			// si T7 es mayor a T2
			if (disp.get(1).getCant_disparos() < disp.get(6).getCant_disparos()) {
				return 0; // T1
			}
			// Si T7 es mayor a T3
			else if (disp.get(2).getCant_disparos() < disp.get(6).getCant_disparos()) {
				return 0; // T1
			} else if (disp.get(2).getCant_disparos() == disp.get(6).getCant_disparos()) {
				m.setDato(2, 0, 0);
				return Aleatorio(2, m);

			} else if (disp.get(1).getCant_disparos() == disp.get(6).getCant_disparos()) {
				m.setDato(1, 0, 0);
				return Aleatorio(2, m);

			} else
				return 6;// T7
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
				T[j] = new Info(i, 0, pertence(i));
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

//		int j = 1;
		log.registrarDisparo("=====================================", 1);
//		for (int veces : vecesPorInvariante) {
//			if (j == 1) {
//				// System.out.println("Invariante " + j + ": " + veces + " veces [T1 T2 T4 T6] "
//				// );
		log.registrarDisparo("Invariante " + 1 + ": " + inv1 + " veces  [T1 T2 T4 T6] ", 1);
//			}
//			if (j == 2) {
		// System.out.println("Invariante " + j + ": " + veces + " veces [T1 T3 T5 T7]"
		// );
		log.registrarDisparo("Invariante " + 2 + ": " + inv2 + " veces  [T1 T3 T5 T6]", 1);
//			}
//			if (j == 3) {
		// System.out.println("Invariante " + j + ": " + veces + " veces [T7 T8 T9 T10]"
		// );
		log.registrarDisparo("Invariante " + 3 + ": " + inv3 + " veces  [T7 T8 T9 T10]", 1);
//			}
//
//			j++;
//		}
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
