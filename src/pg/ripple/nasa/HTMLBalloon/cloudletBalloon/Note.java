package pg.ripple.nasa.HTMLBalloon.cloudletBalloon;

/**
 * This class is a container for all information associated with a note made by
 * the first responder.
 * 
 * @author Pawel
 * 
 */
public final class Note {
	private String responderID;
	private NoteType noteType;
	private NoteLocation noteLocation;
	private String noteContent;
	
	/**
	 * There are two type of notes: text note or image note.
	 * 
	 * @author Pawel
	 * 
	 */
	public enum NoteType {
		TEXT,
		IMAGE
	}
	
	/**
	 * This enum type defines the body location that the note is referring to.
	 * 
	 * @author Pawel
	 * 
	 */
	public enum NoteLocation {
		GENERAL, 
		FRONT_HEAD, 
		FRONT_TORSO, 
		FRONT_RIGHT_ARM, 
		FRONT_LEFT_ARM, 
		FRONT_RIGHT_LEG, 
		FRONT_LEFT_LEG
	}
	
	/**
	 * Creates a new note.
	 * 
	 * @param responderID
	 *            ID of the responder that created a note
	 * @param noteType
	 *            see NoteTyp
	 * @param noteLocation
	 *            see NoteLocation
	 * @param noteContent
	 *            a message that the note carries. If this is an image note,
	 *            this string should contain an image encoded with base64
	 *            scheme.
	 */
	protected Note(String responderID, NoteType noteType, NoteLocation noteLocation, String noteContent) {
		this.responderID = responderID;
		this.noteType = noteType;
		this.noteLocation = noteLocation;
		this.noteContent = noteContent;
	}

	public String getResponderID() {
		return responderID;
	}

	protected void setResponderID(String responderID) {
		this.responderID = responderID;
	}

	public NoteType getNoteType() {
		return noteType;
	}

	protected void setNoteType(NoteType noteType) {
		this.noteType = noteType;
	}

	public NoteLocation getNoteLocation() {
		return noteLocation;
	}

	protected void setNoteLocation(NoteLocation noteLocation) {
		this.noteLocation = noteLocation;
	}

	/**
	 * If this note is of text type, this method returns a message that this
	 * note carries. If this note is of image type, this method returns a String
	 * that represents image encoded with base64 scheme.
	 * 
	 * @return
	 */
	public String getNoteContent() {
		return noteContent;
	}

	protected void setNoteContent(String noteContent) {
		this.noteContent = noteContent;
	}
	
	
}
