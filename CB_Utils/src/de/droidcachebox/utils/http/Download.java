package de.droidcachebox.utils.http;

import java.io.BufferedOutputStream;
import java.io.InputStream;

import de.droidcachebox.utils.AbstractFile;
import de.droidcachebox.utils.FileFactory;
import de.droidcachebox.utils.ProgressChangedEvent;
import de.droidcachebox.utils.log.Log;

public class Download {
    private static final String sClass = "Download";
    private ProgressChangedEvent progressIndicator;
    private boolean canceled;

    public Download(ProgressChangedEvent progressIndicator) {
        this.progressIndicator = progressIndicator;
        canceled = false;
    }

    public boolean download(String remote, String local) {
        boolean err = false;
        AbstractFile localFile = FileFactory.createFile(local);
        /* create parent directories, if necessary */
        final AbstractFile directory = localFile.getParentFile();
        if ((directory != null) && !directory.exists()) {
            directory.mkdirs();
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
                outStream = new BufferedOutputStream(localFile.getFileOutputStream());

                byte[] buffer = new byte[1024];
                int count;
                int kiloByteCount = 0;
                while ((count = inStream.read(buffer)) != -1) {
                    outStream.write(buffer, 0, count);
                    kiloByteCount = kiloByteCount + 1;
                    if (progressIndicator != null)
                        progressIndicator.progressChanged("", "", kiloByteCount);
                    if (canceled)
                        throw new Exception("canceled");
                }
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
                    if (canceled)
                        Log.info(sClass, "canceled");
                    else
                        Log.err(sClass, remote + " to " + local, ex);
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
                localFile.delete();
            } catch (Exception ignored) {
            }
            return false;
        } else {
            return localFile.exists();
        }
    }

    public void cancelDownload() {
        canceled = true;
    }
}
