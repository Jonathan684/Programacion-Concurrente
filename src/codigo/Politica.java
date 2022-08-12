package codigo;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import log.Log;

public class Politica {
	// Campos
    private List<Integer> disparos;
	private RDP rdp;
	private int ultima;
	private int inv1;
	private int inv2;
	private int inv3;
	private Info[] Transiciones;
	private PrintWriter pw,registro_disparo;
    private static int[][] invariantes = { { 1, 2, 4, 6 }, // Invariante 1
			{ 1, 3, 5, 6 }, // Invariante 2
			{ 7, 8, 9, 10 } };// Invariane 3
   public Politica(PrintWriter pw, RDP rdp,PrintWriter registro_disparo) {
		ultima = 0;
		inv1 = 0;
		inv2 = 0;
		inv3 = 0;
		disparos = new ArrayList<Integer>(Collections.nCopies(10, 0));
		this.rdp = rdp;
		Transiciones = new Info[rdp.get_numero_Transiciones()];
		this.pw = pw;
		this.registro_disparo = registro_disparo;
		for (int i = 0; i < rdp.get_numero_Transiciones(); i++) {
			Transiciones[i] = new Info(i, 0, pertence(i));
		}

     }

	public int pertence(int ingreso) {

		int retorna = -1;
		for (int i = 0; i < invariantes.length; i++) {

			for (int j = 0; j < (invariantes[i].length); j++) {
				if (invariantes[i][j] == (ingreso + 1)) {
					return i;
				}
			}
		}
		return retorna;
	}

