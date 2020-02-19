/**
 * Display the contents of the current working directory.
 * The format is similar to the Unix ls -l
 * <em>This is an example of a bsh command written in Java for speed.</em>
 *
 * @method void dir( [ String dirname ] )
 */
package de.droidcachebox.locator.bsh.commands;

import de.droidcachebox.locator.bsh.CallStack;
import de.droidcachebox.locator.bsh.Interpreter;
import de.droidcachebox.utils.AbstractFile;
import de.droidcachebox.utils.FileFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class dir {
    static final String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};

    public static String usage() {
        return "usage: dir( String dir )\n       dir()";
    }

    /**
     * Implement dir() command.
     */
    public static void invoke(Interpreter env, CallStack callstack) {
        String dir = ".";
        invoke(env, callstack, dir);
    }

    /**
     * Implement dir( String directory ) command.
     */
    public static void invoke(Interpreter env, CallStack callstack, String dir) {
        AbstractFile abstractFile;
        try {
            abstractFile = env.pathToFile(dir);
        } catch (IOException e) {
            env.println("error reading path: " + e);
            return;
        }

        if (!abstractFile.exists() || !abstractFile.canRead()) {
            env.println("Can't read " + abstractFile);
            return;
        }
        if (!abstractFile.isDirectory()) {
            env.println("'" + dir + "' is not a directory");
        }

        String[] files = abstractFile.list();
        Arrays.sort(files);

        for (int i = 0; i < files.length; i++) {
            AbstractFile f = FileFactory.createFile(dir + AbstractFile.separator + files[i]);
            StringBuilder sb = new StringBuilder();
            sb.append(f.canRead() ? "r" : "-");
            sb.append(f.canWrite() ? "w" : "-");
            sb.append("_");
            sb.append(" ");

            Date d = new Date(f.lastModified());
            GregorianCalendar c = new GregorianCalendar();
            c.setTime(d);
            int day = c.get(Calendar.DAY_OF_MONTH);
            sb.append(months[c.get(Calendar.MONTH)] + " " + day);
            if (day < 10)
                sb.append(" ");

            sb.append(" ");

            // hack to get fixed length 'length' field
            int fieldlen = 8;
            StringBuilder len = new StringBuilder();
            for (int j = 0; j < fieldlen; j++)
                len.append(" ");
            len.insert(0, f.length());
            len.setLength(fieldlen);
            // hack to move the spaces to the front
            int si = len.toString().indexOf(" ");
            if (si != -1) {
                String pad = len.toString().substring(si);
                len.setLength(si);
                len.insert(0, pad);
            }

            sb.append(len.toString());

            sb.append(" " + f.getName());
            if (f.isDirectory())
                sb.append("/");

            env.println(sb.toString());
        }
    }
}
