package de.droidcachebox.components;

import android.content.res.AssetManager;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import de.droidcachebox.utils.AbstractFile;
import de.droidcachebox.utils.FileFactory;

// Kopiert die OrdnerStructur des Asset-Ordners auf die SD-Karte (Cachebox Arbeitsverzeichniss)
public class CopyAssetFolder {
    public static ArrayList<String> FileList = new ArrayList<String>();

    private static void copyFile(AssetManager assets, String source, String targetPath) throws IOException {
        String target = targetPath + "/" + source;

        InputStream myInput = assets.open(source);

        AbstractFile ziel = FileFactory.createFile(target);
        ziel.getParentFile().mkdirs();
        final OutputStream myOutput = new FileOutputStream(target);

        int dotposition = target.lastIndexOf(".");
        String ext = target.substring(dotposition + 1, target.length()).toLowerCase();
        if (ext.equals("xml") && !targetPath.toLowerCase().equals("/mnt/sdcard/cachebox")) {
            // in xml files replace the fixed path "/mnt/sdcard/cachebox" by the actual workpath
            // Reson for this: in the mapsforge theme files (xml) the path to the image files must be entered absolute, no relative paths
            // are allowed
            // Copy text file and replace paths if found
            BufferedReader r = new BufferedReader(new InputStreamReader(myInput));
            String x = "";
            x = r.readLine();

            BufferedWriter w = new BufferedWriter(new OutputStreamWriter(myOutput));

            while (x != null) {
                // case insensitive replace of the orginal path by the new workPath
                x = x.replaceAll("(?i)/mnt/sdcard/cachebox", targetPath);
                w.write(x + "\n");
                x = r.readLine();
            }
            w.close();
            r.close();
        } else {
            // transfer bytes from the inputfile to the outputfile
            byte[] buffer = new byte[1024];
            int length;
            while ((length = myInput.read(buffer)) > 0) {
                myOutput.write(buffer, 0, length);
            }
        }

        // Close the streams
        myOutput.flush();
        myOutput.close();

        myInput.close();

    }

    public void copyAll(AssetManager assets, String targetPath, String[] exludeFolder) {
        FileList = new ArrayList<String>();
        try {
            listDir(assets, "", exludeFolder);
        } catch (IOException e) {

            e.printStackTrace();
        }

        try {
            for (String tmp : FileList) {
                try {
                    copyFile(assets, tmp, targetPath);
                } catch (IOException e) {

                    e.printStackTrace();
                }
            }
        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    public void copyAll(AssetManager assets, String targetPath) {
        String[] leer = new String[]{""};
        copyAll(assets, targetPath, leer);
    }

    private void listDir(AssetManager assets, String dir, String[] excludeFolder) throws IOException {
        String[] files = assets.list(dir);
        if (files != null) {
            for (String file : files) {
                boolean exclude = false;
                for (String tmp : excludeFolder) {
                    if (file.equals(tmp)) {
                        exclude = true;
                        break;
                    }
                }
                if (!exclude) {
                    String Entry = (dir.length() == 0) ? file : dir + "/" + file;
                    if (!Entry.contains(".")) {
                        listDir(assets, Entry, excludeFolder);
                    } else {
                        FileList.add(Entry);

                    }
                }
            }
        }
    }

}
