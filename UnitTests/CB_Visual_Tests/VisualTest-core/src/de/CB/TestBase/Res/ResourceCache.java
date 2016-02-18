package de.CB.TestBase.Res;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.loader.G3dModelLoader;
import com.badlogic.gdx.graphics.g3d.loader.ObjLoader;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import com.badlogic.gdx.utils.UBJsonReader;

import CB_UI_Base.GL_UI.ButtonSprites;
import CB_UI_Base.GL_UI.Sprites;
import CB_UI_Base.GL_UI.Controls.MessageBox.GL_MsgBox;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxIcon;
import CB_UI_Base.GL_UI.Skin.CB_Skin;
import CB_UI_Base.GL_UI.Skin.SkinBase;
import CB_UI_Base.GL_UI.utils.ColorDrawable;
import CB_UI_Base.Math.UI_Size_Base;

public class ResourceCache extends Sprites {

	private static Model model_Box, model_Box2, model_field1, model_field2, model_PortalLegBottom, model_PortalLegCenter, model_PortalLegTop, model_PortalJibLeft, model_PortalJibCenter, model_PortalJibRight, model_PortalRunWay;
	private static float size, halfsize, dpi;
	private static ModelBuilder modelBuilder;
	private static Color trans;
	private static float dimens;
	private static Material matBlue;
	private static Material matGreen;
	private static Material matRed;
	private static long attributes;
	public static ButtonSprites btnSpritesHome;

	public interface IResourceChanged {
		public void resourceChanged();
	}

	public static ArrayList<IResourceChanged> list = new ArrayList<IResourceChanged>();

	public static void Add(IResourceChanged event) {
		synchronized (list) {
			if (!list.contains(event))
				list.add(event);
		}
	}

	public static void Remove(IResourceChanged event) {
		synchronized (list) {
			list.remove(event);
		}
	}

	public static void CallResourceChanged() {
		for (IResourceChanged event : list) {
			if (event == null)
				continue;
			event.resourceChanged();
		}
	}

	public static void LoadSprites(boolean reload) {
		if (!reload)
			setPath(CB_Skin.INSTANCE);

		loadDefaultUi();

		modelBuilder = new ModelBuilder();
		trans = new Color(0, 1, 0, 0.4f);

		size = 6;

		// Load Box Model and read Boundingbox for Size initialisations
		{

			// chk if g3db on root
			if (Gdx.files.absolute("./cube.g3db").exists()) {
				model_Box = new G3dModelLoader(new UBJsonReader()).loadModel(Gdx.files.internal("./cube.g3db"));
			} else if (Gdx.files.classpath("./cube.obj").exists()) {
				ObjLoader loader = new ObjLoader();
				model_Box = loader.loadModel(Gdx.files.classpath("./cube.obj"));
			} else {
				// model_Box = new G3dModelLoader(new UBJsonReader()).loadModel(Gdx.files.internal("skins/default/day/cube.g3db"));
				model_Box = modelBuilder.createBox(size, size, size, new Material(ColorAttribute.createDiffuse(trans)), Usage.Position | Usage.Normal);
			}

			ModelInstance instance = new ModelInstance(model_Box);
			BoundingBox box = new BoundingBox();
			instance.calculateBoundingBox(box);

			size = box.getDimensions(new Vector3()).x;
			halfsize = size / 2;
			dpi = UI_Size_Base.that.getScale();
			instance = null;

		}

		model_Box2 = modelBuilder.createBox(size, size, size, new Material(ColorAttribute.createDiffuse(trans)), Usage.Position | Usage.Normal);

		model_field1 = modelBuilder.createBox(size, 0.1f, size, new Material(ColorAttribute.createDiffuse(Color.BLACK)), Usage.Position | Usage.Normal | Usage.TextureCoordinates);

		model_field2 = modelBuilder.createBox(size, 0.1f, size, new Material(ColorAttribute.createDiffuse(Color.GRAY)), Usage.Position | Usage.Normal | Usage.TextureCoordinates);

		initialPortalModels();

		if (Arrows == null)
			Arrows = new ArrayList<Sprite>();
		synchronized (Arrows) {

			float scale = UI_Size_Base.that.getScale();

			Arrows.clear();
			Arrows.add(getSprite("arrow-Compass")); // 0
			Arrows.add(getSprite("arrow-Compass-Trans")); // 1
			Arrows.add(getSprite("arrow-GPS")); // 2
			Arrows.add(getSprite("arrow-GPS-Trans")); // 3
			Arrows.add(getSprite("target-arrow")); // 4
			Arrows.add(getSprite("track-line", scale)); // 5
			Arrows.add(getSprite("arrow-down")); // 6
			Arrows.add(getSprite("arrow-up")); // 7
			Arrows.add(getSprite("arrow-left")); // 8
			Arrows.add(getSprite("arrow-right")); // 9
			Arrows.add(getSprite("track-point", scale)); // 10
			Arrows.add(getSprite("ambilwarna-arrow-right")); // 11
			Arrows.add(getSprite("ambilwarna-arrow-down")); // 12
			Arrows.add(getSprite("draw-line", scale)); // 13
			Arrows.add(getSprite("draw-point", scale)); // 14
			Arrows.add(getSprite("arrow-Compass-car")); // 15

		}

		MapScale = new Drawable[3];
		MapScale[0] = new SpriteDrawable(getSprite("MapScale-3"));
		MapScale[1] = new SpriteDrawable(getSprite("MapScale-4"));
		MapScale[2] = new SpriteDrawable(getSprite("MapScale-5"));

		ZoomValueBack = getSprite("zoom-back");

	}

