package ooad.demo.controller;

import com.jcraft.jsch.JSchException;
import lombok.extern.slf4j.Slf4j;
import ooad.demo.Service.DockerPoolService;
import ooad.demo.mapper.DataBaseMapper;
import ooad.demo.pojo.Database;
import ooad.demo.utils.ResultCode;
import ooad.demo.utils.ResultTool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;

@RestController
@Slf4j
public class DatabaseController {
    @Autowired
    DockerPoolService dockerPoolService;

    @Autowired
    DataBaseMapper dataBaseMapper;

    @GetMapping("/admin/initDatabaseDocker")
    public void initDatabaseDocker(
            @RequestParam(value = "database_id") Integer database_id,
            @RequestParam(value = "operation_type") String operation_type,
            HttpServletResponse response){
        try {
            dockerPoolService.InitDockerPool(database_id, operation_type);
            ResultTool.writeResponseSuccess(response);
        } catch (IOException | JSchException e) {
            log.error("InitDatabaseDocker Fails! ", e);
            ResultTool.writeResponseFail(response, ResultCode.COMMON_FAIL);
        }
    }

    @GetMapping("/admin/queryDatabaseList")
    public ArrayList<Database> queryDatabaseList(){
        return dataBaseMapper.queryDatabaseList();
        // database_id name database_description  time
    }

    @GetMapping("/admin/deleteDatabaseById")
    public void deleteDatabaseById(
            @RequestParam(value = "database_id") Integer database_id
    ){
        int ret_del = dataBaseMapper.deleteDatabase(database_id);

    }


}
