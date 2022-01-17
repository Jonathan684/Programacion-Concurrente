package codigo;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Main {

	private static final int numeroHilos = 7;
	private static int[][] secComunes = { { 1 }, { 6 } };
	private static int[][] secInvariante = { { 2, 4 }, { 3, 5 }, { 7},{ 8, 9, 10 } };
	private static Hilo[] hilos;
	private static Hilo_Ingreso_1 hilo_Ingreso;
	private static Hilo_Ingreso_2 hilo_Ingreso_2;
//	private static Hilo_Ingreso_2 hilo_Ingreso_3;
//	private static Hilo_Ingreso_2 hilo_Ingreso_4;
	private static Thread[] threads;
	private static final int tiempoCorrida = 1000; // milisegundos
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
		//hilos = new Hilo[numeroHilos];
		hilos = new Hilo[5];
		mutex = new Mutex();
		Log log2 = new Log(REPORT_FILE_NAME_2);
		redDePetri = new RDP(mutex, log2);
		Politica politica = new Politica(secInvariante, redDePetri);
		Monitor monitor = new Monitor(mutex, redDePetri, politica, log2);

		threads = new Thread[numeroHilos];

		hilo_Ingreso = new Hilo_Ingreso_1(monitor, secComunes[0]); // T1
		
		hilo_Ingreso_2 = new Hilo_Ingreso_2(monitor, secInvariante[2]); // T7
		//hilo_Ingreso_3 = new Hilo_Ingreso_2(monitor, secInvariante[3]); // T7,T8,T9,T10
		//hilo_Ingreso_4 = new Hilo_Ingreso_2(monitor, secInvariante[3]); // T7,T8,T9,T10
		
		
		System.out.println("Valor de la secuencia:" + secComunes[1][0]);
		hilos[0] = new Hilo(monitor, secComunes[1]); // T6
		hilos[1] = new Hilo(monitor, secInvariante[0]); // T2,T4
		hilos[2] = new Hilo(monitor, secInvariante[1]); // T3,T5
		hilos[3] = new Hilo(monitor, secInvariante[3]);
		hilos[4] = new Hilo(monitor, secInvariante[3]);
		//hilos[3] = new Hilo(monitor, secInvariante[2]); // T7,T8,T9,T10
		//hilos[4] = new Hilo(monitor, secInvariante[2]); // T7,T8,T9,T10
		//hilos[5] = new Hilo(monitor, secInvariante[2]); // T7,T8,T9,T10
		
//		for (int i = 0; i < 3 ; i++) {
//		threads[i] = new Thread(hilos[i], "" + i);
//		}
		
		threads[0] = new Thread(hilos[0], "" + 0);
		threads[1] = new Thread(hilos[1], "" + 1);
		threads[2] = new Thread(hilos[2], "" + 2);
		
		threads[3] = new Thread(hilos[3], "" + 3);
	    threads[4] = new Thread(hilos[4], "" + 4);
		threads[5] = new Thread(hilo_Ingreso_2, "" + 5);
	    
		threads[6] = new Thread(hilo_Ingreso, "" + 6);
		
		threads[0].start();
		threads[1].start();
		threads[2].start();
		threads[3].start();
		threads[4].start();
		threads[5].start();
		threads[6].start();
//		hilos[3] = new Hilo(monitor, secInvariante[2]);	//T7,T8,T9,T10 
//		hilos[4] = new Hilo(monitor, secInvariante[2]); //T7,T8,T9,T10
//		hilos[5] = new Hilo(monitor, secInvariante[2]); //T7,T8,T9,T10

		
		
		
//		for (int i = 0; i < numeroHilos - 1; i++) {
//			threads[i] = new Thread(hilos[i], "" + i);
//		}
//
//		for (int i = 0; i < numeroHilos; i++) {
//			threads[i].start();
//		}

		try {
			Thread.sleep(tiempoCorrida);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
		log2.registrarDisparo(dtf.format(LocalDateTime.now()), 1);

		for (int k = 0; k < numeroHilos - 2; k++) {
			hilos[k].set_Fin();
			threads[k].interrupt();
		}
		
		hilo_Ingreso_2.set_Fin();
		threads[5].interrupt();
//		hilo_Ingreso_3.set_Fin();
//		threads[4].interrupt();
//		hilo_Ingreso_4.set_Fin();
//		threads[5].interrupt();
		hilo_Ingreso.set_Fin();
		threads[6].interrupt();

		log2.registrarDisparo("\n************************ Fin ****************************", 1);
		log.registrarDisparo("Tiempo de ejecucion : " + (tiempoCorrida / 1000) + "seg.", 1);
		politica.imprimir(log);
	}

}
