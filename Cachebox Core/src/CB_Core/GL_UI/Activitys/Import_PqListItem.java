package CB_Core.GL_UI.Activitys;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

import CB_Core.Api.PocketQuery.PQ;
import CB_Core.GL_UI.Fonts;
import CB_Core.GL_UI.Controls.Dialog;
import CB_Core.GL_UI.Controls.Label;
import CB_Core.GL_UI.Controls.chkBox;
import CB_Core.GL_UI.Controls.chkBox.OnCheckedChangeListener;
import CB_Core.GL_UI.Controls.List.ListViewItemBackground;
import CB_Core.Math.CB_RectF;

public class Import_PqListItem extends ListViewItemBackground
{
	private chkBox chk;
	private Label lblName, lblInfo;

	public Import_PqListItem(CB_RectF rec, int Index, final PQ pq)
	{
		super(rec, Index, "");

		lblName = new Label(getLeftWidth(), this.halfHeight, this.width - getLeftWidth() - getRightWidth(), this.halfHeight, "");
		lblInfo = new Label(getLeftWidth(), 0, this.width - getLeftWidth() - getRightWidth(), this.halfHeight, "");

		lblName.setFont(Fonts.getNormal());
		lblInfo.setFont(Fonts.getSmall());

		lblName.setText(pq.Name);

		SimpleDateFormat postFormater = new SimpleDateFormat("dd.MM.yy");
		String dateString = postFormater.format(pq.DateLastGenerated);
		DecimalFormat df = new DecimalFormat("###.##");
		String FileSize = df.format(pq.SizeMB) + " MB";
		String Count = "   Count=" + String.valueOf(pq.PQCount);
		lblInfo.setText(dateString + "  " + FileSize + Count);

		chk = new chkBox("");
		chk.setX(this.width - getRightWidth() - chk.getWidth() - Dialog.margin);
		chk.setY(this.halfHeight - chk.getHalfHeight());
		chk.setChecked(pq.downloadAvible);
		chk.setOnCheckedChangeListener(new OnCheckedChangeListener()
		{

			@Override
			public void onCheckedChanged(chkBox view, boolean isChecked)
			{
				pq.downloadAvible = isChecked;
			}
		});

		this.addChild(lblName);
		this.addChild(lblInfo);
		this.addChild(chk);
	}

	@Override
	protected void SkinIsChanged()
	{

	}

}
