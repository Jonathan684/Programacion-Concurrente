package codigo;

public class Info {
	private int transicion;
	private int cant_disparos;
	public Info(int transicion,int cant_disparos) {
		this.transicion = transicion; 
		this.cant_disparos=cant_disparos;
		}
	public int getCant_disparos() {
		return cant_disparos;
	}
	public void setCant_disparos(int cant_disparos) {
		this.cant_disparos = cant_disparos;
	}
	public int get_transicion() {
		return transicion;
	}
	public void set_transicion(int transicion) {
		this.transicion = transicion;
	}
	public void update(int transicion,int cantidad) {
		this.transicion = transicion;
		this.cant_disparos = cantidad;
	}
	
}
