package CB_UI.GL_UI.Activitys.APIs;

import CB_RpcCore.Functions.RpcAnswer_GetExportList;
import CB_UI_Base.GL_UI.Controls.CB_CheckBox;
import CB_UI_Base.GL_UI.Controls.CB_CheckBox.OnCheckChangedListener;
import CB_UI_Base.GL_UI.Controls.CB_Label;
import CB_UI_Base.GL_UI.Controls.List.ListViewItemBackground;
import CB_UI_Base.GL_UI.Fonts;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.UI_Size_Base;

public class ImportAPIListItem extends ListViewItemBackground {
    private CB_CheckBox chk;
    private CB_Label lblName, lblInfo;

    public ImportAPIListItem(CB_RectF rec, int Index, final RpcAnswer_GetExportList.ListItem item) {
        super(rec, Index, "");

        lblName = new CB_Label(this.name + " lblName", getLeftWidth(), this.getHalfHeight(), this.getWidth() - getLeftWidth() - getRightWidth(), this.getHalfHeight());
        lblInfo = new CB_Label(this.name + " lblInfo", getLeftWidth(), 0, this.getWidth() - getLeftWidth() - getRightWidth(), this.getHalfHeight());

        lblName.setFont(Fonts.getNormal());
        lblInfo.setFont(Fonts.getSmall());

        lblName.setText(item.getDescription());

        // SimpleDateFormat postFormater = new SimpleDateFormat("dd.MM.yy");
        // String dateString = postFormater.format(pq.lastGenerated);
        // DecimalFormat df = new DecimalFormat("###.##");
        // String FileSize = df.format(pq.sizeMB) + " MB";
        String Count = "   Count=" + String.valueOf(item.getCacheCount());
        lblInfo.setText(Count);

        // lblInfo.setText("---");

        chk = new CB_CheckBox("");
        chk.setX(this.getWidth() - getRightWidth() - chk.getWidth() - UI_Size_Base.that.getMargin());
        chk.setY(this.getHalfHeight() - chk.getHalfHeight());
        chk.setChecked(false);
        chk.setOnCheckChangedListener(new OnCheckChangedListener() {

            @Override
            public void onCheckedChanged(CB_CheckBox view, boolean isChecked) {
                item.setDownload(isChecked);
            }
        });

        this.addChild(lblName);
        this.addChild(lblInfo);
        this.addChild(chk);
    }

}
