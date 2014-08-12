/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package pg.ripple.nasa.ww;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.*;
import gov.nasa.worldwind.render.AbstractBrowserBalloon;
import gov.nasa.worldwind.render.BalloonAttributes;
import gov.nasa.worldwind.render.BasicBalloonAttributes;
import gov.nasa.worldwind.render.GlobeBrowserBalloon;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import pg.ripple.nasa.HTMLBalloon.cloudletBalloon.CloudletInfoManager;

import java.awt.*;
import java.awt.event.*;
import java.util.Arrays;
import java.util.Hashtable;

/**
 * Panel to display a list of layers. A layer can be turned on or off by clicking a check box next to the layer name.
 *
 * @version $Id: LayerPanel.java 1171 2013-02-11 21:45:02Z dcollins $
 *
 * @see LayerTreeUsage
 * @see OnScreenLayerManager
 */
public class RippleLayerPanel extends JPanel
{
    protected JPanel layersPanel;
    protected JPanel westPanel;
    protected JPanel westRipplePanel;
    protected JPanel rippleLayers;
    protected JTabbedPane westTabbedPanel;
    protected JScrollPane scrollPane;
    protected JScrollPane scrollRipplePane;
    protected Font defaultFont;

    /**
     * Create a panel with the default size.
     *
     * @param wwd WorldWindow to supply the layer list.
     */
    public RippleLayerPanel(WorldWindow wwd, boolean tabbedPanel)
    {
        // Make a panel at a default size.
        super(new BorderLayout());
        if (!tabbedPanel)
        	this.makePanel(wwd, new Dimension(200, 400));
    }

    /**
     * Create a panel with a size.
     *
     * @param wwd  WorldWindow to supply the layer list.
     * @param size Size of the panel.
     */
    public RippleLayerPanel(WorldWindow wwd, Dimension size, boolean tabbedPanel)
    {
        // Make a panel at a specified size.
        super(new BorderLayout());
        if (!tabbedPanel)
        	this.makePanel(wwd, size);
    }

    protected void makePanel(WorldWindow wwd, Dimension size)
    {
        // Make and fill the panel holding the layer titles.
        this.layersPanel = new JPanel(new GridLayout(0, 1, 0, 4));
        this.layersPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        this.fill(wwd);

        // Must put the layer grid in a container to prevent scroll panel from stretching their vertical spacing.
        JPanel dummyPanel = new JPanel(new BorderLayout());
        dummyPanel.add(this.layersPanel, BorderLayout.NORTH);

        // Put the name panel in a scroll bar.
        this.scrollPane = new JScrollPane(dummyPanel);
        this.scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        if (size != null)
            this.scrollPane.setPreferredSize(size);

        // Add the scroll bar and name panel to a titled panel that will resize with the main window.
        westPanel = new JPanel();
        westPanel.setBorder(
            new CompoundBorder(BorderFactory.createEmptyBorder(9, 9, 9, 9), new TitledBorder("General layers")));
        westPanel.setToolTipText("General layers to Show");
        westPanel.add(scrollPane);
        this.add(westPanel, BorderLayout.CENTER);
    }
    
    protected void createRipplePanel(WorldWindow wwd, Dimension size) {
    	rippleLayers = new JPanel(new GridLayout(0, 1, 0, 4));
    	rippleLayers.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    	
    	JPanel dummyPanel = new JPanel(new BorderLayout());
        dummyPanel.add(rippleLayers, BorderLayout.NORTH);
        
    	this.scrollRipplePane = new JScrollPane(dummyPanel);
    	this.scrollRipplePane.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
    	if (size != null)
            this.scrollRipplePane.setPreferredSize(size);
    	
    	if (westRipplePanel == null)
    		westRipplePanel = new JPanel();
    	westRipplePanel.setBorder(new CompoundBorder(BorderFactory.createEmptyBorder(9, 9, 9, 9), new TitledBorder("Ripple layers")));
    	westRipplePanel.setToolTipText("Ripple layers to Show");
    	westRipplePanel.add(scrollRipplePane);
    	
    	this.add(westRipplePanel, BorderLayout.SOUTH);
    }
    
    protected void addRippleLayer(WorldWindow wwd, Layer layer, AbstractBrowserBalloon balloon) {
    	String layerName = layer.getName();
    	
    	LayerList list = wwd.getModel().getLayers();
    	layer.setName("ON/OFF");
    	list.add(0, layer);
    	
    	this.fillRippleLayer(rippleLayers, wwd, layer, layerName, balloon);
    }
    
    protected void removeRippleLayer(WorldWindow wwd, Layer layerToRemove) {
    	LayerList list = wwd.getModel().getLayers();
    	list.remove(layerToRemove);
    	
    	for (Component c : rippleLayers.getComponents()) {
    		BorderLayout layout = (BorderLayout) ((JPanel)c).getLayout();
    		JCheckBox jcb = (JCheckBox)(layout.getLayoutComponent(BorderLayout.CENTER));
    		Layer layer = ((LayerAction)jcb.getAction()).getLayer();
    		if (layer == layerToRemove) {
    			rippleLayers.remove(c);
    			return;
    		}
    	}
    }
    
