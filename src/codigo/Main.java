package codigo;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import hilos.Hilo;
import log.Log;
import log.Logger;

public class Main {

	private static final int numeroHilos = 7;
	private static final int tiempo_ejecucion = 15000;// milisegundos(200000 PARA 10000 DISPAROS)
	private static int[] T1 = { 1 };
	private static int[] T2_T4 = { 2, 4 };
	private static int[] T3_T5 = { 3, 5 };
	private static int[] T6 = { 6 };
	private static int[] T7_8_9_10 = { 7, 8, 9, 10 };
	private static Hilo[] hilos;
	private static Thread[] threads;
	private final static String REPORT_FILE_NAME = "Consola/Reporte.txt";
	private static Log reporte;
	private static Logger registrar;
	private static FileWriter archivo1 = null;
	private static PrintWriter pw = null;
	private static FileWriter archivo2 = null;
	private static PrintWriter registro_disparo = null;
	private static Thread info;

	public static void main(String[] args) {
		iniciarPrograma();
	}

	public static void iniciarPrograma() {

		System.out.println("	=======================    ");
		System.out.println("	        TP final		   ");
		System.out.println("	=======================    ");
		try {
			archivo1 = new FileWriter("Consola/consola.txt");
			archivo2 = new FileWriter("Python/log.txt");
			reporte = new Log(REPORT_FILE_NAME);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		pw = new PrintWriter(archivo1);
		pw.println("* INICIO");

		registro_disparo = new PrintWriter(archivo2);
		Monitor monitor = new Monitor(pw, registro_disparo, archivo1, archivo2);

		registrar = new Logger(monitor, reporte, pw, registro_disparo, archivo1, archivo2);
		hilos = new Hilo[numeroHilos];

		hilos[0] = new Hilo(monitor, T1); // T1
		hilos[1] = new Hilo(monitor, T2_T4); // T2 Y T4
		hilos[2] = new Hilo(monitor, T3_T5); // T3 Y T5
		hilos[3] = new Hilo(monitor, T6); // T6
		hilos[4] = new Hilo(monitor, T7_8_9_10);// T7 T8 T9 T10
		hilos[5] = new Hilo(monitor, T7_8_9_10);// T7 T8 T9 T10
		hilos[6] = new Hilo(monitor, T7_8_9_10);// T7 T8 T9 T10

		threads = new Thread[numeroHilos];
		for (int i = 0; i < numeroHilos; i++)
			threads[i] = new Thread(hilos[i], "" + i);

		info = new Thread();

		for (Thread T : threads)
			T.start();
		info.start();
		try {
			Thread.sleep(tiempo_ejecucion);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		for (Hilo H : hilos)
			H.set_Fin();

		registrar.imprimir();
		for (Thread t : threads)
			t.interrupt();
		info.interrupt();

//		for (Thread t : threads)
//			try {
//				t.join();
//				info.join();
//			} catch (InterruptedException e1) {
//				// TODO Auto-generated catch block
//				e1.printStackTrace();
//			}
		

		System.out.println("	=======================    ");
		System.out.println("	         Fin		   ");
		System.out.println("	=======================    ");
	}
}