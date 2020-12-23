package ooad.demo.pojo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QuestionTrigger {

    private Integer id;
    private Integer question_id;
    private Integer ans_table_file_id;
    private Integer test_data_file_id;
    private Integer test_config;
    private String target_table;

    public QuestionTrigger(Integer question_id, Integer ans_table_file_id,
                           Integer test_data_file_id,
                           Integer test_config,
                           String target_table) {
        this.question_id = question_id;
        this.ans_table_file_id = ans_table_file_id;
        this.test_data_file_id = test_data_file_id;
        this.test_config = test_config;
        this.target_table = target_table;
    }
}
