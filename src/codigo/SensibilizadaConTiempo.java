package codigo;
public class SensibilizadaConTiempo {
	
	private int cantidad_transiciones;
	private long timeStamp[]; // Primer vector de la tupla
	private int SetEsperando[]; //
	private Log consola;
	
	
     public SensibilizadaConTiempo(int cantidad_transiciones, Log consola2) {
		// TODO Auto-generated constructor stub
		// TODO Auto-generated constructor stub
				this.cantidad_transiciones = cantidad_transiciones;
				this.consola = consola2;
				timeStamp  = new long[cantidad_transiciones];
				SetEsperando = new int[cantidad_transiciones];
				for(int i = 0; i<timeStamp.length ; i++) {
					timeStamp[i] = 0;
					SetEsperando[i] = 0;
				}
	}

	public void setNuevoTimeStamp(int transicion) {
		
		if(timeStamp[transicion] == 0)
		{
			timeStamp[transicion] = System.currentTimeMillis();
			consola.registrarDisparo("* Timestamp - T"+ (transicion+1) +" - "+timeStamp[transicion],1);
		}
		/*else
		{
			System.out.println("La transicion "+ (transicion+1)+" ya tiene un timeStamp");
		}*/
	}
	
	/*public void esperar(int transicion) {
		try{//obtengo el valor en milisegundos que espera
			red.setEsperando(transicion);
			System.out.println("ahora"+System.currentTimeMillis());
			System.out.println("Tiempo para dormir :: "+transicion+" "+(red.Intervalo().getDato(1, transicion)+timeStamp[transicion]-System.currentTimeMillis()));
			Thread.sleep(red.Intervalo().getDato(1, transicion));
			
		} catch (InterruptedException e) {
		//e.printStackTrace();
		Thread.currentThread().interrupt(); 
		}
		//System.out.println("No Dormir");
	}*/
	/*public boolean Temporal_Sensibilizada(int transicion) {
	//	System.out.println("---> "+ transicion);
		/*
		 * Va preguntar si estoy despues de la ventana
		 * 
		 
		
		if ((es_temporal(transicion)==true) && (red.getVectorExtendido().getDato(transicion,0)==1)) {
			return true;
		}
		else return false;
	}*/

	public void resetEsperando(int transicion)
	{
	       	timeStamp[transicion] = 0;
	       	SetEsperando[transicion] = 0;
	       /*	System.out.println("RESETEO EL TIMESTAMP");
	       	 for(int i = 0; i<timeStamp.length ; i++) {
				System.out.print(timeStamp[i]+" ");
			}
	       	System.out.println();
	       	System.out.println("------------------");*/
	}
	/*
	 * Este metodo verifica si  se habilitaron transiciones temporales 
	 */
	public void T_temporales()
	{
		/*System.out.println("Inicio de la parte con tiempo");
		
		for(int i=0;i<10;i++) {
			if (Temporal_Sensibilizada(i) && (timeStamp[i] == 0)) { /*Se cumple que no tenia tiempo antes y esta sensibilizada con tiempo
			   System.out.println("Seteamos el TimeStamp:"+i);
			   setNuevoTimeStamp(i);
			}
		}
		Three_tupla();
		*/
	}
	/*
	 * Metodo que dice si el tiempo esta entre alfa y beta, es decir TimeStamp-ahora > alfa anda < a beta
	 */
	/*System.out.println("////////////////////////////////////");
	for(int j=0 ; j<cantidad_transiciones;j++)
	{
		System.out.print(Intervalo.getDato(0, j)+" ");
	}
	System.out.println();
	for(int i=0 ; i<cantidad_transiciones;i++)
	{
		System.out.print(Intervalo.getDato(1, i)+" ");
	}
	System.out.println();
	System.out.println("////////////////////////////////////");*/
	
	public boolean testVentana(int transicion,Matriz Intervalo)
	{
	if(timeStamp[transicion] != 0 )
		{
		    /*Pregunto si la diferencia entre en TimeStamp y ahora es menor que alfa*/
		int corrector = 1;	
		int alfa = Intervalo.getDato(0, transicion);
			int TimeStamp_ahora =(int)(System.currentTimeMillis()-timeStamp[transicion])+corrector; 
			consola.registrarDisparo("* Alfa: "+alfa+"ms",1);
			consola.registrarDisparo("* TimeStamp_ahora: "+TimeStamp_ahora+"ms",1);
			if((TimeStamp_ahora) >= alfa)
			{
				consola.registrarDisparo("* Estoy en la ventana de tiempo para dispararme ",1);
				return true;
			}
		}
		consola.registrarDisparo("* No estoy en la ventana de tiempo para dispararme : T"+(transicion+1),1);
			return false;
	}
	
	public long[] getTimeStamp() {return timeStamp; }
	
	
	public boolean antesDeLaVentana(int transicion,Matriz Intervalo) {
		int alfa = Intervalo.getDato(0, transicion);
		int beta = Intervalo.getDato(1, transicion);
		int TimeStamp_ahora =(int)(System.currentTimeMillis()-timeStamp[transicion]); 
		consola.registrarDisparo("* Alfa: "+alfa+"ms",1);
		consola.registrarDisparo("* TimeStamp_ahora: "+TimeStamp_ahora+"ms",1);
		if(TimeStamp_ahora <= alfa)
		{
			consola.registrarDisparo("* Estoy antes de la ventana. Hilo:"+ Thread.currentThread().getName(),1);
			return true;
		}
		consola.registrarDisparo("* No estoy antes de la ventana. Hilo: "+ Thread.currentThread().getName(),1);
		return false;
	}
	public void setEsperando(int transicion){
		/*System.out.print("\n_______________________\n");
		System.out.println("Vector Esperando");
		for(int k=0;k <SetEsperando.length;k++ ) {
			System.out.print(SetEsperando[k]+" ");
		}
		System.out.println();*/
		SetEsperando[transicion] = 1;
		/*for(int c :SetEsperando ) {
			System.out.print(c+" ");
		}
		System.out.println("\n_______________________\n");
		System.out.println();
	*/
	}
public boolean getEsperando(int transicion){
		
	/*System.out.println("Valor se set esperando");
	    for(int c : SetEsperando ) {
	    	System.out.print(c+" ");
	    }
		System.out.println();*/
		if(SetEsperando[transicion] == 1)
		{
			return true;
		}
		return false;
	
	}
		
}
