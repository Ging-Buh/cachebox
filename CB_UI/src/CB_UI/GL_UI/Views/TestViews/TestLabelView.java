package CB_UI.GL_UI.Views.TestViews;

import CB_UI_Base.Enums.WrapType;
import CB_UI_Base.GL_UI.CB_View_Base;
import CB_UI_Base.GL_UI.COLOR;
import CB_UI_Base.GL_UI.Controls.EditTextField;
import CB_UI_Base.GL_UI.Controls.Label;
import CB_UI_Base.GL_UI.Controls.Label.HAlignment;
import CB_UI_Base.GL_UI.Controls.Label.VAlignment;
import CB_UI_Base.GL_UI.Controls.RadioButton;
import CB_UI_Base.GL_UI.Controls.RadioGroup;
import CB_UI_Base.GL_UI.Controls.RadioGroup.ISelectionChangedListener;
import CB_UI_Base.GL_UI.utils.ColorDrawable;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.UI_Size_Base;

public class TestLabelView extends CB_View_Base {

    public TestLabelView(CB_RectF rec, String Name) {
	super(rec, Name);
	this.setBackground(new ColorDrawable(COLOR.getMenuBackColor()));
    }

    @Override
    protected void Initial() {
	float margin = UI_Size_Base.that.getMargin();

	final RadioButton rb = new RadioButton("1 segment");

	float rbmargin = rb.getHeight() - margin;

	rb.setPos(5, this.getHeight() - rbmargin);
	rb.setWidth(this.getHalfWidth() - rb.getX());
	rb.setText("left/center");
	this.addChild(rb);

	final RadioButton rb2 = new RadioButton("Test");
	rb2.setPos(5, rb.getY() - rbmargin);
	rb2.setWidth(this.getHalfWidth() - rb.getX());
	rb2.setText("center/center");
	this.addChild(rb2);

	final RadioButton rb3 = new RadioButton("Test");
	rb3.setPos(5, rb2.getY() - rbmargin);
	rb3.setWidth(this.getHalfWidth() - rb.getX());
	rb3.setText("right/center");
	this.addChild(rb3);

	final RadioButton rb4 = new RadioButton("Test");
	rb4.setPos(5, rb3.getY() - rbmargin);
	rb4.setWidth(this.getHalfWidth() - rb.getX());
	rb4.setText("left/top");
	this.addChild(rb4);

	final RadioButton rb5 = new RadioButton("Test");
	rb5.setPos(5, rb4.getY() - rbmargin);
	rb5.setWidth(this.getHalfWidth() - rb.getX());
	rb5.setText("center/top");
	this.addChild(rb5);

	final RadioButton rb6 = new RadioButton("Test");
	rb6.setPos(5, rb5.getY() - rbmargin);
	rb6.setWidth(this.getHalfWidth() - rb.getX());
	rb6.setText("right/top");
	this.addChild(rb6);

	final RadioButton rb7 = new RadioButton("Test");
	rb7.setPos(5, rb6.getY() - rbmargin);
	rb7.setWidth(this.getHalfWidth() - rb.getX());
	rb7.setText("left/bottom");
	this.addChild(rb7);

	final RadioButton rb8 = new RadioButton("Test");
	rb8.setPos(5, rb7.getY() - rbmargin);
	rb8.setWidth(this.getHalfWidth() - rb.getX());
	rb8.setText("center/bottom");
	this.addChild(rb8);

	final RadioButton rb9 = new RadioButton("Test");
	rb9.setPos(5, rb8.getY() - rbmargin);
	rb9.setWidth(this.getHalfWidth() - rb.getX());
	rb9.setText("right/bottom");
	this.addChild(rb9);

	final RadioGroup Group = new RadioGroup();
	Group.add(rb);
	Group.add(rb2);
	Group.add(rb3);
	Group.add(rb4);
	Group.add(rb5);
	Group.add(rb6);
	Group.add(rb7);
	Group.add(rb8);
	Group.add(rb9);
	Group.aktivate(rb);

	Group.addSelectionChangedListener(new ISelectionChangedListener() {

	    @Override
	    public void selectionChanged(RadioButton radio, int idx) {
		switch (idx) {
		case 0:
		    lbl.setHAlignment(HAlignment.LEFT);
		    lbl.setVAlignment(VAlignment.CENTER);
		    break;
		case 1:
		    lbl.setHAlignment(HAlignment.CENTER);
		    lbl.setVAlignment(VAlignment.CENTER);
		    break;
		case 2:
		    lbl.setHAlignment(HAlignment.RIGHT);
		    lbl.setVAlignment(VAlignment.CENTER);
		    break;
		case 3:
		    lbl.setHAlignment(HAlignment.LEFT);
		    lbl.setVAlignment(VAlignment.TOP);
		    break;
		case 4:
		    lbl.setHAlignment(HAlignment.CENTER);
		    lbl.setVAlignment(VAlignment.TOP);
		    break;
		case 5:
		    lbl.setHAlignment(HAlignment.RIGHT);
		    lbl.setVAlignment(VAlignment.TOP);
		    break;
		case 6:
		    lbl.setHAlignment(HAlignment.LEFT);
		    lbl.setVAlignment(VAlignment.BOTTOM);
		    break;
		case 7:
		    lbl.setHAlignment(HAlignment.CENTER);
		    lbl.setVAlignment(VAlignment.BOTTOM);
		    break;
		case 8:
		    lbl.setHAlignment(HAlignment.RIGHT);
		    lbl.setVAlignment(VAlignment.BOTTOM);
		    break;
		}
	    }
	});

	CB_RectF rec = new CB_RectF(2, 10, this.getWidth() - 2, 100);
	lbl = new Label(this.name + " lbl", rec, "Test Text gq!\nABCDEFGHIJKLMNOPQRSTUVWXYZ");
	lbl.setWrapType(WrapType.MULTILINE);
	lbl.setBackground(new ColorDrawable(COLOR.getHighLightFontColor()));
	//	this.addChild(lbl);

	rec.setHeight(300);

	EditTextField edit = new EditTextField(rec, this, this.name + " edit");
	edit.setWrapType(WrapType.MULTILINE);
	edit.setText("Test Text\nin zwei Zeilen");
	edit.showFromLineNo(0);
	edit.setSelection(4, 16);

	this.addChild(edit);

    }

    Label lbl;

    @Override
    protected void SkinIsChanged() {
	// TODO Auto-generated method stub

    }

}
