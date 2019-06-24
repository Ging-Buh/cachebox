package CB_UI_Base.GL_UI.Main;

import java.util.ArrayList;

public class CB_ButtonList {

    public ArrayList<GestureButton> Buttons;

    public CB_ButtonList() {

    }

    public void addButton(GestureButton Button) {
        if (Buttons == null)
            Buttons = new ArrayList<>();
        Buttons.add(Button);
    }

}
