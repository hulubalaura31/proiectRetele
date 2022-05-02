package common;

public class Room {

	private int idCamera;
	private String status;
	private String reservedOnName;
	private boolean withTowels;

	public Room(int idCamera) {
		super();
		this.idCamera = idCamera;
		this.status = "Available";
		this.reservedOnName = "";
		this.withTowels = false;
	}

	public int getIdCamera() {
		return idCamera;
	}

	public void setIdCamera(int idCamera) {
		this.idCamera = idCamera;
	}

	public boolean isWithTowels() {
		return withTowels;
	}

	public void setWithTowels(boolean withTowels) {
		this.withTowels = withTowels;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getReservedOnName() {
		return reservedOnName;
	}

	public void setReservedOnName(String reservedOnName) {
		this.reservedOnName = reservedOnName;
	}

	@Override
	public String toString() {
		if (withTowels) {
			return "status=" + status + ", reservedOnName=" + reservedOnName + ", wants towels=" + withTowels;
		} else {
			return "status=" + status + ", reservedOnName=" + reservedOnName;
		}
	}

}
