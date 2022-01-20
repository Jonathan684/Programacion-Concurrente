package codigo;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class Politica {
	//Campos

	private int[][] invariantes;
	private List<Integer> vecesPorInvariante;
	private List<Integer> disparos;
	private RDP red;
	
	public Politica(int[][] invariantes,RDP red){
		this.invariantes = invariantes;
		vecesPorInvariante = new ArrayList<>();
		disparos = new ArrayList<Integer>(Collections.nCopies(10, 0));
		this.red = red;
		for(int i = 0; i < invariantes.length; i++){
			vecesPorInvariante.add(0);
		}
	}
	/**
	 * Metodo que devuelve una transicion
	 * @param m matriz que contiene el resultado de Vc and Vs
	 * @return Transicion
	 */

	public int cual(Matriz m) {
		//T1 T2 T4 T6
		//T1 T2 T4 T6
		//T7 T8 T9 T10
		
		//m.getTranspuesta().imprimirMatriz();;
		
//		System.out.println("Total del invariante :"+vecesPorInvariante.toString());
//		System.out.println("Valor del primer invariante :"+vecesPorInvariante.get(0));
//		System.out.println(); 
		/*
		 * Si hay hay dos transiciones de distintos invaraiante
		 * se va a disparar la transicion del invariante con menor disparos
		 *  
		 */
		List<Integer> aux_1 =  new ArrayList<>();
		List<Integer> aux_2 =  new ArrayList<>();
		    int tmp;
			int k=0;
		    for(int i = 0 ; i<red.get_numero_Transiciones() ; i++)
		    {
		    	if(m.getDato(i, 0)==1)
		    	{
		    		aux_1.add(k,i);
		    		k++;
		    	}
		    }
//			System.out.println(aux_1.toString());
//			System.out.println(aux_1.get(0));
			tmp = disparos.get(aux_1.get(0));
			//System.out.println("Disparos :"+ disparos.toString()); 
			 
			 //disparos.get(aux_1.get(1));
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
//		    	System.out.println("retorno n==1 :"+ aux_2.get(0));
//		    	System.out.println("________________________________________________________\n");
		    	return aux_2.get(0);
		    }
		    else
		    {
		    	
		    	int number = (int) (Math.random() * n);
		    	//System.out.println("Size antes :"+aux_2.size());
		    	//System.out.println("Retorno :"+aux_2.get(number));
		    	//System.out.println("Size despues :"+aux_2.size());
		    	//System.out.println("retorno n!=1"+ aux_2.get(0));
		    	//System.out.println("________________________________________________________\n");
		    	return aux_2.get(number);
		    }
	   // return disp_transicion;
	}

	public void registrarDisparo(int nTransicion){ //No considera a T1 ni a T6
		if(nTransicion == 3) {
			//System.out.println("Se disparo primer invariante");
			vecesPorInvariante.set(0, (vecesPorInvariante.get(0)+1));
		}
		if(nTransicion == 4) {
			//System.out.println("Se disparo segundo invariante");
			vecesPorInvariante.set(1, (vecesPorInvariante.get(1)+1));
		}
		if(nTransicion == 9) {
			//System.out.println("Se disparo tercer invariante");
			vecesPorInvariante.set(2, (vecesPorInvariante.get(2)+1));
		}
//		if(nTransicion == 6) { //Cada disparo de T7 se cuenta doble para balancear.
//			disparos.set(nTransicion , (disparos.get(nTransicion))+2);	
//		}
		//else
		disparos.set(nTransicion , (disparos.get(nTransicion)+1));
		//System.out.println(disparos.toString());
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
   
	public void imprimir(Log log){
		//System.out.println("=================================");
		int j = 1;
		log.registrarDisparo("=====================================", 1);
		for(int veces: vecesPorInvariante){
			if(j==1) {
				//System.out.println("Invariante " + j + ": " + veces + " veces  [T1 T2 T4 T6] " );
				log.registrarDisparo("Invariante " + j + ": " + veces + " veces  [T1 T2 T4 T6] " , 1);
			}
			if(j==2) {
				//System.out.println("Invariante " + j + ": " + veces + " veces   [T1 T3 T5 T7]" );
				log.registrarDisparo("Invariante " + j + ": " + veces + " veces  [T1 T3 T5 T6]" , 1);
			}
			if(j==3) {
				//System.out.println("Invariante " + j + ": " + veces + " veces  [T7 T8 T9 T10]" );
				log.registrarDisparo("Invariante " + j + ": " + veces + " veces  [T7 T8 T9 T10]" , 1);
			}
			
			j++;
		}
		log.registrarDisparo("=====================================", 1);
	   for(int i=0;i< disparos.size();i++) {
		//System.out.println("Transicion: "+(i+1)+ " disparos: "+disparos.get(i));
		   if(i==6)log.registrarDisparo("Transicion: "+(i+1)+ " disparos: "+(disparos.get(i)), 1); 		   
		   else	 log.registrarDisparo("Transicion: "+(i+1)+ " disparos: "+disparos.get(i), 1);
	   }
	   // System.out.println("Mayor " + Collections.max(disparos)) ;
	}
	
}
/*
 *   List<Integer> aux_1 =  new ArrayList<>();
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
	    	//System.out.println("Size antes :"+aux_2.size());
	    	//System.out.println("Retorno :"+aux_2.get(number));
	    	//System.out.println("Size despues :"+aux_2.size());
	    	return aux_2.get(number);
	    }
*/