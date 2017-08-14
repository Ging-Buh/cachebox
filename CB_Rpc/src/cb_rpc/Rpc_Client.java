package cb_rpc;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;

import cb_rpc.Functions.RpcAnswer;
import cb_rpc.Functions.RpcMessage;
import cb_rpc.Settings.CB_Rpc_Settings;

public class Rpc_Client {
	// XmlRpc Objecte f�r den Zugriff auf den RPC-Server
	private XmlRpcClient client = null;

	public Rpc_Client() {

	}

	/**
	 * Erstellt die Config-Objecte f�r den Zugriff auf den PCharge-Server �ber XmlRpc
	 */
	private void createRpcConfig() {
		client = null;
		// create configuration
		XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();

		try {
			System.setProperty("sun.net.http.allowRestrictedHeaders", "true");
			String CBS_IP = CB_Rpc_Settings.CBS_IP.getValue();
			if (CBS_IP.indexOf(":") <= 0)
				CBS_IP += ":9911";
			URL url = new URL("http://" + CBS_IP + "/xmlrpc");
			config.setServerURL(url);
			//	config.setEncoding("UTF-8");
			config.setGzipCompressing(false);
			config.setGzipRequesting(true);
			config.setEnabledForExceptions(true);
		} catch (MalformedURLException e) {
			System.out.println("SendRpcToPChargeServer - Error: " + e.getMessage());
			return;
		}
		config.setEnabledForExtensions(true);
		config.setEncoding("UTF-8");

		client = new XmlRpcClient();
		client.setConfig(config);
	}

	/**
	 * Sendet eine XmlRpc-Nachricht an den RPC-Server und liefert dessen
	 * Antwort zur�ck
	 * 
	 * @param message
	 * @return
	 */
	public RpcAnswer sendRpcToServer(RpcMessage message) {
		if (client == null)
			createRpcConfig();
		if (client == null) {
			System.out.println("SendRpcToServer - Cannot create Client!");
			return null;
		}
		try {
			System.out.println("SendRpcToServer");
			Object obj = client.execute("Rpc_Functions.Msg", new Object[] { message });
			if ((obj == null) || (!(obj instanceof RpcAnswer))) {
				System.out.println("SendRpcToServer - Result == null");
				return null;
			} else {
				System.out.println("SendRpcToServer - Result = " + obj.toString());
				return (RpcAnswer) obj;
			}
		} catch (Exception ex) {
			System.out.println(ex.toString() + " - " + ex.getMessage());
			return null;
		}

	}

}
