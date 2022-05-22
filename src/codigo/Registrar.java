package codigo;

import java.io.FileWriter;
import java.io.PrintWriter;

public class Registrar {

	
	public Registrar()
    {
        
     
    }
	public void write(String linea) {
		FileWriter fichero = null;
	    PrintWriter pw = null;
		   try
	        {
	            fichero = new FileWriter("Consola/log2.txt");
	            pw = new PrintWriter(fichero);

	            
	             pw.println(linea);

	        } catch (Exception e) {
	            e.printStackTrace();
	        } finally {
	           try {
	           // Nuevamente aprovechamos el finally para 
	           // asegurarnos que se cierra el fichero.
	           if (null != fichero)
	              fichero.close();
	           } catch (Exception e2) {
	              e2.printStackTrace();
	           }
	        }
	}
}
