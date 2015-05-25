package ro.pub.cs.systems.pdsd.practicaltest02;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

import ro.pub.cs.systems.pdsd.practicaltest02.Constants;
import ro.pub.cs.systems.pdsd.practicaltest02.Utilities;
import android.util.Log;
import android.widget.TextView;

public class ClientThread extends Thread {
	
	private String   address;
	private int      port;
	private TextView timeView;
	
	private Socket   socket;
	
	public ClientThread(
			String address,
			int port,
			TextView timeTextView) {
		this.address                 = address;
		this.port                    = port;
		this.timeView = timeTextView;
	}
	
	@Override
	public void run() {
		try {
			socket = new Socket(address, port);
			if (socket == null) {
				Log.e(Constants.TAG, "[CLIENT THREAD] Could not create socket!");
			}
			
			BufferedReader bufferedReader = Utilities.getReader(socket);
			PrintWriter    printWriter    = Utilities.getWriter(socket);
			if (bufferedReader != null && printWriter != null) {
				printWriter.println(Constants.requestContent);
				String timeInfo;
				while ((timeInfo = bufferedReader.readLine()) != null) {
					final String finalizedTime = timeInfo;
					timeView.post(new Runnable() {
						@Override
						public void run() {
							timeView.append(finalizedTime + "\n");
						}
					});
				}
			} else {
				Log.e(Constants.TAG, "[CLIENT THREAD] BufferedReader / PrintWriter are null!");
			}
			socket.close();
		} catch (IOException ioException) {
			Log.e(Constants.TAG, "[CLIENT THREAD] An exception has occurred: " + ioException.getMessage());
			if (Constants.DEBUG) {
				ioException.printStackTrace();
			}
		}
	}

}
