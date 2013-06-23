package CB_Core.GL_UI.Views;

import CB_Core.GL_UI.CB_View_Base;
import CB_Core.GL_UI.render3D;
import CB_Core.GL_UI.GL_Listener.GL;
import CB_Core.Math.CB_RectF;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.lights.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.lights.Lights;
import com.badlogic.gdx.graphics.g3d.materials.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.materials.Material;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;

public class SpoilerView extends CB_View_Base implements render3D
{
	// http://www.badlogicgames.com/forum/viewtopic.php?f=11&t=1615&p=9122&hilit=Camera#p9122
	// http://blog.xoppa.com/basic-3d-using-libgdx-2/

	private Model model;
	private ModelInstance instance;
	private Lights lights;
	float rotValue = 0;

	public SpoilerView(CB_RectF rec, String Name)
	{
		super(rec, Name);

	}

	@Override
	public void onShow()
	{
		GL.that.register3D(this);
		GL.that.addRenderView(this, GL.FRAME_RATE_FAST_ACTION);
	}

	@Override
	public void onHide()
	{
		GL.that.unregister3D();
	}

	@Override
	protected void Initial()
	{

	}

	@Override
	protected void SkinIsChanged()
	{

	}

	@Override
	public void render3d(ModelBatch modelBatch)
	{
		++rotValue;
		if (rotValue >= 360) rotValue = 0;
		instance.transform.setToRotation(Vector3.Y, rotValue);
		// instance.transform.setToTranslation(0, 0, rotValue);

		modelBatch.render(instance, lights);

	}

	private PerspectiveCamera myCam;

	@Override
	public PerspectiveCamera get3DCamera(PerspectiveCamera cam3d)
	{
		if (myCam == null)
		{
			myCam = new PerspectiveCamera(67f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
			myCam.position.set(10f, 10, 10f);
			myCam.lookAt(0, 0, 0);
			myCam.near = 0.1f;
			myCam.far = 300;
			myCam.update();
		}
		if (cam3d != myCam) return myCam;

		return null;
	}

	private boolean is3D_Initial = false;

	@Override
	public boolean is3D_Initial()
	{
		return is3D_Initial;
	}

	@Override
	public void Initial3D()
	{

		ModelBuilder modelBuilder = new ModelBuilder();
		model = modelBuilder.createBox(5f, 5f, 5f, new Material(ColorAttribute.createDiffuse(Color.GREEN)), Usage.Position | Usage.Normal
				| Usage.TextureCoordinates);

		instance = new ModelInstance(model);

		lights = new Lights();
		lights.ambientLight.set(0.4f, 0.4f, 0.4f, 1f);
		lights.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));

		is3D_Initial = true;
	}

}
