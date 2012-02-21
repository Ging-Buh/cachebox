package CB_Core.Map;

import java.util.ArrayList;

public class ManagerBase
{
	public static ManagerBase Manager = null;

	public ArrayList<Layer> Layers = new ArrayList<Layer>();

	public Layer GetLayerByName(String Name, String friendlyName, String url)
	{
		if (Name == "OSM") Name = "Mapnik";

		for (Layer layer : Layers)
		{
			if (layer.Name.equalsIgnoreCase(Name)) return layer;
		}

		Layer newLayer = new Layer(Name, Name, url);
		Layers.add(newLayer);

		return newLayer;
	}

	public byte[] LoadLocalPixmap(String layer, Descriptor desc)
	{
		return null;
	}

	public byte[] LoadLocalPixmap(Layer layer, Descriptor desc)
	{
		return null;
	}

}
