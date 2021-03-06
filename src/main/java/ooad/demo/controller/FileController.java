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

    // ???????????????????????? 12
    @GetMapping("/admin/files/findByUserId")
    @ResponseBody
    public List<UserFile> findByUserId(HttpServletRequest request){
        //????????????session??????????????????id
        Principal userPrincipal = request.getUserPrincipal();
        if (userPrincipal != null){
            //????????????id????????????????????????
            List<UserFile> userFiles = userFileService.findByUserId(Integer.parseInt(userPrincipal.getName()));
            return userFiles;
        }
        return null;
    }

    /*
    * ?????????????????? 10
    * */
    @GetMapping("/admin/files/deleteFile")
    public void delete(@RequestParam("file_id") Integer id, HttpServletResponse response){
        //??????id????????????
        UserFile userFile = userFileService.findById(id);
        if (userFile == null){
            ResultTool.writeResponseFailWithData(response, ResultCode.COMMON_FAIL, "??????????????????");
            return;
        }
        try {
            //????????????
            String realPath = ResourceUtils.getURL("classpath:").getPath() + userFile.getRelative_path();
            File file = new File(realPath, userFile.getNew_file_name());
            // ???????????????
            if(file.exists())
                file.delete();//????????????
            //???????????????????????????
            userFileService.delete(id);
        } catch (Exception e){
            e.printStackTrace();
            ResultTool.writeResponseFail(response);
            return;
        }
        ResultTool.writeResponseSuccess(response);
    }

    /***
     * ???????????? 11
     * @param openStyle
     * @param id ??????????????????????????????ID
     * @param response
     * @throws IOException
     */
    @GetMapping("/admin/files/download")
    public void download(String openStyle, @RequestParam("file_id") Integer id, HttpServletResponse response) throws IOException {
        //??????????????????
        openStyle = openStyle==null ? "attachment" : openStyle;
        //??????????????????
        UserFile userFile = userFileService.findById(id);
        // ????????????????????????
        if (userFile == null){
            JsonResult result = ResultTool.fail();
            response.getWriter().write(JSON.toJSONString(result));
            return;
        }

        if("attachment".equals(openStyle)){
            //??????????????????
            userFile.setDown_counts(userFile.getDown_counts()+1);
            userFileService.update(userFile);
        }
        //???????????????????????????????????????????????????????????????????????????
        String realPath = ResourceUtils.getURL("classpath:").getPath() + userFile.getRelative_path();

        // Test path variables
//        System.out.println(realPath);
//        System.out.println(ResourceUtils.getURL("classpath:").getPath());

        //?????????????????????
        FileInputStream is = new FileInputStream(new File(realPath, userFile.getNew_file_name()));
        //????????????
        response.setHeader("content-disposition",openStyle+";fileName="+ URLEncoder.encode(userFile.getOld_file_name(),"UTF-8"));
        //?????????????????????
        ServletOutputStream os = response.getOutputStream();
        //????????????
        IOUtils.copy(is, os);
        IOUtils.closeQuietly(is);
        IOUtils.closeQuietly(os);

    }


    /*
     * ?????????????????? ???????????????????????????????????? 14
     * ???????????????
     * */
    // TODO:  ????????????!!!!!
    @AccessLimit(maxCount = 10, seconds = 60)
    @PostMapping(value = "/admin/files/upload", consumes = "multipart/form-data")
    public void upload(@RequestParam("file") @NotNull MultipartFile  aaa,
                       HttpServletRequest request, HttpServletResponse response) throws IOException {
        //???????????????id
        Principal userPrincipal = request.getUserPrincipal();
        if (userPrincipal == null || aaa == null || aaa.isEmpty()){
            //????????????id????????????????????????
            ResultTool.writeResponseFailWithData(response, ResultCode.COMMON_FAIL, "?????????????????????");
            return;
        }
        //????????????????????????
        String originalFilename = aaa.getOriginalFilename();
        //??????????????????
        String extension = "." + FilenameUtils.getExtension(aaa.getOriginalFilename());
        //????????????????????????
        String newFileName = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + UUID.randomUUID().toString().replace("-", "") + extension;

        //????????????
        long size = aaa.getSize();
        //????????????
        String type = aaa.getContentType();
        System.out.println(type);
        //?????????????????????????????? classpath:/static/files
        //?????????????????????????????? classpath:/files_for_students

        // TODO: check file_path

        String dateFormat = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        String relative_path =  "/static/files_for_students/questions_files/" +  dateFormat + "/";
        String realPath = ResourceUtils.getURL("classpath:").getPath() + relative_path;

        File post_file = new File(realPath);

        if (!post_file.exists()){ post_file.mkdirs();}

        System.out.println(post_file);
        //??????????????????
        aaa.transferTo(new File(post_file, newFileName));

        //?????????????????????????????????
        // TODO: ????????????
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
        // ????????????????????????????????????
        userFileService.save(userFile);
        String retrieve_url = realPath + newFileName;
        ResultTool.writeResponseSuccessWithData(response, retrieve_url);
    }



    //    ???????????????????????? 45
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
        //???????????????id
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
        System.out.println("uploadToRemoteDatabase: success!");
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
             System.out.println("database: " + database);
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
        //???????????????id
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
        //????????????????????????
        String originalFilename = file.getOriginalFilename();
        //??????????????????
        String extension = "." + FilenameUtils.getExtension(file.getOriginalFilename());
        //????????????????????????
        String newFileName = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + UUID.randomUUID().toString().replace("-", "") + extension;
        //????????????
        long size = file.getSize();
        //????????????
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
        log.info("upload path: " + realPath);
        //?????????????????? ???????????????????????????
        file.transferTo(new File(post_file, newFileName));

        //?????????????????????????????????
        // TODO: ????????????
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

        // ????????????????????????????????????
        userFileService.save(userFile);
        int file_id = userFile.getId();
        FileInfo fileInfo = new FileInfo(file_id, newFileName);

        // ===== ?????????????????????????????? =====
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
