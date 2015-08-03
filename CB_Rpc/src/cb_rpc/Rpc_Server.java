package cb_rpc;

import org.apache.xmlrpc.server.PropertyHandlerMapping;
import org.apache.xmlrpc.server.XmlRpcServerConfigImpl;
import org.apache.xmlrpc.server.XmlRpcStreamServer;
import org.apache.xmlrpc.webserver.WebServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class Rpc_Server {
	// WebServer für die RPC-Communication
	private WebServer webServer = null;
	public Logger log;

	public Rpc_Server(Class rpcFunctions) {
		log = LoggerFactory.getLogger(Rpc_Server.class);
		startWebserver(rpcFunctions);
	}

	/**
	 * Startet den Webserver, der die XmlRpc-Meldungen empfängt
	 */
	private void startWebserver(Class rpcFunctions) {
		try {
			webServer = new WebServer(9911);

			XmlRpcStreamServer xmlRpcServer = webServer.getXmlRpcServer();

			PropertyHandlerMapping phm = new PropertyHandlerMapping();

			phm.addHandler("Rpc_Functions", rpcFunctions);

			xmlRpcServer.setHandlerMapping(phm);

			webServer.setParanoid(false);
			XmlRpcServerConfigImpl serverConfig = (XmlRpcServerConfigImpl) xmlRpcServer.getConfig();
			serverConfig.setEnabledForExtensions(true);
			serverConfig.setContentLengthOptional(true);
			serverConfig.setEnabledForExceptions(true);
			xmlRpcServer.setConfig(serverConfig);
			webServer.start();
			log.info("RpcWebServer started on port " + 9911);
		} catch (Exception ex) {
			log.error("Error starting RpcWebServer: " + ex.getMessage());
		}

	}

}