	private static void loadDefaultUi() {
		if (ChkIcons == null)
			ChkIcons = new ArrayList<Sprite>();
		synchronized (ChkIcons) {

			ChkIcons.clear();
			ChkIcons.add(getSprite("check-off"));
			ChkIcons.add(getSprite("check-on"));

		}

		if (Dialog == null)
			Dialog = new ArrayList<Sprite>();
		synchronized (Dialog) {
			Dialog.clear();
			Dialog.add(getSprite("dialog-header"));
			Dialog.add(getSprite("dialog-center"));
			Dialog.add(getSprite("dialog-footer"));
			Dialog.add(getSprite("dialog-title"));
			Dialog.add(getSprite("menu-divider"));

		}

		if (ToggleBtn == null)
			ToggleBtn = new ArrayList<Sprite>();
		synchronized (ToggleBtn) {
			ToggleBtn.clear();
			ToggleBtn.add(getSprite(IconName.btnNormal.name()));
			ToggleBtn.add(getSprite("btn-pressed"));
			ToggleBtn.add(getSprite("toggle-led-gr"));

		}

		Progress = getSprite("progress");

		if (ZoomBtn == null)
			ZoomBtn = new ArrayList<Sprite>();
		synchronized (ZoomBtn) {
			ZoomBtn.clear();
			ZoomBtn.add(getSprite("day-btn-zoom-down-normal"));
			ZoomBtn.add(getSprite("day-btn-zoom-down-pressed"));
			ZoomBtn.add(getSprite("day-btn-zoom-down-disabled"));
			ZoomBtn.add(getSprite("day-btn-zoom-up-normal"));
			ZoomBtn.add(getSprite("day-btn-zoom-up-pressed"));
			ZoomBtn.add(getSprite("day-btn-zoom-up-disabled"));

		}

		ZoomValueBack = getSprite("zoom-back");

		loadButtnSprites();

		createDrawables();
	}

	protected static void loadButtnSprites() {
		Sprites.loadButtonSprites();
		btnSpritesHome = new ButtonSprites(getSprite("InstBack-Normal"), getSprite("InstBack-Pressed"), getSprite("InstBack-Disabled"), getSprite("InstBack-Focus"));
	}

