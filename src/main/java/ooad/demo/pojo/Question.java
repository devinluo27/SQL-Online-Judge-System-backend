package ooad.demo.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Data
@NoArgsConstructor
public class Question implements Serializable{
    private Integer id;
    private Integer question_id;
    private String question_name;
    private Integer question_of_assignment;
    private String question_description;
    private String question_output;
    private Integer question_index;
    private Integer is_finished;
    private String question_standard_ans;
    private Integer database_id;
    private Integer is_visible;

    public Question(int question_id, String question_name, int question_of_assignment, String question_description,
                    String question_output, int question_index, int is_finished,
                    String question_standard_ans, int database_id) {
        this.question_id = question_id;
        this.question_name = question_name;
        this.question_of_assignment = question_of_assignment;
        this.question_description = question_description;
        this.question_output = question_output;
        this.question_index = question_index;
        this.is_finished = is_finished;
        this.question_standard_ans = question_standard_ans;
        this.database_id = database_id;
    }
}
