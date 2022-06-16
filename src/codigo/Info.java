package codigo;

public class Info {
	private int transicion;
	private int cant_disparos;
	private	int invariante;
	private int cant_invariante;
	public Info(int transicion,int cant_disparos, int invariante) {
		this.transicion = transicion; 
		this.cant_disparos=cant_disparos;
		this.invariante = invariante;
	}
	
	public int getCant_disparos() {
		return cant_disparos;
	}
	public void setCant_disparos(int cant_disparos) {
		this.cant_disparos = cant_disparos;
	}
	public void setInvariante(int invariante) {
		this.invariante = invariante;
	}
	public int getInvariante() {
		return invariante;
	}
	public void setcantInvariante(int cant_invariante) {
		this.cant_invariante = cant_invariante;
	}
	public int getcantInvariante() {
		return cant_invariante;
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
