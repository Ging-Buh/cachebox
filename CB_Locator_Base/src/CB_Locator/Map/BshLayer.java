package CB_Locator.Map;

import java.io.FileNotFoundException;
import java.io.IOException;

import CB_Utils.Util.FileIO;
import bsh.EvalError;
import bsh.Interpreter;

public class BshLayer extends Layer
{
	private final String filename;

	public BshLayer(Type LayerType, String filename)
	{
		super(LayerType, "B- " + FileIO.GetFileNameWithoutExtension(filename), FileIO.GetFileNameWithoutExtension(filename), "");
		this.filename = filename;
	}

	@Override
	public String GetUrl(Descriptor desc)
	{
		if (desc == null) return null;
		Interpreter i = new Interpreter();
		try
		{
			i.source(filename);
			Object result = i.eval("getTileUrl(" + desc.Zoom + "," + desc.X + "," + desc.Y + ")");
			return (String) result;
		}
		catch (FileNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (EvalError e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
