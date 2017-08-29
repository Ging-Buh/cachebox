package CB_UI.GL_UI.Activitys.APIs;

import CB_RpcCore.Functions.RpcAnswer_GetExportList;
import CB_UI_Base.GL_UI.Fonts;
import CB_UI_Base.GL_UI.Controls.Label;
import CB_UI_Base.GL_UI.Controls.ChkBox;
import CB_UI_Base.GL_UI.Controls.ChkBox.OnCheckChangedListener;
import CB_UI_Base.GL_UI.Controls.List.ListViewItemBackground;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.UI_Size_Base;

public class ImportAPIListItem extends ListViewItemBackground {
	private ChkBox chk;
	private Label lblName, lblInfo;

	public ImportAPIListItem(CB_RectF rec, int Index, final RpcAnswer_GetExportList.ListItem item) {
		super(rec, Index, "");

		lblName = new Label(this.name + " lblName", getLeftWidth(), this.getHalfHeight(), this.getWidth() - getLeftWidth() - getRightWidth(), this.getHalfHeight());
		lblInfo = new Label(this.name + " lblInfo", getLeftWidth(), 0, this.getWidth() - getLeftWidth() - getRightWidth(), this.getHalfHeight());

		lblName.setFont(Fonts.getNormal());
		lblInfo.setFont(Fonts.getSmall());

		lblName.setText(item.getDescription());

		// SimpleDateFormat postFormater = new SimpleDateFormat("dd.MM.yy");
		// String dateString = postFormater.format(pq.DateLastGenerated);
		// DecimalFormat df = new DecimalFormat("###.##");
		// String FileSize = df.format(pq.SizeMB) + " MB";
		String Count = "   Count=" + String.valueOf(item.getCacheCount());
		lblInfo.setText(Count);

		// lblInfo.setText("---");

		chk = new ChkBox("");
		chk.setX(this.getWidth() - getRightWidth() - chk.getWidth() - UI_Size_Base.that.getMargin());
		chk.setY(this.getHalfHeight() - chk.getHalfHeight());
		chk.setChecked(false);
		chk.setOnCheckChangedListener(new OnCheckChangedListener() {

			@Override
			public void onCheckedChanged(ChkBox view, boolean isChecked) {
				item.setDownload(isChecked);
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
