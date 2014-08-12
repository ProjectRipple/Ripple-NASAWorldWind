package pg.ripple.nasa.patientFrame.mainFrame;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;

import pg.ripple.nasa.HTMLBalloon.cloudletBalloon.Note;
import pg.ripple.nasa.HTMLBalloon.cloudletBalloon.Patient;
import pg.ripple.nasa.patientFrame.graphs.BloodPressureGraph;
import pg.ripple.nasa.patientFrame.graphs.ECGGraph;
import pg.ripple.nasa.patientFrame.graphs.HeartRateGraph;
import pg.ripple.nasa.patientFrame.graphs.RespirationRateGraph;
import pg.ripple.nasa.patientFrame.graphs.TemperatureGraph;
import pg.ripple.nasa.patientFrame.responderNotes.ResponderNotes;

public class PatientWindow {

	private JFrame mainFrame;
	private JLabel firstName;
	private JLabel lastName;
	private JLabel age;
	private JLabel heartRate;
	private JLabel respirationRate;
	private JLabel bpSYS;
	private JLabel bpDIA;
	private JLabel temperature;
	private JLabel triageColor;
	private JPanel centerPanel;
	private JPanel leftPanel_OverView;
	private JPanel navPanel;

	private ResponderNotes responderNotes;
	private HeartRateGraph heartRateGraph;
	private RespirationRateGraph respirationRateGraph;
	private TemperatureGraph temperatureGraph;
	private BloodPressureGraph bloodPressureGraph;
	private ECGGraph ecgGraph;
	
	private final String patientID;
	private JLabel nbc;
	
	public static PatientWindow createPatientWindow(String patientID, String patientStatus, boolean lazyLoad) {
		PatientWindow window = new PatientWindow(patientID, patientStatus,lazyLoad);
		window.mainFrame.setVisible(true);
		return window;
	}

