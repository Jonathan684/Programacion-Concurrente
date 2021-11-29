package codigo;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Main {

	
	private static final int numeroHilos = 4;
	private static int[][] secComunes = {{1,6}};//,{6}};
	private static int[][] secInvariante = {{2,4},{3,5},{7,8,9,10}};
	private static Hilo[] hilos;
	private static Thread[] threads;
	//private static int SEGUNDOS = 0;
	private static final int tiempoCorrida = (180000); // 60000; //milisegundos
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
		hilos[1] = new Hilo(monitor, secInvariante[0]);	//T2,T4
	    hilos[2] = new Hilo(monitor, secInvariante[1]);	//T3,T5
		//hilos[3] = new Hilo(monitor, secComunes[1]);	//T6
		hilos[3] = new Hilo(monitor, secInvariante[2]);	//T7,T8,T9,T10 
		//hilos[5] = new Hilo(monitor, secInvariante[2]);	//T7,T8,T9,T10
		//hilos[6] = new Hilo(monitor, secInvariante[2]); //T7,T8,T9,T10
		
		for(int i=0; i<numeroHilos;i++) {
			threads[i] = new Thread(hilos[i], "" +i);
		}
		 for(int i=0; i<numeroHilos;i++) {
				threads[i].start();
	       }
       //----------------------------------------------------------------
     
     /*  threads[0].start();
       threads[1].start();
       threads[2].start();
       threads[3].start();
       threads[4].start();
       threads[5].start();
       threads[6].start();
       */
      /* 
       threads[0].start(); //T1
       try {
			Thread.sleep(5); //espero que termine el interrupt
		}
		catch(InterruptedException e) {
			e.printStackTrace();
			System.out.println("Error al intentar dormir el hilo principal");
		}
      //----------------------------------------------------------------       
       threads[2].start();//T2,T4
       try {
			Thread.sleep(5); //espero que termine el interrupt
		}
		catch(InterruptedException e) {
			e.printStackTrace();
			System.out.println("Error al intentar dormir el hilo principal");
		}
     //----------------------------------------------------------------
       threads[1].start();//T3,T5
       try {
			Thread.sleep(10); //espero que termine el interrupt
		}
		catch(InterruptedException e) {
			e.printStackTrace();
			System.out.println("Error al intentar dormir el hilo principal");
		}
     //----------------------------------------------------------------
       threads[3].start();//T6
      
       */
       
      
       
	    
	     
	     try {
				Thread.sleep(tiempoCorrida);
			}
			catch(InterruptedException e) {
				e.printStackTrace();
				System.out.println("Error al intentar dormir el hilo principal");
			}
	     DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
	     log2.registrarDisparo(dtf.format(LocalDateTime.now()),1);
		
			for(int k=0;k<numeroHilos;k++) {
				hilos[k].set_Fin();;
			}
			for(int k=0;k<numeroHilos;k++) {
				threads[k].interrupt();
			}
			try{
				Thread.sleep(100); //espero que termine el interrupt
			}
			catch(InterruptedException e) {
				e.printStackTrace();
				System.out.println("Error al intentar dormir el hilo principal");
			}
			 log2.registrarDisparo("\n************************ Fin ****************************",1);
			//politica.imprimirVecesPorInvariante();
			log.registrarDisparo("Tiempo de ejecucion : "+(tiempoCorrida/1000)+"seg.",1);
			politica.imprimir(log);
	//		System.out.println("FIN");
	}

}
