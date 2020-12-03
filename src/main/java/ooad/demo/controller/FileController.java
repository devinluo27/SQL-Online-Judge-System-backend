package ooad.demo.controller;



import com.alibaba.fastjson.JSON;
import ooad.demo.Service.UserFileService;
import ooad.demo.config.JsonResult;
import ooad.demo.config.ResultCode;
import ooad.demo.config.ResultTool;
import ooad.demo.pojo.UserFile;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.ResourceUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import javax.validation.Valid;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.constraints.Max;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.security.Principal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/files")
public class FileController  {

    @Autowired
    private UserFileService userFileService;

    //    展示所有文件信息
    @GetMapping("/findByUserId")
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
    * 删除文件信息
    * */
    @GetMapping("/deleteFile")
    public void delete(@RequestParam("file_id") Integer id, HttpServletResponse response) throws IOException {
        response.setContentType("text/json;charset=utf-8");
        //根据id查询信息
        UserFile userFile = userFileService.findById(id);
        if (userFile == null){
            JsonResult result = ResultTool.fail();
            response.getWriter().write(JSON.toJSONString(result));
            return;
        }
        //删除文件
        String realPath = ResourceUtils.getURL("classpath:").getPath() + userFile.getRelative_path();
        File file = new File(realPath, userFile.getNew_file_name());

        // 删除该文件
        if(file.exists())
            file.delete();//立即删除

        //删除数据库中的记录
        userFileService.delete(id);
        JsonResult result = ResultTool.success();
        response.getWriter().write(JSON.toJSONString(result));
    }

    /***
     * 下载文件
     * @param openStyle
     * @param id 文件在数据库中对应的ID
     * @param response
     * @throws IOException
     */
    @GetMapping("/download")
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
        IOUtils.copy(is,os);
        IOUtils.closeQuietly(is);
        IOUtils.closeQuietly(os);

    }

    /*
    * 上传文件处理 并保存文件信息到数据库中
    * */
    @PostMapping("/upload")
    public void upload(@RequestParam("file") @NotNull MultipartFile  aaa,
                       @RequestParam("assignment_id") @Validated @NotBlank(message = "作业号不允许为空")  @Max(100) String assignment_id,
//                        BindingResult bindingResult,
                       @RequestParam("question_id") String question_id,
                       @RequestParam("sort_num") Integer sort_num,
                       HttpServletRequest request, HttpServletResponse response) throws IOException {
//        if (bindingResult.hasErrors()){
//            return;
//        }

        response.setContentType("text/json;charset=utf-8");
        //获取用户的id
        Principal userPrincipal = request.getUserPrincipal();

        if (userPrincipal == null || assignment_id == null || question_id == null || aaa == null){
            //根据用户id查询有的文件信息
            JsonResult result = ResultTool.fail(ResultCode.USER_NOT_LOGIN);
            response.getWriter().write(JSON.toJSONString(result));
            return;
        }


        //获取文件原始名称
        String originalFilename = aaa.getOriginalFilename();
        //获取文件后缀
        String extension = "."+FilenameUtils.getExtension(aaa.getOriginalFilename());
        //生成新的文件名称
        String newFileName = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + UUID.randomUUID().toString().replace("-", "") + extension;

        //文件大小
        long size = aaa.getSize();
        //文件类型
        String type = aaa.getContentType();
        System.out.println(type);
        //处理根据日期生成目录 classpath:/static/files
        //处理根据题目生成目录 classpath:/files_for_students

//        String realPath = ResourceUtils.getURL("classpath:").getPath() + "/static/files"
//                +"/A"+ assignment_id + "/Q" + question_id + "/";
        // TODO
        String relative_path =  "static/files_for_students" + "/A" + assignment_id + "/Q" + question_id + "/";
        String realPath = ResourceUtils.getURL("classpath:").getPath() + relative_path;

//        String dateFormat = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
//        String dateDirPath = realPath + "/" + dateFormat + "/";
        File post_file = new File(realPath);

        if (!post_file.exists()) post_file.mkdirs();

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
                .setAssignment_id(Integer.parseInt(assignment_id))
                .setQuestion_id(Integer.parseInt(question_id))
                .setSort_num(sort_num);

        userFile.setRelative_path(relative_path);
        System.out.println(userFile);

        // 保存文件相关信息到数据库
        userFileService.save(userFile);
        String retrieve_url = realPath + newFileName;
        JsonResult<String> result = ResultTool.success();
        result.setData(retrieve_url);
        response.getWriter().write(JSON.toJSONString(result));
    }


    //    展示所有文件信息
    @GetMapping("/showAllFiles")
    @ResponseBody
    public List<UserFile> findAll(HttpServletResponse response) throws IOException {
//        User user = (User) session.getAttribute("user");
        List<UserFile> userFiles = userFileService.getAllFileInfo();
//        model.addAttribute("files",userFiles);
//        response.setContentType("text/json;charset=utf-8");
//        response.getWriter().write(String.valueOf(userFiles));
        return userFiles;
    }
}