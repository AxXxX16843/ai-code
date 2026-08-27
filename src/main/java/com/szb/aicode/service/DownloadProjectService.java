package com.szb.aicode.service;

import jakarta.servlet.http.HttpServletResponse;

public interface DownloadProjectService {

    void downloadProjectAsZip(String projectPath, String downloadFileName, HttpServletResponse response);

}
