package codigo;



import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class Log {
    private FileWriter file;
    private PrintWriter pw;
  //PRIVATES VARIABLES
    //------------------------------------------------------------------------------------------------------------------
    private final String REPORT_FILE_NAME;
    Log(String REPORT_FILE_NAME){
        this.REPORT_FILE_NAME = REPORT_FILE_NAME;
        BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter(REPORT_FILE_NAME));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        try {
			bw.write("");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        try {
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    public void registrarDisparo(String cadena,int tipo) {
       if(tipo == 0)
    	   
       {
    	try {
            file = new FileWriter(REPORT_FILE_NAME, true);
            pw = new PrintWriter(file);
            pw.print(cadena);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (null != file) {
                    file.close();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
       }
       else
       {
    	   try {
               file = new FileWriter(REPORT_FILE_NAME, true);
               pw = new PrintWriter(file);
               pw.println(cadena);
           } catch (Exception e) {
               e.printStackTrace();
           } finally {
               try {
                   if (null != file) {
                       file.close();
                   }
               } catch (Exception e2) {
                   e2.printStackTrace();
               }
           }
       }
    }
}
