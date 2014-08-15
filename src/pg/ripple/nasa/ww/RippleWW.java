package pg.ripple.nasa.ww;

import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;

import java.util.ArrayList;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import pg.ripple.nasa.HTMLBalloon.cloudletBalloon.CloudletInfoManager;
import pg.ripple.nasa.ww.RippleApplication.RippleFrame;
import ds.ripple.common.XML.Event;
import ds.ripple.sub.Publisher;
import ds.ripple.sub.PublisherList;
import ds.ripple.sub.PublisherListListener;
import ds.ripple.sub.RippleEventListener;
import ds.ripple.sub.Subscriber;
import ds.ripple.sub.exceptions.InvalidURLException;

public class RippleWW {
	
	static Subscriber subscriber;
	static RippleFrame rippleFrame;
	
	static CloudletInfoManager cloudletManager = null;
	
	public static void main(String[] args) {
    	// attempt to make the window look pretty
    	try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException
				| IllegalAccessException | UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
    	
    	// load world wind configuration
    	Configuration.insertConfigurationDocument("pg/ripple/nasa/config/worldwind.xml");
    	
    	// start the NASA World Wind
    	rippleFrame = RippleApplication.start("Ripple World Wind Application", RippleFrame.class);
    	cloudletManager = CloudletInfoManager.getInstance(rippleFrame);
    	
    	/***************************************************************/
    	/*The IP address below is the Directory Services server address*/
    	/***************************************************************/
    	// start subscriber
    	subscriber = new Subscriber("tcp://localhost", new NASAPublisherListListener());
    	subscriber.connect();
	}
	
	/**
	 * Creates patient balloon (and its icon) with a "transparent" circle around
	 * the balloon's icon.
	 * 
	 * @param balloonId
	 *            - balloon id must be the same as patient id
	 * @param position
	 *            - position of the balloon's icon
	 * @param balloonName
	 *            - balloon name (will be displayed as text next to the
	 *            balloon's icon)
	 */
	public static void createPatientBalloon(String balloonId, Position position, String balloonName){
		rippleFrame.addBalloon("linkToPatientBalloon.html", balloonId, position, balloonName);
		rippleFrame.addCloudLetCircle(250, new LatLon(position.getLatitude(), position.getLongitude()), "cloudlet");
	}
	
	static class NASAPublisherListListener implements PublisherListListener {
		ArrayList<Publisher> publisherList = new ArrayList<Publisher>();
		ArrayList<String> producerList = new ArrayList<String>();
		
		/**
		 * Called when a publisher list is updated at the Directory Services
		 * server in the Ripple-Cloud. Publisher list contains currently active
		 * publishers.
		 */
		@Override
		public void publisherListUpdate(PublisherList pl) {
			System.out.println("Got update form the Directory Services server: number of publishers"
							+ pl.getPublishers().length);

			// unsubscribe from all publishers
			for (Publisher p : publisherList) {
				try {
					subscriber.unsubscriberFromRippleEvents(p.getURL());
				} catch (InvalidURLException e) {
					e.printStackTrace();
				}
			}
			publisherList.clear();
			
			// subscribe to all available publishers
			for (Publisher p : pl.getPublishers()) {
				publisherList.add(p);
				try {
					subscribeToPublisher(p);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			System.out.println("Subscribed to " + publisherList.size() + " publishers");
		}
		
		/**
		 * This method subscribes to Ripple-Events of a given publisher
		 * @param publihser
		 */
		public void subscribeToPublisher(Publisher publihser) {
			subscriber.subscriberForRippleEvents(publihser.getURL(), new RippleEventListener() {
				
				/**
				 * Called when a message from the publisher arrives.
				 */
				@Override
				public void publishedEvent(Event event) {
					//System.out.println(event);
					cloudletManager.parseXMLMessage(event);
				}
			});
		}
	}
}
