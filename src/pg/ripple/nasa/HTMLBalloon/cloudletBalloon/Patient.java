package pg.ripple.nasa.HTMLBalloon.cloudletBalloon;

import java.util.ArrayList;

/**
 * This class is a container for all information associated with a patient.
 * 
 * @author Pawel
 * 
 */
public final class Patient {
	private static final boolean DEBUG = false;
	private static final String TAG = "PatientClass";
	
	// patient information
	private String id;
	private String firstName;
	private String lastName;
	private String age;
	private String NBCStatus;
	private String sex;
	private String woundState;
	private String status;
	private String temperature;
	private String spO2;
	private String heartRate;
	private String bloodPressure_SYS;
	private String bloodPressure_DIA;
	private String respirationRate;
	private String painLevel;
	private ArrayList<Note> notes = new ArrayList<Note>();
	
	// webSocketString is a string that contains all information about this patient
	// including notes. Patient balloon WebSocket can extract information about the 
	// patient form this string.
	private String webSocketString;
	private boolean isBuildingString = false;
	private Thread webSocketMsgBuilder;
	
	protected Patient(String patientID) {
		this.id = patientID;
		this.status = "N/A";
		
		this.firstName = "N/A";
		this.lastName = "N/A";
		this.age = "N/A";
		this.NBCStatus = "N/A";
		this.sex = "N/A";
		this.woundState = "white"; // white stands for N/A
		this.status = "N/A";
		
		this.temperature = "N/A";
		this.spO2 = "N/A";
		this.heartRate = "N/A";
		this.bloodPressure_SYS = "N/A";
		this.bloodPressure_DIA = "N/A";
		this.respirationRate = "N/A";
		this.painLevel = "N/A";
	}
	
	protected Patient(String patientID, String patientStatus) {
		this.id = patientID;
		this.status = patientStatus;
		
		this.firstName = "N/A";
		this.lastName = "N/A";
		this.age = "N/A";
		this.NBCStatus = "N/A";
		this.sex = "N/A";
		this.woundState = "white"; // white stands for N/A
		this.status = "N/A";
		
		this.temperature = "N/A";
		this.spO2 = "N/A";
		this.heartRate = "N/A";
		this.bloodPressure_SYS = "N/A";
		this.bloodPressure_DIA = "N/A";
		this.respirationRate = "N/A";
		this.painLevel = "N/A";
	}

	public String getId() {
		return id;
	}

	protected void setId(String id) {
		this.id = id;
	}

	public String getFirstName() {
		return firstName;
	}

	protected void setFirstName(String name) {
		this.firstName = name;
	}

	
	public String getLastName() {
		return lastName;
	}

	protected void setLastName(String name) {
		this.lastName = name;
	}
	
	public String getAge() {
		return age;
	}
	
	/**
	 * Attempts to convert patient's age to integer.
	 * 
	 * @return aged of the patient or -1 on conversion error
	 */
	public int getAgeInt() {
		int age;
		try {
			age = Integer.parseInt(this.age);
		} catch (NumberFormatException e) {
			printDebugMsg(e.toString());
			age = -1;
		}
		return age;
	}

	protected void setAge(String age) {
		try {
			int _age = Integer.parseInt(age);
			if (_age < 0) {
				age = "N/A";
			}
		} catch (NumberFormatException e) {
			printDebugMsg(e.toString());
		}
		this.age = age;
	}

	public String getNBCStatus() {
		return NBCStatus;
	}

	protected void setNBCStatus(String nBCStatus) {
		NBCStatus = nBCStatus;
	}

	public String getSex() {
		return sex;
	}

	protected void setSex(String sex) {
		this.sex = sex;
	}

	public String getWoundState() {
		return woundState;
	}

	protected void setWoundState(String woundState) {
		this.woundState = woundState;
	}

	public String getStatus() {
		return status;
	}

	protected void setStatus(String status) {
		this.status = status;
	}

	public String getTemperature() {
		return temperature;
	}
	
