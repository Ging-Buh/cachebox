package de.droidcachebox.gdx.main;

import java.util.ArrayList;

public class CB_ButtonBar {

    public ArrayList<GestureButton> Buttons;

    public CB_ButtonBar() {

    }

    public void addButton(GestureButton Button) {
        if (Buttons == null)
            Buttons = new ArrayList<>();
        Buttons.add(Button);
    }

}