	protected static void createDrawables() {
		patch = (int) (Sprites.getSprite("activity-back").getWidth() / 7);

		activityBackground = new NinePatchDrawable(new NinePatch(Sprites.getSprite("activity-back"), patch, patch, patch, patch));
		activityBorderMask = new NinePatchDrawable(new NinePatch(Sprites.getSprite("activity-border"), patch, patch, patch, patch));
		ListBack = new ColorDrawable(SkinBase.getThemedColor("background"));
		ButtonBack = new SpriteDrawable(getSprite("button-list-back"));
		// AboutBack = new SpriteDrawable(getThemedSprite("splash-back"));
		// InfoBack = new NinePatchDrawable(new NinePatch(getThemedSprite("InfoPanelBack"), patch, patch, patch, patch));
		ProgressBack = new NinePatchDrawable(new NinePatch(ToggleBtn.get(0), patch, patch, patch, patch));
		ProgressFill = new NinePatchDrawable(new NinePatch(Sprites.Progress, patch - 1, patch - 1, patch - 1, patch - 1));
		btn = new NinePatchDrawable(new NinePatch(Sprites.getSprite(IconName.btnNormal.name()), patch, patch, patch, patch));
		btnPressed = new NinePatchDrawable(new NinePatch(Sprites.getSprite("btn-pressed"), patch, patch, patch, patch));
		btnDisabled = new NinePatchDrawable(new NinePatch(Sprites.getSprite("btn-disabled"), patch, patch, patch, patch));

		chkOn = new SpriteDrawable(getSprite("check-on"));
		chkOff = new SpriteDrawable(getSprite("check-off"));
		chkOnDisabled = new SpriteDrawable(getSprite("check-disable"));
		chkOffDisabled = new SpriteDrawable(getSprite("check-off"));

		radioOn = new SpriteDrawable(getSprite("RadioButtonSet"));
		radioBack = new SpriteDrawable(getSprite("RadioButtonBack"));

		textFieldBackground = new NinePatchDrawable(new NinePatch(Sprites.getSprite("text-field-back"), patch, patch, patch, patch));
		textFieldBackgroundFocus = new NinePatchDrawable(new NinePatch(Sprites.getSprite("text-field-back-focus"), patch, patch, patch, patch));

		selection = new SpriteDrawable(getSprite("Selection"));
		selection_set = new SpriteDrawable(getSprite("Selection-set"));
		selection_left = new SpriteDrawable(getSprite("Selection-Left"));
		selection_right = new SpriteDrawable(getSprite("Selection-Right"));

		copy = new SpriteDrawable(getSprite("tf-copy"));
		paste = new SpriteDrawable(getSprite("tf-paste"));
		cut = new SpriteDrawable(getSprite("tf-cut"));

		textFieldCursor = new NinePatchDrawable(new NinePatch(Sprites.getSprite("selection-input-icon"), 1, 1, 2, 2));

	}

	private static void initialPortalModels() {
		ModelBuilder modelBuilder = new ModelBuilder();

		dimens = size / 4;
		matBlue = new Material(ColorAttribute.createDiffuse(Color.BLUE));
		matGreen = new Material(ColorAttribute.createDiffuse(Color.GREEN));
		matRed = new Material(ColorAttribute.createDiffuse(Color.RED));
		attributes = Usage.Position | Usage.Normal;

		model_PortalLegBottom = modelBuilder.createCylinder(dimens, size, dimens, 16, matBlue, attributes);
		model_PortalLegCenter = modelBuilder.createCylinder(dimens, size, dimens, 16, matGreen, attributes);
		model_PortalLegTop = modelBuilder.createCylinder(dimens, size, dimens, 16, matRed, attributes);
		model_PortalJibLeft = modelBuilder.createBox(size / 2, dimens, dimens, matBlue, attributes);
		model_PortalJibCenter = modelBuilder.createBox(size, dimens, dimens, matGreen, attributes);
		model_PortalJibRight = modelBuilder.createBox(size / 2, dimens, dimens, matRed, attributes);
		model_PortalRunWay = modelBuilder.createBox(dimens * 3, dimens * 2, dimens * 2, new Material(ColorAttribute.createDiffuse(Color.RED)), attributes);
	}

