package com.elishaazaria.sayboard.downloader;

import android.os.Parcel;
import android.os.Parcelable;

public class DownloadRequest implements Parcelable {

    private String tag;

    private boolean requiresUnzip;

    private String serverFilePath;

    private String localFilePath;

    private String unzipAtFilePath;

    private boolean deleteZipAfterExtract = true;

    public DownloadRequest(String serverFilePath, String localPath, boolean requiresUnzip) {

        this.serverFilePath = serverFilePath;

        this.localFilePath = localPath;

        this.requiresUnzip = requiresUnzip;
    }

    protected DownloadRequest(Parcel in) {

        requiresUnzip = in.readByte() != 0x00;
        serverFilePath = in.readString();
        localFilePath = in.readString();
        unzipAtFilePath = in.readString();
        deleteZipAfterExtract = in.readByte() != 0x00;
    }

    @Override
    public int describeContents() {

        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeByte((byte) (requiresUnzip ? 0x01 : 0x00));
        dest.writeString(serverFilePath);
        dest.writeString(localFilePath);
        dest.writeString(unzipAtFilePath);
        dest.writeByte((byte) (deleteZipAfterExtract ? 0x01 : 0x00));
    }

    @SuppressWarnings("unused")
    public static final Creator<DownloadRequest> CREATOR = new Creator<DownloadRequest>() {

        @Override
        public DownloadRequest createFromParcel(Parcel in) {

            return new DownloadRequest(in);
        }

        @Override
        public DownloadRequest[] newArray(int size) {

            return new DownloadRequest[size];
        }
    };

    public boolean isRequiresUnzip() {

        return requiresUnzip;
    }

    public void setRequiresUnzip(boolean requiresUnzip) {

        this.requiresUnzip = requiresUnzip;
    }

    public String getServerFilePath() {

        return serverFilePath;
    }

    public void setServerFilePath(String serverFilePath) {

        this.serverFilePath = serverFilePath;
    }

    public String getLocalFilePath() {

        return localFilePath;
    }

    public void setLocalFilePath(String localFilePath) {

        this.localFilePath = localFilePath;
    }

    public static Creator<DownloadRequest> getCreator() {

        return CREATOR;
    }

    public String getUnzipAtFilePath() {
        return unzipAtFilePath;
    }

    public void setUnzipAtFilePath(String unzipAtFilePath) {
        this.unzipAtFilePath = unzipAtFilePath;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public boolean isDeleteZipAfterExtract() {
        return deleteZipAfterExtract;
    }

    public void setDeleteZipAfterExtract(boolean deleteZipAfterExtract) {
        this.deleteZipAfterExtract = deleteZipAfterExtract;
    }
}
