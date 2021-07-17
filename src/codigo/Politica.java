package codigo;



import java.util.*;


public class Politica {
	//Campos

	private int[][] invariantes;
	private List<Integer> vecesPorInvariante;
	private List<Integer> disparos;

	public Politica(int[][] invariantes){
		this.invariantes = invariantes;
		vecesPorInvariante = new ArrayList<>();
		disparos = new ArrayList<Integer>(Collections.nCopies(10, 0));
		for(int i = 0; i < invariantes.length; i++){
			vecesPorInvariante.add(0);
		}
	}
	/**
	 * Metodo que devuleve una transicion
	 * @param m matriz que contiene el resultado de Vc and Vs
	 * @return
	 */

	public int cual(Matriz m) {
	    List<Integer> aux_1 =  new ArrayList<>();
	    List<Integer> aux_2 =  new ArrayList<>();
	    int tmp;
		int k=0;
	    
		for(int i = 0 ; i<10 ; i++)
	    {
	    	if(m.getDato(i, 0)==1)
	    	{
	    		aux_1.add(k,i);
	    		k++;
	    	}
	    }
		tmp = disparos.get(aux_1.get(0));
	    for(int i = 0 ; i<aux_1.size();i++)
	    {
	    	if( disparos.get(aux_1.get(i)) < tmp)
	     	{
	     			tmp = disparos.get(aux_1.get(i));
	     	}
	    }
	    
	    for(int j = 0; j<aux_1.size();j++)
	    {
	    	if(tmp !=  disparos.get(aux_1.get(j)))
	    	{
	    		aux_1.set(j, -1);
	    	}
	    }
	    
	    for (Integer candidato : aux_1) {
		       if (candidato != -1) {
		          aux_2.add(candidato);
		       }
		    }
	    
	    for (Integer deleteCandidate : aux_2) {
		       aux_1.remove(deleteCandidate);
		    }
	    int n = aux_2.size();
	    if(n == 1)
	    {
	    	return aux_2.get(0);
	    }
	    else
	    {
	    	int number = (int) (Math.random() * n);
	    	return aux_2.get(number);
	    }
	}

	public void registrarDisparo(int nTransicion){ //No considera a T1 ni a T6
		/*for(int i = 0; i< invariantes.length; i++){
			for (int j = 0; j < invariantes[i].length; j++){
				if(invariantes[i][j] == nTransicion) {
					vecesPorInvariante.set(i,vecesPorInvariante.get(i) +1);
				}
			}
		}*/
		//Hardcodeado - ver de hacer alguna forma mas general, tipo lo de arriba pero que funcione bien
		//VecesPorInvariante contiene la cantidad de veces que se dispararon los invariantes
		if(nTransicion == 3) {
			System.out.println("Se disparo primer invariante");
			vecesPorInvariante.set(0, (vecesPorInvariante.get(0)+1));
		}
		if(nTransicion == 4) {
			System.out.println("Se disparo segundo invariante");
			vecesPorInvariante.set(1, (vecesPorInvariante.get(1)+1));
		}
		if(nTransicion == 9) {
			System.out.println("Se disparo tercer invariante");
			vecesPorInvariante.set(2, (vecesPorInvariante.get(2)+1));
		}
		
		disparos.set(nTransicion , (disparos.get(nTransicion)+1));
	}

	public int perteneceAInvariante(int transicion){
		for(int i = 0; i < invariantes.length; i++){
			for (int j = 0; j < invariantes[i].length; j++){
				if(invariantes[i][j] == transicion) {
					return i; //Devuelve el numero de invariante al que pertenece la transicion
				}
			}
		}
		return -1; //Si la transicion no pertenece a ningun invariante
	}
   
	public void imprimirVecesPorInvariante(){
		int i = 1;
		for(int veces: vecesPorInvariante){
			System.out.println("Invariante " + i + ": " + veces + " veces" );
			i++;
		}

	}
	public void imprimirDisparos(){
		System.out.println("=================================");
	   for(int i=0;i< disparos.size();i++)
		System.out.println("Transicion: "+(i+1)+ " disparos: "+disparos.get(i));
	   // System.out.println("Mayor " + Collections.max(disparos)) ;
	}
}
