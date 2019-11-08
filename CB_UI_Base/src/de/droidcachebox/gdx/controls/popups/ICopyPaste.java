package de.droidcachebox.gdx.controls.popups;

public interface ICopyPaste {
    String pasteFromClipboard();

    String copyToClipboard();

    String cutToClipboard();

    boolean isEditable();
}
