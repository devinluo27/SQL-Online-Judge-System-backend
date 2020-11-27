//package ooad.demo.controller;
//
//import lombok.extern.slf4j.Slf4j;
//import org.mybatis.logging.Logger;
//import org.mybatis.logging.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.core.parameters.P;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.multipart.MultipartFile;
//import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
//import org.springframework.web.servlet.mvc.support.RedirectAttributes;
//
//import java.io.File;
//
//@Controller
//public class FileUploadController {
//
//    private static final Logger logger = LoggerFactory.getLogger(FileUploadController.class);
//
//
//    @PostMapping("/upload")
//    public String upload(@RequestParam("file") MultipartFile file){
//        if(file.isEmpty()) {
//            return "Fail";
//        }
//        String fileName = file.getOriginalFilename();
//        String filepath = "";
//        File dest = new java.io.File(filepath + fileName);
//        return "";
//    }
//}