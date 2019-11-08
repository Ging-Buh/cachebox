package de.droidcachebox.gdx.controls;

import java.util.ArrayList;

public class RadioGroup {
    private final ArrayList<RadioButton> radios;
    RadioButton aktSelected;
    private ISelectionChangedListener listener = null;

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
        if (listener != null) {
            listener.selectionChanged(aktSelected, selectedIdx);
        }
    }

    public RadioButton getActSelection() {
        return aktSelected;
    }

    public void addSelectionChangedListener(ISelectionChangedListener listener) {
        this.listener = listener;
    }

    public interface ISelectionChangedListener {
        public void selectionChanged(RadioButton radio, int idx);
    }

}
