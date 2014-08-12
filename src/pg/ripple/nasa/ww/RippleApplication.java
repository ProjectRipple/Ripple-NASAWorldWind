package pg.ripple.nasa.ww;

import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.Model;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.awt.WorldWindowGLCanvas;
import gov.nasa.worldwind.event.RenderingExceptionListener;
import gov.nasa.worldwind.event.SelectEvent;
import gov.nasa.worldwind.event.SelectListener;
import gov.nasa.worldwind.exception.WWAbsentRequirementException;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.CompassLayer;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.LayerList;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.layers.ViewControlsLayer;
import gov.nasa.worldwind.layers.ViewControlsSelectListener;
import gov.nasa.worldwind.layers.WorldMapLayer;
import gov.nasa.worldwind.layers.placename.PlaceNameLayer;
import gov.nasa.worldwind.render.AbstractBrowserBalloon;
import gov.nasa.worldwind.render.AbstractBrowserBalloon.BrowserControl;
import gov.nasa.worldwind.render.BalloonAttributes;
import gov.nasa.worldwind.render.BasicBalloonAttributes;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.GlobeAnnotation;
import gov.nasa.worldwind.render.GlobeBrowserBalloon;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.Offset;
import gov.nasa.worldwind.render.PointPlacemark;
import gov.nasa.worldwind.render.PointPlacemarkAttributes;
import gov.nasa.worldwind.render.Renderable;
import gov.nasa.worldwind.render.ShapeAttributes;
import gov.nasa.worldwind.render.Size;
import gov.nasa.worldwind.render.SurfaceCircle;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.StatisticsPanel;
import gov.nasa.worldwind.util.StatusBar;
import gov.nasa.worldwind.util.WWIO;
import gov.nasa.worldwind.util.WWUtil;
import gov.nasa.worldwindx.examples.ClickAndGoSelectListener;
import gov.nasa.worldwindx.examples.Placemarks;
import gov.nasa.worldwindx.examples.util.BalloonController;
import gov.nasa.worldwindx.examples.util.HighlightController;
import gov.nasa.worldwindx.examples.util.HotSpotController;
import gov.nasa.worldwindx.examples.util.ToolTipController;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Random;

import javax.media.opengl.GL;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import pg.ripple.nasa.HTMLBalloon.patientBalloon.HTMLStringModifier;
import pg.ripple.nasa.HTMLBalloon.patientBalloon.PatientBalloonNotifier;

public class RippleApplication
{
	
    public static class RipplePanel extends JPanel 
    {
    	protected WorldWindow wwd; 
    	protected StatusBar statusBar;
    	protected ToolTipController toolTipController;
    	protected HighlightController highlightController;
    	
    	public RipplePanel(Dimension canvasSize, boolean includeStatusBar) {
    		super(new BorderLayout());
    		
    		this.wwd = this.createWorldWindow();
    		((Component) this.wwd).setPreferredSize(canvasSize);
    		
    		Model m = (Model) WorldWind.createConfigurationComponent(AVKey.MODEL_CLASS_NAME);
    		this.wwd.setModel(m);
    		
    		this.wwd.addSelectListener(new ClickAndGoSelectListener(this.getWwd(), WorldMapLayer.class));
    		
    		this.add((Component) this.wwd, BorderLayout.CENTER);
    		if (includeStatusBar) {
    			this.statusBar = new StatusBar();
    			this.add(statusBar, BorderLayout.PAGE_END);
    			this.statusBar.setEventSource(wwd);
    		}
    		
    		this.toolTipController = new ToolTipController(this.getWwd(), AVKey.DISPLAY_NAME, null);
    		this.highlightController = new HighlightController(this.getWwd(), SelectEvent.ROLLOVER);
    	}
    	
    	protected WorldWindow createWorldWindow() {
    		return new WorldWindowGLCanvas();
    	}
    	
        public WorldWindow getWwd()
        {
            return wwd;
        }

        public StatusBar getStatusBar()
        {
            return statusBar;
        }
    }
    
    public static class RippleFrame extends JFrame {
    	Rectangle screenBounds = GraphicsEnvironment
				.getLocalGraphicsEnvironment().getMaximumWindowBounds();
    	
    	private Dimension canvasSize = new Dimension(2*screenBounds.width/3, screenBounds.height - 100);
    	
