package CB_UI.GL_UI.Views;

import CB_UI_Base.Events.platformConector;
import CB_UI_Base.GL_UI.CB_View_Base;
import CB_UI_Base.GL_UI.Fonts;
import CB_UI_Base.GL_UI.SpriteCacheBase;
import CB_UI_Base.GL_UI.ViewConst;
import CB_UI_Base.GL_UI.Controls.Label;
import CB_UI_Base.Math.CB_RectF;
import CB_Utils.Plattform;

public class JokerView extends CB_View_Base {

    public JokerView(CB_RectF rec, String Name) {
	super(rec, Name);

	Label lblDummy = new Label(this.name + " lblDummy", CB_RectF.ScaleCenter(rec, 0.8f));
	lblDummy.setFont(Fonts.getNormal());
	lblDummy.setText("Dummy JokerView");
	setBackground(SpriteCacheBase.ListBack);

	if (Plattform.used == Plattform.Desktop)
	    this.addChild(lblDummy);

    }

    @Override
    public void onShow() {
	// Rufe ANDROID VIEW auf
	platformConector.showView(ViewConst.JOKER_VIEW, 0, 0, this.getWidth(), this.getHeight());
    }

    @Override
    public void onHide() {
	platformConector.hideView(ViewConst.JOKER_VIEW);
    }

    @Override
    protected void Initial() {

    }

    @Override
    protected void SkinIsChanged() {

    }
}
