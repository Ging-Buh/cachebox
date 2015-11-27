package CB_UI.GL_UI.Activitys;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

import CB_Core.Api.PocketQuery.PQ;
import CB_UI_Base.GL_UI.Fonts;
import CB_UI_Base.GL_UI.Controls.Label;
import CB_UI_Base.GL_UI.Controls.chkBox;
import CB_UI_Base.GL_UI.Controls.chkBox.OnCheckedChangeListener;
import CB_UI_Base.GL_UI.Controls.List.ListViewItemBackground;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.UI_Size_Base;

public class Import_PqListItem extends ListViewItemBackground {
    private chkBox chk;
    private Label lblName, lblInfo;

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
	String Count = "   Count=" + String.valueOf(pq.PQCount);
	lblInfo.setText(dateString + "  " + FileSize + Count);

	chk = new chkBox("");
	chk.setRec(chk.ScaleCenter(0.6f));
	chk.setX(this.getWidth() - getRightWidth() - chk.getWidth() - UI_Size_Base.that.getMargin());
	chk.setY((this.getHalfHeight() - chk.getHalfHeight()) + chk.getHalfHeight());
	chk.setChecked(pq.downloadAvible);
	chk.setOnCheckedChangeListener(new OnCheckedChangeListener() {

	    @Override
	    public void onCheckedChanged(chkBox view, boolean isChecked) {
		pq.downloadAvible = isChecked;
	    }
	});

	this.addChild(lblName);
	this.addChild(lblInfo);
	this.addChild(chk);
    }

    @Override
    protected void SkinIsChanged() {

    }

}