    	protected RipplePanel wwjPanel;
    	protected RippleLayerPanel layerPanel;
    	protected StatisticsPanel statsPanel;
    	
    	protected HotSpotController hotSpotController;
        protected BalloonController balloonController;
    	
    	public RippleFrame() {
    		this.initialize(true, true, true);
    	}
    	
    	public RippleFrame(Dimension size) {
    		this.canvasSize = size;
    		this.initialize(true, true, true);
    	}
    	
    	public RippleFrame(boolean includeStatusBar, boolean includeLayerPanel, boolean includeStatsPanel) {
            this.initialize(includeStatusBar, includeLayerPanel, includeStatsPanel);
        }
    	
    	
    	public Layer addBalloon(String htmlFile, String id, Position balloonPosition, String ballonName) {
    		String htmlString = null;
    		InputStream contentStream = null;
    		
    		try {
                contentStream = WWIO.openFileOrResourceStream(htmlFile, this.getClass());
                htmlString = WWIO.readStreamToString(contentStream, null);
    		} catch (Exception e) {
    			e.printStackTrace();
    		} finally {
    			WWIO.closeStream(contentStream, "Ripple HTML Balloon Stream");
    		}
    		
    		if (htmlString == null) {
                htmlString = Logging.getMessage("generic.ExceptionAttemptingToReadFile", "Ripple HTML Balloon Stream");
    		}
             
    		// add id, longitude, and latitude information to the htmlString
    		htmlString = HTMLStringModifier.addPatientIdLatLong(htmlString, id, balloonPosition.getLatitude().degrees + "", balloonPosition.getLongitude().degrees + "");
    		
    		GlobeBrowserBalloon balloon = new GlobeBrowserBalloon(htmlString, balloonPosition);
    		balloon.setVisibilityAction(AVKey.VISIBILITY_ACTION_RELEASE);
    		balloon.setVisible(false);
    		balloon.removeAllBrowserControls();
            balloon.addBrowserControl(new BrowserControl(AVKey.CLOSE, new Offset(30.0, 25.0, AVKey.INSET_PIXELS, AVKey.INSET_PIXELS),
                "images/browser-close-16x16.gif"));
    		
            
    		
    		BalloonAttributes attrs = new BasicBalloonAttributes();
            attrs.setSize(new Size(Size.EXPLICIT_DIMENSION, 680,  AVKey.PIXELS, Size.EXPLICIT_DIMENSION, 440,  AVKey.PIXELS));
            
            balloon.setAttributes(attrs);
            balloon.setAlwaysOnTop(true);
            
            PointPlacemark placemark = new PointPlacemark(balloonPosition);
            placemark.setLabelText(ballonName); 
            placemark.setLineEnabled(true);
            
            placemark.setValue(AVKey.BALLOON, balloon);
            PointPlacemarkAttributes placemarkAttrs = new PointPlacemarkAttributes();
            placemarkAttrs.setImageAddress("htmlBalloonsIcons/ballon_pin.png");
            placemarkAttrs.setAntiAliasHint(GL.GL_NICEST);
            placemark.setAttributes(placemarkAttrs);
            
            RenderableLayer layer = new RenderableLayer();
            layer.setName(ballonName);
            layer.addRenderable(balloon);
            layer.addRenderable(placemark);
            
            layer.setMaxActiveAltitude(3000.0);     
            
            this.layerPanel.addRippleLayer(this.wwjPanel.getWwd(), layer, balloon);
            
    		return layer;
    	}
    	
    	/**
    	 * Removes the balloonLayer from the list of layers (don't try to remove
    	 * other RippleLayers with this function). Causes to the layer to disappear. 
    	 * @param balloonLayer
    	 */
    	public void removeBalloon(Layer balloonLayer) {
    		this.layerPanel.removeRippleLayer(this.wwjPanel.getWwd(), balloonLayer);
    	}
    	
