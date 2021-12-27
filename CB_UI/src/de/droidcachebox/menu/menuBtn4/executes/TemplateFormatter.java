package de.droidcachebox.menu.menuBtn4.executes;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import de.droidcachebox.GlobalCore;
import de.droidcachebox.dataclasses.Draft;
import de.droidcachebox.dataclasses.Trackable;
import de.droidcachebox.settings.Settings;

public class TemplateFormatter {
    public static String replaceTemplate(String template, Draft draft) {
        template = template.replace("##finds##", String.valueOf(draft.foundNumber));
        DateFormat iso8601Format = new SimpleDateFormat("HH:mm");
        String stime = iso8601Format.format(draft.timestamp);
        iso8601Format = new SimpleDateFormat("dd-MM-yyyy");
        String sdate = iso8601Format.format(draft.timestamp);

        template = template.replace("<br>", "\n");
        template = template.replace("##date##", sdate);
        template = template.replace("##time##", stime);
        if (GlobalCore.isSetSelectedCache() && draft.gcCode.equals(GlobalCore.getSelectedCache().getGeoCacheCode())) {
            template = template.replace("##owner##", GlobalCore.getSelectedCache().getOwner());
        } else {
            template = template.replace("##owner##", "????????");
        }

        template = template.replace("##gcusername##", Settings.GcLogin.getValue());

        return template;
    }

    public static String replaceTemplate(String template, Trackable trackable) {
        return replaceTemplate(template, new Date());
    }

    public static String replaceTemplate(String template, Date timestamp) {
        DateFormat iso8601Format = new SimpleDateFormat("HH:mm");
        String stime = iso8601Format.format(timestamp);
        iso8601Format = new SimpleDateFormat("dd-MM-yyyy");
        String sdate = iso8601Format.format(timestamp);

        template = template.replace("<br>", "\n");
        template = template.replace("##date##", sdate);
        template = template.replace("##time##", stime);
        if (GlobalCore.isSetSelectedCache()) {
            // todo TB is possibly not in the selected cache
            template = template.replace("##owner##", GlobalCore.getSelectedCache().getOwner());
        } else {
            template = template.replace("##owner##", "????????");
        }

        template = template.replace("##gcusername##", Settings.GcLogin.getValue());

        return template;
    }

}
