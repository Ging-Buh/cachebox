package CB_Locator.Map;

public class TmsLayer extends Layer {
    private final TmsMap tmsMap;

    public TmsLayer(Type LayerType, TmsMap tmsMap) {
	super(MapType.ONLINE, LayerType, Layer.StorageType.PNG, tmsMap.name, tmsMap.name, tmsMap.url);
	this.tmsMap = tmsMap;
    }

    @Override
    public String GetUrl(Descriptor desc) {
	if (desc == null)
	    return null;

	String url = tmsMap.url;
	url = url.replace("{$x}", String.valueOf(desc.getX()));
	url = url.replace("{$y}", String.valueOf(desc.getY()));
	url = url.replace("{$z}", String.valueOf(desc.getZoom()));

	return url;
    }

}
