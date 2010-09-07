package org.freeswitch.client.client;

import com.fredhat.gwt.xmlrpc.client.XmlRpcClient;
import com.fredhat.gwt.xmlrpc.client.XmlRpcRequest;
import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.RequestTimeoutException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

public abstract class FSCommand {
	final static private String methodName = "freeswitch.api";
	final static private XmlRpcClient client = new XmlRpcClient(GWT.getModuleBaseURL() + "../RPC2");
	final static private String eventSinkUrl = GWT.getModuleBaseURL()	+ "../api/event_sink";

	final static private int STATUS_CODE_OK = 200;
	static private int listenId = 0;

	public static void eventRequestListener() {
		RequestBuilder builder = new RequestBuilder(RequestBuilder.POST, eventSinkUrl);

		try {
			builder.setHeader("Content-type", "application/x-www-form-urlencoded");
			builder.sendRequest(null, new RequestCallback() {

				public void onError(Request request, Throwable exception) {
					if (exception instanceof RequestTimeoutException) {
						Window.alert("Erro: request timeout.");
					} else {
						Window.alert("Erro: " + exception.getMessage());
					}
				}

				public void onResponseReceived(Request request,
						Response response) {
					if (STATUS_CODE_OK == response.getStatusCode()) {
						Window.alert("RET: " + response.getText());
					} else {
						Window.alert("Falha ao executar comando. Status: " + response.getStatusText());
					}
				}
			});
		} catch (RequestException e) {
			Window.alert("Failed to send the request: " + e.getMessage());
		}
	}

	/**
	 * Sends the command and sets the callback.
	 * 
	 * @param cmd
	 *            : fsapi command to be run
	 * @param callbackClass
	 *            : class' instance to be called when the response is ready
	 */
	static public int runCommand(String cmd, AsyncCallback<String> callbackClass) {
		Object[] arglist = cmd.split(" ");
		String args = "";

		if (cmd.length() == 0) {
			return -1;
		}

		for (int i = 1; i < arglist.length; i++)
			args += " " + arglist[i];

		Object[] params = new Object[] { arglist[0], args };

		XmlRpcRequest<String> request = new XmlRpcRequest<String>(client,
				methodName, params, callbackClass);
		request.execute();

		return 0;
	}
}
