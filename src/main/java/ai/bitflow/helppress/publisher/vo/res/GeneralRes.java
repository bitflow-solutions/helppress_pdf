package ai.bitflow.helppress.publisher.vo.res;

import lombok.Data;

@Data
public class GeneralRes {

	private int status = 200;
	private String error;
	
	public void setSuccessResponse() {
		this.status = 200;
	}
	
	public void setFailResponse() {
		this.status = 500;
	}
	
	public void setFailResponse(String error) {
		this.error = error;
	}

	public void setFailResponse(int status) {
		this.status = status;
	}
	
	public void setFailResponse(int status, String error) {
		this.status = status;
		this.error = error;
	}
	
}
