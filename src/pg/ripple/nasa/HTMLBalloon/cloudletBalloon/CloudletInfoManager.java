package pg.ripple.nasa.HTMLBalloon.cloudletBalloon;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.Layer;

import java.util.ArrayList;

import pg.ripple.nasa.HTMLBalloon.cloudletBalloon.CloudletBalloonNotifier.CloudletBalloonRequestListener;
import pg.ripple.nasa.HTMLBalloon.patientBalloon.PatientBalloonNotifier;
import pg.ripple.nasa.ww.RippleApplication.RippleFrame;
import ds.ripple.common.XML.Event;

/**
 * This singleton class is responsible for:
 * - parsing Ripple-Cloud XML messages, 
 * - maintaining communication between patient/cloudlet balloons,
 * - registration of new cloudlets/patients and creation of the balloons,
 * - creating java swing window that replicates patient balloon functionalities
 * 
 * @author Pawel
 *
 */
public final class CloudletInfoManager {
	private static CloudletInfoManager managerInstance = null;
	
	private RippleFrame rippleFrame;
	private ArrayList<CloudletInfo> cloudletList = new ArrayList<CloudletInfo>();
	
	private PatientBalloonNotifier patientBalloonNotifier;
	private CloudletBalloonNotifier cloudletBalloonNotifier;
	
	/**
	 * Private constructor.
	 * 
	 * @param rippleFrame
	 *            Reference to JFrame that holds main World Wind view
	 */
	private CloudletInfoManager(RippleFrame rippleFrame) {
		this.rippleFrame = rippleFrame;
		patientBalloonNotifier = new PatientBalloonNotifier(8080);
		cloudletBalloonNotifier = new CloudletBalloonNotifier(8081, new MyCloudletBalloonRequestListener());
		patientBalloonNotifier.start();
		cloudletBalloonNotifier.start();
	}
	
	/**
	 * Returns an instance of CloudletInfoManager.
	 * 
	 * @param rippleFrame
	 *            Reference to JFrame that holds main World Wind view
	 * @return
	 */
	public static synchronized CloudletInfoManager getInstance(RippleFrame rippleFrame) {
		if (managerInstance == null) {
			managerInstance = new CloudletInfoManager(rippleFrame);
		}
		return managerInstance;
	}

	/**
	 * Returns an instance of CloudletInfoManager. Returns null, if the instance
	 * doesn't exist.
	 */
	public static synchronized CloudletInfoManager getInstance() {
		return managerInstance;
	}
	
	/**
	 * Creates and returns World Wind layer that will contain patient balloon.
	 * 
	 * @param htmlFile
	 *            Relative path to linkToPatientBalloon.html file
	 * @param id
	 *            Patient id
	 * @param balloonPosition
	 *            Patient position
	 * @param ballonName
	 *            This name will be displayed next to patient icon, should be
	 *            identifying the patient (patientID, patient name, etc)
	 * @return
	 */
	public Layer createPatientBalloon(String htmlFile, String id, Position balloonPosition, String ballonName) {
		return rippleFrame.addBalloon(htmlFile, id, balloonPosition, ballonName);
	}
	
	public synchronized void parseXMLMessage(Event event) {
		switch (event.getContext().getProducer().getProducerType()) {
		case CLOUDLET:
			updateCloudletPatientList(event);
			broadcastCloudletInfo(event);
			break;
		case PATIENT:
			updateCloudletPatientInfo(event);
			broadcastPatientInfo(event);
			break;
		case RESPONDER:
			updateCloudletResponderInfo(event);
			break;
		default:
			break;
		
		}
	}
	
	/**
	 * Broadcasts an XML message embedded in the event to all patient balloons.
	 */
	private void broadcastPatientInfo(Event event) {
		patientBalloonNotifier.send(event.toString());
	}
	
	
	/**
	 * Broadcasts an XML message embedded in the event to all cloudlet balloons.
	 */
	private void broadcastCloudletInfo(Event event) {
		cloudletBalloonNotifier.send(event.toString());
	}

