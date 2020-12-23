package ooad.demo.pojo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Record implements Serializable {
    private Integer record_id;

    private Integer record_sid;

    @NotNull
    private Integer record_question_id;
    private Integer record_status;
    private Timestamp record_time;

    @NotNull
    private String record_code;

    private String record_code_type;

    private Double running_time = -1.0;

    public Record(@NotNull Integer record_sid, Integer record_question_id, Integer record_status, String record_code,
                  String record_code_type) {
        this.record_sid = record_sid;
        this.record_question_id = record_question_id;
        this.record_status = record_status;
        this.record_time = new Timestamp(System.currentTimeMillis());
        this.record_code = record_code;
        this.record_code_type = record_code_type;
    }

    public Record(@NotNull Integer record_sid, Integer record_question_id, Integer record_status, String record_code,
                  String record_code_type, Double running_time) {
        this.record_sid = record_sid;
        this.record_question_id = record_question_id;
        this.record_status = record_status;
        this.record_code = record_code;
        this.record_code_type = record_code_type;
        this.running_time = running_time;
    }

}
