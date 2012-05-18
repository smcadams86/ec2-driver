package llc.rockford.webcast.worker;

public class WorkerResult {
	
	private String status;
	private String id;
	
	public WorkerResult(String status, String id) {
		this.status = status;
		this.id = id;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

}
