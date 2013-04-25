package CB_Core.Map;

public class TmsLayer extends Layer
{
	private TmsMap tmsMap;

	public TmsLayer(Type LayerType, TmsMap tmsMap)
	{
		super(LayerType, tmsMap.name, tmsMap.name, tmsMap.url);
		this.tmsMap = tmsMap;
	}

	@Override
	public String GetUrl(Descriptor desc)
	{
		if (desc == null) return null;

		String url = tmsMap.url;
		url = url.replace("{$x}", String.valueOf(desc.X));
		url = url.replace("{$y}", String.valueOf(desc.Y));
		url = url.replace("{$z}", String.valueOf(desc.Zoom));

		return url;
	}

}
