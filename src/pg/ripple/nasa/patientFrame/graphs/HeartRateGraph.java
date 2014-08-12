package pg.ripple.nasa.patientFrame.graphs;

import java.awt.Color;
import java.awt.Dimension;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.RectangleInsets;

/**
 * This class defines a JPanel that can display a heart rate graph.
 * 
 * @author Pawel
 * 
 */
public class HeartRateGraph extends JPanel{
	
	private static final long serialVersionUID = 1L;
	
	static {
		ChartFactory.setChartTheme(new StandardChartTheme("JFree/Shadow", true));
	}
	
	private TimeSeries s1;
	private TimeSeriesCollection dataset;
	private String graphTitle;
	
	private ChartPanel chartPanel;
	
	/**
	 * Creates a new heart rate graph.
	 * 
	 * @param graphTitle
	 *            This title will be displayed above the graph
	 * @param preferredDimension
	 */
	public HeartRateGraph(String graphTitle, Dimension preferredDimension) {
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
		JFreeChart chart = ChartFactory.createTimeSeriesChart(graphTitle,
				"Date", "Value", dataset, false, true, false);
		
		chart.setBackgroundPaint(Color.lightGray);
		chart.setAntiAlias(true);
	
		XYPlot plot = (XYPlot) chart.getPlot();
		plot.setBackgroundPaint(Color.black);
		plot.setDomainGridlinePaint(Color.red);
		plot.setRangeGridlinePaint(Color.gray);
		plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
		plot.setDomainCrosshairVisible(true);
        plot.setRangeCrosshairVisible(true);
        
        XYItemRenderer r = plot.getRenderer();
        if (r instanceof XYLineAndShapeRenderer) {
            XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) r;
            renderer.setBaseShapesVisible(true);
            renderer.setBaseShapesFilled(true);
            renderer.setDrawSeriesLineAsPath(true);
        }
        
        DateAxis axis = (DateAxis) plot.getDomainAxis();
        axis.setDateFormatOverride(new SimpleDateFormat("HH:mm:ss"));
		
		return chart;
	}

	/**
	 * Initializes the graph.
	 * 
	 * @return
	 */
	private XYDataset getDataset() {
		s1 = new TimeSeries(graphTitle);
		dataset = new TimeSeriesCollection();
		dataset.addSeries(s1);
		return dataset;
	}
	
	/**
	 * Pushes new data into the graph.
	 * 
	 * @param date Timestamp of new data.
	 * @param value Heart Rate value
	 */
	public void insertData(Date date, double value) {
		s1.addOrUpdate(new Millisecond(date), value);
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
