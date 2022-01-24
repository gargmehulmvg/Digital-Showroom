package com.digitaldukaan.models.dto;

public class ImageFolder {
    private String mPath = "";
    private String mFolderName = "";
    private int mNumberOfPics = 0;
    private String mFirstPic = "";

    public ImageFolder() {

    }

    public ImageFolder(String path, String folderName) {
        this.mPath = path;
        this.mFolderName = folderName;
    }

    public String getPath() {
        return mPath;
    }

    public void setPath(String path) {
        this.mPath = path;
    }

    public String getFolderName() {
        return mFolderName;
    }

    public void setFolderName(String folderName) {
        this.mFolderName = folderName;
    }

    public int getNumberOfPics() {
        return mNumberOfPics;
    }

    public void setNumberOfPics(int numberOfPics) {
        this.mNumberOfPics = numberOfPics;
    }

    public void addpics() {
        this.mNumberOfPics++;
    }

    public String getFirstPic() {
        return mFirstPic;
    }

    public void setFirstPic(String firstPic) {
        this.mFirstPic = firstPic;
    }
}
