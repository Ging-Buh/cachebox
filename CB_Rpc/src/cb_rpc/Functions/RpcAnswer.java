package cb_rpc.Functions;

import java.io.Serializable;

public class RpcAnswer  implements Serializable {	
	private static final long serialVersionUID = -7038676849339519111L;
	
	private int result;
	
	public RpcAnswer(int result) {
		this.result = result;
	}

	public int getResult() {
		return result;
	}
	
	public String toString() {
		return "RpcAnswer: Result=" + result;
	}
}
