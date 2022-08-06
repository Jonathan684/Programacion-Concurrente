package codigo;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import hilos.Hilo;
import log.Log;

public class Main {

	private static final int numeroHilos = 9;
	private static final int tiempoCorrida = 1600;//milisegundos
	private static int[] T1 = { 1 };
	private static int[] T2 = { 2 };
	private static int[] T3 = { 3 };
	private static int[] T4 = { 4 };
	private static int[] T5 = { 5 };
	private static int[] T6 = { 6 };
	private static int[] T7_8_9_10 = { 7,8,9,10 };
	private static Hilo[] hilos;
	private static Thread[] threads;
	private static RDP redDePetri;
	private final static String REPORT_FILE_NAME_3 = "Consola/Reporte.txt";
	private final static String REPORT_FILE_NAME_2 = "Consola/log.txt";
	private static Log log;
	private static FileWriter fichero = null;
    private static PrintWriter pw = null;

	public static void main(String[] args) {
		iniciarPrograma(); 
	}

	public static void iniciarPrograma() {
		
		System.out.println("	=======================    ");
		System.out.println("	        TP final		   ");
		System.out.println("	=======================    ");
		
		log = new Log(REPORT_FILE_NAME_3);
		Log log2 = new Log(REPORT_FILE_NAME_2);
		redDePetri = new RDP(log2);
		try {
			fichero = new FileWriter("Consola/log2.txt");
		    } catch (IOException e1) {
			// TODO Auto-generated catch block
					e1.printStackTrace();
		  }
	            pw = new PrintWriter(fichero);

        Monitor monitor = new Monitor(pw,redDePetri);
		hilos = new Hilo[numeroHilos];

		hilos[0] = new Hilo(monitor, T1);	//T1
		hilos[1] = new Hilo(monitor, T2);	//T2
		hilos[2] = new Hilo(monitor, T3);
		hilos[3] = new Hilo(monitor, T6);
		hilos[4] = new Hilo(monitor, T4);
		hilos[5] = new Hilo(monitor, T5);
		hilos[6] = new Hilo(monitor, T7_8_9_10);
		hilos[7] = new Hilo(monitor, T7_8_9_10);
		hilos[8] = new Hilo(monitor, T7_8_9_10);
//		hilos[7] = new Hilo(monitor, T4);
//		hilos[8] = new Hilo(monitor, T5);
		

		
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
		//for(Thread t : threads)t.interrupt();
		
		
		pw.println("* Fin *\n");
		try {
			fichero.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
		System.out.println("	======== Fin ==========");
		log2.registrarDisparo("\n************************ Fin **************************** Tiempo :" + System.currentTimeMillis(), 1);
		log2.registrarDisparo(dtf.format(LocalDateTime.now()), 1);
		log.registrarDisparo("Tiempo de ejecucion : " + (tiempoCorrida / 1000) + "seg.", 1);
		monitor.imprimir(log);
	}
}