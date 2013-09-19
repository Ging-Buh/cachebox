package CB_Locator.Map;

import java.io.FileNotFoundException;
import java.io.IOException;

import CB_Utils.Util.FileIO;
import bsh.EvalError;
import bsh.Interpreter;

public class BshLayer extends Layer
{
	private final String filename;
	private Interpreter interpreter;

	public BshLayer(Type LayerType, String filename)
	{
		super(LayerType, "B- " + FileIO.GetFileNameWithoutExtension(filename), FileIO.GetFileNameWithoutExtension(filename), "");
		this.filename = filename;
		this.interpreter = new Interpreter();
		try
		{
			this.interpreter.source(filename);
		}
		catch (FileNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			this.interpreter = null;
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			this.interpreter = null;
		}
		catch (EvalError e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			this.interpreter = null;
		}
	}

	@Override
	public String GetUrl(Descriptor desc)
	{
		if (desc == null) return null;
		if (interpreter == null) return null;
		try
		{
			Object result = interpreter.eval("getTileUrl(" + desc.Zoom + "," + desc.X + "," + desc.Y + ")");
			System.out.println(result);
			return (String) result;
		}
		catch (EvalError e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
