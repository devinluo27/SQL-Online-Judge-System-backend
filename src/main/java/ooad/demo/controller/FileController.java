package ooad.demo.controller;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import ooad.demo.Service.UserFileService;
import ooad.demo.utils.AccessLimit;
import ooad.demo.utils.JsonResult;
import ooad.demo.utils.ResultCode;
import ooad.demo.utils.ResultTool;
import ooad.demo.judge.Remote;
import ooad.demo.mapper.DataBaseMapper;
import ooad.demo.pojo.Database;
import ooad.demo.pojo.UserFile;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ResourceUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.Max;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.security.Principal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Controller
@Slf4j
@Transactional
public class FileController  {

    @Autowired
    private UserFileService userFileService;

    @Autowired
    private DataBaseMapper dataBaseMapper;

    @Autowired
    private Remote remote;

    @Value("${judge.remote.file-path}")
//    private  String remoteFilePath = "/data2/DBOJ/JudgeFile/";
    private  String remoteFilePath;

    @Value("${judge.remote.database-path}")
    private String remoteDatabasePath;
//    private String remoteDatabasePath = "/data2/DBOJ/DockerTest/";

    private String localRemoteFileRelativePATH =  "/static/remote_files/";

    private String localRemoteDatabaseFileRelativePATH =  "/static/remote_files/database/";

    private static class FileInfo{
        int file_id;
        String newFileName;
        public FileInfo(int file_id, String newFileName) {
            this.file_id = file_id;
            this.newFileName = newFileName;
        }
    }

    // 展示所有文件信息 12
    @GetMapping("/admin/files/findByUserId")
    @ResponseBody
    public List<UserFile> findByUserId(HttpServletRequest request){
        //在登录的session中获取用户的id
        Principal userPrincipal = request.getUserPrincipal();
        if (userPrincipal != null){
            //根据用户id查询有的文件信息
            List<UserFile> userFiles = userFileService.findByUserId(Integer.parseInt(userPrincipal.getName()));
            return userFiles;
        }
        return null;
    }

    /*
    * 删除文件信息 10
    * */
    @GetMapping("/admin/files/deleteFile")
    public void delete(@RequestParam("file_id") Integer id, HttpServletResponse response){
        //根据id查询信息
        UserFile userFile = userFileService.findById(id);
        if (userFile == null){
            ResultTool.writeResponseFailWithData(response, ResultCode.COMMON_FAIL, "此文件不存在");
            return;
        }
        try {
            //删除文件
            String realPath = ResourceUtils.getURL("classpath:").getPath() + userFile.getRelative_path();
            File file = new File(realPath, userFile.getNew_file_name());
            // 删除该文件
            if(file.exists())
                file.delete();//立即删除
            //删除数据库中的记录
            userFileService.delete(id);
        } catch (Exception e){
            e.printStackTrace();
            ResultTool.writeResponseFail(response);
            return;
        }
        ResultTool.writeResponseSuccess(response);
    }

    /***
     * 下载文件 11
     * @param openStyle
     * @param id 文件在数据库中对应的ID
     * @param response
     * @throws IOException
     */
    @GetMapping("/admin/files/download")
    public void download(String openStyle, @RequestParam("file_id") Integer id, HttpServletResponse response) throws IOException {
        //获取打开方式
        openStyle = openStyle==null ? "attachment" : openStyle;
        //获取文件信息
        UserFile userFile = userFileService.findById(id);
        // 判断文件是否存在
        if (userFile == null){
            JsonResult result = ResultTool.fail();
            response.getWriter().write(JSON.toJSONString(result));
            return;
        }

        if("attachment".equals(openStyle)){
            //更新下载次数
            userFile.setDown_counts(userFile.getDown_counts()+1);
            userFileService.update(userFile);
        }
        //根据文件信息中文件名字和文件存储路径获取文件输入流
        String realPath = ResourceUtils.getURL("classpath:").getPath() + userFile.getRelative_path();

        // Test path variables
//        System.out.println(realPath);
//        System.out.println(ResourceUtils.getURL("classpath:").getPath());

        //获取文件输入流
        FileInputStream is = new FileInputStream(new File(realPath, userFile.getNew_file_name()));
        //附件下载
        response.setHeader("content-disposition",openStyle+";fileName="+ URLEncoder.encode(userFile.getOld_file_name(),"UTF-8"));
        //获取响应输出流
        ServletOutputStream os = response.getOutputStream();
        //文件拷贝
        IOUtils.copy(is, os);
        IOUtils.closeQuietly(is);
        IOUtils.closeQuietly(os);

    }


