package de.droidcachebox.locator.map;

import de.droidcachebox.locator.bsh.EvalError;
import de.droidcachebox.locator.bsh.Interpreter;
import de.droidcachebox.utils.FileIO;

public class BshLayer extends Layer {
    private final String pathAndName;
    private final Interpreter interpreter;

    /**
     todo modify to input-stream + Android 11 Access
     */
    public BshLayer(String filename) throws Exception {
        super(MapType.ONLINE, LayerUsage.normal, Layer.StorageType.PNG, "B- " + FileIO.getFileNameWithoutExtension(filename), FileIO.getFileNameWithoutExtension(filename), "");
        pathAndName = filename;
        interpreter = new Interpreter();
    }

    @Override
    public boolean prepareLayer(boolean isCarMode) {
        try {
            interpreter.source(pathAndName);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    @Override
    public String getUrl(Descriptor desc) {
        if (desc == null)
            return null;
        if (interpreter == null)
            return null;
        try {
            return (String) interpreter.eval("getTileUrl(" + desc.getZoom() + "," + desc.getX() + "," + desc.getY() + ")");
        } catch (EvalError e) {
            e.printStackTrace();
        }
        return null;
    }
}
