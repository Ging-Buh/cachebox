package de.droidcachebox.utils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class Copy {
    ArrayList<CopyRule> mRules;

    public Copy(ArrayList<CopyRule> rules) {
        mRules = rules;
    }

    public Copy(CopyRule rule) {
        mRules = new ArrayList<CopyRule>();
        mRules.add(rule);
    }

    public static void copyFolder(File src, File dest) throws IOException {

        if (src.isDirectory()) {

            // if directory not exists, create it
            if (!dest.exists()) {
                dest.mkdir();
            }

            // list all the directory contents
            String files[] = src.list();

            for (String file : files) {
                if (file.contains(".svn"))
                    continue;
                // construct the src and dest file structure
                File srcFile = FileFactory.createFile(src, file);
                File destFile = FileFactory.createFile(dest, file);
                // recursive copy
                copyFolder(srcFile, destFile);
            }

        } else {

            File parent = FileFactory.createFile(dest.getParent());

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

    public void Run() throws IOException {
        Run(null);
    }

    public void Run(CopyMsg MsgCallBack) throws IOException {
        for (CopyRule rule : mRules) {
            if (MsgCallBack != null)
                MsgCallBack.Msg("Copy: " + rule.Name);
            copyFolder(rule.sourcePath, rule.targetPath);
        }
    }

    public interface CopyMsg {
        public void Msg(String msg);
    }
}