    /*
     * 上传文件处理 并保存文件信息到数据库中 14
     * 新建一个
     * */
    // TODO:  异常处理!!!!!
    @AccessLimit(maxCount = 10, seconds = 60)
    @PostMapping(value = "/admin/files/upload", consumes = "multipart/form-data")
    public void upload(@RequestParam("file") @NotNull MultipartFile  aaa,
                       HttpServletRequest request, HttpServletResponse response) throws IOException {
        //获取用户的id
        Principal userPrincipal = request.getUserPrincipal();
        if (userPrincipal == null || aaa == null || aaa.isEmpty()){
            //根据用户id查询有的文件信息
            ResultTool.writeResponseFailWithData(response, ResultCode.COMMON_FAIL, "文件上传失败！");
            return;
        }
        //获取文件原始名称
        String originalFilename = aaa.getOriginalFilename();
        //获取文件后缀
        String extension = "." + FilenameUtils.getExtension(aaa.getOriginalFilename());
        //生成新的文件名称
        String newFileName = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + UUID.randomUUID().toString().replace("-", "") + extension;

        //文件大小
        long size = aaa.getSize();
        //文件类型
        String type = aaa.getContentType();
        System.out.println(type);
        //处理根据日期生成目录 classpath:/static/files
        //处理根据题目生成目录 classpath:/files_for_students

        // TODO: check file_path

        String dateFormat = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        String relative_path =  "/static/files_for_students/questions_files/" +  dateFormat + "/";
        String realPath = ResourceUtils.getURL("classpath:").getPath() + relative_path;

        File post_file = new File(realPath);

        if (!post_file.exists()){ post_file.mkdirs();}

        System.out.println(post_file);
        //处理文件上传
        aaa.transferTo(new File(post_file, newFileName));

        //将文件信息放入数据库中
        // TODO: 异常处理
        UserFile userFile = new UserFile();
        userFile.setOld_file_name(originalFilename)
                .setNew_file_name(newFileName)
                .setExt(extension)
                .setFile_size(String.valueOf(size))
                .setFile_type(type)
                .setUser_id(Integer.parseInt(userPrincipal.getName()))
                .setAssignment_id(-1)
                .setQuestion_id(-1);

        userFile.setRelative_path(relative_path);
        System.out.println(userFile);
        // 保存文件相关信息到数据库
        userFileService.save(userFile);
        String retrieve_url = realPath + newFileName;
        ResultTool.writeResponseSuccessWithData(response, retrieve_url);
    }



    //    展示所有文件信息 45
    @GetMapping("/admin/files/showAllFiles")
    @ResponseBody
    public List<UserFile> findAll(HttpServletResponse response) throws IOException {
        List<UserFile> userFiles = userFileService.getAllFileInfoList();
        return userFiles;
    }

    // 46
    @AccessLimit(seconds = 100, maxCount = 10)
    @PostMapping("/admin/files/uploadToRemoteDatabase")
    public void uploadToRemoteDatabase(@RequestParam(value = "file") MultipartFile  file,
                                       HttpServletRequest request, HttpServletResponse response) throws Exception {
        //获取用户的id
        Principal userPrincipal = request.getUserPrincipal();
        String extension = FilenameUtils.getExtension(file.getOriginalFilename());
        if (userPrincipal == null || file.isEmpty() || extension == null || !extension.toLowerCase().equals("sql")){
            ResultTool.writeResponseFail(response, ResultCode.FILE_TYPE_ERROR);
            return;
        }
        FileInfo fileInfo = uploadToRemoteProcessing(file, Integer.parseInt(userPrincipal.getName()), true);
        if (fileInfo.file_id == -1){
            ResultTool.writeResponseFailWithData(response, ResultCode.COMMON_FAIL,"upload to remote failed!");
            return;
        }
        ResultTool.writeResponseSuccessWithData(response,fileInfo.file_id);
    }

    // TODO: NEW URL 100
    @AccessLimit(seconds = 100, maxCount = 10)
    @PostMapping("/admin/files/createDatabase")
    public void createDatabase(@RequestParam(value = "file_id") Integer file_id,
                                       @RequestParam(value = "database_name") String database_name,
                                       @RequestParam(value = "database_description") String database_description,
                                       HttpServletRequest request, HttpServletResponse response) throws Exception {
         try {
             UserFile userFile = userFileService.findById(file_id);
             Database database = new Database(file_id, database_name, database_description, remoteDatabasePath, userFile.getNew_file_name());
             dataBaseMapper.addDatabase(database);
             ResultTool.writeResponseSuccess(response);
         }catch (Exception e){
             e.printStackTrace();
             ResultTool.writeResponseFail(response);
         }
    }



