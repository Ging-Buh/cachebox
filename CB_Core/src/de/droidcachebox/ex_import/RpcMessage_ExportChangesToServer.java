package de.droidcachebox.ex_import;

import de.droidcachebox.rpc.RpcMessage;

public class RpcMessage_ExportChangesToServer extends RpcMessage {
    private static final long serialVersionUID = -2905989642061445374L;
    private ExportList exportList;

    public RpcMessage_ExportChangesToServer(ExportList exportList) {
        this.exportList = exportList;
    }

    public ExportList getExportList() {
        return exportList;
    }

}
