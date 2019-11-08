package de.droidcachebox.gdx.controls;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import de.droidcachebox.gdx.CB_View_Base;
import de.droidcachebox.gdx.graphics.GradiantFill;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.utils.MathUtils;

public class GradiantFilledRectangle extends CB_View_Base {

    private GradiantFill gradiant;
    private TextureRegion tex;

    private float drawW = 0;
    private float drawH = 0;
    private float drawCX = 0;
    private float drawCY = 0;
    private float drawX = 0;
    private float drawY = 0;

    public GradiantFilledRectangle(CB_RectF rec, GradiantFill fill) {
        super(rec, "");
        gradiant = fill;
    }

    public void setGradiant(GradiantFill fill) {
        gradiant = fill;
        tex = null;
    }

    @Override
    protected void render(Batch batch) {
        if (tex == null || tex != gradiant.getTexture()) {
            if (gradiant.getTexture() != null) {
                tex = gradiant.getTexture();

                // TODO handle angle over 90�

                double alpha = (gradiant.getDirection() * MathUtils.DEG_RAD);

                float x1 = (float) (getWidth() * Math.cos(alpha));
                float x2 = (float) (getHeight() * Math.sin(alpha));

                float y1 = (float) (getWidth() * Math.sin(alpha));
                float y2 = (float) (getHeight() * Math.cos(alpha));

                drawW = x1 + x2;
                drawH = y1 + y2;

                drawCX = (drawW / 2);
                drawCY = (drawH / 2);

                drawX = -(drawCX - this.getHalfWidth());
                drawY = -(drawCY - this.getHalfHeight());

            }
        }

        if (tex != null)
            batch.draw(tex, drawX, drawY, drawCX, drawCY, drawW, drawH, 1f, 1f, gradiant.getDirection());

    }
}
