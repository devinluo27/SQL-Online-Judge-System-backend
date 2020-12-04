package ooad.demo.pojo;


import java.sql.Timestamp;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.util.List;

import ooad.demo.pojo.Question;

import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Assignment implements Serializable {

    private Integer id;
    @NotNull
    private Integer assignment_id;
    @NotNull
    private String assignment_name;

    private Timestamp assignment_create_time;
    @NotNull
    private Timestamp assignment_start_time;
    @NotNull
    private Timestamp assignment_end_time;

    private String assignment_description;

    private List<Question> questions;

    @NotNull
    private Integer is_visible;

    public Assignment(int assignment_id, String assignment_name, Timestamp assignment_create_time,
                      Timestamp assignment_start_time, Timestamp assignment_end_time,
                      String assignment_description, int is_visible) {
        this.assignment_id = assignment_id;
        this.assignment_name = assignment_name;
        this.assignment_create_time = assignment_create_time;
        this.assignment_start_time = assignment_start_time;
        this.assignment_end_time = assignment_end_time;
        this.assignment_description = assignment_description;
        this.is_visible = is_visible;
    }
}
