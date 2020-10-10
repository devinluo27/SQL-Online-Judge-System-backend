package ooad.demo.pojo;


import java.sql.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Data
@NoArgsConstructor
//@AllArgsConstructor
public class Assignment implements Serializable {
    private int assignment_id;
    private String assignment_name;
    private Timestamp assignment_create_time;
    private Timestamp assignment_start_time;
    private Timestamp assignment_end_time;
    private String assignment_description;

    public Assignment(int id, String name, Timestamp create_time, Timestamp start_time, Timestamp end_time, String descrition) {
        this.assignment_id = id;
        this.assignment_name = name;
        this.assignment_create_time = create_time;
        this.assignment_end_time = end_time;
        this.assignment_start_time = start_time;
        this.assignment_description = descrition;
    }
}
