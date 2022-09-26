package log;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import codigo.Monitor;

public class Logger implements Runnable {

	private PrintWriter pw, registro_disparo;
	private FileWriter archivo1, archivo2;
	private static Log reporte;
	private Monitor monitor;

	public Logger(Monitor monitor, Log reporte, PrintWriter pw, PrintWriter registro_disparo, FileWriter archivo1,
			FileWriter archivo2) {
		this.pw = pw;
		this.registro_disparo = registro_disparo;
		this.archivo1 = archivo1;
		this.archivo2 = archivo2;
		this.monitor = monitor;
		this.reporte = reporte;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		while (true) {
		}
	}

	public void imprimir() {
		// pw.println("* FIN");
		monitor.imprimir(reporte);
		try {
			archivo1.close();
			archivo2.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
