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
    private String ans_table_path;
    private String test_data_path;
    private String test_config;
    private String target_table;
}
