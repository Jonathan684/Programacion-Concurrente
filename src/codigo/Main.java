package codigo;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import hilos.kuka_0;
import hilos.kuka_1;
import hilos.kuka_2;
import hilos.kuka_3;
import hilos.kuka_4_5_6;
import log.Log;

public class Main {

	private static final int numeroHilos = 7;
	
	private static int[] T1 = { 1 };
	private static int[] T2_T4 = { 2, 4 };
	private static int[] T3_T5 = { 3, 5 };
	private static int[] T6 = { 6 };
	private static int[] T7_T8_T9_T10 = { 7, 8, 9, 10 };
	private static int[][] secInvariante = {{2,4},{3,5},{7,8,9,10}};
	
	private static kuka_0 hilo_Ingreso_1;
	private static kuka_1 Invariante_1;
	private static kuka_2 Invariante_2;
	private static kuka_3 Salida;
	private static kuka_4_5_6 Invariante_3_0;
	private static kuka_4_5_6 Invariante_3_1;
	private static kuka_4_5_6 Invariante_3_2;
	
	private static Thread[] threads;
	private static final int tiempoCorrida = 2000; // milisegundos
	private static RDP redDePetri;
	private static Mutex mutex;
	private final static String REPORT_FILE_NAME_3 = "Consola/Reporte.txt";
	private final static String REPORT_FILE_NAME_2 = "Consola/log.txt";
	private static Log log;

	public static void main(String[] args) {
		iniciarPrograma(); 
	}

	public static void iniciarPrograma() {

		log = new Log(REPORT_FILE_NAME_3);
		
		mutex = new Mutex();
		Log log2 = new Log(REPORT_FILE_NAME_2);
		redDePetri = new RDP(mutex, log2);
		Politica politica = new Politica(secInvariante, redDePetri);
		Monitor monitor = new Monitor(mutex, redDePetri, politica, log2);

		threads = new Thread[numeroHilos];
		
		hilo_Ingreso_1 = new kuka_0(monitor, T1); // T1 Tiempo en entrar una nueva pieza 10 ms
		Invariante_1 = new kuka_1(monitor, T2_T4); // T2,T4
		Invariante_2 = new kuka_2(monitor, T3_T5); // T3,T5
		Salida = new kuka_3(monitor, T6);
		Invariante_3_0 = new kuka_4_5_6(monitor, T7_T8_T9_T10);
		Invariante_3_1 = new kuka_4_5_6(monitor, T7_T8_T9_T10);
		Invariante_3_2 = new kuka_4_5_6(monitor, T7_T8_T9_T10);

		threads[0] = new Thread(hilo_Ingreso_1, "" + 0); // T1
		threads[1] = new Thread(Invariante_1, "" + 1); // T2 T4
		threads[2] = new Thread(Invariante_2, "" + 2); // T3 T5
		threads[3] = new Thread(Salida, "" + 3); // T6
		threads[4] = new Thread(Invariante_3_0, "" + 4); // T7 T8 T9 T10
		threads[5] = new Thread(Invariante_3_1, "" + 5); // T7 T8 T9 T10
		threads[6] = new Thread(Invariante_3_2, "" + 6); // T7 T8 T9 T10

		for (int k = 0; k < numeroHilos; k++) {
			threads[k].start();
		}

		try {
			Thread.sleep(tiempoCorrida);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		

		hilo_Ingreso_1.set_Fin();
		Invariante_1.set_Fin();
		Invariante_2.set_Fin();
		Salida.set_Fin();
		Invariante_3_0.set_Fin();
		Invariante_3_1.set_Fin();
		Invariante_3_2.set_Fin();
		for (int k = 0; k < numeroHilos; k++) {
			threads[k].interrupt();
		}
		
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
		log2.registrarDisparo(dtf.format(LocalDateTime.now()), 1);
		log2.registrarDisparo("\n************************ Fin ****************************", 1);
		
		log.registrarDisparo("Tiempo de ejecucion : " + (tiempoCorrida / 1000) + "seg.", 1);
		
		politica.imprimir(log);
	}

}