    	public Layer addCloudletBalloon(String htmlFile, String id, LatLon balloonPosition, String ballonName) {
    		// create the layer
            RenderableLayer summaryBalloonLayer = new RenderableLayer();
            
            // read in html file
    		String htmlString = null;
    		InputStream contentStream = null;
    		
    		try {
                contentStream = WWIO.openFileOrResourceStream(htmlFile, this.getClass());
                htmlString = WWIO.readStreamToString(contentStream, null);
    		} catch (Exception e) {
    			e.printStackTrace();
    		} finally {
    			WWIO.closeStream(contentStream, "Ripple HTML Balloon Stream");
    		}
    		
    		if (htmlString == null) {
                htmlString = Logging.getMessage("generic.ExceptionAttemptingToReadFile", "Ripple HTML Balloon Stream");
    		}
             
    		// add id to the htmlString
    		htmlString = HTMLStringModifier.addCloudletId(htmlString, id);
    		
    		GlobeBrowserBalloon balloon = new GlobeBrowserBalloon(htmlString, new Position(balloonPosition, 0.1));
    		balloon.setVisibilityAction(AVKey.VISIBILITY_ACTION_RELEASE);
    		balloon.setVisible(false);
    		balloon.removeAllBrowserControls();
            balloon.addBrowserControl(new BrowserControl(AVKey.CLOSE, new Offset(30.0, 25.0, AVKey.INSET_PIXELS, AVKey.INSET_PIXELS),
                "images/browser-close-16x16.gif"));
            
    		
    		BalloonAttributes attrs = new BasicBalloonAttributes();
            attrs.setSize(new Size(Size.EXPLICIT_DIMENSION, 675,  AVKey.PIXELS, Size.EXPLICIT_DIMENSION, 495,  AVKey.PIXELS));
            
            balloon.setAttributes(attrs);
            
            PointPlacemark placemark = new PointPlacemark(new Position(balloonPosition, 0.1));
            placemark.setLabelText(ballonName); 
            placemark.setLineEnabled(true);
            
            placemark.setValue(AVKey.BALLOON, balloon);
            PointPlacemarkAttributes placemarkAttrs = new PointPlacemarkAttributes();
            placemarkAttrs.setImageAddress("htmlBalloonsIcons/balloon_responder_pin.png");
            placemarkAttrs.setAntiAliasHint(GL.GL_NICEST);
            placemark.setAttributes(placemarkAttrs);
    
            summaryBalloonLayer.addRenderable(balloon);
            summaryBalloonLayer.addRenderable(placemark);
            summaryBalloonLayer.setMaxActiveAltitude(3000);
            
            // draw the layer
            insertBeforeCompass(this.wwjPanel.getWwd(), summaryBalloonLayer);
            
    		return summaryBalloonLayer;
    	}
    	
    	/**
    	 * Creates transparent circle with given radius, at the given position.
    	 * To delete remove from the screen see removeCloudLetCircle method.
    	 * 
    	 * @param radius
    	 * @param position
    	 * 
    	 * @return Layer
    	 */
    	public Layer addCloudLetCircle(double radius, LatLon position, String id) {
    		// create the layer
            RenderableLayer cloudletLayer = new RenderableLayer();
            
            // we don't want the layer to be selectable
            cloudletLayer.setPickEnabled(false);
            
            // self-explanatory
            ShapeAttributes normalAttribitues = new BasicShapeAttributes();
            normalAttribitues.setInteriorMaterial(Material.DARK_GRAY);
            normalAttribitues.setInteriorOpacity(0.35);
            normalAttribitues.setOutlineMaterial(Material.RED);
            
            // create the actual circle and add it to the layer
            SurfaceCircle circle = new SurfaceCircle(normalAttribitues, position, radius, 80 );
            circle.setAttributes(normalAttribitues);
            cloudletLayer.addRenderable(circle);
            
            // since the circle itself is really small, lets create samll balloon,
            // that will indicate the position of the cloudlet when it is 
            // the screen is zoom-out
            GlobeAnnotation globeAnnotation = new GlobeAnnotation("Ripple Cloudlet.\nID: " + id, new Position(position, 0.1), Font.decode("Arial-BOLD-13"));
            globeAnnotation.getAttributes().setBorderColor(Color.RED);
            globeAnnotation.getAttributes().setBackgroundColor(Color.WHITE);
            globeAnnotation.getAttributes().setBorderWidth(2.0);
            globeAnnotation.setMinActiveAltitude(3000);
            globeAnnotation.getAttributes().setSize(new Dimension(400, 80));
            
            // attach annotation to the layer
            cloudletLayer.addRenderable(globeAnnotation);
            
            // add "summary" balloon
            // create layer for the balloon and its icon
//            RenderableLayer summaryBalloonLayer = new RenderableLayer();
//            summaryBalloonLayer.setPickEnabled(true);
//            ArrayList<Renderable> summaryBallon = addCloudletBalloon("linkToResponderBalloon.html", id, new Position(position, 0.1), "Cloudlet summary");
//            for (Renderable renderable : summaryBallon) {
//            	//cloudletLayer.addRenderable(renderable);
//            	summaryBalloonLayer.addRenderable(renderable);
//            }
            
            // draw circle layer on the screen
            insertBeforeCompass(this.wwjPanel.getWwd(), cloudletLayer);
//            // draw balloon layer on the screen
//            insertBeforeCompass(this.wwjPanel.getWwd(), summaryBalloonLayer);
            
            
            return cloudletLayer;
    	}
    	
