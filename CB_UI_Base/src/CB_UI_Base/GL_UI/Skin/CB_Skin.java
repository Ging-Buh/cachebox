package CB_UI_Base.GL_UI.Skin;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

import CB_UI_Base.Global;
import CB_UI_Base.settings.CB_UI_Base_Settings;

public abstract class CB_Skin extends SkinBase {

	public static final CB_Skin INSTANCE = new CB_Skin() {
		@Override
		protected void initial() {
			SkinSettings cfg = new SkinSettings();

			FileHandle fh;
			if (CB_UI_Base_Settings.SkinFolder.getValue().equals("default")) {
				fh = Global.getInternalFileHandle("skins/default");
			} else if (CB_UI_Base_Settings.SkinFolder.getValue().equals("small")) {
				fh = Global.getInternalFileHandle("skins/small");
			} else {
				fh = Gdx.files.absolute(CB_UI_Base_Settings.SkinFolder.getValue());
			}

			cfg.SkinFolder = fh;
			cfg.DefaultSkinFolder = Global.getInternalFileHandle("skins/default");

			cfg.SizeBiggest = CB_UI_Base_Settings.FONT_SIZE_COMPASS_DISTANCE.getValue();
			cfg.SizeBig = CB_UI_Base_Settings.FONT_SIZE_BIG.getValue();
			cfg.SizeNormal = CB_UI_Base_Settings.FONT_SIZE_NORMAL.getValue();
			cfg.SizeNormalbubble = CB_UI_Base_Settings.FONT_SIZE_NORMAL_BUBBLE.getValue();
			cfg.SizeSmall = CB_UI_Base_Settings.FONT_SIZE_SMALL.getValue();
			cfg.SizeSmallBubble = CB_UI_Base_Settings.FONT_SIZE_SMALL_BUBBLE.getValue();
			cfg.Nightmode = CB_UI_Base_Settings.nightMode.getValue();

			settings = cfg;
		}

	};

	private CB_Skin() {
		super();
		initial();
		that = this;
	}

	protected abstract void initial();

}
