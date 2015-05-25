package ro.pub.cs.systems.pdsd.practicaltest02;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import ro.pub.cs.systems.pdsd.practicaltest02.Constants;
import ro.pub.cs.systems.pdsd.practicaltest02.Utilities;
import android.util.Log;

public class CommunicationThread extends Thread {
	
	private ServerThread serverThread;
	private Socket       socket;
	
	public CommunicationThread(ServerThread serverThread, Socket socket) {
		this.serverThread = serverThread;
		this.socket       = socket;
	}
	
	public String getCurrentTime() throws JSONException {

		HttpClient httpClient = new DefaultHttpClient();

		HttpGet httpGet = new HttpGet(Constants.WEB_SERVICE_ADDRESS);
		ResponseHandler handler = new BasicResponseHandler();

		String content = null;
		try {
			content = httpClient.execute(httpGet, handler);

		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return content;
	}
	
	@Override
	public void run() {
		if (socket != null) {
			try {
				BufferedReader bufferedReader = Utilities.getReader(socket);
				PrintWriter    printWriter    = Utilities.getWriter(socket);
				if (bufferedReader != null && printWriter != null) {
					Log.i(Constants.TAG, "[COMMUNICATION THREAD] Waiting for request)!");
					
					String request            = bufferedReader.readLine();
					
					if (request.equals("time")) {
						if(serverThread.getData().containsKey(socket.getRemoteSocketAddress().toString().split(":")[0])) {
							if(System.currentTimeMillis() - serverThread.getData().get(socket.getRemoteSocketAddress().toString().split(":")[0]) > 60000) {
								printWriter.println(getCurrentTime());
								serverThread.setData(socket.getRemoteSocketAddress().toString(), System.currentTimeMillis());
								Log.i(Constants.TAG, "[SET IP TIME]" + socket.getRemoteSocketAddress().toString().split(":")[0] + " " + System.currentTimeMillis());
							}
							else {
								printWriter.println("[ERROR] Exceede 1 request / minute limit!");
							}
						}
						else {
							printWriter.println(getCurrentTime());
							serverThread.setData(socket.getRemoteSocketAddress().toString().split(":")[0], System.currentTimeMillis());
							Log.i(Constants.TAG, "[SET IP TIME]" + socket.getRemoteSocketAddress().toString().split(":")[0] + " " + System.currentTimeMillis());
						}
						
						
						
					} else {
						Log.e(Constants.TAG, "[COMMUNICATION THREAD] Error receiving parameters from client (only time supported)!");
					}
				} else {
					Log.e(Constants.TAG, "[COMMUNICATION THREAD] BufferedReader / PrintWriter are null!");
				}
				socket.close();
			} catch (IOException ioException) {
				Log.e(Constants.TAG, "[COMMUNICATION THREAD] An exception has occurred: " + ioException.getMessage());
				if (Constants.DEBUG) {
					ioException.printStackTrace();
				}
			} catch (JSONException jsonException) {
				Log.e(Constants.TAG, "[COMMUNICATION THREAD] An exception has occurred: " + jsonException.getMessage());
				if (Constants.DEBUG) {
					jsonException.printStackTrace();
				}				
			}
		} else {
			Log.e(Constants.TAG, "[COMMUNICATION THREAD] Socket is null!");
		}
	}

}