    	/**
    	 * Removes the cloudletCircle from the list of layers (don't try to remove
    	 * other RippleLayers with this function). Causes to the layer to disappear. 
    	 * @param cloudletCircle
    	 */
    	public void removeCloudLetCircle(Layer cloudletCircle) {
    		this.wwjPanel.getWwd().getModel().getLayers().remove(cloudletCircle);
    	}
    	
    	public void removeSummaryBalloonLayer(Layer summaryBalloon) {
    		this.wwjPanel.getWwd().getModel().getLayers().remove(summaryBalloon);
    	}
    	
    	/**
    	 * Updates the location of cloudletCircle
    	 * 
    	 * @param cloudletCircle Layer that has the cloudletCircle
    	 * @param position new location of cloudletCircle
    	 */
    	public void updateCloudLetCircleLocation(Layer cloudletCircle, LatLon position) {
    		for (Renderable renderable : ((RenderableLayer)cloudletCircle).getRenderables()) {
    			if (renderable instanceof SurfaceCircle) {
    				((SurfaceCircle)renderable).setCenter(position);
    			} else if (renderable instanceof GlobeAnnotation) {
    				((GlobeAnnotation)renderable).setPosition(new Position(position, 0.1));
    			}
    		}
    		// repaint the World Wind view
    		this.wwjPanel.paint(wwjPanel.getGraphics());
    	}
    	
    	public void updateSummaryBalloonLocation(Layer summaryBalloon, LatLon position) {
    		for (Renderable renderable : ((RenderableLayer)summaryBalloon).getRenderables()) {
    			if (renderable instanceof GlobeBrowserBalloon) {
    				((GlobeBrowserBalloon)renderable).setPosition(new Position(position, 0.1));
    			} else if (renderable instanceof PointPlacemark) {
    				((PointPlacemark)renderable).setPosition(new Position(position, 0.1));
    			}
    		}
    		// repaint the World Wind view
    		this.wwjPanel.paint(wwjPanel.getGraphics());
    	}
    	
    	/**
    	 * This is extremely inefficient way of moving all the patient balloons. The person who wrote this
    	 * code is ashamed of himself, but he's also too lazy to fix it.
    	 *  
    	 * @param patientBalloons - Layers that will be moved
    	 * @param newLatLon - new location of the cloudlet center
    	 * @param oldLatLon - old location of the cloudlet center
    	 * 
    	 * @author Wishes to remain anonymous
    	 */
    	public void updatePatientBalloonsLocation(ArrayList<Layer> patientBalloons, LatLon newLatLon, LatLon oldLatLon) {
    		Position newPosition = new Position(newLatLon, 0.1);
    		Position oldPosition = new Position(oldLatLon, 0.1);
    		ArrayList<Position> tmp = new ArrayList<Position>();
    		for (Layer layer : patientBalloons) {
    			for (Renderable renderable : ((RenderableLayer)layer).getRenderables()) {
    				if (renderable instanceof GlobeBrowserBalloon) {
    					Position oldBalloonPosition = ((GlobeBrowserBalloon)renderable).getPosition();
    					tmp.add(oldBalloonPosition);
    					Position newBalloonPosition = (Position.computeShiftedPositions(oldPosition, newPosition, tmp)).get(0);
        				((GlobeBrowserBalloon)renderable).setPosition(newBalloonPosition);
        				tmp.clear();
        			} else if (renderable instanceof PointPlacemark) {
        				Position oldBalloonPosition = ((PointPlacemark)renderable).getPosition();
    					tmp.add(oldBalloonPosition);
    					Position newBalloonPosition = (Position.computeShiftedPositions(oldPosition, newPosition, tmp)).get(0);
        				((PointPlacemark)renderable).setPosition(newBalloonPosition);
        				tmp.clear();
        			}
    			}
    		}
    		// repaint the World Wind view
    		this.wwjPanel.paint(wwjPanel.getGraphics());
    	}
    	
