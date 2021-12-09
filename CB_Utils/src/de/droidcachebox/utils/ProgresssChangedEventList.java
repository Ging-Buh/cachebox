package de.droidcachebox.utils;

public class ProgresssChangedEventList {
    public static CB_List<ProgressChangedEvent> listeners = new CB_List<>();

    public static void add(ProgressChangedEvent listener) {
        listeners.add(listener);
    }

    public static void remove(ProgressChangedEvent listener) {
        listeners.remove(listener);
    }

    public static void progressChanged(String Msg, String ProgressMessage, int Progress) {
        for (ProgressChangedEvent listener : listeners) {
            listener.progressChanged(Msg, ProgressMessage, Progress);
        }
    }

    public static void progressChanged(String ProgressMessage, int Progress) {
        for (ProgressChangedEvent listener : listeners) {
            listener.progressChanged("", ProgressMessage, Progress);
        }
    }

}
