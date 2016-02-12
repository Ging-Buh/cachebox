package CB_Core.Api;

public class ApiGroundspeakResult {
	private int result;
	private String message;

	public ApiGroundspeakResult(int result, String message) {
		this.setResult(result);
		this.setMessage(message);
	}

	public int getResult() {
		return result;
	}

	public void setResult(int result) {
		this.result = result;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	@Override
	public String toString() {
		return "ApiGroundspeakResult: " + result + " - " + message;
	}
}
