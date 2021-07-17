package codigo;



import java.util.concurrent.Semaphore;

public class Monitor {

	//Campos
	private RDP red;
	private Cola cola; 		//cola donde se pondran los hilos
	private Politica politica;
	private Log log;
	private Log consola;
	private boolean retorna_del_sleep;
	private int nTransicion;
	private final String REPORT_FILE_NAME_1 = "Python/log.txt";
	private final String REPORT_FILE_NAME_2 = "Consola/log.txt";
	private Semaphore mutex;
	private Matriz m;
    private SensibilizadaConTiempo Temporizadas;

	/**
	 * Constructor de la clase Monitor
	 */
	public Monitor(RDP red,Politica politica) {
		System.out.println("COMIENZO DEL MONITOR");
		this.red = red; 										//la red sobre la cual se trabajara
		this.politica = politica;
		this.log = new Log(REPORT_FILE_NAME_1);
		this.consola = new Log(REPORT_FILE_NAME_2);
		cola = new Cola(red.getSensibilizadas().getNumFilas());
		nTransicion = 0;
		mutex = new Semaphore(1,true);						//el semaforo que se utilizara, solo uno puede entrar y es justo.
		red.sensibilizar();
		Temporizadas = new SensibilizadaConTiempo(red);
		retorna_del_sleep = false;
	}
	public void dispararTransicion(int siguienteEnHilo) {
		try{
				mutex.acquire();
			} catch (InterruptedException e) 
		    {
				e.printStackTrace();
			}
		boolean k = true;
		while(k){
			

		if(Temporizadas.Temporal_Sensibilizada(siguienteEnHilo))
		{//Retorna true si es temporal y esta sensibilizada
			Temporizadas.setNuevoTimeStamp(siguienteEnHilo);
			//System.out.println("notifica "+ (siguienteEnHilo+1));
			mutex.release();
			Temporizadas.esperar(siguienteEnHilo);
			//System.out.println("desperte "+ (siguienteEnHilo+1));
			try {
				mutex.acquire();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			retorna_del_sleep = true;
		}
    
	if(retorna_del_sleep)
	{
		//System.out.println("Viene de un sleep");
		//System.out.println("Hilo "+ Thread.currentThread().getName() +" "+ (siguienteEnHilo+1));
		retorna_del_sleep = false;
		Temporizadas.resetEsperando(siguienteEnHilo);
		//System.out.println("Despues de un sleep");
		red.sensibilizar();
		red.mostrar(red.getSensibilizadas(),0);
		//System.out.println("=========================");
	}       
	        red.mostrar(red.getSensibilizadas(),0);
	        red.mostrar(red.getVectorMA(), 2);
	        k = red.Disparar(siguienteEnHilo);// Hilo "+ Thread.currentThread().getName()
			System.out.println("#################################################  Disparo T"+ (siguienteEnHilo+1));
			if(k)
			{ 
				consola.registrarDisparo(red.sensibilidadas(red.getSensibilizadas()) +" Disparo "+(siguienteEnHilo+1),2);
			    consola.registrarDisparo(red.Marcado(red.getVectorMA()),2);//+"\n");
				politica.registrarDisparo(siguienteEnHilo); 
				
				if((siguienteEnHilo+1) == 10)
					log.registrarDisparo("T"+0,0);
				else 	log.registrarDisparo("T"+(siguienteEnHilo+1),0);
	    	
				m = calcularVsAndVc();
				if (m.esNula())
			    {
			    	k = false;  //No hay transiciones esperando para disparar
					//System.out.println("No hay transiciones compatibles en la cola");
					//System.out.println("Tam "+ cola.Tamanio());
					//System.out.println(cola.imprimirCola()); //Imprime cola de hilos esperando
				}
				else
				{ //Transiciones que estaban en la cola ahora pueden disparar
					    nTransicion = politica.cual(m); //De las transiciones sensibilizadas que estan en la cola, cual deberia disparar?
						//System.out.println("TRANSICION A DESPERTAR :::::::> " + (nTransicion+1));
						cola.sacar_de_Cola(nTransicion);//Ya no esta esperando
						k = false; //Para que salga del monitor
					
				}
			 }
			else
			{   // " + Thread.currentThread().getName() + " 
				System.out.println("Encolar Transicion "+ (siguienteEnHilo+1));
				//solo se agrega si no hay otro hilo esperando por la misma transicion
				boolean agregado;
				agregado = cola.ponerEnCola(siguienteEnHilo);
				if(agregado)
				{
				mutex.release();
				cola.poner_EnCola(siguienteEnHilo);
				try {
						mutex.acquire();
					} 
				catch (InterruptedException e)
					{
						e.printStackTrace();
					}
				k = true;
				//System.out.println("Hilo ->"+ Thread.currentThread().getName()  );
				}
				else k=false;
			}
				
			}
		    //System.out.println("Saliendo : " + Thread.currentThread().getName());
			mutex.release();
		}
	/**
	 * Metodo que realiza la operacion And entre Vs y Vc, luego 
	 * se examina la matriz resultante y devuelve 1 o 0
	 * @return 
	 */
	public Matriz calcularVsAndVc(){
		Matriz Vs = red.getSensibilizadas();
		Matriz Vc = cola.quienesEstan();
		Matriz m = Vs.getAnd(Vc);
		return m;
	}

}
