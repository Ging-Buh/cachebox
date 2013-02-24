package CB_Core.Map;

import CB_Core.Map.Layer.Type;

public class DefaultLayerList extends java.util.ArrayList<Layer>
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public DefaultLayerList()
	{
		this.add(new Layer(Type.normal, "Mapnik", "Mapnik", "http://a.tile.openstreetmap.org/"));
		this.add(new Layer(Type.normal, "OSM Cycle Map", "Open Cycle Map", "http://c.tile.opencyclemap.org/cycle/"));
		this.add(new Layer(Type.overlay, "HillShade", "HillShade", "http://129.206.74.245:8004/tms_hs.ashx"));
	}

}
