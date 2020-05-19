package com.example.demo.util;

import javax.servlet.http.HttpServletRequest;
import java.io.File;

/**
 * Created by pyh on 2019/2/25.
 */
public class FileUploadUtils {
    public static boolean isImageType(String imageContentType) {
        if ("image/gif".equals(imageContentType)) {
            return true;
        }
        if ("image/jpeg".equals(imageContentType)) {
            return true;
        }
        if ("image/png".equals(imageContentType)) {
            return true;
        }
        return false;
    }

    /**
     * 把相对项目的文件路径改为文件系统的具体位置
     *
     * @param request
     * @param origiPath
     * @return
     */
    public static String convertToSysPath(HttpServletRequest request, String origiPath) {
        String realPath = request.getSession().getServletContext().getRealPath(File.separator);
        String contextPath = request.getSession().getServletContext().getContextPath();
        origiPath = origiPath.replace(contextPath,"");
//        String sysPath = realPath.replace(File.separator+contextPath.substring(1),"");
        return realPath+origiPath;
    }
}
