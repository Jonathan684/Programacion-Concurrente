package codigo;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import hilos.Hilo;
import log.Log;

public class Main {

	private static final int numeroHilos = 10;
	//private static final int numeroHilos = 3;
	private static final int tiempoCorrida =4000;//milisegundos
	private static int[] T1 = { 1 };
	private static int[] T2 = { 2 };
	private static int[] T3 = { 3 };
	private static int[] T4 = { 4 };
	private static int[] T5 = { 5 };
	private static int[] T6 = { 6 };
	private static int[] T7 = { 7 };
	private static int[] T8 = { 8 };
	private static int[] T9 = { 9 };
	private static int[] T10 = { 10 };
	
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
		System.out.println("	=======================    ");
		System.out.println("	 Prueba piloto TP final	");
		System.out.println("	=======================   ");
		log = new Log(REPORT_FILE_NAME_3);
		
		//mutex = new Mutex();
		Log log2 = new Log(REPORT_FILE_NAME_2);
		redDePetri = new RDP(log2);
		
		Monitor monitor = new Monitor(mutex, redDePetri,log2);
		hilos = new Hilo[numeroHilos];

		hilos[0] = new Hilo(monitor, T1);	//T1
		hilos[1] = new Hilo(monitor, T2);	//T2
		hilos[2] = new Hilo(monitor, T3);	//T3
		hilos[3] = new Hilo(monitor, T4);	//T4
		hilos[4] = new Hilo(monitor, T5);	//T5
		hilos[5] = new Hilo(monitor, T6);	//T6
		hilos[6] = new Hilo(monitor, T7);	//T7
		hilos[7] = new Hilo(monitor, T8);	//T8
		hilos[8] = new Hilo(monitor, T9);	//T9
		hilos[9] = new Hilo(monitor, T10);	//T10
		
		threads = new Thread[numeroHilos];
		for(int i=0; i<numeroHilos;i++)threads[i] = new Thread(hilos[i], "" +i);
		
		for(Thread T : threads)T.start();
		
		try {
			Thread.sleep(tiempoCorrida);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		for(Hilo H:hilos)H.set_Fin();
		monitor.vaciarcolas();
		for(Thread t : threads)t.interrupt();
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
		System.out.println("Fin");
		log2.registrarDisparo("\n************************ Fin **************************** Tiempo :" + System.currentTimeMillis(), 1);
		log2.registrarDisparo(dtf.format(LocalDateTime.now()), 1);
		log.registrarDisparo("Tiempo de ejecucion : " + (tiempoCorrida / 1000) + "seg.", 1);
		monitor.imprimir(log);
	}
}