	/**
	 * Attempts to convert patient's temperature to double.
	 * 
	 * @return aged of the patient or -1.0 on conversion error
	 */
	public double getTemperatureDouble() {
		double temp;
		try {
			temp = Double.parseDouble(this.temperature);
		} catch (NumberFormatException e) {
			printDebugMsg(e.toString());
			temp = -1.0;
		}
		return temp;
	}

	protected void setTemperature(String temperature) {
		this.temperature = temperature;
	}

	public String getSpO2() {
		return spO2;
	}
	
	/**
	 * Attempts to convert patient's saturation to double.
	 * 
	 * @return aged of the patient or -1.0 on conversion error
	 */
	public double getSpO2Double() {
		double spO2;
		try {
			spO2 = Double.parseDouble(this.spO2);
		} catch (NumberFormatException e) {
			printDebugMsg(e.toString());
			spO2 = -1.0;
		}
		return spO2;
	}

	protected void setSpO2(String spO2) {
		this.spO2 = spO2;
	}

	public String getHeartRate() {
		return heartRate;
	}
	
	/**
	 * Attempts to convert patient's heart rate to integer.
	 * 
	 * @return aged of the patient or -1 on conversion error
	 */
	public int getHeartRateInt() {
		int hr;
		try {
			hr = Integer.parseInt(this.heartRate);
		} catch (NumberFormatException e) {
			printDebugMsg(e.toString());
			hr = -1;
		}
		return hr;
	}

	protected void setHeartRate(String heartRate) {
		this.heartRate = heartRate;
	}

	public String getBloodPressure_SYS() {
		return bloodPressure_SYS;
	}

	/**
	 * Attempts to convert patient's blood pressure (systolic value) to integer.
	 * 
	 * @return aged of the patient or -1 on conversion error
	 */
	public int getBloodPressure_SYSInt() {
		int bp;
		try {
			bp = Integer.parseInt(this.bloodPressure_SYS);
		} catch (NumberFormatException e) {
			printDebugMsg(e.toString());
			bp = -1;
		}
		return bp;
	}
	
	protected void setBloodPressure_SYS(String bloodPressure_SYS) {
		this.bloodPressure_SYS = bloodPressure_SYS;
	}

	public String getBloodPressure_DIS() {
		return bloodPressure_DIA;
	}
	
	/**
	 * Attempts to convert patient's blood pressure (diastolic value) to integer.
	 * 
	 * @return aged of the patient or -1 on conversion error
	 */
	public int getBloodPressure_DIAInt() {
		int bp;
		try {
			bp = Integer.parseInt(this.bloodPressure_DIA);
		} catch (NumberFormatException e) {
			printDebugMsg(e.toString());
			bp = -1;
		}
		return bp;
	}

	protected void setBloodPressure_DIS(String bloodPressure_DIS) {
		this.bloodPressure_DIA = bloodPressure_DIS;
	}

	public String getRespirationRate() {
		return respirationRate;
	}
	
	/**
	 * Attempts to convert patient's respiration rate to integer.
	 * 
	 * @return aged of the patient or -1.0 on conversion error
	 */
	public double getRespirationRateDouble() {
		double rr;
		try {
			rr = Integer.parseInt(this.respirationRate);
		} catch (NumberFormatException e) {
			printDebugMsg(e.toString());
			rr = -1.0;
		}
		return rr;
	}

	protected void setRespirationRate(String respirationRate) {
		this.respirationRate = respirationRate;
	}

	public String getPainLevel() {
		return painLevel;
	}

	protected void setPainLevel(String painLevel) {
		this.painLevel = painLevel;
	}
	
	protected void addNote(Note note) {
		notes.add(note);
	}
	
	public ArrayList<Note> getNotes() {
		return this.notes;
	}

	/**
	 * If the WebSocket message is being built, this method returns false.
	 * Returns true otherwise.
	 * 
	 * @return
	 */
	protected boolean isWebSocketMsgReady() {
		return this.isBuildingString;
	}
	
