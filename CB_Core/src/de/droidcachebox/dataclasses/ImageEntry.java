package de.droidcachebox.dataclasses;

import java.io.Serializable;
import java.net.URI;

import de.droidcachebox.database.CoreCursor;
import de.droidcachebox.ex_import.DescriptionImageGrabber;

public class ImageEntry implements Serializable {
    private static final long serialVersionUID = 4216092006574290607L;
    private String description = "";
    private String name = "";
    private String imageUrl = "";
    private String localPath = "";
    private long cacheId = -1;
    private String gcCode = "";
    private boolean isCacheImage = false;

    public ImageEntry() {
    }

    public ImageEntry(CoreCursor reader) {
        cacheId = reader.getLong(0);
        gcCode = reader.getString(1).trim();
        name = reader.getString(2);
        description = reader.getString(3);
        imageUrl = reader.getString(4);
        isCacheImage = reader.getInt(5) == 1;

        localPath = DescriptionImageGrabber.buildDescriptionImageFilename(gcCode, URI.create(imageUrl));
    }

    public String getFilename() {
        return localPath.substring(localPath.lastIndexOf('/') + 1);
    }

    public void clear() {
        description = "";
        name = "";
        imageUrl = "";
        cacheId = -1;
        gcCode = "";
        isCacheImage = false;
        localPath = "";
    }

    public void dispose() {
        description = null;
        name = null;
        imageUrl = null;
        gcCode = null;
        localPath = null;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getLocalPath() {
        return localPath;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }

    public long getCacheId() {
        return cacheId;
    }

    public void setCacheId(long cacheId) {
        this.cacheId = cacheId;
    }

    public String getGcCode() {
        return gcCode;
    }

    public void setGcCode(String gcCode) {
        this.gcCode = gcCode;
    }

    /**
     * image is from geocache description
     */
    public boolean isCacheImage() {
        return isCacheImage;
    }

    public void setCacheImage(boolean cacheImage) {
        isCacheImage = cacheImage;
    }
}
