package CB_Locator.Map;

import java.io.FileNotFoundException;
import java.io.IOException;

import CB_Locator.Tag;
import CB_Utils.Util.FileIO;
import bsh.EvalError;
import bsh.Interpreter;

import com.badlogic.gdx.Gdx;

public class BshLayer extends Layer
{
	private Interpreter interpreter;

	public BshLayer(Type LayerType, String filename)
	{
		super(LayerType, "B- " + FileIO.GetFileNameWithoutExtension(filename), FileIO.GetFileNameWithoutExtension(filename), "");
		this.interpreter = new Interpreter();
		try
		{
			this.interpreter.source(filename);
		}
		catch (FileNotFoundException e)
		{

			Gdx.app.error(Tag.TAG, "", e);
			this.interpreter = null;
		}
		catch (IOException e)
		{

			Gdx.app.error(Tag.TAG, "", e);
			this.interpreter = null;
		}
		catch (EvalError e)
		{

			Gdx.app.error(Tag.TAG, "", e);
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
			Object result = interpreter.eval("getTileUrl(" + desc.getZoom() + "," + desc.getX() + "," + desc.getY() + ")");
			System.out.println(result);
			return (String) result;
		}
		catch (EvalError e)
		{

			Gdx.app.error(Tag.TAG, "", e);
		}
		return null;
	}
}
