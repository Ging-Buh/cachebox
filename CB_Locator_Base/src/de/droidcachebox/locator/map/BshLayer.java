package de.droidcachebox.locator.map;

import de.droidcachebox.locator.bsh.EvalError;
import de.droidcachebox.locator.bsh.Interpreter;
import de.droidcachebox.utils.FileIO;

public class BshLayer extends Layer {
    private Interpreter interpreter;

    public BshLayer(String filename) throws Exception {
        super(MapType.ONLINE, LayerUsage.normal, Layer.StorageType.PNG, "B- " + FileIO.getFileNameWithoutExtension(filename), FileIO.getFileNameWithoutExtension(filename), "");
        interpreter = new Interpreter();
        interpreter.source(filename);
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