	public void registrarDisparo(int nTransicion) { // No considera a T1 ni a T6

		// T1
		//registro_disparo.println("T"+(nTransicion+1));
		//pw.println("T"+(nTransicion+1));
		if((nTransicion+1) == 10)
			registro_disparo.print("T"+0);
		else 	registro_disparo.print("T"+(nTransicion+1));
		
		
		
		if (nTransicion == 0) {// T1
			if (inv1 > inv2) { // DEBERIA DE TOMAR LA CANTIDAD DE VECES QUE SE DISPARO PARA ESE INVARIANTE
				Transiciones[0].setInvariante(1);
				
			} else
				Transiciones[0].setInvariante(0);
		}

		if (nTransicion == 3) {// T4
			// System.out.println("Se disparo primer invariante");
			//vecesPorInvariante.set(0, (vecesPorInvariante.get(0) + 1));
			ultima = 3;
			Transiciones[5].setInvariante(0);
		}
		if (nTransicion == 4) { // T5
			// System.out.println("Se disparo segundo invariante");
			ultima = 4;
			//vecesPorInvariante.set(1, (vecesPorInvariante.get(1) + 1));
			Transiciones[5].setInvariante(1);
		}
		if (nTransicion == 9) {// T10
			
			inv3 = inv3 + 1;
			//update todo el invariante 3
			//vecesPorInvariante.set(2, (vecesPorInvariante.get(2) + 1));
			//Transiciones[6].setInvariante(2);
			Transiciones[6].setcantInvariante(inv3);//T7
			
			//Transiciones[7].setInvariante(2);
			Transiciones[7].setcantInvariante(inv3);//T8
			
			//Transiciones[8].setInvariante(2);
			Transiciones[8].setcantInvariante(inv3);//T9
			//vecesPorInvariante.set(2, (vecesPorInvariante.get(2) + 1));
		}
		if (nTransicion == 5) {// T6
			if (ultima == 3) { // T4 --> T6
				inv1 = inv1 + 1;
				//Update todo el invariante 0
				Transiciones[1].setcantInvariante(inv1);//T2
				Transiciones[3].setcantInvariante(inv1);//T4
				if (inv1 > inv2) {      // DEBERIA DE TOMAR LA CANTIDAD DE VECES QUE SE DISPARO PARA ESE INVARIANTE
					Transiciones[0].setInvariante(1);
					Transiciones[0].setcantInvariante(inv2);//T1
				}
				else if (inv2 == inv1) { // T1
					
					//Transiciones[0].setInvariante(0);
					//Transiciones[0].setcantInvariante(inv2);//T1
					int t = (int) (Math.random() * 2);
					if(t==1) {
						Transiciones[0].setInvariante(0);
						Transiciones[0].setcantInvariante(inv1);//T1
					}
					else {
						Transiciones[0].setInvariante(1);
						Transiciones[0].setcantInvariante(inv2);//T1
					}
				}
				else if (inv2 > inv1) { // DEBERIA DE TOMAR LA CANTIDAD DE VECES QUE SE DISPARO PARA ESE INVARIANTE
					Transiciones[0].setInvariante(0);
					Transiciones[0].setcantInvariante(inv1);//T1
				}
			}
			if (ultima == 4) { // T5 --> T6
				
				inv2 = inv2 + 1;
				Transiciones[2].setcantInvariante(inv2);//T3
				Transiciones[4].setcantInvariante(inv2);//T5
				if (inv2 > inv1) { // DEBERIA DE TOMAR LA CANTIDAD DE VECES QUE SE DISPARO PARA ESE INVARIANTE
					Transiciones[0].setInvariante(0);
					Transiciones[0].setcantInvariante(inv1);//T1
				}
				else if (inv2 == inv1) { // T1
					
					int t = (int) (Math.random() * 2);
					if(t==1) {
						Transiciones[0].setInvariante(0);
						Transiciones[0].setcantInvariante(inv1);//T1
					}
					else {
						Transiciones[0].setInvariante(1);
						Transiciones[0].setcantInvariante(inv2);//T1
					}
				}
				else if (inv2 < inv1) { // DEBERIA DE TOMAR LA CANTIDAD DE VECES QUE SE DISPARO PARA ESE INVARIANTE
					Transiciones[0].setInvariante(1);
					Transiciones[0].setcantInvariante(inv2);//T1
				}
				//update todo el invariante 2
			}
		}
		
		
		//Update de la cantidad de veces que se dispara el invariante
		if (Transiciones[nTransicion].getInvariante() == 0) {
			Transiciones[nTransicion].setcantInvariante(inv1);
		}
		if (Transiciones[nTransicion].getInvariante() == 1) {
			Transiciones[nTransicion].setcantInvariante(inv2);
		}
		if (Transiciones[nTransicion].getInvariante() == 2) {
			Transiciones[nTransicion].setcantInvariante(inv3);
		}
		disparos.set(nTransicion, (disparos.get(nTransicion) + 1));
	}

//------------------------------------------------------------------------------------
	/**
	 * Metodo que devuelve una transicion
	 * 
	 * @param m matriz que contiene el resultado de Vc and Vs
	 * @return Transicion
	 */
	/*
	 * CRITERIO DE LA POLITICA SI LAS TRANSICIONES PERTENECEN AL MISMO INVARIANTE SE
	 * DISPARA LAS MAS GRANDE. PRIORIDAD A COMPLETAR EL INVARIANTE OBSERVO LA
	 * CANTIDAD DE VECES QUE SE DISPARO EL INVARIANTE, NO LA TRANSICION.
	 */
	public int cual(Matriz m) {
// 		Se elije invariante que menos se disparo y si son iguales aleatorio
		
		int cantidad = 0;
		int transicion_aux = -1;
		for (int i = 0; i < rdp.get_numero_Transiciones(); i++) {
			// _m += " " + m.getDato(i, 0);
			if (m.getDato(i, 0) == 1) {
				//pw.println("* [T" + (i + 1) + "] disp:" + disp.get(i).getCant_disparos() + " cant_vcs_inv:"
					//	+ Transiciones[i].getcantInvariante() + " Inv-->>" + Transiciones[i].getInvariante());
				cantidad++;
				transicion_aux = i;
			}
		}
		// SI HAY MAS DE UNA TRANSICION EN  "m".
		if (cantidad > 1) {
			int transicion_a_disparar = -1;
			boolean inicio = true;
			int disparos_de_invariantes = 0;
			
			for (int i = 0; i < rdp.get_numero_Transiciones(); i++) {
				
				if (m.getDato(i, 0) == 1) { //BUSCO LA TRANSICION EN m.
					// PRIMERA VEZ QUE ENCUENTRE UNA TRANSICION
					if (inicio) {
						inicio = false;
						disparos_de_invariantes = Transiciones[i].getcantInvariante();
						transicion_a_disparar = i;
					}
					// CANTIDAD DE VECES QUE SE DISPARO EL INVARIANTE ES MAYOR QUE EL ACTUAL
					else if ((Transiciones[i].getcantInvariante() < disparos_de_invariantes)&& (Transiciones[transicion_a_disparar].getInvariante() != Transiciones[i].getInvariante())) {
						disparos_de_invariantes = Transiciones[i].getcantInvariante();
						transicion_a_disparar = i;
					}
					// SI SON IGUALES ALMACENO LA TRANSICION DE MAYOR TAMAÑO POR LA PRIORIDAD A LA SALIDA
					else if ((disparos_de_invariantes == Transiciones[i].getcantInvariante()) && (Transiciones[transicion_a_disparar].getInvariante() == Transiciones[i].getInvariante())) {
					    //pw.println("* Prioridad a la salida"+"* Transicion_a_disparar = "+(i+1));
						disparos_de_invariantes = Transiciones[i].getcantInvariante();
						transicion_a_disparar = i;
					}
					// SI SON IGUALES PERO DE DISTINTOS INVARIANTES LOS ALMACENO Y ELIJO UNO ALEATORIAMENTE
					else if ((disparos_de_invariantes == Transiciones[i].getcantInvariante()) && (Transiciones[transicion_a_disparar].getInvariante() != Transiciones[i].getInvariante())) {
						//pw.println("* Iguales y de distinto invariantes T"+(i+1)+" == T"+(transicion_a_disparar+1));
						int t = (int) (Math.random() * 2);
						if(t==1)transicion_a_disparar = i;	
						//pw.println("* Se eligió T"+(transicion_a_disparar+1)+" t="+t);
						
					}

				}
			}
			return transicion_a_disparar;
		}
        //SI HAY UNA SOLA TRANSICION EN "m"
		return transicion_aux;
	}

	public void imprimir(Log log) {

		log.registrarDisparo("=====================================");
		log.registrarDisparo("Invariante " + 1 + ": " + inv1 + " veces  [T1 T2 T4 T6] Tiempo:"+((inv1*(145))/1000));
		log.registrarDisparo("Invariante " + 2 + ": " + inv2 + " veces  [T1 T3 T5 T6] Tiempo:"+((inv1*(55))/1000));
		log.registrarDisparo("Invariante " + 3 + ": " + inv3 + " veces  [T7 T8 T9 T10] Tiempo:"+((inv1*(50))/1000));
		log.registrarDisparo("=====================================");
		for (int i = 0; i < disparos.size(); i++) {
			if (i == 6)
				log.registrarDisparo("Transicion: " + (i + 1) + " disparos: " + (disparos.get(i)));
			else
				log.registrarDisparo("Transicion: " + (i + 1) + " disparos: " + disparos.get(i));
		}
	}

}
