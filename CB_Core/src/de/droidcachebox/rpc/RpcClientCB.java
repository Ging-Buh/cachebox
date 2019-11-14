package de.droidcachebox.rpc;

import de.droidcachebox.ex_import.ExportList;

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
