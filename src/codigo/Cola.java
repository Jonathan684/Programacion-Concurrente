package codigo;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Semaphore;

public class Cola {
	//Campos
	private Set<Integer> transicionesEsperando;
	private int cantTransiciones;
	private Semaphore [] semaforos;
	
	/**
	 * Constructor de la clase Cola que inicia un semaforo para cada elemento
	 * que se encuentre en la misma, de esta manera el hilo se queda esperando en la cola.
	 * @param cantTransiciones Cantidad de transiciones para armar vector Vc
	 */
	public Cola(int cantTransiciones) {
		this.cantTransiciones = cantTransiciones;
		//transicionesEsperando = new ArrayList<>();
		transicionesEsperando = new TreeSet<>();
		semaforos = new Semaphore[cantTransiciones]; 
		for (int i = 0; i < cantTransiciones; i++) {
			semaforos[i] = new Semaphore(0);
        }
		
     }
	/**
	 * Metodo que debe devolver el vector con los hilos que estan en cola 
	 * @return Vc matriz con los hilos que esperan 
	 */
	public Matriz quienesEstan(){
		Matriz Vc = new Matriz(cantTransiciones,1);
		for(Integer transicion : transicionesEsperando){
			Vc.setDato(transicion, 0, 1);
		}
		
		return Vc;
	}
	
	/**
	 * Metodo que debe poner en cola el hilo en una ubicacion determinada para esa transicion
	 * @param transicion transicion que intento realizar el disparo
	 */
	 
	public boolean ponerEnCola(Integer transicion) {
		boolean agregado;
		agregado = transicionesEsperando.add(transicion);
		//agregado = transicionesEsperando.add(3);
		//System.out.println(agregado);
		return agregado;
	}
	public String imprimirCola(){
		String esperando = "Esperando: ";
		
		for(Integer transicion : transicionesEsperando){
			esperando += " T"+(transicion+1);
		}
		return esperando;
	}
    
	public int Tamanio(){
		return transicionesEsperando.size();
	}
	public boolean isVacia(){
		return transicionesEsperando.isEmpty();
	}
	
	public void poner_EnCola(int Transicion) {
		   
		//System.out.println("Se pone en la cola "+ (Transicion+1));
		//transicionesEsperando.add(Transicion);
		if(semaforos[Transicion]!=null) {
			
			try {
					semaforos[Transicion].acquire(); //se queda esperando
			}
			catch(InterruptedException e){
				//System.out.println("Error al intentar poner en cola");
				Thread.currentThread().interrupt(); 
				//e.printStackTrace();
				
			}
		}	
	}
	public void sacar_de_Cola(int nTransicion) {
		transicionesEsperando.remove(nTransicion);
		if(semaforos[nTransicion]!=null){
		 semaforos[nTransicion].release();
		}
    }
}

