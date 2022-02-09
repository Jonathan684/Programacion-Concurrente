package codigo;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import hilos.Hilo;
import log.Log;

public class Main {

	private static final int numeroHilos = 7;
	private static final int tiempoCorrida = 220000;//milisegundos
	
	private static int[] T1 = { 1 };
	private static int[] T2_T4 = { 2, 4 };
	private static int[] T3_T5 = { 3, 5 };
	private static int[] T6 = { 6 };
	private static int[] T7_T8_T9_T10 = { 7, 8, 9, 10 };
	private static Hilo[] hilos;
	private static Thread[] threads;
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
		
		//mutex = new Mutex();
		Log log2 = new Log(REPORT_FILE_NAME_2);
		redDePetri = new RDP(log2);
		
		Monitor monitor = new Monitor(mutex, redDePetri,log2);
		hilos = new Hilo[numeroHilos];
		
		hilos[0] = new Hilo(monitor, T1);	//T1
		hilos[1] = new Hilo(monitor, T2_T4);	//T2,T4
	    hilos[2] = new Hilo(monitor, T3_T5);	//T3,T5
		hilos[3] = new Hilo(monitor, T6);	//T6
		hilos[4] = new Hilo(monitor, T7_T8_T9_T10 );	//T7,T8,T9,T10 
		hilos[5] = new Hilo(monitor, T7_T8_T9_T10 );	//T7,T8,T9,T10
		hilos[6] = new Hilo(monitor, T7_T8_T9_T10 ); //T7,T8,T9,T10
		
		threads = new Thread[numeroHilos];
		for(int i=0; i<numeroHilos;i++)threads[i] = new Thread(hilos[i], "" +i);
		
		for(Thread T : threads)T.start();
		
		try {
			Thread.sleep(tiempoCorrida);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		for(Hilo h:hilos)h.set_Fin();
		monitor.vaciarcolas();
		for(Thread t : threads)t.interrupt();
		
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
		log2.registrarDisparo(dtf.format(LocalDateTime.now()), 1);
		
//		for (Thread k : threads) {
//			try {
//				k.join();
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
		
		log2.registrarDisparo("\n************************ Fin **************************** Tiempo :" + System.currentTimeMillis(), 1);
		log.registrarDisparo("Tiempo de ejecucion : " + (tiempoCorrida / 1000) + "seg.", 1);
		monitor.imprimir(log);
	}
}
