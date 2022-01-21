package codigo;

import log.Log;

public class SensibilizadaConTiempo {
	
	
	private long timeStamp[]; 
	private int SetEsperando[];
	private Log consola;
	
	public SensibilizadaConTiempo(int cantidad_transiciones, Log consola2) {
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
	}
	
	public void resetEsperando(int transicion)
	{
	       	timeStamp[transicion] = 0;
	       	SetEsperando[transicion] = 0;
	}
	/*
	 * Este metodo verifica si  se habilitaron transiciones temporales 
	 */
	
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
		//int beta = Intervalo.getDato(1, transicion);
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
		SetEsperando[transicion] = 1;
	}
	
	public boolean getEsperando(int transicion){
		
	if(SetEsperando[transicion] == 1)return true;
		
	return false;
	}
		
}