    	protected void initialize(boolean includeStatusBar, boolean includeLayerPanel, boolean includeStatusPanel) {
            this.wwjPanel = this.createRipplePanel(this.canvasSize, includeStatusPanel);
            this.wwjPanel.setPreferredSize(canvasSize);
            
            // Add a controller to send input events to BrowserBalloons.
            this.hotSpotController = new HotSpotController(this.getWwd());
            // Add a controller to handle link and navigation events in BrowserBalloons.
            this.balloonController = new BalloonController(this.getWwd());
            
            
            this.getContentPane().add(wwjPanel, BorderLayout.CENTER);
            if (includeLayerPanel) {
                String[] generalMapsSatellite = new String[]{
                        "Blue Marble May 2004",
                        "i-cubed Landsat",
                        "USDA NAIP",
                        "MS Virtual Earth Aerial",
                        "Bing Imagery"
                };
                String[] generalMapsTopography = new String[]{
                        "USGS Topographic Maps 1:250K",
                        "USGS Topographic Maps 1:100K",
                        "USGS Topographic Maps 1:24K",
                        "Open Street Map",
                        "FAA Sectionals",
                        "MS Virtual Earth Roads"
                };
                String[] generalMapsMixed = new String[]{
                        "MS Virtual Earth Hybrid"
                };
                String[] mapUtils = new String[]{
                        "Political Boundaries",
                        "Place Names",
                        "World Map",
                        "Scale bar",
                        "Compass",
                        "Crosshairs",
                        "Lat-Lon Graticule"
                };
                
                this.layerPanel = new RippleLayerPanel(this.wwjPanel.getWwd(), null, true);
                this.layerPanel.addTabbedPanel(this.wwjPanel.getWwd(), new Dimension(this.wwjPanel.getPreferredSize().width/3, this.wwjPanel.getPreferredSize().height/3), "Maps utils", mapUtils);
                this.layerPanel.addTabbedPanel(this.wwjPanel.getWwd(), new Dimension(this.wwjPanel.getPreferredSize().width/3, this.wwjPanel.getPreferredSize().height/3), "Hybrid maps", generalMapsMixed);
                this.layerPanel.addTabbedPanel(this.wwjPanel.getWwd(), new Dimension(this.wwjPanel.getPreferredSize().width/3, this.wwjPanel.getPreferredSize().height/3), "Topography maps", generalMapsTopography);
                this.layerPanel.addTabbedPanel(this.wwjPanel.getWwd(), new Dimension(this.wwjPanel.getPreferredSize().width/3, this.wwjPanel.getPreferredSize().height/3), "Satellite maps", generalMapsSatellite);                
           
                this.layerPanel.createRipplePanel(this.wwjPanel.getWwd(), new Dimension(this.wwjPanel.getPreferredSize().width/3, this.wwjPanel.getPreferredSize().height/2));
                this.getContentPane().add(this.layerPanel, BorderLayout.WEST); 
            }
           
            
            ViewControlsLayer viewControlsLayer = new ViewControlsLayer();
            insertBeforeCompass(getWwd(), viewControlsLayer);
            this.getWwd().addSelectListener(new ViewControlsSelectListener(this.getWwd(), viewControlsLayer));
            
            this.wwjPanel.getWwd().addRenderingExceptionListener(new RenderingExceptionListener()
            {
                public void exceptionThrown(Throwable t)
                {
                    if (t instanceof WWAbsentRequirementException)
                    {
                        String message = "Computer does not meet minimum graphics requirements.\n";
                        message += "Please install up-to-date graphics driver and try again.\n";
                        message += "Reason: " + t.getMessage() + "\n";
                        message += "This program will end when you press OK.";

                        JOptionPane.showMessageDialog(RippleFrame.this, message, "Unable to Start Program",
                            JOptionPane.ERROR_MESSAGE);
                        System.exit(-1);
                    }
                }
            });
            
            for (Layer layer : this.wwjPanel.getWwd().getModel().getLayers())
            {
                if (layer instanceof SelectListener)
                {
                    this.getWwd().addSelectListener((SelectListener) layer);
                }
            }
            
            this.pack();
            
            WWUtil.alignComponent(null, this, AVKey.CENTER);
            this.setResizable(true);
    	}
    	
