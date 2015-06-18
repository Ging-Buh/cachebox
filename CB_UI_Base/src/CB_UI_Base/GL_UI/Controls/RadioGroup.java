package CB_UI_Base.GL_UI.Controls;

import java.util.ArrayList;

public class RadioGroup {
    private final ArrayList<RadioButton> radios;
    RadioButton aktSelected;

    public interface selectionChangedListner {
	public void selectionChanged(RadioButton radio, int idx);
    }

    private selectionChangedListner listner = null;

    public RadioGroup() {
	radios = new ArrayList<RadioButton>();
    }

    public void add(RadioButton radio) {
	radio.setRadioGroup(this);
	radios.add(radio);
    }

    public void remove(RadioButton radio) {
	radios.remove(radio);
    }

    public void aktivate(RadioButton radioButton) {
	aktSelected = radioButton;

	// alle anderen ausschalten
	int idx = -1;
	int selectedIdx = -1;
	for (RadioButton tmp : radios) {
	    idx++;
	    if (tmp == aktSelected) {
		tmp.setChecked(true);
		selectedIdx = idx;
		continue;
	    }
	    tmp.setChecked(false);
	}
	if (listner != null) {
	    listner.selectionChanged(aktSelected, selectedIdx);
	}
    }

    public RadioButton getActSelection() {
	return aktSelected;
    }

    public void addSelectionChangedListner(selectionChangedListner listner) {
	this.listner = listner;
    }

}
