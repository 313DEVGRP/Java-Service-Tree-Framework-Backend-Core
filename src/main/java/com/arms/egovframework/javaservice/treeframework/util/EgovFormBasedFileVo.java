package com.arms.egovframework.javaservice.treeframework.util;


import java.io.Serializable;

public class EgovFormBasedFileVo implements Serializable {
    private String fileName = "";
    private String contentType = "";
    private String serverSubPath = "";
    private String physicalName = "";
    private long size = 0L;
    private String name = "";
    private String url = "";
    private String thumbnailUrl = "";
    private String delete_url = "";
    private String delete_type = "POST";

    public EgovFormBasedFileVo() {
    }

    public String getFileName() {
        return this.fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getContentType() {
        return this.contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getServerSubPath() {
        return this.serverSubPath;
    }

    public void setServerSubPath(String serverSubPath) {
        this.serverSubPath = serverSubPath;
    }

    public String getPhysicalName() {
        return this.physicalName;
    }

    public void setPhysicalName(String physicalName) {
        this.physicalName = physicalName;
    }

    public long getSize() {
        return this.size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getName() {
        return this.name;
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getThumbnailUrl() {
        return this.thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public String getDelete_url() {
        return this.delete_url;
    }

    public void setDelete_url(String delete_url) {
        this.delete_url = delete_url;
    }

    public String getDelete_type() {
        return this.delete_type;
    }

    public void setDelete_type(String delete_type) {
        this.delete_type = delete_type;
    }
}