    protected void addTabbedPanel(WorldWindow wwd, Dimension size, String tabName, String[] layersNames) {
    	// Make and fill the panel holding the layer titles
    	JPanel jp = new JPanel(new GridLayout(0, 1, 0, 4));
    	jp.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    	this.fill(jp, wwd, layersNames);
    	
    	// Must put the layer grid in a container to prevent scroll panel from stretching their vertical spacing
    	JPanel dummyPanel = new JPanel(new BorderLayout());
    	dummyPanel.add(jp, BorderLayout.NORTH);
    	
    	// put the name panel in a scroll bar
    	JScrollPane jsp = new JScrollPane(dummyPanel);
    	jsp.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        if (size != null)
        	jsp.setPreferredSize(size);

        // Add the scroll bar and name panel to a titled panel that will resize with the main window.
        if (westTabbedPanel == null) {
        	westTabbedPanel = new JTabbedPane();
        	westTabbedPanel.setBorder(
                    new CompoundBorder(BorderFactory.createEmptyBorder(9, 9, 9, 9), new TitledBorder("General Layers")));
            westTabbedPanel.setToolTipText("General layers to Show");
        }
        westTabbedPanel.addTab(tabName, jsp);
        this.add(westTabbedPanel, BorderLayout.CENTER);
    }
    
	protected void fill(JPanel jp, WorldWindow wwd, String[] layersNames) {
		for (Layer layer : wwd.getModel().getLayers()) {
			if (Arrays.asList(layersNames).contains(layer.getName())) {
				LayerAction action = new LayerAction(layer, wwd,
						layer.isEnabled());
				JCheckBox jcb = new JCheckBox(action);
				jcb.setSelected(action.selected);
				jp.add(jcb);
				if (defaultFont == null) {
					this.defaultFont = jcb.getFont();
				}
			}
		}
	}
	
	protected void fillRippleLayer(JPanel jp, final WorldWindow wwd, final Layer layer, final String layerName, final AbstractBrowserBalloon balloon) {
		JPanel tmp = new JPanel(new BorderLayout());
		tmp.setBorder(BorderFactory.createLineBorder(Color.red));
		
		LayerAction action = new LayerAction(layer, wwd, layer.isEnabled());
		JCheckBox jcb = new JCheckBox(action);
		jcb.setSelected(action.selected);
		
		JPanel lables = new JPanel(new BorderLayout());
		JLabel lbl1 = new JLabel(layerName);
		JLabel lbl2 = new JLabel("Opacity");
		lables.add(lbl1, BorderLayout.WEST);
		lables.add(lbl2, BorderLayout.EAST);
		
		JSlider slider = new JSlider(JSlider.HORIZONTAL, 0, 100, 100);
		slider.setPreferredSize(new Dimension(100, 10));
		slider.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent ce) {
				//layer.setOpacity(((double)((JSlider)ce.getSource()).getValue())/100.0);
				BalloonAttributes attrs = balloon.getAttributes();
				attrs.setInteriorOpacity(((double)((JSlider)ce.getSource()).getValue())/100.0);
				balloon.setAttributes(attrs);
				wwd.redraw();
			}
		});
		
		JPanel buttons = new JPanel(new GridLayout(1, 2, 0, 4));
		JButton button = new JButton();
		button.setText("Show Patient Stats");
		button.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				CloudletInfoManager manager = CloudletInfoManager.getInstance();
				manager.createPatientWindow(layerName);
			}
		});
		JButton button2 = new JButton();
		button2.setText("Center on Event");
		button2.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				Position position = ((GlobeBrowserBalloon)balloon).getPosition();
				wwd.getView().goTo(position, 2500);
			}
		});
		
		buttons.add(button);
		buttons.add(button2);
		
		tmp.add(lables, BorderLayout.NORTH);
		tmp.add(jcb, BorderLayout.CENTER);
		tmp.add(slider, BorderLayout.EAST);
		tmp.add(buttons, BorderLayout.SOUTH);
		
		jp.add(tmp);
		if (defaultFont == null) {
			this.defaultFont = jcb.getFont();
		}
	}

    protected void fill(WorldWindow wwd)
    {
        // Fill the layers panel with the titles of all layers in the world window's current model.
        for (Layer layer : wwd.getModel().getLayers())
        {
            LayerAction action = new LayerAction(layer, wwd, layer.isEnabled());
            JCheckBox jcb = new JCheckBox(action);
            jcb.setSelected(action.selected);
            this.layersPanel.add(jcb);
            if (defaultFont == null)
            {
                this.defaultFont = jcb.getFont();
            }
        }
    }

    /**
     * Update the panel to match the layer list active in a WorldWindow.
     *
     * @param wwd WorldWindow that will supply the new layer list.
     */
    public void update(WorldWindow wwd)
    {
        // Replace all the layer names in the layers panel with the names of the current layers.
        this.layersPanel.removeAll();
        this.fill(wwd);
        this.westTabbedPanel.revalidate();
        this.westTabbedPanel.repaint();
    }

    @Override
    public void setToolTipText(String string)
    {
        this.scrollPane.setToolTipText(string);
    }

    protected static class LayerAction extends AbstractAction
    {
        protected WorldWindow wwd;
        protected Layer layer;
        protected boolean selected;

        public LayerAction(Layer layer, WorldWindow wwd, boolean selected)
        {
            super(layer.getName());
            this.wwd = wwd;
            this.layer = layer;
            this.selected = selected;
            this.layer.setEnabled(this.selected);
        }

        public void actionPerformed(ActionEvent actionEvent)
        {
            // Simply enable or disable the layer based on its toggle button.
            if (((JCheckBox) actionEvent.getSource()).isSelected())
                this.layer.setEnabled(true);
            else
                this.layer.setEnabled(false);

            wwd.redraw();
        }
        
        public Layer getLayer() {
        	return layer;
        }
    }
    
}
