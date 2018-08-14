package CB_Locator.Map;

import CB_Utils.Util.FileIO;
import bsh.EvalError;
import bsh.Interpreter;

import java.io.FileNotFoundException;
import java.io.IOException;

public class BshLayer extends Layer {
    private Interpreter interpreter;

    public BshLayer(LayerType LayerType, String filename) {
        super(MapType.ONLINE, LayerType, Layer.StorageType.PNG, "B- " + FileIO.GetFileNameWithoutExtension(filename), FileIO.GetFileNameWithoutExtension(filename), "");
        this.interpreter = new Interpreter();
        try {
            this.interpreter.source(filename);
        } catch (FileNotFoundException e) {

            e.printStackTrace();
            this.interpreter = null;
        } catch (IOException e) {

            e.printStackTrace();
            this.interpreter = null;
        } catch (EvalError e) {

            e.printStackTrace();
            this.interpreter = null;
        }
    }

    @Override
    public String GetUrl(Descriptor desc) {
        if (desc == null)
            return null;
        if (interpreter == null)
            return null;
        try {
            Object result = interpreter.eval("getTileUrl(" + desc.getZoom() + "," + desc.getX() + "," + desc.getY() + ")");
            System.out.println(result);
            return (String) result;
        } catch (EvalError e) {

            e.printStackTrace();
        }
        return null;
    }
}
