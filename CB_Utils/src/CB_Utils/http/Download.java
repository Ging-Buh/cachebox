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

        boolean redirected = false;
        do {
            Response<InputStream> response = null;
            try {
                response = Webb.create()
                        .get(remote)
                        .ensureSuccess()
                        .asStream();
                inStream = response.getBody();
                outStream = new BufferedOutputStream(localFile.getFileOutputStream());
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
                    inStream.close();
                    outStream.close();
                } catch (Exception e) {
                    // egal
                }
            }
        }
        while (redirected);

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