    	protected RipplePanel createRipplePanel(Dimension canvasSize, boolean includeStatusBar)
        {
            return new RipplePanel(canvasSize, includeStatusBar);
        }
    	
    	public Dimension getCanvasSize()
        {
            return canvasSize;
        }
    	
    	public RipplePanel getWwjPanel()
        {
            return wwjPanel;
        }
    	
    	public WorldWindow getWwd()
        {
            return this.wwjPanel.getWwd();
        }

        public StatusBar getStatusBar()
        {
            return this.wwjPanel.getStatusBar();
        }

        public RippleLayerPanel getLayerPanel()
        {
            return layerPanel;
        }

        public StatisticsPanel getStatsPanel()
        {
            return statsPanel;
        }
        
        public void setToolTipController(ToolTipController controller)
        {
            if (this.wwjPanel.toolTipController != null)
                this.wwjPanel.toolTipController.dispose();

            this.wwjPanel.toolTipController = controller;
        }

        public void setHighlightController(HighlightController controller)
        {
            if (this.wwjPanel.highlightController != null)
                this.wwjPanel.highlightController.dispose();

            this.wwjPanel.highlightController = controller;
        }
    }
    
    public static void insertBeforeCompass(WorldWindow wwd, Layer layer)
    {
        // Insert the layer into the layer list just before the compass.
        int compassPosition = 0;
        LayerList layers = wwd.getModel().getLayers();
        for (Layer l : layers)
        {
            if (l instanceof CompassLayer)
                compassPosition = layers.indexOf(l);
        }
        layers.add(compassPosition, layer);
    }
    
    public static void insertBeforePlacenames(WorldWindow wwd, Layer layer)
    {
        // Insert the layer into the layer list just before the placenames.
        int compassPosition = 0;
        LayerList layers = wwd.getModel().getLayers();
        for (Layer l : layers)
        {
            if (l instanceof PlaceNameLayer)
                compassPosition = layers.indexOf(l);
        }
        layers.add(compassPosition, layer);
    }

    public static void insertAfterPlacenames(WorldWindow wwd, Layer layer)
    {
        // Insert the layer into the layer list just after the placenames.
        int compassPosition = 0;
        LayerList layers = wwd.getModel().getLayers();
        for (Layer l : layers)
        {
            if (l instanceof PlaceNameLayer)
                compassPosition = layers.indexOf(l);
        }
        layers.add(compassPosition + 1, layer);
    }
    
    public static void insertBeforeLayerName(WorldWindow wwd, Layer layer, String targetName)
    {
        // Insert the layer into the layer list just before the target layer.
        int targetPosition = 0;
        LayerList layers = wwd.getModel().getLayers();
        for (Layer l : layers)
        {
            if (l.getName().indexOf(targetName) != -1)
            {
                targetPosition = layers.indexOf(l);
                break;
            }
        }
        layers.add(targetPosition, layer);
    }
    
    static
    {
        System.setProperty("java.net.useSystemProxies", "true");
        if (Configuration.isMacOS())
        {
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", "World Wind Application");
            System.setProperty("com.apple.mrj.application.growbox.intrudes", "false");
            System.setProperty("apple.awt.brushMetalLook", "true");
        }
        else if (Configuration.isWindowsOS())
        {
            System.setProperty("sun.awt.noerasebackground", "true"); // prevents flashing during window resizing
        }
    }
    
