package pg.ripple.nasa.HTMLBalloon.cloudletBalloon;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.Layer;

import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

import pg.ripple.nasa.patientFrame.mainFrame.PatientWindow;
import ds.ripple.common.XML.Event;
import ds.ripple.common.XML.FeedItem;
import ds.ripple.common.XML.FeedItem.ItemType;
import ds.ripple.common.XML.Location;
import ds.ripple.common.XML.Producer.ProducerType;

/**
 * This class is container for all information associated with a cloudlet.
 * 
 * @author Pawel
 * 
 */
public final class CloudletInfo {
	private String cloudletID;
	private Location cloudletLocation;
	// list of all patients in the cloudlet
	private ArrayList<Patient> patientList = new ArrayList<Patient>();
	// list of layers that are used to render patients balloon (patients that
	// are in THIS cloudlet)
	private ArrayList<Layer> patientBalloons = new ArrayList<Layer>();
	// list of currently opened java swing windows that are used to display information
	// about patients from this cloudlet
	private ArrayList<PatientWindow> patientWindows = new ArrayList<PatientWindow>();
	// this layer represents a transparent "circle" that is drawn where the cloudlet is located at
	private Layer cloudletLayer;
	// this layer renders cloudlet balloon
	private Layer summaryBalloonLayer;
	
	/**
	 * All parameters are mandatory. 
	 * 
	 * @param cloudletID - the unique id of this cloudlet
	 * @param cloudletLayer - layer that represents a transparent "circle" that is drawn where the cloudlet is located at
	 * @param summaryBalloonLayer - layer that renders cloudlet balloon
	 */
	public CloudletInfo(String cloudletID, Layer cloudletLayer, Layer summaryBalloonLayer) {
		this.cloudletID = cloudletID;
		this.cloudletLayer = cloudletLayer;
		this.summaryBalloonLayer = summaryBalloonLayer;
	}
	
	/**
	 * Returns the unique id of this cludlet.
	 * 
	 * @return id
	 */
	public String getCloudletID() {
		return this.cloudletID;
	}
	
	/**
	 * Returns a list of all patients' ids that are currently registered in this
	 * cloudlet. If there are no patients, the list will be empty.
	 * 
	 * @return List with patients' ids. 
	 */
	public ArrayList<String> getPatientIDs() {
		ArrayList<String> idList = new ArrayList<String>();
		for (Patient p : patientList) {
			idList.add(p.getId());
		}
		return idList;
	}
	
	/**
	 * Returns layer that represents a transparent "circle" that is drawn where
	 * the cloudlet is located at.
	 * 
	 * @return
	 */
	public Layer getCloudletLayer() {
		return this.cloudletLayer;
	}
	
	/**
	 * Returns the layer that renders cloudlet balloon.
	 * 
	 * @return
	 */
	public Layer getSummaryBalloonLayer() {
		return this.summaryBalloonLayer;
	}
	
	/**
	 * Returns a list of layers that are used to render patients balloon
	 * (patients that are in THIS cloudlet).
	 * 
	 * @return
	 */
	public ArrayList<Layer> getPatientBalloonLayers() {
		return this.patientBalloons;
	}
	
	/**
	 * Returns the location of this cloudlet.
	 * 
	 * @return
	 */
	public Location getCloudletLocation() {
		return this.cloudletLocation;
	}
	
	/**
	 * Returns the list of patients that are currently registered in this
	 * cloudlet.
	 * 
	 * @return
	 */
	public ArrayList<Patient> getPatients() {
		return this.patientList;
	}
	
	/**
	 * Parses Ripple-Cloud XML message. All the information in the XML message
	 * will replace current information about this cloudlet.<br>
	 * <br>
	 * 
	 * If the cloudlet information changed after parsing the XML message, all
	 * currently opened java swing windows that are used to display information
	 * about patients from this cloudlet will receive that update.<br>
	 * <br>
	 * 
	 * If the XML message (cloudlet type) contains information about patients
	 * that are not currently registered in this clouldet, they will be registered.
	 * 
	 * @param event
	 *            Ripple-Cloud XML message
	 */
	public void parseXMLMessage(Event event) {
		boolean isStateChanged = false;
		ProducerType msgType = event.getContext().getProducer().getProducerType();
		switch (msgType) {
		case CLOUDLET:
			// process cloudlet XML message
			isStateChanged = parseCloudletMessage(event);
			break;
		case PATIENT:
			// process patient XML message
			isStateChanged = parsePatientMessage(event);
			break;
		case RESPONDER:
			// process responder XML message
			isStateChanged = parseResponderMessage(event);
			break;
		default:
			break;
		}
		
		if (isStateChanged) {
			updatePatientWindows();
		}
	}