	public static Model getBoxModel() {
		return model_Box;
	}

	public static Model getBox2Model() {
		return model_Box2;
	}

	public static Model getField1Model() {
		return model_field1;
	}

	public static Model getField2Model() {
		return model_field2;
	}

	public static Model getPortalLegBottomModel() {
		return model_PortalLegBottom;
	}

	public static Model getPortalLegCenterModel() {
		return model_PortalLegCenter;
	}

	public static Model getPortalLegTopModel() {
		return model_PortalLegTop;
	}

	public static Model getPortalJibLeftModel() {
		return model_PortalJibLeft;
	}

	public static Model getPortalJibCenterModel() {
		return model_PortalJibCenter;
	}

	public static Model getPortalJibRightModel() {
		return model_PortalJibRight;
	}

	public static Model getPortalRunWay() {
		System.out.print("getRunWayModel");
		return model_PortalRunWay;
	}

	public static float getSize() {
		return size;
	}

	public static float getHalfSize() {
		return halfsize;
	}

	public static float getDpi() {
		return dpi;
	}

	public static void loadBoxModel(final String Path) {
		Model dispose = model_Box;
		ObjLoader loader = new ObjLoader();
		try {
			model_Box = loader.loadModel(Gdx.files.absolute(Path));
		} catch (Exception e) {
			GL_MsgBox.Show(e.toString(), "Fehler", MessageBoxIcon.Error);
		}
		CallResourceChanged();
		if (dispose != null)
			dispose.dispose();
		dispose = null;
	}

	public static void resetBoxModel() {
		Model dispose = model_Box;
		model_Box = modelBuilder.createBox(size, size, size, new Material(ColorAttribute.createDiffuse(trans)), Usage.Position | Usage.Normal);
		CallResourceChanged();
		if (dispose != null)
			dispose.dispose();
		dispose = null;
	}

	public static void loadRunWayModel(final String Path) {
		Model dispose = model_PortalRunWay;
		ObjLoader loader = new ObjLoader();
		try {
			model_PortalRunWay = loader.loadModel(Gdx.files.absolute(Path));
		} catch (Exception e) {
			GL_MsgBox.Show(e.toString(), "Fehler", MessageBoxIcon.Error);
		}
		CallResourceChanged();
		if (dispose != null)
			dispose.dispose();
		dispose = null;
	}

	public static void resetRunWayModel() {
		Model dispose = model_PortalRunWay;
		model_PortalRunWay = modelBuilder.createBox(dimens * 3, dimens * 2, dimens * 2, new Material(ColorAttribute.createDiffuse(Color.RED)), attributes);
		CallResourceChanged();
		if (dispose != null)
			dispose.dispose();
		dispose = null;
	}

	public static void loadPortalLegBottomModel(final String Path) {
		Model dispose = model_PortalLegBottom;
		ObjLoader loader = new ObjLoader();
		try {
			model_PortalLegBottom = loader.loadModel(Gdx.files.absolute(Path));
		} catch (Exception e) {
			GL_MsgBox.Show(e.toString(), "Fehler", MessageBoxIcon.Error);
		}
		CallResourceChanged();
		if (dispose != null)
			dispose.dispose();
		dispose = null;
	}

	public static void resetPortalLegBottomModel() {
		Model dispose = model_PortalLegBottom;
		model_PortalLegBottom = modelBuilder.createCylinder(dimens, size, dimens, 16, matBlue, attributes);
		CallResourceChanged();
		if (dispose != null)
			dispose.dispose();
		dispose = null;
	}