	/**
	 * Blocks until WebSocket message is built.
	 */
	protected void waitForSocketMsg() {
		if (webSocketMsgBuilder != null && webSocketMsgBuilder.isAlive()) {
			try {
				webSocketMsgBuilder.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void setWebSocketMsgRead(boolean isReady) {
		this.isBuildingString = isReady;
	}
	
	/**
	 * Returns a WebSocket message. WebSocket String is a string that contains
	 * all information about this patient including notes. Patient balloon
	 * WebSocket can extract information about the patient form this string.
	 * 
	 * @return
	 */
	protected String getWebSocketMsg() {
		if (isWebSocketMsgReady()) {
			return this.webSocketString;
		}
		else {
			waitForSocketMsg();
			return this.webSocketString;
		}
	}
	
	/**
	 * This method should be called when the patient information were updated.
	 */
	protected synchronized void prebuildPatientMsg() {
		webSocketMsgBuilder = new Thread(new AsyncWebSocketMsgBuilder());
		webSocketMsgBuilder.start();
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(id + "\n")
			.append(status + "\n")
			.append(firstName + "\n")
			.append(lastName + "\n")
			.append(age + "\n")
			.append(NBCStatus + "\n")
			.append(sex + "\n")
			.append(woundState + "\n")
			.append(status + "\n")
			.append(temperature + "\n")
			.append(spO2 + "\n")
			.append(heartRate + "\n")
			.append(bloodPressure_SYS + "\n")
			.append(bloodPressure_DIA + "\n")
			.append(respirationRate + "\n")
			.append(painLevel);
		for (Note n : notes) {
			builder.append(n.getNoteType() + "\n")
				.append(n.getNoteLocation() + "\n")
				.append(n.getNoteContent());
		}	
		return builder.toString();
	}
	
	private void printDebugMsg(String msg) {
		if (DEBUG) {
			System.out.print(TAG + ": " + msg);
		}
	}
	
	/**
	 * This class builds a WebSocket message. WebSocket message may contain
	 * notes that may include images encoded using base64 schema. Sometimes it
	 * can take a while to build such a message - this class will prebuild the
	 * message, so that it can be ready when it's needed.
	 * 
	 * @author Pawel
	 * 
	 */
	private class AsyncWebSocketMsgBuilder implements Runnable {
		
		@Override
		public void run() {
			setBusyStatus();
			webSocketString = buildPatientMsg();
			setNotBusyStatus();
		}
		
		private void setBusyStatus() {
			setWebSocketMsgRead(true);
		}
		
		private void setNotBusyStatus() {
			setWebSocketMsgRead(false);
		}
		
		private String buildPatientMsg() {
			StringBuilder msgBuilder = new StringBuilder();
			msgBuilder
					.append("patientData")
					.append("ID ")
					.append(getId() + " ")
					.append("NAME ")
					.append(getLastName() + ","
							+ getFirstName() + " ").append("AGE ")
					.append(getAge() + " ").append("NBC ")
					.append(getNBCStatus() + " ").append("SEX ")
					.append(getSex() + " ").append("WOUNDSTATE ")
					.append(getWoundState() + " ").append("STATUS ")
					.append(getStatus() + " ").append("TEMPERATURE ")
					.append(getTemperature() + " ").append("SPO2 ")
					.append(getSpO2() + " ").append("HEARTRATE ")
					.append(getHeartRate() + " ")
					.append("BLOODPR_DIA ")
					.append(getBloodPressure_DIS() + " ")
					.append("BLOODPR_SYS ")
					.append(getBloodPressure_SYS() + " ")
					.append("RESPIRATION ")
					.append(getRespirationRate() + " ").append("PAIN ")
					.append(getPainLevel() + " ");
			for (Note note : getNotes()) {
				msgBuilder.append("NOTE ").append(
						note.getResponderID() + " " + note.getNoteType() + " "
								+ note.getNoteLocation() + " "
								+ note.getNoteContent() + " ");

			}
			return msgBuilder.toString();
		}
	}
}
