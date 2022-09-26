package codigo;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import log.Log;

public class Politica {
	// Campos
	private List<Integer> disparos;
	private Matriz Intervalo;
	private int ultimaTrancisionDisparada, N_transiciones;
	private int inv1;
	private int inv2;
	private int inv3;
	private Info[] Transiciones;
	private PrintWriter pw, registro_disparo;
	private ArrayList<Integer> Valores_Aleatorio;
	private Random random;
	private static int[][] invariantes = { { 1, 2, 4, 6 }, // Invariante 1
			{ 1, 3, 5, 6 }, // Invariante 2
			{ 7, 8, 9, 10 } };// Invariane 3
	// private boolean aleatorio;

	public Politica(PrintWriter pw, Matriz Intervalo, int numero_Transiciones, PrintWriter registro_disparo) {
		Valores_Aleatorio = new ArrayList<>();
		ultimaTrancisionDisparada = 0;
		inv1 = 0;
		inv2 = 0;
		inv3 = 0;
		// aleatorio = true;
		disparos = new ArrayList<Integer>(Collections.nCopies(10, 0));
		this.Intervalo = Intervalo;
		Transiciones = new Info[numero_Transiciones];
		this.pw = pw;
		this.registro_disparo = registro_disparo;
		N_transiciones = numero_Transiciones;
		for (int i = 0; i < numero_Transiciones; i++) {
			Transiciones[i] = new Info(i, 0, pertence(i));
		}
		random = new Random();
	}

	/**
	 * 
	 * @param transicion
	 * @return retorna a que invariante pertenece la transicion ingresa por
	 *         parametro
	 */
	private int pertence(int transicion) {

		int retorna = -1;
		for (int i = 0; i < invariantes.length; i++) {

			for (int j = 0; j < (invariantes[i].length); j++) {
				if (invariantes[i][j] == (transicion + 1)) {
					return i;
				}
			}
		}
		return retorna;
	}

	/**
	 * Este metodo registra los disparos de las transiciones
	 * 
	 * @param nTransicion
	 */
	public void registrarDisparo(int nTransicion) {

		if (nTransicion == 0) {// T1
			if (inv1 == inv2)
				Transiciones[0].setInvariante(aleatorio());
			else if (inv1 > inv2)
				Transiciones[0].setInvariante(1);
			else
				Transiciones[0].setInvariante(0);
		}
		if (nTransicion == 3) {// T4
			ultimaTrancisionDisparada = 3;
		}
		if (nTransicion == 4) { // T5
			ultimaTrancisionDisparada = 4;
		}
		if (nTransicion == 9) {// T10
			inv3 = inv3 + 1;
			actualizar_invariante(2);
		}
		if (nTransicion == 5) {// T6
			if (ultimaTrancisionDisparada == 3) { // T4 --> T6
				inv1 = inv1 + 1;
				actualizar_invariante(0);
			} else if (ultimaTrancisionDisparada == 4) { // T5 --> T6

				inv2 = inv2 + 1;
				actualizar_invariante(1);
			}
		}
		disparos.set(nTransicion, (disparos.get(nTransicion) + 1));
	}

	/**
	 * Este metodo retorna la transicion a disparar de manera balanceada.
	 * 
	 * @param m = transiciones sesnsibilizadas y que estan en la cola.
	 * @return -1 -> si la ocurrio un error transicion_a_disparar -> Si se
	 *         selecciono una transicion para disparar.
	 */
	public int cual(Matriz m) {
		int cantidad = 0;
		int transicion_a_disparar = -1;
		for (int i = 0; i < N_transiciones; i++) {
			if (m.getDato(i, 0) == 1) {
				cantidad++;
				transicion_a_disparar = i;
			}
		}
		// SI HAY MAS DE UNA TRANSICION EN "m".
		if (cantidad > 1) {
			for (int i = 0; i < N_transiciones; i++) {

				if (m.getDato(i, 0) == 1) {
					Valores_Aleatorio.add(i); // Se agregan al array.
				}
			}
			transicion_a_disparar = Valores_Aleatorio.get(random.nextInt(Valores_Aleatorio.size()));
		}

		return transicion_a_disparar;
	}

	private void actualizar_invariante(int invariante) {
		if (invariante == 0) {// Invariante 1
			Transiciones[1].setcantInvariante(this.inv1);// T2
			Transiciones[3].setcantInvariante(this.inv1);// T4
		}
		if (invariante == 1) {// Invariante 2
			Transiciones[2].setcantInvariante(inv2);// T3
			Transiciones[4].setcantInvariante(inv2);// T5
		}

		if (invariante == 2) {// Invariante 3
			Transiciones[6].setcantInvariante(this.inv3);// T7
			Transiciones[7].setcantInvariante(this.inv3);// T8
			Transiciones[8].setcantInvariante(this.inv3);// T9
		}
	}

	private int aleatorio() {
		int valor = (int) (Math.random() * 2);
		return valor;
	}

	public void imprimir(Log log) {
		int tiempo_inv1 = Intervalo.getDato(0, 0) + Intervalo.getDato(0, 1) + Intervalo.getDato(0, 3)
				+ Intervalo.getDato(0, 5);
		int tiempo_inv2 = Intervalo.getDato(0, 0) + Intervalo.getDato(0, 2) + Intervalo.getDato(0, 4)
				+ Intervalo.getDato(0, 5);
		int tiempo_inv3 = Intervalo.getDato(0, 6) + Intervalo.getDato(0, 7) + Intervalo.getDato(0, 8)
				+ Intervalo.getDato(0, 9);
		// System.out.println("Intervalo 3 "+ tiempo_inv3 );
		log.registrarDisparo("=====================================");
		log.registrarDisparo("Invariante " + 1 + ": " + inv1 + " veces  [T1 T2 T4 T6] Tiempo:[" + tiempo_inv1 + "]"
				+ (inv1 * tiempo_inv1) / 1000);
		log.registrarDisparo("Invariante " + 2 + ": " + inv2 + " veces  [T1 T3 T5 T6] Tiempo:[" + tiempo_inv2 + "]"
				+ (inv2 * tiempo_inv2) / 1000);
		log.registrarDisparo("Invariante " + 3 + ": " + inv3 + " veces  [T7 T8 T9 T10] Tiempo:[" + tiempo_inv3 + "]"
				+ (inv3 * tiempo_inv3) / 1000);
		log.registrarDisparo("=====================================");
		for (int i = 0; i < disparos.size(); i++) {
			if (i == 6)
				log.registrarDisparo("Transicion: " + (i + 1) + " disparos: " + (disparos.get(i)));
			else
				log.registrarDisparo("Transicion: " + (i + 1) + " disparos: " + disparos.get(i));
		}
	}
}
