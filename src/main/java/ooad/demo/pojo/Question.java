package ooad.demo.pojo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Question implements Serializable{
    private Integer id;
    @NotNull
    private Integer question_id;
    @NotNull
    private String question_name;

    private Integer question_of_assignment;
    @NotNull
    private String question_description;

    private String question_output;
    @NotNull
    private Integer question_index;

    private Integer is_finished;
    private String question_standard_ans;
    @NotNull
    private Integer database_id;
    private Integer is_visible;
    private String operation_type;
    private Boolean is_order;

    // postgresql or sqlite or mysql
    @NotNull
    private String question_sql_type;

    public Question(Integer id, Integer question_id, String question_name,
                    Integer question_of_assignment, String question_description,
                    String question_output, Integer question_index, Integer is_finished
            , String question_standard_ans, Integer database_id, Integer is_visible, String operation_type) {
        this.id = id;
        this.question_id = question_id;
        this.question_name = question_name;
        this.question_of_assignment = question_of_assignment;
        this.question_description = question_description;
        this.question_output = question_output;
        this.question_index = question_index;
        this.is_finished = is_finished;
        this.question_standard_ans = question_standard_ans;
        this.database_id = database_id;
        this.is_visible = is_visible;
        this.operation_type = operation_type;
    }
}
