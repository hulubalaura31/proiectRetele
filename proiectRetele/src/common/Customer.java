package common;

public class Customer {
	private String name;
	private int idCamera;
	
	public Customer(String name) {
		super();
		this.name = name;
		this.idCamera = -1;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getIdCamera() {
		return idCamera;
	}
	public void setIdCamera(int idCamera) {
		this.idCamera = idCamera;
	}
	@Override
	public String toString() {
		return "name=" + name + " - idCamera=" + idCamera;
	}
	

}
