package cb_rpc.Functions;

public class RpcAnswer_Error extends RpcAnswer {
	private static final long serialVersionUID = 3061996872508611838L;
	
	private String message;
	
	public RpcAnswer_Error(int result, String message) {
		super(result);
		this.message = message;
	}

	public String getMessage() {
		return message;
	}
}
