package ooad.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

public class Jdbc_controller {
    @Autowired
    JdbcTemplate jdbc_template;

//    public List<Map<String, Object>> list_maps = jdbc_template.queryForList(sql);

}
