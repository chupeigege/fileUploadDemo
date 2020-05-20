package com.example.demo.controller;

import com.example.demo.util.FastDFSClientUtil;
import com.example.demo.util.FileUploadUtils;
import com.example.demo.util.R;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

/**
 * 图片上传controller
 *
 * @author wcp
 * @date 2019/9/6
 */
@Controller
public class UploadController {
    //SpringBoot或者是多模板才需要是这种
    private static final String UPLOAD_PATH = File.separator + "WEB-INF" + File.separator + "classes" + File.separator + "static" + File.separator + "upload";
    private static final String RELATIVE_PATH = File.separator + "static" + File.separator + "upload";
    @Autowired
    private FastDFSClientUtil dfsClient;

    /**
     * 全栈方式实现
     * @param request
     * @param response
     * @throws IOException
     * @throws FileUploadException
     */
    @RequestMapping(value = "upload")
    public void upload(HttpServletRequest request, HttpServletResponse response) throws IOException, FileUploadException {
        //图片相对地址
        try {
            String relativePath = File.separator + "static" + File.separator + "upload" + File.separator + "img";
			//如果是多模块，或者是springboot项目用这个路径上传
			String uploadPath = File.separator+"WEB-INF"+File.separator+
                    "classes"+File.separator + "static" + File.separator +
                    "upload";
            String contextPath = request.getSession().getServletContext().getContextPath();
            String realPath = request.getSession().getServletContext().getRealPath(relativePath);

            File file = new File(realPath);
            if (!file.exists()) {
                file.mkdirs();
            }
            MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
            //获取上传上来的文件
            Map<String, MultipartFile> fileMap = multipartRequest.getFileMap();
            String path = "";

            MultipartFile multipartFile = fileMap.get("multipartFile");
            // 此处不用配置文件限制上传文件的类型和大小，因为配置文件是全局性的
            if (!FileUploadUtils.isImageType(multipartFile.getContentType())) {
                response.getWriter().print("typeIllegal");
                return;
            }

            String fileName = multipartFile.getOriginalFilename();
            String uuid = UUID.randomUUID().toString().replace("-", "");
            path = realPath + File.separator + uuid + "_" + fileName;
            multipartFile.transferTo(new File(path));
			String imgPath = contextPath + relativePath + File.separator + uuid + "_" + fileName;
//            String imgPath = relativePath + File.separator + uuid + "_" + fileName;
            response.getWriter().print(imgPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 接口方式
     * @param uploadFile
     * @param request
     * @return
     */
    @PostMapping(value = "/uploadFile", headers = "content-type=multipart/form-data")
    public R uploadFile(MultipartFile uploadFile, HttpServletRequest request) {
        try {
            //预备文件大小。
            uploadFile.getSize();

            //上传文件格式限制
            String originalFilename = uploadFile.getOriginalFilename();
            String suffix = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
            if (!validateFileType(suffix)) {
                return R.error("上传失败，不支持的文件类型");
            }

            String realPath = request.getSession().getServletContext().getRealPath(UPLOAD_PATH);
            File file = new File(realPath);
            if (!file.exists()) {
                file.mkdirs();
            }

            //生成随机文件名
            String fileName = File.separator + UUID.randomUUID().toString().replace("-", "") + "_" + uploadFile.getOriginalFilename();
            uploadFile.transferTo(new File(file, fileName));
            //生成的文件相对路径
            String filePath = RELATIVE_PATH + fileName;
            filePath = filePath.replaceAll("\\\\", "/");
//            String filePath = dfsClient.uploadFile(uploadFile); 文件服务器实现方式
            return R.ok().put("filePath", filePath);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return R.error("上传失败");
    }

    /**
     * 集群方式
     * @param uploadFile
     * @param request
     * @return
     */
    @PostMapping(value = "/dfsUploadFile", headers = "content-type=multipart/form-data")
    public R dfsUploadFile(MultipartFile uploadFile,HttpServletRequest request) {
        try {
            //预备文件大小。
            uploadFile.getSize();

            //上传文件格式限制
            String originalFilename = uploadFile.getOriginalFilename();
            String suffix = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
            if (!validateFileType(suffix)) {
                return R.error("上传失败，不支持的文件类型");
            }

            String filePath = dfsClient.uploadFile(uploadFile);
            return R.ok().put("filePath", filePath);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return R.error("上传失败");
    }

    /**
     * http://localhost/deleteFile?filePath=group1/M00/00/00/wKgIZVzZaRiAZemtAARpYjHP9j4930.jpg
     * @param filePath  group1/M00/00/00/wKgIZVzZaRiAZemtAARpYjHP9j4930.jpg
     * @param request
     * @param response
     * @return
     */
    @RequestMapping("/deleteFile")
    public R delFile(String filePath , HttpServletRequest request ,HttpServletResponse response)  {

        try {
            dfsClient.delFile(filePath);
        } catch(Exception e) {
            // 文件不存在报异常 ： com.github.tobato.fastdfs.exception.FdfsServerException: 错误码：2，错误信息：找不到节点或文件
            // e.printStackTrace();
        }

        return R.ok();
    }

    private boolean validateFileType(String suffix) {
        String typeStr = "jpg|jpeg|png|gif|bmp";
        String[] types = typeStr.split("\\|");
        for (int i = 0; i < types.length; ++i) {
            if (types[i].equals(suffix)) {
                return true;
            }
        }
        return false;
    }



}
