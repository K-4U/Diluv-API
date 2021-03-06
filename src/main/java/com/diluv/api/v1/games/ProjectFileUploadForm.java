package com.diluv.api.v1.games;

import java.io.InputStream;

import javax.ws.rs.FormParam;

public class ProjectFileUploadForm {

    @FormParam("project_id")
    public Long projectId;

    @FormParam("version")
    public String version;

    @FormParam("changelog")
    public String changelog;

    @FormParam("file")
    public InputStream file;

    @FormParam("filename")
    public String fileName;

    @FormParam("releaseType")
    public String releaseType;

    @FormParam("classifier")
    public String classifier;

    @FormParam("game_versions")
    public String gameVersions;

    @FormParam("dependencies")
    public String dependencies;
}