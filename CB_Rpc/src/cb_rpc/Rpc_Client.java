package cb_rpc;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;

import cb_rpc.Functions.RpcAnswer;
import cb_rpc.Functions.RpcMessage;

public class Rpc_Client {
	// XmlRpc Objecte für den Zugriff auf den RPC-Server
	private XmlRpcClient client = null;

	public Rpc_Client() {
		
	}
	
	/**
	 * Erstellt die Config-Objecte für den Zugriff auf den PCharge-Server über XmlRpc
	 */
	private void createRpcConfig() {
		client = null;
		// create configuration
		XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
		
		try {
			System.setProperty("sun.net.http.allowRestrictedHeaders", "true");
//			URL url = new URL("http://192.168.100.115:9911/xmlrpc");
//			URL url = new URL("http://192.168.1.69:9911/xmlrpc");
//			URL url = new URL("http://192.168.1.30:9911/xmlrpc");
			URL url = new URL("http://localhost:9911/xmlrpc");
			config.setServerURL(url);
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
	 * Antwort zurück
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


