package CB_Core;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import CB_Core.Types.FieldNoteEntry;
import CB_Core.Types.Trackable;

public class TemplateFormatter
{
	public static String ReplaceTemplate(String template, FieldNoteEntry fieldNote)
	{
		template = template.replace("##finds##", String.valueOf(fieldNote.foundNumber));
		return ReplaceTemplate(template, fieldNote.timestamp);
	}

	public static String ReplaceTemplate(String template, Trackable TB)
	{
		return ReplaceTemplate(template, new Date());
	}

	private static String ReplaceTemplate(String template, Date timestamp)
	{
		DateFormat iso8601Format = new SimpleDateFormat("HH:mm");
		String stime = iso8601Format.format(timestamp);
		iso8601Format = new SimpleDateFormat("dd-MM-yyyy");
		String sdate = iso8601Format.format(timestamp);

		template = template.replace("<br>", "\n");
		template = template.replace("##date##", sdate);
		template = template.replace("##time##", stime);
		if (GlobalCore.getSelectedCache() != null)
		{
			template = template.replace("##owner##", GlobalCore.getSelectedCache().Owner);
		}
		else
		{
			template = template.replace("##owner##", "????????");
		}

		template = template.replace("##gcusername##", Config.settings.GcLogin.getValue());

		return template;
	}

}
