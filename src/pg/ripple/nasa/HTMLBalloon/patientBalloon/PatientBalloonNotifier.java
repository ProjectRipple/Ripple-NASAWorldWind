package pg.ripple.nasa.HTMLBalloon.patientBalloon;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketHandler;

/**
 * This class starts jetty server that will maintain communication with patient
 * balloons.
 * 
 * @author Pawel
 * 
 */
public class PatientBalloonNotifier {
	protected final String TAG = "PatientBalloonNotifier: ";
	protected final boolean DEBUG = false;
	
	private Server server;
	private PatientBalloonSocketHandler balloonSocketHandler;

	/**
	 * Patient balloon will communicate on port 8080 by default. Call {@link #start()} 
	 * to start the server. 
	 * 
	 * @param portNumber Port number that the jetty server will be running on
	 */
	public PatientBalloonNotifier(int portNumber) {
		this.server = new Server(portNumber);
		
		balloonSocketHandler = new PatientBalloonSocketHandler();
		balloonSocketHandler.setHandler(new DefaultHandler());
		
		server.setHandler(balloonSocketHandler);
	}

	/**
	 * Starts the jetty server.
	 */
	public void start() {
		try {		
			server.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Stops the jetty server.
	 */
	public void stop() {

		try {
			server.stop();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * This function will broadcast specified message to all patient balloons
	 * that are currently connected to this server.
	 * 
	 * @param message
	 *            Message to be broadcasted.
	 */
	public void send(String message) {
		balloonSocketHandler.broadcastMessage(message);
	}

	/**
	 * Internal class of PatientBalloonNotifier class. It's responsible for
	 * managing the socket connections between this jetty server and patient
	 * balloons.
	 * 
	 * @author Pawel
	 * 
	 */
	private class PatientBalloonSocketHandler extends WebSocketHandler {

		private final Set<PatientBalloonSocket> sockets = new CopyOnWriteArraySet<PatientBalloonSocket>();

		public PatientBalloonSocketHandler() {
			
		}
		
		/**
		 * Called when a request to create WebSocket connection arrives.
		 * 
		 * @param arg0
		 * @param arg1
		 * @return
		 */
		@Override
		public WebSocket doWebSocketConnect(HttpServletRequest arg0, String arg1) {
			return new PatientBalloonSocket();
		}

		/**
		 * Broadcasts specified message to all currently connected patient
		 * balloons.
		 * 
		 * @param message
		 *            Message to be broadcasted.
		 */
		public void broadcastMessage(String messge) {
			if (DEBUG) {
				System.out.println(TAG + " sending broadcast to " + sockets.size() + " clients.");
			}
			for (PatientBalloonSocket bs : sockets) {
				try {
					bs.connection.sendMessage(messge);
				} catch (IOException e) {
					sockets.remove(bs);
				}
			}
		}

		/**
		 * This class represents WebSocket implementation.
		 * 
		 * @author Pawel
		 * 
		 */
		private class PatientBalloonSocket implements WebSocket.OnTextMessage {

			private Connection connection;
			
			private PingService ping;
			private boolean isPongReceived = true;
			
			/**
			 * Called when the socket is created. Automatically starts to ping
			 * the patient balloon WebSocket periodically.
			 */
			public PatientBalloonSocket() {
				PingService ping = new PingService(this);
				new Thread(ping).start();
			}
			
			/**
			 * Called when the WebSocket disconnects.
			 */
			@Override
			public void onClose(int arg0, String arg1) {
				if (DEBUG) {
					System.out.println(TAG + " Socket " + connection.hashCode() + " disconnected.");
					System.out.println("Reason: " + arg0 + " " + arg1);
				}
				sockets.remove(this);
				ping.stop();
			}

			/**
			 * Called when the connection to cloudlet balloon WebSocket is established.
			 */
			@Override
			public void onOpen(Connection connection) {
				if (DEBUG) {
					System.out.println(TAG + " New socket " + connection.hashCode() + " connected");
				}
				connection.setMaxIdleTime(0); // 0 == infinity
				this.connection = connection;
				sockets.add(this);
			}

			/**
			 * Called when a message arrives. There is only one possible message
			 * that the patient balloon can send to jetty server: "PING"
			 * message.
			 */
			@Override
			public void onMessage(String msg) {
				if (msg.equals(PingService.PONG)) {
					isPongReceived = true;
				}
				if (DEBUG) {
					System.out.println(TAG + " received message on socket " + connection.hashCode() + 
							". Message: " + msg);
				}
			}

			/**
			 * This class will periodically ping patient balloon WebSocket. (2
			 * seconds sleep time -> ping message is sent -> 2 seconds sleep
			 * time). If there is no response from the WebSocket, the connection
			 * to this WebSocket will be forcibly closed.
			 * 
			 * @author Pawel
			 * 
			 */
			private class PingService implements Runnable {
				protected final static String PING = "ping";
				protected final static String PONG = "pong";

				private WeakReference<PatientBalloonSocket> balloonSocket2;

				private boolean stop = false;

				public PingService(PatientBalloonSocket balloonSocket) {
					balloonSocket2 = new WeakReference<PatientBalloonSocket>(
							balloonSocket);
				}

				@Override
				public void run() {
					while (!stop) {
						try {
							Thread.sleep(2000);
							connection.sendMessage(PING);
							Thread.sleep(2000);
							if (balloonSocket2.get() == null) {
								return;
							}
							if (isPongReceived) {
								isPongReceived = false;
							} else {
								sockets.remove(this.balloonSocket2.get());
								stop();
								if (DEBUG) {
									System.out.println("Socket " + this.balloonSocket2.get().connection.hashCode() +  " removed due to ping-pong inactivity.");
								}
							}
						} catch (InterruptedException | IOException e) {
							e.printStackTrace();
							stop();
						}
					}
				}

				protected void stop() {
					stop = true;
				}

			}

		}

	}

}