	/**
	 * Parses Ripple-Cloud XML message (cloudlet type).
	 */
	private void updateCloudletPatientList(Event event) {
		String cloudletID = event.getContext().getProducer().getProducerId();
		// check if we have that cloudlet in the system, if the cloudlet is already
		// registered, update its position and return
		for (CloudletInfo cloudlet : cloudletList) {
			if (cloudlet.getCloudletID().equals(cloudletID)) {
				LatLon oldPosition = new LatLon(Angle.fromDegrees(Double.parseDouble(cloudlet.getCloudletLocation().getLatitude())), Angle.fromDegrees(Double.parseDouble(cloudlet.getCloudletLocation().getLongitude())));
				cloudlet.parseXMLMessage(event);
				rippleFrame.updateCloudLetCircleLocation(cloudlet.getCloudletLayer(), new LatLon(Angle.fromDegrees(Double.parseDouble(cloudlet.getCloudletLocation().getLatitude())), Angle.fromDegrees(Double.parseDouble(cloudlet.getCloudletLocation().getLongitude()))));
				rippleFrame.updateSummaryBalloonLocation(cloudlet.getSummaryBalloonLayer(), new LatLon(Angle.fromDegrees(Double.parseDouble(cloudlet.getCloudletLocation().getLatitude())), Angle.fromDegrees(Double.parseDouble(cloudlet.getCloudletLocation().getLongitude()))));
				rippleFrame.updatePatientBalloonsLocation(cloudlet.getPatientBalloonLayers(), new LatLon(Angle.fromDegrees(Double.parseDouble(cloudlet.getCloudletLocation().getLatitude())), Angle.fromDegrees(Double.parseDouble(cloudlet.getCloudletLocation().getLongitude()))), oldPosition);
				return;
			}
		}
		
		// if we are here, that means the cloudlet was not found. Create a new one
		// create a transparent circle layer
		Layer circleLayer = rippleFrame.addCloudLetCircle(
				200,
				new LatLon(Angle.fromDegrees(Double.parseDouble(event
						.getContext().getLocation().getLatitude())), Angle
						.fromDegrees(Double.parseDouble(event.getContext()
								.getLocation().getLongitude()))), cloudletID);
		// create cloudlet balloon layer
		Layer balloonLayer = rippleFrame.addCloudletBalloon(
				"htmlServer/http-server/public/linkToResponderBalloon.html",
				cloudletID,
				new LatLon(Angle.fromDegrees(Double.parseDouble(event
						.getContext().getLocation().getLatitude())), Angle
						.fromDegrees(Double.parseDouble(event.getContext()
								.getLocation().getLongitude()))),
				"Cloudlet summary");
		// create a new instance of cloudlet object
		CloudletInfo cloudletInfo = new CloudletInfo(cloudletID, circleLayer, balloonLayer);
		cloudletList.add(cloudletInfo);
		// let the new cloudlet parse the XML message
		cloudletInfo.parseXMLMessage(event);
	}
	
	/**
	 * Parses Ripple-Cloud XML message (patient type).
	 */
	private void updateCloudletPatientInfo(Event event) {
		// find the cloudlet that has the patient that is being described by the XML message in the event
		String patientID = event.getContext().getProducer().getProducerId();
		for (CloudletInfo cloudlet : cloudletList) {
			for (String id : cloudlet.getPatientIDs()) {
				if (patientID.equals(id)) {
					// found a match, let's send update about thiMMyesponderBalloonRequestListener, and return
					cloudlet.parseXMLMessage(event);
					return;
				}
			}
		}
		// if the cloudlet was not found, do nothing
	}
	
	/**
	 * Parses Ripple-Cloud XML message (responder type).
	 */
	private void  updateCloudletResponderInfo(Event event) {
		// find the cloudlet that has the patient that is being described by the XML message in the event
		String patientID = event.getContent().getList().get(0).getValues().get(0);
		for (CloudletInfo cloudlet : cloudletList) {
			for (String id : cloudlet.getPatientIDs()) {
				if (patientID.equals(id)) {
					// found a match, let's send update about this
					// patient to this cloudlet, and return
					cloudlet.parseXMLMessage(event);
					return;
				}
			}
		}
		// if the cloudlet was not found, do nothing
	}
	
	/**
	 * Creates a new java swing window, that replicates patient balloon
	 * functionalities. Does nothing if the patient with the specified id was
	 * not registered in any cloudlet.
	 */
	public void createPatientWindow(String patientID) {
		for (CloudletInfo cloudlet : cloudletList) {
			for (Patient patient : cloudlet.getPatients()) {
				if (patientID.equals(patient.getId())) {
					cloudlet.createPatientWindow(patientID);
					return;
				}
			}
		}
	}
	
	/**
	 * Implements interface defined in CloudletBalloonRequestListener. This class is responsible
	 * for handling the cloudlet balloon requests about patient and cloudlet information.
	 */
	private class MyCloudletBalloonRequestListener implements CloudletBalloonRequestListener {

		/**
		 * Called when a request about patient information is made by one of the cloudlet balloons.
		 */
		@Override
		public Patient cloudletBalloonPatientInfoRequest(String cloudletID, String patientID) {
			for (CloudletInfo cloudlet : cloudletList) {
				if (cloudlet.getCloudletID().equals(cloudletID)) {
					for (Patient patient : cloudlet.getPatients()) {
						if (patient.getId().equals(patientID)) {
							return patient;
						}
					}
				}
			}
			return null;
		}

		/**
		 * Called when a request about cloudlet patient list is made by one of the cloudlet balloons.
		 */
		@Override
		public ArrayList<Patient> cloudletBalloonPatientListRequest(String cloudletID) {
			for (CloudletInfo cloudlet : cloudletList) {
				if (cloudlet.getCloudletID().equals(cloudletID)) {
					return cloudlet.getPatients();
				}
			}
			return null;
		}
		
	}
}
