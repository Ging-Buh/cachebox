package CB_Utils.http;

import CB_Utils.Log.Log;
import CB_Utils.fileProvider.File;
import CB_Utils.fileProvider.FileFactory;

import java.io.BufferedOutputStream;
import java.io.InputStream;

public class Download {

    public static Boolean Download(String remote, String local) {

        boolean err = false;
        File localFile = FileFactory.createFile(local);
        /* create parent directories, if necessary */
        final File parent = localFile.getParentFile();
        if ((parent != null) && !parent.exists()) {
            parent.mkdirs();
        }
        InputStream inStream = null;
        BufferedOutputStream outStream = null;
        try {
            outStream = new BufferedOutputStream(localFile.getFileOutputStream());
            inStream = Webb.create()
                    .get(remote)
                    .ensureSuccess()
                    .asStream()
                    .getBody();
            WebbUtils.copyStream(inStream, outStream);
        } catch (Exception e) {
            Log.err("Download", remote + " to " + local, e);
            err = true;
        } finally {
            try {
                inStream.close();
                outStream.close();
            } catch (Exception e) {
                // egal
            }
        }
        if (err) {
            try {
                localFile.delete();
            } catch (Exception e) {
                // wie egal
            }
            return false;
        } else {
            return localFile.exists();
        }
    }
}
