package de.droidcachebox.utils.http;

import de.droidcachebox.utils.AbstractFile;
import de.droidcachebox.utils.FileFactory;
import de.droidcachebox.utils.log.Log;

import java.io.BufferedOutputStream;
import java.io.InputStream;

public class Download {

    public static boolean download(String remote, String local) {
        boolean err = false;
        AbstractFile localAbstractFile = FileFactory.createFile(local);
        /* create parent directories, if necessary */
        final AbstractFile parent = localAbstractFile.getParentFile();
        if ((parent != null) && !parent.exists()) {
            parent.mkdirs();
        }
        InputStream inStream = null;
        BufferedOutputStream outStream = null;

        boolean redirected;
        int redirCount = 0;
        do {
            redirected = false;
            redirCount++;
            Response<InputStream> response;
            try {
                response = Webb.create()
                        .get(remote)
                        .ensureSuccess()
                        .asStream();
                inStream = response.getBody();
                outStream = new BufferedOutputStream(localAbstractFile.getFileOutputStream());
                WebbUtils.copyStream(inStream, outStream);
            } catch (Exception ex) {
                if (ex instanceof WebbException) {
                    WebbException we = (WebbException) ex;
                    Response re = we.getResponse();
                    if (re != null) {
                        int APIError = re.getStatusCode();
                        if (APIError >= 300 && APIError < 400) {
                            if (remote.startsWith("http:")) {
                                redirected = true;
                                remote = "https:" + remote.substring(5);
                            } else {
                                // other cases should have been handled automatically
                                Log.err("Download", remote + " to " + local, ex);
                                err = true;
                            }
                        }
                    }
                } else {
                    Log.err("Download", remote + " to " + local, ex);
                    err = true;
                }
            } finally {
                try {
                    if (inStream != null)
                        inStream.close();
                    if (outStream != null)
                        outStream.close();
                } catch (Exception ignored) {
                }
            }
        }
        while (redirected && redirCount < 2);

        if (err) {
            try {
                localAbstractFile.delete();
            } catch (Exception e) {
                // wie egal
            }
            return false;
        } else {
            return localAbstractFile.exists();
        }
    }
}
