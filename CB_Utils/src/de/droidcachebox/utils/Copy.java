package de.droidcachebox.utils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class Copy {
    ArrayList<CopyJobDefinition> mRules;

    public Copy(ArrayList<CopyJobDefinition> rules) {
        mRules = rules;
    }

    public Copy(CopyJobDefinition rule) {
        mRules = new ArrayList<>();
        mRules.add(rule);
    }

    public static void copyFolder(AbstractFile src, AbstractFile dest) throws IOException {

        if (src.isDirectory()) {

            // if directory not exists, create it
            if (!dest.exists()) {
                dest.mkdir();
            }

            // list all the directory contents
            String[] files = src.list();

            for (String file : files) {
                if (file.contains(".svn"))
                    continue;
                // construct the src and dest file structure
                AbstractFile srcAbstractFile = FileFactory.createFile(src, file);
                AbstractFile destAbstractFile = FileFactory.createFile(dest, file);
                // recursive copy
                copyFolder(srcAbstractFile, destAbstractFile);
            }

        } else {

            AbstractFile parent = FileFactory.createFile(dest.getParent());

            if (!parent.exists())
                parent.mkdir();

            // if file, then copy it
            // Use bytes stream to support all file types
            InputStream in = src.getFileInputStream();
            FileOutputStream out = dest.getFileOutputStream();

            byte[] buffer = new byte[1024];

            int length;
            // copy the file content in bytes
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }

            in.close();
            out.close();
        }
    }

    public void run() throws IOException {
        run(null);
    }

    public void run(CopyMsg msgCallBack) throws IOException {
        for (CopyJobDefinition rule : mRules) {
            if (msgCallBack != null)
                msgCallBack.msg("Copy: " + rule.nameOfJob);
            copyFolder(rule.sourcePath, rule.targetPath);
        }
    }

    public interface CopyMsg {
        void msg(String msg);
    }
}