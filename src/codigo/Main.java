package codigo;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Main {

	
	private static final int numeroHilos = 7;
	private static int[][] secComunes = {{1},{6}};
	private static int[][] secInvariante = {{2,4},{3,5},{7,8,9,10}};
	private static Hilo[] hilos;
	private static Thread[] threads;
	private static final int tiempoCorrida = 400; // milisegundos
	private static RDP redDePetri;
	private static Mutex mutex;
	private final static String REPORT_FILE_NAME_3 = "Consola/Reporte.txt";
	private final static String REPORT_FILE_NAME_2 = "Consola/log.txt";
	private static Log log;
	
	public static void main(String[] args) {
		iniciarPrograma();
	}
	
	public static void iniciarPrograma(){
		 
		log = new Log(REPORT_FILE_NAME_3);
		hilos = new Hilo[numeroHilos];
		mutex = new Mutex();
		Log log2 = new Log(REPORT_FILE_NAME_2);
        redDePetri = new RDP(mutex,log2);
        Politica politica = new Politica(secInvariante);
		Monitor monitor = new Monitor(mutex,redDePetri,politica,log2);

		threads = new Thread[numeroHilos];

		hilos[0] = new Hilo(monitor, secComunes[0]);	//T1
		hilos[1] = new Hilo(monitor, secComunes[1]);	//T6
		hilos[2] = new Hilo(monitor, secInvariante[0]);	//T2,T4
	    hilos[3] = new Hilo(monitor, secInvariante[1]);	//T3,T5
		hilos[4] = new Hilo(monitor, secInvariante[2]);	//T7,T8,T9,T10 
		hilos[5] = new Hilo(monitor, secInvariante[2]); //T7,T8,T9,T10
		hilos[6] = new Hilo(monitor, secInvariante[2]); //T7,T8,T9,T10
		
		
		
		for(int i=0; i<numeroHilos;i++) {
			threads[i] = new Thread(hilos[i], "" +i);}
		
		for(int i=0; i<numeroHilos;i++) {
				threads[i].start();}
		
		try {
				Thread.sleep(tiempoCorrida);
			}
		catch(InterruptedException e) {
			e.printStackTrace();
			}
        
	     DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
	     log2.registrarDisparo(dtf.format(LocalDateTime.now()),1);
		
		for(int k=0;k<numeroHilos;k++) {
			hilos[k].set_Fin();;
			threads[k].interrupt();
		}
		
		
		log2.registrarDisparo("\n************************ Fin ****************************",1);
		log.registrarDisparo("Tiempo de ejecucion : "+(tiempoCorrida/1000)+"seg.",1);
		politica.imprimir(log);
	}

}
