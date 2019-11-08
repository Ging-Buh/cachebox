package de.droidcachebox.ex_import;

import de.droidcachebox.rpc.RpcAnswer;

public class RpcAnswer_ExportChangesToServer extends RpcAnswer {
    private static final long serialVersionUID = -7370827484237509850L;
    public ExportList exportList;

    public RpcAnswer_ExportChangesToServer(int result, ExportList exportList) {
        super(result);
        this.exportList = exportList;
    }

}
