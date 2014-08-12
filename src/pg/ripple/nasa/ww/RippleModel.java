package pg.ripple.nasa.ww;

import java.util.logging.Level;

import org.w3c.dom.Element;

import gov.nasa.worldwind.BasicFactory;
import gov.nasa.worldwind.BasicModel;
import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.Model;
import gov.nasa.worldwind.WWObjectImpl;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.event.Message;
import gov.nasa.worldwind.geom.Extent;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.LayerList;
import gov.nasa.worldwind.util.Logging;

public class RippleModel extends WWObjectImpl implements Model {
    private Globe globe;
    private LayerList layers;
    private boolean showWireframeInterior = false;
    private boolean showWireframeExterior = false;
    private boolean showTessellationBoundingVolumes = false;
    
    public RippleModel() {
    	String globeName = Configuration.getStringValue(AVKey.GLOBE_CLASS_NAME);
    	if (globeName == null) {
    		return;
    	}
    	
    	this.setGlobe((Globe) WorldWind.createComponent(globeName));
    	
    	LayerList layers = null;
    	String layerNames = Configuration.getStringValue(AVKey.LAYERS_CLASS_NAMES);
    	if (layerNames != null) {
    		layers = this.createLayersFromProperties(layerNames);
    	} else
        {
            Element el = Configuration.getElement("./LayerList");
            if (el != null)
                layers = this.createLayersFromElement(el);
        }

        this.setLayers(layers != null ? layers : new LayerList());
    }
    
    public RippleModel(Globe globe, LayerList layers) {
    	this.setGlobe(globe);
    	this.setLayers(layers != null ? layers : new LayerList());
    }
    
    protected LayerList createLayersFromElement(Element element) {

        Object o = BasicFactory.create(AVKey.LAYER_FACTORY, element);

        if (o instanceof LayerList)
            return (LayerList) o;

        if (o instanceof Layer)
            return new LayerList(new Layer[] {(Layer) o});

        if (o instanceof LayerList[])
        {
            LayerList[] lists = (LayerList[]) o;
            if (lists.length > 0)
                return LayerList.collapseLists((LayerList[]) o);
        }

        return null;
    }
    
    protected LayerList createLayersFromProperties(String layerNames)
    {
        LayerList layers = new LayerList();
        if (layerNames == null)
            return null;

        String[] names = layerNames.split(",");
        for (String name : names)
        {
            try
            {
                if (name.length() > 0)
                {
                    Layer l = (Layer) WorldWind.createComponent(name);
                    layers.add(l);
                }
            }
            catch (Exception e)
            {
                Logging.logger().log(Level.WARNING, Logging.getMessage("BasicModel.LayerNotFound", name), e);
            }
        }

        return layers;
    }
    
    public void setGlobe(Globe globe)
    {
        // don't raise an exception if globe == null. In that case, we are disassociating the model from any globe

        //remove property change listener "this" from the current globe.
        if (this.globe != null)
            this.globe.removePropertyChangeListener(this);

        // if the new globe is not null, add "this" as a property change listener.
        if (globe != null)
            globe.addPropertyChangeListener(this);

        Globe old = this.globe;
        this.globe = globe;
        this.firePropertyChange(AVKey.GLOBE, old, this.globe);
    }
    
    public void setLayers(LayerList layers)
    {
        // don't raise an exception if layers == null. In that case, we are disassociating the model from any layer set

        if (this.layers != null)
            this.layers.removePropertyChangeListener(this);
        if (layers != null)
            layers.addPropertyChangeListener(this);

        LayerList old = this.layers;
        this.layers = layers;
        this.firePropertyChange(AVKey.LAYERS, old, this.layers);
    }

	@Override
    public Extent getExtent()
    {
        // See if the layers have it.
        LayerList layers = RippleModel.this.getLayers();
        if (layers != null)
        {
            for (Object layer1 : layers)
            {
                Layer layer = (Layer) layer1;
                Extent e = (Extent) layer.getValue(AVKey.EXTENT);
                if (e != null)
                    return e;
            }
        }

        // See if the Globe has it.
        Globe globe = this.getGlobe();
        if (globe != null)
        {
            Extent e = globe.getExtent();
            if (e != null)
                return e;
        }

        return null;
    }

	@Override
	public Globe getGlobe() {
		// TODO Auto-generated method stub
		return this.globe;
	}

	@Override
	public LayerList getLayers() {
		// TODO Auto-generated method stub
		return this.layers;
	}


	@Override
	public void setShowWireframeInterior(boolean show) {
		// TODO Auto-generated method stub
		this.showWireframeInterior = show;
	}

	@Override
	public void setShowWireframeExterior(boolean show) {
		// TODO Auto-generated method stub
		this.showWireframeExterior = show;
	}

	@Override
	public boolean isShowWireframeInterior() {
		// TODO Auto-generated method stub
		return this.showWireframeInterior;
	}

	@Override
	public boolean isShowWireframeExterior() {
		// TODO Auto-generated method stub
		return this.showWireframeExterior;
	}

	@Override
	public boolean isShowTessellationBoundingVolumes() {
		// TODO Auto-generated method stub
		return showTessellationBoundingVolumes;
	}

	@Override
	public void setShowTessellationBoundingVolumes(
			boolean showTileBoundingVolumes) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
    public void onMessage(Message msg)
    {
        if (this.getLayers() != null)
        {
            for (Layer layer : this.getLayers())
            {
                try
                {
                    if (layer != null)
                    {
                        layer.onMessage(msg);
                    }
                }
                catch (Exception e)
                {
                    String message = Logging.getMessage("generic.ExceptionInvokingMessageListener");
                    Logging.logger().log(Level.SEVERE, message, e);
                    // Don't abort; continue on to the next layer.
                }
            }
        }
    }

}
