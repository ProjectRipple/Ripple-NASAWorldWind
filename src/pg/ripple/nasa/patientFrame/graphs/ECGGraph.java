package pg.ripple.nasa.patientFrame.graphs;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleInsets;

/**
 * This class defines a JPanel that can display a ecg graph.
 * 
 * TODO: IT HAS NEVER BEEN TESTED WITH REAL DATA.
 * 
 * @author Pawel
 * 
 */
public class ECGGraph extends JPanel{	
	private static final long serialVersionUID = 1L;
	
	private int dataSampleCount = 0;
	
	static {
		ChartFactory.setChartTheme(new StandardChartTheme("JFree/Shadow", true));
	}

	private XYSeries s1;
	private XYSeriesCollection dataset;
	private String graphTitle;
	
	private ChartPanel chartPanel;
	
	/**
	 * Creates a new ECG graph.
	 * 
	 * @param graphTitle
	 *            This title will be displayed above the graph
	 * @param preferredDimension
	 */
	public ECGGraph(String graphTitle, Dimension preferredDimension) {
		super();
		this.graphTitle = graphTitle;
		
		chartPanel = (ChartPanel) createDemoPanel();
		chartPanel.setPreferredSize(preferredDimension);
		this.add(chartPanel);
	}
	
	/**
	 * Initializes the graph.
	 * 
	 * @return
	 */
	private ChartPanel createDemoPanel() {
		JFreeChart chart = createChart(getDataset());
        ChartPanel panel = new ChartPanel(chart);
        panel.setFillZoomRectangle(true);
        panel.setMouseWheelEnabled(true);
        return panel;
	}
	
	/**
	 * Initializes the graph.
	 * 
	 * @return
	 */
	private JFreeChart createChart(XYDataset dataset) {	
		JFreeChart chart = ChartFactory.createXYLineChart(graphTitle,
				"Sample No.", "Value", dataset, PlotOrientation.VERTICAL,
				false, true, false);
		
		chart.setBackgroundPaint(Color.lightGray);
		chart.setAntiAlias(true);
	
		XYPlot plot = (XYPlot) chart.getXYPlot();
		plot.setBackgroundPaint(Color.black);
		plot.setDomainGridlinePaint(Color.red);
		plot.setRangeGridlinePaint(Color.gray);
		plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
		plot.setDomainCrosshairVisible(true);
        plot.setRangeCrosshairVisible(true);
        
        XYItemRenderer r = plot.getRenderer();
        if (r instanceof XYLineAndShapeRenderer) {
            XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) r;
            renderer.setBaseShapesFilled(true);
            renderer.setDrawSeriesLineAsPath(true);
        }
        
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		
		return chart;
	}

	/**
	 * Initializes the graph.
	 * 
	 * @return
	 */
	private XYDataset getDataset() {
		s1 = new XYSeries(graphTitle);
		dataset = new XYSeriesCollection();
		dataset.addSeries(s1);
		return dataset;
	}
	
	/**
	 * Pushes new data into the graph.
	 * 
	 * @param value An array of double that contains new ecg readings.
	 */
	public void insertData(double[] value) {
		for (double sample : value) {
			s1.addOrUpdate(dataSampleCount++, sample);
		}
		this.invalidate();
	}
	
	/**
	 * Deletes all the data from this graph. Clears and resets the graph.
	 */
	public void clear() {
		s1.clear();
		this.invalidate();
	}
}
