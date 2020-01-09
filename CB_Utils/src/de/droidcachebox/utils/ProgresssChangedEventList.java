package de.droidcachebox.utils;

public class ProgresssChangedEventList {
    public static CB_List<ProgressChangedEvent> list = new CB_List<ProgressChangedEvent>();

    public static void Add(ProgressChangedEvent event) {
        list.add(event);
    }

    public static void Call(String Msg, String ProgressMessage, int Progress) {
        for (int i = 0, n = list.size(); i < n; i++) {
            ProgressChangedEvent event = list.get(i);
            event.progressChangedEventCalled(Msg, ProgressMessage, Progress);
        }
    }

    public static void Call(String ProgressMessage, int Progress) {
        for (int i = 0, n = list.size(); i < n; i++) {
            ProgressChangedEvent event = list.get(i);
            event.progressChangedEventCalled("", ProgressMessage, Progress);
        }
    }

    public static void Remove(ProgressChangedEvent event) {
        list.remove(event);
    }

}
