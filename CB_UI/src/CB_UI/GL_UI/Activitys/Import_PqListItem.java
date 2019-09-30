package CB_UI.GL_UI.Activitys;

import CB_Core.Api.GroundspeakAPI.PQ;
import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI_Base.GL_UI.Controls.CB_CheckBox;
import CB_UI_Base.GL_UI.Controls.CB_Label;
import CB_UI_Base.GL_UI.Controls.List.ListViewItemBackground;
import CB_UI_Base.GL_UI.Fonts;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.UiSizes;

import java.text.SimpleDateFormat;

public class Import_PqListItem extends ListViewItemBackground {
    private final CB_CheckBox chk;
    private final CB_Label lblName, lblInfo;

    public Import_PqListItem(CB_RectF rec, int Index, final PQ pq) {
        super(rec, Index, "");

        lblName = new CB_Label(this.name + " lblName", getLeftWidth(), this.getHalfHeight(), this.getWidth() - getLeftWidth() - getRightWidth(), this.getHalfHeight());
        lblInfo = new CB_Label(this.name + " lblInfo", getLeftWidth(), 0, this.getWidth() - getLeftWidth() - getRightWidth(), this.getHalfHeight());

        lblName.setFont(Fonts.getSmall());
        lblInfo.setFont(Fonts.getBubbleSmall());

        lblName.setText(pq.name);

        SimpleDateFormat postFormater = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        String dateString = Translation.get("PQcreationDate") + ": " + postFormater.format(pq.lastGenerated);
        //DecimalFormat df = new DecimalFormat("###.##");
        //String FileSize = df.format(pq.sizeMB) + " MB";
        String Count = "\n" + Translation.get("Count") + ": " + String.valueOf(pq.cacheCount);
        lblInfo.setText(dateString + Count); // + "  " + FileSize

        chk = new CB_CheckBox("");
        chk.setRec(chk.ScaleCenter(0.6f));
        chk.setX(this.getWidth() - getRightWidth() - chk.getWidth() - UiSizes.getInstance().getMargin());
        chk.setY((this.getHalfHeight() - chk.getHalfHeight()) + chk.getHalfHeight());
        chk.setChecked(pq.doDownload);
        chk.setOnCheckChangedListener((view, isChecked) -> pq.doDownload = isChecked);
        this.addChild(lblName);
        this.addChild(lblInfo);
        this.addChild(chk);
    }

}
