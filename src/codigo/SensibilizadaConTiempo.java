package codigo;
public class SensibilizadaConTiempo {
	
	private RDP red;
	private long SensibilizadaConTiempo[];
	public SensibilizadaConTiempo(RDP red) {
		// TODO Auto-generated constructor stub
		this.red = red;
		SensibilizadaConTiempo  = new long[red.get_numero_Transiciones()];
	}
    
	public boolean es_temporal(int transicion) {
		if((red.Intervalo().getDato(0, transicion)-red.Intervalo().getDato(1, transicion)) != 0)
	    {
	    	//System.out.println("Transicion : " + (transicion+1));
			return true;
	    }
	    return false;
	}
	
	public void setNuevoTimeStamp(int transicion) {
		
		SensibilizadaConTiempo[transicion] = System.currentTimeMillis();
		//System.out.println("Timestamp --> "+SensibilizadaConTiempo[transicion]);
	}
	
	public void esperar(int transicion) {
		try{//obtengo el valor en milisegundos que espera
			red.setEsperando(transicion); 
			Thread.sleep(red.Intervalo().getDato(1, transicion));
		} catch (InterruptedException e) {
		//e.printStackTrace();
		Thread.currentThread().interrupt(); 
		}
		//System.out.println("No Dormir");
	}
	public boolean Temporal_Sensibilizada(int transicion) {
	//	System.out.println("---> "+ transicion);
		if ((es_temporal(transicion)==true) && (red.getSensibilizadas().getDato(transicion,0)==1)) {
			return true;
		}
		else return false;
	}

	public void resetEsperando(int transicion) {
	       	red.resetEsperando(transicion);
	       	SensibilizadaConTiempo[transicion]=0;
	}
}
