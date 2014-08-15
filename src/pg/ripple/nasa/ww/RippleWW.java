package pg.ripple.nasa.ww;

import java.util.ArrayList;
import java.util.Date;

import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import ds.ripple.common.XML.Event;
import ds.ripple.common.XML.FeedItem.ItemType;
import ds.ripple.common.XML.XMLMessage;
import ds.ripple.common.XML.Producer.ProducerType;
import ds.ripple.common.XML.XMLMessage.XMLMessageBuilder;
import ds.ripple.sub.Publisher;
import ds.ripple.sub.PublisherList;
import ds.ripple.sub.PublisherListListener;
import ds.ripple.sub.RippleEventListener;
import ds.ripple.sub.Subscriber;
import ds.ripple.sub.exceptions.InvalidURLException;
import ds.ripple.sub.exceptions.XMLMissingArgumentException;
import pg.ripple.nasa.HTMLBalloon.cloudletBalloon.CloudletInfoManager;
import pg.ripple.nasa.HTMLBalloon.patientBalloon.PatientBalloonNotifier;
import pg.ripple.nasa.ww.RippleApplication.RippleFrame;

public class RippleWW {
	
	static Subscriber subscriber;
	static RippleFrame rippleFrame;
	static PatientBalloonNotifier ballonNotifier;
	
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
    	
    	// start subscriber
    	subscriber = new Subscriber("tcp://localhost", new NASAPublisherListListener());
    	subscriber.connect();
	}
	
	public static void createPatientBalloon(String balloonId, Position position, String ballonName){
		rippleFrame.addBalloon("linkToPatientBalloon.html", balloonId, position, ballonName);
		rippleFrame.addCloudLetCircle(250, new LatLon(position.getLatitude(), position.getLongitude()), "cloudlet");
	}
	
	static class NASAPublisherListListener implements PublisherListListener {
		ArrayList<Publisher> publisherList = new ArrayList<Publisher>();
		ArrayList<String> producerList = new ArrayList<String>();
		
		@Override
		public void publisherListUpdate(PublisherList pl) {
			System.out.println("Got update: number of publishers" + pl.getPublishers().length); 
			
			for (Publisher p : publisherList) {
				try {
					subscriber.unsubscriberFromRippleEvents(p.getURL());
				} catch (InvalidURLException e) {
					e.printStackTrace();
				}
			}
			publisherList.clear();
			
			for (Publisher p : pl.getPublishers()) {
				publisherList.add(p);
				try {
					subscribeToPublisher(p);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			System.out.println("I am subscribed to " + publisherList.size() + " publishers");
		}
		
		public void subscribeToPublisher(Publisher p) {
			subscriber.subscriberForRippleEvents(p.getURL(), new RippleEventListener() {
				
				@Override
				public void publishedEvent(Event event) {
					//System.out.println(event);
					cloudletManager.parseXMLMessage(event);
				}
			});
		}
	}
}
