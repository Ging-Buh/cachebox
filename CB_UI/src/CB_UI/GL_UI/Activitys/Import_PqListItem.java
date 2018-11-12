package CB_UI.GL_UI.Activitys;

import CB_Core.Api.GroundspeakAPI.PQ;
import CB_UI_Base.GL_UI.Controls.ChkBox;
import CB_UI_Base.GL_UI.Controls.ChkBox.OnCheckChangedListener;
import CB_UI_Base.GL_UI.Controls.Label;
import CB_UI_Base.GL_UI.Controls.List.ListViewItemBackground;
import CB_UI_Base.GL_UI.Fonts;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.UI_Size_Base;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

public class Import_PqListItem extends ListViewItemBackground {
    private final ChkBox chk;
    private final Label lblName, lblInfo;

    public Import_PqListItem(CB_RectF rec, int Index, final PQ pq) {
        super(rec, Index, "");

        lblName = new Label(this.name + " lblName", getLeftWidth(), this.getHalfHeight(), this.getWidth() - getLeftWidth() - getRightWidth(), this.getHalfHeight());
        lblInfo = new Label(this.name + " lblInfo", getLeftWidth(), 0, this.getWidth() - getLeftWidth() - getRightWidth(), this.getHalfHeight());

        lblName.setFont(Fonts.getSmall());
        lblInfo.setFont(Fonts.getBubbleSmall());

        lblName.setText(pq.Name);

        SimpleDateFormat postFormater = new SimpleDateFormat("dd.MM.yy");
        String dateString = postFormater.format(pq.DateLastGenerated);
        DecimalFormat df = new DecimalFormat("###.##");
        String FileSize = df.format(pq.SizeMB) + " MB";
        // String Count = "   Count=" + String.valueOf(pq.PQCount);
        lblInfo.setText(dateString + "  " + FileSize); //  + Count

        chk = new ChkBox("");
        chk.setRec(chk.ScaleCenter(0.6f));
        chk.setX(this.getWidth() - getRightWidth() - chk.getWidth() - UI_Size_Base.that.getMargin());
        chk.setY((this.getHalfHeight() - chk.getHalfHeight()) + chk.getHalfHeight());
        chk.setChecked(pq.downloadAvailable);
        chk.setOnCheckChangedListener(new OnCheckChangedListener() {

            @Override
            public void onCheckedChanged(ChkBox view, boolean isChecked) {
                pq.downloadAvailable = isChecked;
            }
        });

        this.addChild(lblName);
        this.addChild(lblInfo);
        this.addChild(chk);
    }

}
