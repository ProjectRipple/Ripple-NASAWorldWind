package pg.ripple.nasa.HTMLBalloon.cloudletBalloon;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketHandler;

/**
 * This class starts jetty server that will answer cloudlet balloon requests
 * (patient vital signs, notes, etc.).
 * 
 * @author Pawel
 * 
 */
public class CloudletBalloonNotifier {
	protected final String TAG = "CloudletBalloonNotifier: ";
	protected final boolean DEBUG = false;

	private Server server;
	private CloudletBalloonSocketHandler balloonSocketHandler;

	/**
	 * Cloudlet balloon will send its requests to port 8081 by default. Call {@link #start()} 
	 * to start the server. 
	 * 
	 * @param portNumber Port number that the jetty server will be running on
	 * @param requestListener see CloudletBalloonRequestListener
	 */
	public CloudletBalloonNotifier(int portNumber, CloudletBalloonRequestListener requestListener) {
		this.server = new Server(portNumber);

		balloonSocketHandler = new CloudletBalloonSocketHandler(requestListener);
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
	 * This function will broadcast specified message to all cloudlet balloons
	 * that are currently connected to this server.
	 * 
	 * @param message
	 *            Message to be broadcasted.
	 */
	public synchronized void send(String message) {
		balloonSocketHandler.broadcastMessage(message);
	}

	/**
	 * Cloudlet balloon can send two type of requests:<br>
	 * 1) patient info request - the answer to this request should contain all
	 * available stats/info about the patient (heart rate, respiration rate,
	 * ecg, name etc.)<br>
	 * 2) cloudlet patient list request - the answer to this request should
	 * contain the list of all patients' ids, their triage colors, and their
	 * statuses.<br><br>
	 * 
	 * The class that implements this interface will be able to capture and
	 * answer the requests.
	 * 
	 * @author Pawel
	 * 
	 */
	public interface CloudletBalloonRequestListener {
		public Patient cloudletBalloonPatientInfoRequest(String cloudletID, String patientID);
		public ArrayList<Patient> cloudletBalloonPatientListRequest(String cloudletID);
	}
	
	/**
	 * Internal class of CloudletBalloonNotifier class. It's responsible for
	 * managing the socket connections between this jetty server and cloudlet
	 * balloons.
	 * 
	 * @author Pawel
	 * 
	 */
	private class CloudletBalloonSocketHandler extends WebSocketHandler {
		private final Set<CloudletBalloonSocket> sockets = new CopyOnWriteArraySet<CloudletBalloonSocket>();
		private final CloudletBalloonRequestListener requestListener;
		
		/**
		 * @param requestListener see CloudletBalloonRequestListener
		 */
		public CloudletBalloonSocketHandler(CloudletBalloonRequestListener requestListener) {
			this.requestListener = requestListener;
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
			return new CloudletBalloonSocket();
		}

		/**
		 * Broadcasts specified message to all currently connected cloudlet
		 * balloons.
		 * 
		 * @param message
		 *            Message to be broadcasted.
		 */
		public void broadcastMessage(String message) {
			if (DEBUG) {
				System.out.println(TAG + " sending broadcast to "
						+ sockets.size() + " clients.");
			}
			for (CloudletBalloonSocket cs : sockets) {
				try {
					cs.connection.sendMessage(message);
				} catch (IOException e) {
					sockets.remove(cs);
				}
			}
		}

		/**
		 * This class represents WebSocket implementation.
		 * 
		 * @author Pawel
		 * 
		 */
		private class CloudletBalloonSocket implements WebSocket.OnTextMessage {
			private Connection connection;

			private PingService ping;
			private boolean isPongReceived = true;

			/**
			 * Called when the socket is created. Automatically starts to ping
			 * the cloudlet balloon WebSocket periodically.
			 */
			public CloudletBalloonSocket() {
				ping = new PingService(this);
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
					System.out.println(TAG + " Socket " + connection.hashCode() + " connected.");
				}
				connection.setMaxIdleTime(0); // 0 == infinity
				this.connection = connection;
				sockets.add(this);
			}

			/**
			 * Called when a message arrives. There are 3 possible messages:
			 * 1) "PONG" - a response to ping message
			 * 2) "cloudletID-patientList" - a request for patient list that are in a cloudlet
			 * 3) "cloudletID-patientID" - a request for patient info
			 */
			@Override
			public void onMessage(String msg) {
				if (DEBUG) {
					System.out.println(TAG + " received message on socket " + connection.hashCode() + ". Message: " + msg);
				}
				if (msg.equals(PingService.PONG)) {
					isPongReceived = true;
					return;
				}
				// two args -> first: cloudletID, second: patientID OR "patientList" string
				String[] args = msg.split("-");
				// sending list of patients
				if (args[1].equals("patientList")) {
					ArrayList<Patient> patientList = requestListener.cloudletBalloonPatientListRequest(args[0]);
					try {
						this.connection.sendMessage(buildPatientListMsg(patientList));
					} catch (IOException e) {
						e.printStackTrace();
					}
					return;
				}
				// sending patient info
				Patient patient = requestListener.cloudletBalloonPatientInfoRequest(args[0], args[1]);
				try {
					if (DEBUG) {
						// DONT UNCOMMENT THOSE COMMENTS UNLESS REALLY NEEDED (TAKES FOREVER TO PRINT THE MESSAGE TO THE CONSOLE)
						//System.out.println(TAG + " sending patient report to the responder balloon:\n" + buildPatientInfoMsg(patient));
						//System.out.println(TAG + " sending patient report to the responder balloon:\n" + patient.getWebSocketMsg());
					}
					this.connection.sendMessage(patient.getWebSocketMsg());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			/**
			 * Prepares list of patients (their ids, status, and triage colors).
			 * The format of returned String is as follows:
			 * "patientListPATIENTID PATIENTSTATUS PATIENTWOUNDSTATE PATIENTID PATIENTSTATUS PATIENTWOUNDSTATE (...)"
			 * 
			 * @param patientList
			 *            All patients that belong to a particular cloudlet
			 * @return the list of patients
			 */
			private String buildPatientListMsg(ArrayList<Patient> patientList) {
				StringBuilder msgBuilder = new StringBuilder().append("patientList");
				for (Patient patient : patientList) {
					msgBuilder.append(patient.getId() + " ")
						.append(patient.getStatus() + " ")
						.append(patient.getWoundState() + " ");
				}
				return msgBuilder.toString();
			}
			
			/**
			 * This class will periodically ping cloudlet balloon WebSocket. (2
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

				private WeakReference<CloudletBalloonSocket> balloonSocket2;

				private boolean stop = false;

				public PingService(CloudletBalloonSocket balloonSocket) {
					balloonSocket2 = new WeakReference<CloudletBalloonSocket>(
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
									System.out.println("Socket " + this.balloonSocket2.get().connection.hashCode() + " removed due to ping-pong inactivity.");
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
