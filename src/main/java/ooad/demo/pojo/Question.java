package ooad.demo.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Data
@NoArgsConstructor
public class Question implements Serializable{
    private int question_id;
    private String question_name;
    private int question_of_assignment;
    private String question_description;
    private String question_output;
    private int question_index;
    private int is_finished;
    private String standard_ans;

    public Question(int question_id, String question_name, int question_of_assignment,
                    String question_description, String question_output, int question_index,
                    int is_finished, String standard_ans) {
        this.question_id = question_id;
        this.question_name = question_name;
        this.question_of_assignment = question_of_assignment;
        this.question_description = question_description;
        this.question_output = question_output;
        this.question_index = question_index;
        this.is_finished = is_finished;
        this.standard_ans = standard_ans;
    }

    public void setStandard_ans(String standard_ans) {
        this.standard_ans = standard_ans;
    }
}