	public static void loadPortalLegCenterModel(final String Path) {
		Model dispose = model_PortalLegCenter;
		ObjLoader loader = new ObjLoader();
		try {
			model_PortalLegCenter = loader.loadModel(Gdx.files.absolute(Path));
		} catch (Exception e) {
			GL_MsgBox.Show(e.toString(), "Fehler", MessageBoxIcon.Error);
		}
		CallResourceChanged();
		if (dispose != null)
			dispose.dispose();
		dispose = null;
	}

	public static void resetPortalLegCenterModel() {
		Model dispose = model_PortalLegCenter;
		model_PortalLegCenter = modelBuilder.createCylinder(dimens, size, dimens, 16, matGreen, attributes);
		CallResourceChanged();
		if (dispose != null)
			dispose.dispose();
		dispose = null;
	}

	public static void loadPortalLegTopModel(final String Path) {
		Model dispose = model_PortalLegTop;
		ObjLoader loader = new ObjLoader();
		try {
			model_PortalLegTop = loader.loadModel(Gdx.files.absolute(Path));
		} catch (Exception e) {
			GL_MsgBox.Show(e.toString(), "Fehler", MessageBoxIcon.Error);
		}
		CallResourceChanged();
		if (dispose != null)
			dispose.dispose();
		dispose = null;
	}

	public static void resetPortalLegTopModel() {
		Model dispose = model_PortalLegTop;
		model_PortalLegTop = modelBuilder.createCylinder(dimens, size, dimens, 16, matRed, attributes);
		CallResourceChanged();
		if (dispose != null)
			dispose.dispose();
		dispose = null;
	}

	public static void loadPortalJibLeftModel(final String Path) {
		Model dispose = model_PortalJibLeft;
		ObjLoader loader = new ObjLoader();
		try {
			model_PortalJibLeft = loader.loadModel(Gdx.files.absolute(Path));
		} catch (Exception e) {
			GL_MsgBox.Show(e.toString(), "Fehler", MessageBoxIcon.Error);
		}
		CallResourceChanged();
		if (dispose != null)
			dispose.dispose();
		dispose = null;
	}

	public static void resetPortalJibLeftModel() {
		Model dispose = model_PortalJibLeft;
		model_PortalJibLeft = modelBuilder.createBox(size / 2, dimens, dimens, matBlue, attributes);
		CallResourceChanged();
		if (dispose != null)
			dispose.dispose();
		dispose = null;
	}

	public static void loadPortalJibCenterModel(final String Path) {
		Model dispose = model_PortalJibCenter;
		ObjLoader loader = new ObjLoader();
		try {
			model_PortalJibCenter = loader.loadModel(Gdx.files.absolute(Path));
		} catch (Exception e) {
			GL_MsgBox.Show(e.toString(), "Fehler", MessageBoxIcon.Error);
		}
		CallResourceChanged();
		if (dispose != null)
			dispose.dispose();
		dispose = null;
	}

	public static void resetPortalJibCenterModel() {
		Model dispose = model_PortalJibCenter;
		model_PortalJibCenter = modelBuilder.createBox(size, dimens, dimens, matGreen, attributes);
		CallResourceChanged();
		if (dispose != null)
			dispose.dispose();
		dispose = null;
	}

	public static void loadPortalJibRightModel(final String Path) {
		Model dispose = model_PortalJibRight;
		ObjLoader loader = new ObjLoader();
		try {
			model_PortalJibRight = loader.loadModel(Gdx.files.absolute(Path));
		} catch (Exception e) {
			GL_MsgBox.Show(e.toString(), "Fehler", MessageBoxIcon.Error);
		}
		CallResourceChanged();
		if (dispose != null)
			dispose.dispose();
		dispose = null;
	}

	public static void resetPortalJibRightModel() {
		Model dispose = model_PortalJibRight;
		model_PortalJibRight = modelBuilder.createBox(size / 2, dimens, dimens, matRed, attributes);
		CallResourceChanged();
		if (dispose != null)
			dispose.dispose();
		dispose = null;
	}

}
