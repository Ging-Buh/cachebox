package CB_UI.GL_UI.Activitys.APIs;

import CB_Core.Types.ExportEntry;
import CB_UI_Base.GL_UI.Fonts;
import CB_UI_Base.GL_UI.Controls.Label;
import CB_UI_Base.GL_UI.Controls.chkBox;
import CB_UI_Base.GL_UI.Controls.chkBox.OnCheckedChangeListener;
import CB_UI_Base.GL_UI.Controls.List.ListViewItemBackground;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.UI_Size_Base;

public class ExportCBServerListItem extends ListViewItemBackground {
    private chkBox chk;
    private Label lblName, lblInfo;

    public ExportCBServerListItem(CB_RectF rec, int Index, final ExportEntry item) {
	super(rec, Index, "");

	lblName = new Label(this.name + " lblName", getLeftWidth(), this.getHalfHeight(), this.getWidth() - getLeftWidth() - getRightWidth(), this.getHalfHeight());
	lblInfo = new Label(this.name + " lblInfo", getLeftWidth(), 0, this.getWidth() - getLeftWidth() - getRightWidth(), this.getHalfHeight());

	lblName.setFont(Fonts.getNormal());
	lblInfo.setFont(Fonts.getSmall());

	lblName.setText(item.cacheName);

	// SimpleDateFormat postFormater = new SimpleDateFormat("dd.MM.yy");
	// String dateString = postFormater.format(pq.DateLastGenerated);
	// DecimalFormat df = new DecimalFormat("###.##");
	// String FileSize = df.format(pq.SizeMB) + " MB";
	String type = String.valueOf(item.changeType);
	lblInfo.setText(type);

	// lblInfo.setText("---");

	chk = new chkBox("");
	chk.setX(this.getWidth() - getRightWidth() - chk.getWidth() - UI_Size_Base.that.getMargin());
	chk.setY(this.getHalfHeight() - chk.getHalfHeight());
	chk.setChecked(item.toExport);
	chk.setOnCheckedChangeListener(new OnCheckedChangeListener() {

	    @Override
	    public void onCheckedChanged(chkBox view, boolean isChecked) {
		item.setExport(isChecked);
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