    public static RippleFrame start(String appName, Class rippleFrameClass)
    {
        if (Configuration.isMacOS() && appName != null)
        {
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", appName);
        }

        try
        {
            final RippleFrame frame = (RippleFrame) rippleFrameClass.newInstance();
            frame.setTitle(appName);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            java.awt.EventQueue.invokeLater(new Runnable()
            {
                public void run()
                {
                    frame.setVisible(true);
                }
            });

            return frame;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }
    
    public static void main(String[] args)
    {	
    	// start the local server that will be used to push updated to balloons
    	PatientBalloonNotifier ballonNotifier = new PatientBalloonNotifier(8080);
    	ballonNotifier.start();
    	
    	// attempt to make the window look pretty
    	try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException
				| IllegalAccessException | UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
    	
    	// load our configuration
    	Configuration.insertConfigurationDocument("pg/ripple/nasa/config/worldwind.xml");
    	// start the NASA World Wind
    	RippleFrame rippleFrame = RippleApplication.start("Ripple World Wind Application", RippleFrame.class);
    	
    	// start subscriber
    	RenderableLayer l = (RenderableLayer)rippleFrame.addBalloon("linkToPatientBalloon.html", "myProducerId" , Position.fromDegrees(48.883056, -77.016389), "Patient 1");
    	
    	rippleFrame.addCloudLetCircle(200, new LatLon(Angle.fromDegreesLatitude(48.883056), Angle.fromDegreesLongitude( -77.016389)), "FirstCloud");
    	
    	Random r = new Random();
    	while (true) {
    		try {
    			Thread.sleep(1000);
    			int hr = r.nextInt(10) + 65;
    			int rr = r.nextInt(2) + 22;
    			int tp = r.nextInt(5) + 93;
    			int bpsys = r.nextInt(10) + 110;
    			int bpdis = r.nextInt(10) + 70;
				ballonNotifier.send("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n<event id=\"EventName@EventIPAddress\">\n   <context>\n       <producer type=\"Patient\">\n           <id>myProducerId</id>\n       </producer>\n       <timestamp>Sat Jul 12 18:07:34 EDT 2014</timestamp>\n       <location description=\"OptionalDescription\">\n           <longitude>39.7810121</longitude>\n           <latitude>-84.1179554</latitude>\n           <altitude>240</altitude>\n       </location>\n   </context>\n   <content>\n       <Item type=\"temperature\" unit=\"Fahrenheit\">\n           <value>" + tp + "</value>\n       </Item>\n       <Item type=\"bloodPressure\" unit=\"mm Hg\">\n           <value>" + bpsys + "</value>\n           <value>" + bpdis + "</value>\n       </Item>\n       <Item type=\"respirationRate\" unit=\"Breath per minute\">\n           <value>" + rr + "</value>\n       </Item>\n       <Item type=\"age\" unit=\"Years\">\n           <value>24</value>\n       </Item>\n       <Item type=\"cloudletRegistrationMessage\" unit=\"ID\">\n           <value>cloudletID</value>\n       </Item>\n       <Item type=\"cloudletRegistrationMessage\" unit=\"ID\">\n           <value>cloudletID</value>\n       </Item>\n       <Item type=\"heartRate\" unit=\"beats/min\">\n           <value>77</value>\n       </Item>\n       <Item type=\"saturationO2\" unit=\"???\">\n           <value>23</value>\n       </Item>\n       <Item type=\"lastName\" unit=\"N/A\">\n           <value>Smith</value>\n       </Item>\n       <Item type=\"firstName\" unit=\"???\">\n           <value>John</value>\n       </Item>\n       <Item type=\"cloudLetID\" unit=\"ID\">\n           <value>cloudletID</value>\n       </Item>\n       <Item type=\"ecg\" unit=\"200 samples/sec\">\n           <value>0.5</value>\n           <value>0.6</value>\n           <value>1.0</value>\n           <value>0.5</value>\n           <value>-0.3</value>\n           <value>0.5</value>\n       </Item>\n   </content>\n</event>");
				
//				for (Renderable rl : l.getRenderables()) {
//					if (rl instanceof GlobeBrowserBalloon) {
//						GlobeBrowserBalloon balloon = (GlobeBrowserBalloon)rl;
//						balloon.setPosition(Position.fromDegrees(balloon.getPosition().getLatitude().degrees + 0.5001, balloon.getPosition().getLongitude().degrees + 0.5001));
//					}
//					if (rl instanceof PointPlacemark) {
//						PointPlacemark pp = (PointPlacemark)rl;
//						pp.setPosition(Position.fromDegrees(pp.getPosition().getLatitude().degrees + 0.5001, pp.getPosition().getLongitude().degrees + 0.5001));
//					}
//				}
				
				
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
    		
    	}
    	
    }  
 
}
