package de.droidcachebox;

import de.droidcachebox.database.Draft;
import de.droidcachebox.database.Trackable;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TemplateFormatter {
    public static String ReplaceTemplate(String template, Draft draft) {
        template = template.replace("##finds##", String.valueOf(draft.foundNumber));
        DateFormat iso8601Format = new SimpleDateFormat("HH:mm");
        String stime = iso8601Format.format(draft.timestamp);
        iso8601Format = new SimpleDateFormat("dd-MM-yyyy");
        String sdate = iso8601Format.format(draft.timestamp);

        template = template.replace("<br>", "\n");
        template = template.replace("##date##", sdate);
        template = template.replace("##time##", stime);
        if (GlobalCore.isSetSelectedCache() && draft.gcCode.equals(GlobalCore.getSelectedCache().getGcCode())) {
            template = template.replace("##owner##", GlobalCore.getSelectedCache().getOwner());
        } else {
            template = template.replace("##owner##", "????????");
        }

        template = template.replace("##gcusername##", Config.GcLogin.getValue());

        return template;
    }

    public static String ReplaceTemplate(String template, Trackable TB) {
        return ReplaceTemplate(template, new Date());
    }

    public static String ReplaceTemplate(String template, Date timestamp) {
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

        template = template.replace("##gcusername##", Config.GcLogin.getValue());

        return template;
    }

}