	/**
	 * Create the application.
	 */
	private PatientWindow(String patientID, String patientStatus, boolean lazyLoad) {
		this.patientID = patientID;
		initialize(patientID, patientStatus);
		if (!lazyLoad) {
			responderNotes = new ResponderNotes();
			heartRateGraph = new HeartRateGraph("Heart Rate", new Dimension(500, 300));
			respirationRateGraph = new RespirationRateGraph("Respiration Rate", new Dimension(500, 300));
			temperatureGraph = new TemperatureGraph("Temeprature", new Dimension(500, 300));
			bloodPressureGraph = new BloodPressureGraph("Blood Pressure", new Dimension(500, 300));
			ecgGraph = new ECGGraph("ECG", new Dimension(500, 300));
		}
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize(String patientID, String patientStatus) {
		mainFrame = new JFrame();
		mainFrame.setBounds(100, 100, 700, 400);
		mainFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		mainFrame.getContentPane().setLayout(new BorderLayout(0, 0));
		mainFrame.setAlwaysOnTop(true);
		
		mainFrame.setTitle(patientID +", Status: " + patientStatus);
		
		
		leftPanel_OverView = new JPanel();
		
		leftPanel_OverView.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Patient Info", TitledBorder.CENTER, TitledBorder.TOP, new Font("Tahoma", Font.BOLD, 12), new Color(0, 0, 0)));
		mainFrame.getContentPane().add(leftPanel_OverView, BorderLayout.WEST);
		leftPanel_OverView.setLayout(new BoxLayout(leftPanel_OverView, BoxLayout.Y_AXIS));
		
		JLabel lblFirstName = new JLabel("First Name:");
		lblFirstName.setFont(new Font("Tahoma", Font.BOLD, 12));
		leftPanel_OverView.add(lblFirstName);
		
		firstName = new JLabel("N/A");
		firstName.setFont(new Font("Tahoma", Font.PLAIN, 12));
		leftPanel_OverView.add(firstName);
		
		
		JLabel lblLastName = new JLabel("Last Name:");
		lblLastName.setFont(new Font("Tahoma", Font.BOLD, 12));
		leftPanel_OverView.add(lblLastName);
		
		lastName = new JLabel("N/A");
		lastName.setFont(new Font("Tahoma", Font.PLAIN, 12));
		leftPanel_OverView.add(lastName);
		
		JLabel lblAge = new JLabel("Age:");
		lblAge.setFont(new Font("Tahoma", Font.BOLD, 12));
		leftPanel_OverView.add(lblAge);
		
		age = new JLabel("N/A");
		age.setFont(new Font("Tahoma", Font.PLAIN, 12));
		leftPanel_OverView.add(age);
		
		JLabel lblHeartRate = new JLabel("Heart Rate:");
		lblHeartRate.setFont(new Font("Tahoma", Font.BOLD, 12));
		leftPanel_OverView.add(lblHeartRate);
		
		heartRate = new JLabel("N/A");
		heartRate.setFont(new Font("Tahoma", Font.PLAIN, 12));
		leftPanel_OverView.add(heartRate);
		
		JLabel lblRespirationRate = new JLabel("Respiration Rate:");
		lblRespirationRate.setFont(new Font("Tahoma", Font.BOLD, 12));
		leftPanel_OverView.add(lblRespirationRate);
		
		respirationRate = new JLabel("N/A");
		respirationRate.setFont(new Font("Tahoma", Font.PLAIN, 12));
		leftPanel_OverView.add(respirationRate);
		
		JLabel lblBloodPressure = new JLabel("Blood Pressure:");
		lblBloodPressure.setFont(new Font("Tahoma", Font.BOLD, 12));
		leftPanel_OverView.add(lblBloodPressure);
		
		bpSYS = new JLabel("SYS: N/A");
		bpSYS.setFont(new Font("Tahoma", Font.PLAIN, 12));
		leftPanel_OverView.add(bpSYS);
		
		bpDIA = new JLabel("DIA: N/A");
		bpDIA.setFont(new Font("Tahoma", Font.PLAIN, 12));
		leftPanel_OverView.add(bpDIA);
		
		JLabel lblTemperature = new JLabel("Temperature:");
		lblTemperature.setFont(new Font("Tahoma", Font.BOLD, 12));
		leftPanel_OverView.add(lblTemperature);
		
		temperature = new JLabel("N/A");
		temperature.setFont(new Font("Tahoma", Font.PLAIN, 12));
		leftPanel_OverView.add(temperature);
		
		JLabel lblTriageColor = new JLabel("Triage Color:");
		lblTriageColor.setFont(new Font("Tahoma", Font.BOLD, 12));
		leftPanel_OverView.add(lblTriageColor);
		
		triageColor = new JLabel("                     ");
		triageColor.setFont(new Font("Tahoma", Font.PLAIN, 12));
		triageColor.setOpaque(true);
		triageColor.setBackground(Color.LIGHT_GRAY);
		leftPanel_OverView.add(triageColor);
		
		JLabel lblNbc = new JLabel("NBC:");
		lblNbc.setFont(new Font("Tahoma", Font.BOLD, 12));
		leftPanel_OverView.add(lblNbc);
		
		nbc = new JLabel("N/A");
		nbc.setFont(new Font("Tahoma", Font.PLAIN, 12));
		leftPanel_OverView.add(nbc);
		
		centerPanel = new JPanel();
		mainFrame.getContentPane().add(centerPanel, BorderLayout.CENTER);
		GridBagLayout gbl_centerPanel = new GridBagLayout();
		gbl_centerPanel.columnWidths = new int[]{0};
		gbl_centerPanel.rowHeights = new int[]{0};
		gbl_centerPanel.columnWeights = new double[]{Double.MIN_VALUE};
		gbl_centerPanel.rowWeights = new double[]{Double.MIN_VALUE};
		centerPanel.setLayout(gbl_centerPanel);
		
		navPanel = new JPanel();
		navPanel.setBorder(new TitledBorder(null, "", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		mainFrame.getContentPane().add(navPanel, BorderLayout.SOUTH);
		
		JButton btnResponderNotes = new JButton("Responder Notes");
		btnResponderNotes.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (centerPanel.getComponentCount() != 0 && centerPanel.getComponent(0).equals(responderNotes)) {
					return;
				}
				centerPanel.removeAll();
				if (responderNotes == null) {
					responderNotes = new ResponderNotes();
					responderNotes.setVisible(true);
					centerPanel.add(responderNotes);
					mainFrame.revalidate();
					mainFrame.repaint();
				} else {
					centerPanel.add(responderNotes);
					centerPanel.revalidate();
					centerPanel.repaint();
				}
			}
		});
		navPanel.add(btnResponderNotes);
		
		JButton btnHrGraph = new JButton("HR Graph");
		btnHrGraph.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (centerPanel.getComponentCount() != 0 && centerPanel.getComponent(0).equals(heartRateGraph)) {
					return;
				}
				centerPanel.removeAll();
				if (heartRateGraph == null) {
					heartRateGraph = new HeartRateGraph("Heart Rate", new Dimension(500, 300));
					centerPanel.add(heartRateGraph);
					mainFrame.revalidate();
					mainFrame.repaint();
				} else {
					centerPanel.add(heartRateGraph);
					centerPanel.revalidate();
					centerPanel.repaint();
				}
			}
		});
		navPanel.add(btnHrGraph);
		
		JButton btnRrGraph = new JButton("RR Graph");
		btnRrGraph.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (centerPanel.getComponentCount() != 0 && centerPanel.getComponent(0).equals(respirationRateGraph)) {
					return;
				}
				centerPanel.removeAll();
				if (respirationRateGraph == null) {
					respirationRateGraph = new RespirationRateGraph("Respiration Rate", new Dimension(500, 300));
					centerPanel.add(respirationRateGraph);
					mainFrame.revalidate();
					mainFrame.repaint();
				} else {
					centerPanel.add(respirationRateGraph);
					centerPanel.revalidate();
					centerPanel.repaint();
				}
			}
		});
		navPanel.add(btnRrGraph);
		
		JButton btnTempGraph = new JButton("Temp. Graph");
		btnTempGraph.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (centerPanel.getComponentCount() != 0 && centerPanel.getComponent(0).equals(temperatureGraph)) {
					return;
				}
				centerPanel.removeAll();
				if (temperatureGraph == null) {
					temperatureGraph = new TemperatureGraph("Temperature", new Dimension(500, 300));
					centerPanel.add(temperatureGraph);
					mainFrame.revalidate();
					mainFrame.repaint();
				} else {
					centerPanel.add(temperatureGraph);
					centerPanel.revalidate();
					centerPanel.repaint();
				}
			}
		});
		navPanel.add(btnTempGraph);
		
		JButton btnBpGraph = new JButton("BP Graph");
		btnBpGraph.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (centerPanel.getComponentCount() != 0 && centerPanel.getComponent(0).equals(bloodPressureGraph)) {
					return;
				}
				centerPanel.removeAll();
				if (bloodPressureGraph == null) {
					bloodPressureGraph = new BloodPressureGraph("Blood Pressure", new Dimension(500, 300));
					centerPanel.add(bloodPressureGraph);
					mainFrame.revalidate();
					mainFrame.repaint();
				} else {
					centerPanel.add(bloodPressureGraph);
					centerPanel.revalidate();
					centerPanel.repaint();
				}
			}
		});
		navPanel.add(btnBpGraph);
		
		JButton btnEcgGraph = new JButton("ECG Graph");
		btnEcgGraph.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (centerPanel.getComponentCount() != 0 && centerPanel.getComponent(0).equals(ecgGraph)) {
					return;
				}
				centerPanel.removeAll();
				if (ecgGraph == null) {
					ecgGraph = new ECGGraph("ECG", new Dimension(500, 300));
					centerPanel.add(ecgGraph);
					mainFrame.revalidate();
					mainFrame.repaint();
				} else {
					centerPanel.add(ecgGraph);
					centerPanel.revalidate();
					centerPanel.repaint();
				}
			}
		});
		navPanel.add(btnEcgGraph);
	}
	
	public void updatePatientInfo(final Patient patient) {
		if (patient == null) {
			return;
		}
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					setFirstName(patient.getFirstName());
					setLastName(patient.getLastName());
					setAge(patient.getAge());
					setHeartRate(patient.getHeartRate());
					setRespirationRate(patient.getRespirationRate());
					setBpDIA(patient.getBloodPressure_DIS());
					setBpSYS(patient.getBloodPressure_SYS());
					setTemperature(patient.getTemperature());
					setNBCStatus(patient.getNBCStatus());
					setPatientStatus(patient.getStatus());
					setTriageColor(patient.getWoundState());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	public void updateHRGraph(Date date, int value) {
		if (heartRateGraph == null || value < 0) {
			return;
		}
		heartRateGraph.insertData(date, value);
	}
	
	public void updateRRGraph(Date date, double value) {
		if (respirationRateGraph == null || value < 0.0) {
			return;
		}
		respirationRateGraph.insertData(date, value);
	}
	
	public void updateTempGraph(Date date, double value) {
		if (temperatureGraph == null || value < 0.0) {
			return;
		}
		temperatureGraph.insertData(date, value);
	}
	
	public void updateBPGraph(Date date, int sys, int dia) {
		if (bloodPressureGraph == null || dia < 0 || sys < 0) {
			return;
		}
		bloodPressureGraph.insertData(date, sys, dia);
	}
	
	public void updateECGGraph(double[] data) {
		if (ecgGraph == null) {
			return;
		}	
		ecgGraph.insertData(data);
	}
	
	public void updateNotes(ArrayList<Note> notes) {
		if (responderNotes == null) {
			return;
		}
		responderNotes.addNotes(notes);
	}
	
	public void setPatientStatus(String patientStatus) {
		mainFrame.setTitle(patientID + ", STATUS: " + patientStatus);
	}

	public void setFirstName(String firstName) {
		this.firstName.setText(firstName);
	}

	public void setLastName(String lastName) {
		this.lastName.setText(lastName);
	}

	public void setAge(String age) {
		this.age.setText(age);
	}

	public void setHeartRate(String heartRate) {
		this.heartRate.setText(heartRate);
	}

	public void setRespirationRate(String respirationRate) {
		this.respirationRate.setText(respirationRate);
	}

	public void setBpSYS(String bpSYS) {
		this.bpSYS.setText(bpSYS);
	}

	public void setBpDIA(String bpDIA) {
		this.bpDIA.setText(bpDIA);
	}

	public void setTemperature(String temperature) {
		this.temperature.setText(temperature);
	}

	public void setTriageColor(String triageColor) {
		Color color = Color.lightGray;
		if (triageColor.toUpperCase().equals("RED")) {
			color = Color.red;
		} else if (triageColor.toUpperCase().equals("YELLOW")) {
			color = Color.yellow;
		} else if (triageColor.toUpperCase().equals("GREEN")) {
			color = Color.green;
		} else if (triageColor.toUpperCase().equals("BLACK")) {
			color = Color.black;
		}
		this.triageColor.setBackground(color);
	}

	public void setNBCStatus(String nbc) {
		this.nbc.setText(nbc);
	}
	
	public String getPatientID() {
		return this.patientID;
	}
	
}
