package ooad.demo.pojo;


import java.sql.Date;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Assignment implements Serializable {
    private int assignment_id;
    private String assignment_name;
    private Date assignment_start_time;
    private Date assignment_end_time;
    private String assignment_description;


}