	/**
	 * This function parses Ripple-Cloud XML message (cloudlet type).
	 * 
	 * @param event
	 *            XML message
	 * @return true if the state of this cloudlet changed after parsing the
	 *         message, false otherwise
	 */
	private boolean parseCloudletMessage(Event event) {
		boolean isStateChanged = false;
		// check if this message was intended to be received by this cloudlet
		if (!event.getContext().getProducer().getProducerId().equals(this.cloudletID)) {
			return isStateChanged;
		}
		// update cloudlet location
		this.cloudletLocation = event.getContext().getLocation();
		// update patient list (i.e. check if there are new patients)
		for (FeedItem fi : event.getContent().getList()) {
			// make sure the message has correct type of information
			if (fi.getItemType() != ItemType.CLOUDLET_PATIENTID_LIST) {
				return isStateChanged;
			}
			// check all the patient ids from the XML message, if the id 
			// was not found, add a new patient to this cloudlet
			for (String patientId : fi.getValues()) {
				if (!hasPatient(patientId)) {
					Layer patientBalloon;
					Position balloonPosition = new Position(Angle.fromDegrees(this.getRandomLatitude()), Angle.fromDegrees(this.getRandomLongitude()), 0.1);
					patientBalloon = CloudletInfoManager.getInstance().createPatientBalloon("htmlServer/http-server/public/linkToPatientBalloon.html", patientId, balloonPosition, patientId);
					patientBalloons.add(patientBalloon);
					addPatient(patientId);
					isStateChanged = true;
				}
			}
		}
		return isStateChanged;
	}
	
	/**
	 * This function parses Ripple-Cloud XML message (patient type).
	 * 
	 * @param event
	 *            XML message
	 * @return true if the state of this cloudlet changed after parsing the
	 *         message, false otherwise
	 */
	private boolean parsePatientMessage(Event event) {
		// check if the patient referenced in the XML message is registered in this cloudlet
		if (!hasPatient(event.getContext().getProducer().getProducerId())) {
			return false;
		}
		// update all information about the patient that are in the XML message
		Patient p = getPatient(event.getContext().getProducer().getProducerId());
		for (FeedItem fi : event.getContent().getList()) {
			switch (fi.getItemType()) {
			case AGE:
				if (fi.getValues().size() == 1) {
					p.setAge(fi.getValues().get(0));
				}
				break;
			case BLOOD_PRESSURE:
				if (fi.getValues().size() == 2) {
					p.setBloodPressure_DIS(fi.getValues().get(0));
					p.setBloodPressure_SYS(fi.getValues().get(1));
				}
				break;
			case HEART_RATE:
				if (fi.getValues().size() == 1) {
					p.setHeartRate(fi.getValues().get(0));
				}
				break;
			case O2_SATURATION:
				if (fi.getValues().size() == 1) {
					p.setSpO2(fi.getValues().get(0));
				}
				break;
			case PAITENT_LAST_NAME:
				if (fi.getValues().size() == 1) {
					p.setLastName(fi.getValues().get(0));
				}
				break;
			case PATIENT_FIRST_NAME:
				if (fi.getValues().size() == 1) {
					p.setFirstName(fi.getValues().get(0));
				}
				break;
			case RESPIRATION_RATE:
				if (fi.getValues().size() == 1) {
					p.setRespirationRate(fi.getValues().get(0));
				}
				break;
			case TEMPERATURE:
				if (fi.getValues().size() == 1) {
					p.setTemperature(fi.getValues().get(0));
				}
				break;
			case PATIENT_NBC_STATUS:
				if (fi.getValues().size() == 1) {
					p.setNBCStatus(fi.getValues().get(0));
				}
				break;
			case PATIENT_STATUS_DESCRIPTION:
				if (fi.getValues().size() == 1) {
					p.setStatus(fi.getValues().get(0));
				}
				break;
			case PATIENT_TRIAGE_COLOR:
				if (fi.getValues().size() == 1) {
					p.setWoundState(fi.getValues().get(0));
				}
				break;
			case PATIENT_SEX:
				if (fi.getValues().size() == 1) {
					p.setSex(fi.getValues().get(0));
				}
				break;
			default:
				break;
			}
		}
		// start preparing the WebSocket message that will contain all information about 
		// this patient. prebuildPatientMsg() is non-blocking call
		p.prebuildPatientMsg();
		return true;
	}
	