    // 47
    @AccessLimit(seconds = 100, maxCount = 10)
    @PostMapping("/admin/files/uploadToRemote")
    public void uploadToRemote(@RequestParam(value = "file") MultipartFile  file,
                             HttpServletRequest request,
                             HttpServletResponse response) throws Exception {
        //获取用户的id
        Principal userPrincipal = request.getUserPrincipal();
        if (!checkLoginAndFile(userPrincipal, file)){
            ResultTool.writeResponseFailWithData(response, ResultCode.COMMON_FAIL, "Parameters Error!");
            return;
        }
        FileInfo fileInfo = uploadToRemoteProcessing(file, Integer.parseInt(userPrincipal.getName()), false);
        if (fileInfo.file_id == -1){
            ResultTool.writeResponseFailWithData(response, ResultCode.COMMON_FAIL, "upload to remote failed!");
            return;
        }
        ResultTool.writeResponseSuccessWithData(response, (String.valueOf(fileInfo.file_id)));
    }


    private boolean checkLoginAndFile(Principal userPrincipal,  MultipartFile  file){
        return userPrincipal != null && !file.isEmpty();
    }

    /***
     * This function first store the file in backend and then copy it to the judge machine!
     * @param file
     * @param user_id
     * @param is_database
     * @return
     * @throws Exception
     */
    private FileInfo uploadToRemoteProcessing(MultipartFile file, int user_id, boolean is_database) throws Exception {
        //获取文件原始名称
        String originalFilename = file.getOriginalFilename();
        //获取文件后缀
        String extension = "." + FilenameUtils.getExtension(file.getOriginalFilename());
        //生成新的文件名称
        String newFileName = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + UUID.randomUUID().toString().replace("-", "") + extension;
        //文件大小
        long size = file.getSize();
        //文件类型
        String type = file.getContentType();
        String relative_path;
        String remoteFullPath;
        if (is_database){
            relative_path =  localRemoteDatabaseFileRelativePATH;
            remoteFullPath = remoteDatabasePath + newFileName;
        }
        else {
            relative_path =  localRemoteFileRelativePATH;
            remoteFullPath = remoteFilePath + newFileName;
        }
        String realPath = ResourceUtils.getURL("classpath:").getPath() + relative_path;
        File post_file = new File(realPath);
        if (!post_file.exists()) post_file.mkdirs();
        log.info("upload path: "+realPath);
        //处理文件上传 将文件保存到本地来
        file.transferTo(new File(post_file, newFileName));

        //将文件信息放入数据库中
        // TODO: 异常处理
        UserFile userFile = new UserFile();
        userFile.setOld_file_name(originalFilename)
                .setNew_file_name(newFileName)
                .setExt(extension)
                .setFile_size(String.valueOf(size))
                .setFile_type(type)
                .setUser_id(user_id)
                .setAssignment_id(-1)
                .setQuestion_id(-1)
                .setIs_database(is_database)
                .setIs_in_remote(false);

        userFile.setRelative_path(relative_path);
        userFile.setRemote_full_path(remoteFullPath);

        // 保存文件相关信息到数据库
        userFileService.save(userFile);
        int file_id = userFile.getId();
        FileInfo fileInfo = new FileInfo(file_id, newFileName);

        // ===== 复制文件到远程服务器 =====
        boolean is_success = remote.uploadFile(file_id, remoteFullPath);
        if (!is_success){
            fileInfo.file_id = -1;
            return fileInfo;
        }
        // TODO: UPDATE
        userFileService.setFileIsRemoteStatusAndPath(file_id, true, remoteFullPath);
        return fileInfo;
    }


    // TODO: New API 103
    @GetMapping("/admin/deleteRemoteFileById")
    public void deleteRemoteFileById(
            @RequestParam(value = "file_id") Integer file_id,
            HttpServletResponse response
    ){
        UserFile userFile =  userFileService.findById(file_id);
        if(userFile.getIs_in_remote()){
            try {
                remote.deleteFileSftp(userFile.getRemote_full_path());
                userFileService.setFileIsRemoteStatusAndPath(file_id, false, null);
                ResultTool.writeResponseSuccess(response);
            } catch (Exception e) {
                ResultTool.writeResponseFail(response, ResultCode.COMMON_FAIL);
                log.error("File Delete Fails", e);
            }
        }
    }

    // TODO: New API 101
    @GetMapping("/admin/queryRemoteFileList")
    public  void queryRemoteFileList(HttpServletResponse response){
        try{
            List<UserFile> list = userFileService.getRemoteFileInfoList();
            ResultTool.writeResponseSuccessWithData(response, list);
        } catch (Exception e){
            ResultTool.writeResponseFail(response);
        }
    }


    // TODO: New API 50
    @GetMapping("/admin/copyToRemote")
    @ResponseBody
    public String copyToRemote(Integer file_id) throws Exception {
        return "1";
    }


}
