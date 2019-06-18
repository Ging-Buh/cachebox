package CB_UI_Base.GL_UI.Main;

import java.util.ArrayList;

public class CB_ButtonList {

    public ArrayList<CB_CB_Button> Buttons;

    public CB_ButtonList() {

    }

    public void addButton(CB_CB_Button Button) {
        if (Buttons == null)
            Buttons = new ArrayList<>();
        Buttons.add(Button);
    }

}