	/**
	 * This function parses Ripple-Cloud XML message (responder type).
	 * 
	 * @param event
	 *            XML message
	 * @return true if the state of this cloudlet changed after parsing the
	 *         message, false otherwise
	 */
	private boolean parseResponderMessage(Event event) {	
		// get the id of the patient referenced in the message
		String patientID = null;
		for (FeedItem fi : event.getContent().getList()) {
			if (fi.getItemType() == ItemType.PATIENT_ID) {
				if (fi.getValues().size() == 1) {
					patientID = fi.getValues().get(0);
				}
			}
		}
		// check if the patient is registered in this cloud-let
		if (patientID == null) {
			return false;
		}
		// check if ID is valid, and if the patient is registred in this cloud
		if (patientID.isEmpty() || !hasPatient(patientID)) {
			return false;
		}
		// get the patient info
		Patient patient = getPatient(patientID);
		// get responder id
		String responderId = event.getContext().getProducer().getProducerId();
		
		// update information about the patient
		for (FeedItem fi : event.getContent().getList()){
			switch (fi.getItemType()) {
			case PATIENT_NBC_STATUS:
				if (fi.getValues().size() == 1) {
					patient.setNBCStatus(fi.getValues().get(0));
				}
				break;
			case PATIENT_STATUS_DESCRIPTION:
				if (fi.getValues().size() == 1) {
					patient.setStatus(fi.getValues().get(0));
				}
				break;
			case PATIENT_TRIAGE_COLOR:
				if (fi.getValues().size() == 1) {
					patient.setWoundState(fi.getValues().get(0));
				}
				break;
			case AGE:
				if (fi.getValues().size() == 1) {
					patient.setAge(fi.getValues().get(0));
				}
				break;
			case PATIENT_FIRST_NAME:
				if (fi.getValues().size() == 1) {
					patient.setFirstName(fi.getValues().get(0));
				}
				break;
			case PAITENT_LAST_NAME:
				if (fi.getValues().size() == 1) {
					patient.setLastName(fi.getValues().get(0));
				}
				break;
			case PATIENT_SEX:
				if (fi.getValues().size() == 1) {
					patient.setSex(fi.getValues().get(0));
				}
				break;
			case NOTE_IMG_FRONT_HEAD:
				for (String s : fi.getValues()) {
					addPatientNote(responderId, patient, Note.NoteType.IMAGE, Note.NoteLocation.FRONT_HEAD, s);
				}
				break;
			case NOTE_IMG_FRONT_LEFT_ARM:
				for (String s : fi.getValues()) {
					addPatientNote(responderId, patient, Note.NoteType.IMAGE, Note.NoteLocation.FRONT_LEFT_ARM, s);
				}
				break;
			case NOTE_IMG_FRONT_LEFT_LEG:
				for (String s : fi.getValues()) {
					addPatientNote(responderId, patient, Note.NoteType.IMAGE, Note.NoteLocation.FRONT_LEFT_LEG, s);
				}
				break;
			case NOTE_IMG_FRONT_RIGHT_ARM:
				for (String s : fi.getValues()) {
					addPatientNote(responderId, patient, Note.NoteType.IMAGE, Note.NoteLocation.FRONT_RIGHT_ARM, s);
				}
				break;
			case NOTE_IMG_FRONT_RIGHT_LEG:
				for (String s : fi.getValues()) {
					addPatientNote(responderId, patient, Note.NoteType.IMAGE, Note.NoteLocation.FRONT_RIGHT_LEG, s);
				}
				break;
			case NOTE_IMG_FRONT_TORSO:
				for (String s : fi.getValues()) {
					addPatientNote(responderId, patient, Note.NoteType.IMAGE, Note.NoteLocation.FRONT_TORSO, s);
				}
				break;
			case NOTE_IMG_GENERAL:
				for (String s : fi.getValues()) {
					addPatientNote(responderId, patient, Note.NoteType.IMAGE, Note.NoteLocation.GENERAL, s);
				}
				break;
			case NOTE_TEXT_FRONT_HEAD:
				for (String s : fi.getValues()) {
					addPatientNote(responderId, patient, Note.NoteType.TEXT, Note.NoteLocation.FRONT_HEAD, s);
				}
				break;
			case NOTE_TEXT_FRONT_LEFT_ARM:
				for (String s : fi.getValues()) {
					addPatientNote(responderId, patient, Note.NoteType.TEXT, Note.NoteLocation.FRONT_LEFT_ARM, s);
				}
				break;
			case NOTE_TEXT_FRONT_LEFT_LEG:
				for (String s : fi.getValues()) {
					addPatientNote(responderId, patient, Note.NoteType.TEXT, Note.NoteLocation.FRONT_LEFT_LEG, s);
				}
				break;
			case NOTE_TEXT_FRONT_RIGHT_ARM:
				for (String s : fi.getValues()) {
					addPatientNote(responderId, patient, Note.NoteType.TEXT, Note.NoteLocation.FRONT_RIGHT_ARM, s);
				}
				break;
			case NOTE_TEXT_FRONT_RIGHT_LEG:
				for (String s : fi.getValues()) {
					addPatientNote(responderId, patient, Note.NoteType.TEXT, Note.NoteLocation.FRONT_RIGHT_ARM, s);
				}
				break;
			case NOTE_TEXT_FRONT_TORSO:
				for (String s : fi.getValues()) {
					addPatientNote(responderId, patient, Note.NoteType.TEXT, Note.NoteLocation.FRONT_TORSO, s);
				}
				break;
			case NOTE_TEXT_GENERAL:
				for (String s : fi.getValues()) {
					addPatientNote(responderId, patient, Note.NoteType.TEXT, Note.NoteLocation.GENERAL, s);
				}
				break;
			default:
				System.out.println("I DONT KNOW THIS :( : " + fi.getItemType());
				break;
			}
		}
		// start preparing the WebSocket message that will contain all information about 
		// this patient. prebuildPatientMsg() is non-blocking call
		patient.prebuildPatientMsg();
		return true;
	}
	
