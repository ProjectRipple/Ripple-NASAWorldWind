package pg.ripple.nasa.HTMLBalloon.patientBalloon;

/**
 * This class can modify two html files: linkToPatientBalloon.html and
 * linkToResponderBalloon.html. Both of the html files need to have certain
 * information about patient or cloudlet, before they re-direct to the actual
 * patient/cloudlet html files.
 * 
 * linkToPatientBalloon.html must have information about patient id, as well as
 * latitude and longitude of the patient's location. linkToResponderBalloon.html
 * must have information about cloudlet id
 * 
 * @author Pawel
 * 
 */
public final class HTMLStringModifier {
	
	private HTMLStringModifier() {
		
	}
	
	/**
	 * Returns modified linkToPatientBalloon.html, so that it contains patient
	 * id and patient location.
	 * 
	 * @param htmlFile
	 *            relative path to linkToPatientBalloon.html file
	 * @param id
	 *            id of the patient
	 * @param latitude
	 *            latitude of the patient location
	 * @param longitude
	 *            longitude of the patient location
	 * @return
	 */
	public static String addPatientIdLatLong(String htmlFile, String id, String latitude, String longitude) {
		htmlFile = addId(htmlFile, id);
		htmlFile = addLatitude(htmlFile, latitude);
		htmlFile = addLongitude(htmlFile, longitude);
		return htmlFile;
	}

	/**
	 * Returns modified linkToResponderBalloon.html, so that it contains
	 * cloudlet id.
	 * 
	 * @param htmlFile
	 *            relative path to linkToPatientBalloon.html file
	 * @param id
	 *            id of the patient
	 * @param latitude
	 *            latitude of the patient location
	 * @param longitude
	 *            longitude of the patient location
	 * @return
	 */
	public static String addCloudletId(String htmlFile, String id) {
		htmlFile = addId(htmlFile, id);
		return htmlFile;
	}
	
	private static String addId(String htmlFile, String id) {
		return htmlFile.replace("var id = \"\"", "var id = \"" + id + "\"");
	}
	
	private static String addLatitude(String htmlFile, String latitude) {
		return htmlFile.replace("var lat = \"\"", "var lat = \"" + latitude + "\"");
	}
	
	private static String addLongitude(String htmlFile, String longitude) {
		return htmlFile.replace("var _long = \"\"", "var _long = \"" + longitude + "\"");
	}
}
