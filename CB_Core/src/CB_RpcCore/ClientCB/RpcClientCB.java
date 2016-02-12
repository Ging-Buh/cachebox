package CB_RpcCore.ClientCB;

import CB_Core.Types.ExportList;
import CB_RpcCore.Functions.RpcMessage_ExportChangesToServer;
import CB_RpcCore.Functions.RpcMessage_GetExportList;
import cb_rpc.Rpc_Client;
import cb_rpc.Functions.RpcAnswer;

public class RpcClientCB extends Rpc_Client {
	public RpcAnswer getExportList() {
		RpcMessage_GetExportList message = new RpcMessage_GetExportList();
		RpcAnswer result = sendRpcToServer(message);
		if (result == null) {
			return null;
		}
		// if (result instanceof RpcAnswer_GetExportList)
		// {
		// return (RpcAnswer_GetExportList) result;
		// }
		return result;
	}

	public RpcAnswer ExportChangesToServer(ExportList exportList) {
		RpcMessage_ExportChangesToServer message = new RpcMessage_ExportChangesToServer(exportList);
		RpcAnswer result = sendRpcToServer(message);
		if (result == null) {
			return null;
		} else {
			return result;
		}
	}
}