	/**
	 * Adds responder's note about a patient to patient's object.
	 * 
	 * @param responderId
	 *            Id of the responder
	 * @param patient
	 *            patient object
	 * @param type
	 *            type of note
	 * @param location
	 *            location of the note (body location)
	 * @param content
	 *            content of the note
	 */
	private void addPatientNote(String responderId, Patient patient,
			Note.NoteType type, Note.NoteLocation location, String content) {
		Note note = new Note(responderId, type, location, content);
		patient.addNote(note);
	}
	
	/**
	 * Checks if a patient with specified ID is registered in this cloudlet.
	 * 
	 * @param patientID
	 * @return true if the patient is registed, false otherwise
	 */
	private boolean hasPatient(String patientID) {
		for (Patient p : patientList) {
			if (p.getId().equals(patientID)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Returns patient with specified id. If there is no match, returns null.
	 * 
	 * @param patientID
	 * @return
	 */
	private Patient getPatient(String patientID) {
		for (Patient p : patientList) {
			if (p.getId().equals(patientID)) {
				return p;
			}
		}
		return null;
	}
	
	/**
	 * Registers a new patient with specified id in this cloudlet.
	 * 
	 * @param patientID
	 */
	private void addPatient(String patientID) {
		this.patientList.add(new Patient(patientID));
	}
	
	/**
	 * Returns latitude of this cloudlet.
	 * 
	 * @return
	 */
	private double getRandomLatitude() {
		Random r = new Random();
		double rangeMin = -0.00100, rangeMax = 0.00100;
		return Double.parseDouble(this.cloudletLocation.getLatitude())
				+ (rangeMin + (rangeMax - rangeMin) * r.nextDouble());
	}
	
	/**
	 * Returns longitude of this cloudlet.
	 * 
	 * @return
	 */
	private double getRandomLongitude() {
		Random r = new Random();
		double rangeMin = -0.00130, rangeMax = 0.00130;
		return Double.parseDouble(this.cloudletLocation.getLongitude())
				+ (rangeMin + (rangeMax - rangeMin) * r.nextDouble());
	}

	/**
	 * Creates java swing window that is used to display information
	 * about specified patient from this cloudlet.
	 * 
	 * @param patientID
	 */
	protected void createPatientWindow(String patientID) {
		Patient p = getPatient(patientID);
		PatientWindow window = PatientWindow.createPatientWindow(patientID, p.getStatus(), false);
		updatePatientWindow(p, window);
		this.patientWindows.add(window);
	}
	
	/**
	 * Sends all available information about patient to its corresponding java
	 * swing window.
	 * 
	 * @param p patient
	 * @param pw java swing window
	 */
	private void updatePatientWindow(Patient p, PatientWindow pw) {
		// that's all the stuff that sits in the left panel of the window
		pw.updatePatientInfo(p);
		
		// graph updates
		pw.updateBPGraph(new Date(), p.getBloodPressure_SYSInt(), p.getBloodPressure_DIAInt());
		pw.updateHRGraph(new Date(), p.getHeartRateInt());
		pw.updateRRGraph(new Date(), p.getRespirationRateDouble());
		pw.updateTempGraph(new Date(), p.getTemperatureDouble());
		// TODO: ECG graph is missing here :(
		
		// notes updates
		pw.updateNotes(p.getNotes());
	}
	
	/**
	 * Updates all currently opened java swing windows.  
	 */
	private void updatePatientWindows() {
		for (PatientWindow window : patientWindows) {
			Patient p = getPatient(window.getPatientID());
			updatePatientWindow(p, window);
		}
	}
}
