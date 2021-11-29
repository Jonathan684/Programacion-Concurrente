package codigo;


import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Semaphore;


public class Monitor {

	//Campos
	private RDP red;
	private Cola cola; 		//cola donde se pondran los hilos
	private Politica politica;
	private Log log;
	private Log consola;
	private int nTransicion;
	private final String REPORT_FILE_NAME_1 = "Python/log.txt";
	private Mutex mutex;
	private Matriz m;
	private boolean k;
    /**
	 * Constructor de la clase Monitor
	 * @param mutex 
	 */
	public Monitor(Mutex mutex, RDP red,Politica politica,Log consola) {
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
		consola.registrarDisparo(dtf.format(LocalDateTime.now()),1);
		consola.registrarDisparo("**************************************************",1);
		consola.registrarDisparo("*        COMIENZO DEL MONITOR                    *",1);
		consola.registrarDisparo("**************************************************\n",1);
		consola.registrarDisparo("** Informe de los disparos **",1);
		this.mutex = mutex;
		this.red = red; 										//la red sobre la cual se trabajara
		this.politica = politica;
		this.log = new Log(REPORT_FILE_NAME_1);
		this.consola = consola;
		cola = new Cola(red.get_numero_Transiciones());
		nTransicion = 0;
		//mutex = new Semaphore(1,true);						//el semaforo que se utilizara, solo uno puede entrar y es justo.
		red.sensibilizar();
		k = true;
		//System.out.println("Inicio");
	}
	public boolean dispararTransicion(int T_Disparar) {
	    
		mutex._acquire();
	    k = true;
	    consola.registrarDisparo("*** "+red.Marcado(red.getVectorMA()),1);
		consola.registrarDisparo("*** "+red.sensibilidadas(red.getVectorExtendido()),1);// +"Disparo "+(T_Disparar+1),2);
		
	   // consola.registrarDisparo("Inicio ->>"+red.Marcado(red.getVectorMA()),1);
	   // consola.registrarDisparo("Inicio ->> "+red.sensibilidadas(red.getVectorExtendido()),1);// +"Disparo "+(T_Disparar+1),2);
		//red.mostrar(red.getVectorExtendido(), 0);
		//red.mostrar(red.getVectorMA(), 1);
	    while(k){
			
	    	//System.out.println("Dentro de monitor -------------------> T"+(T_Disparar+1));
	    	
			consola.registrarDisparo("\n* Dentro del monitor T"+ (T_Disparar+1),1);//+" Hilo: "+Thread.currentThread().getName(),1);
            consola.registrarDisparo(cola.imprimirCola(),1);
			k = red.Disparar(T_Disparar);// Hilo "+ Thread.currentThread().getName()
			//System.out.println("Valor de k en el monitor:"+k+"  t"+(T_Disparar+1)+"\n");
			if(k) // k = true
			{ 
				consola.registrarDisparo("* Se disparo: T"+ (T_Disparar+1),1);
				consola.registrarDisparo("* "+red.Marcado(red.getVectorMA()),1);
				consola.registrarDisparo("* "+red.sensibilidadas(red.getVectorExtendido()),1);// +"Disparo "+(T_Disparar+1),2);
				//consola.registrarDisparo("->>"+red.Marcado(red.getVectorMA()),1);
			    //consola.registrarDisparo("->> "+red.sensibilidadas(red.getVectorExtendido()),1);// +"Disparo "+(T_Disparar+1),2);
				
				politica.registrarDisparo(T_Disparar); 
				if((T_Disparar+1) == 10)
					log.registrarDisparo("T"+0,0);
				else 	log.registrarDisparo("T"+(T_Disparar+1),0);
	    	   
				
				m = calcularVsAndVc();
				if (m.esNula())
			    {
					k = false;//No hay hilos con transiciones esperando para disparar y que esten sensibilidas
			    	consola.registrarDisparo("* m: nula",1);
					consola.registrarDisparo("* k: "+ k,1);
					
			    }
				else
				{       //Transiciones que estaban en la cola ahora pueden disparar
					    consola.registrarDisparo("* m: no es nula",1);
						nTransicion = politica.cual(m); //De las transiciones sensibilizadas que estan en la cola, cual deberia disparar?
						consola.registrarDisparo("* Se saca de la cola: T"+(nTransicion+1),1);
						//System.out.println(" Se saca de la cola::::::::::::> T"+(nTransicion+1));
						//nTransicion = red.buscar_en_posicion(T_Disparar);
						cola.sacar_de_Cola(nTransicion);//Ya no esta esperando. Release a la cola
					    //consola.registrarDisparo("* Valor de k antes : " + k ,1);
					    return true; //Sale del monitor
				}
			   
			    
			}
			else
			{   
				consola.registrarDisparo("* Encolar: T"+ (T_Disparar+1),1);
				boolean agregado = false;
				//T_Disparar = red.buscar_en_posicion(T_Disparar);
			    //System.out.println("Buscar en posicion retorna"+T_Disparar);
				agregado = cola.agregar(T_Disparar); // Se agrega al Vc
				consola.registrarDisparo("* Valor de agregar: "+ agregado,1);
				if(true)
				{
					mutex._release();
					cola.poner_EnCola(T_Disparar); //Desde aca continua el Hilo dormido
												   //Valor de k=true es el mismo que tenia el hilo que lo desperto 
					                                //consola.registrarDisparo("* Valor de k despues: " + k ,1);
				}		
		    }
		}
		    //System.out.println("Saliendo : " + Thread.currentThread().getName());
		    //consola.registrarDisparo("* Saliendo: " + (T_Disparar+1) ,1);
		    mutex._release();
            return true;
	}
	/**
	 * Metodo que realiza la operacion And entre Vs y Vc, luego 
	 * se examina la matriz resultante y devuelve 1 o 0
	 * @return 
	 */
	public Matriz calcularVsAndVc(){
		
		//System.out.println(":::::::::::::::::Metodo calcular:::::::::::::::::");
		//red.getVectorExtendido().getTranspuesta().imprimirMatriz();
		
		//System.out.println("::::::::::::::::::::::::::::::::::");
		//System.out.println(cola.imprimirCola());
		//consola.registrarDisparo("* "+red.sensibilidadas(red.getVectorExtendido()),1);
		Matriz Vs = red.getVectorExtendido();
		Matriz Vc = cola.quienesEstan();
		Matriz m = Vs.getAnd(Vc);
		//m.getTranspuesta().imprimirMatriz();
		//System.out.println("|||||||||||||||||||||||||||||||||||||||");
		return m;
	}
	

}
