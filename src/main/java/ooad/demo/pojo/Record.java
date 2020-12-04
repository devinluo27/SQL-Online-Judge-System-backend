package ooad.demo.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Record implements Serializable {
    private int record_id;
    @NotNull
    private int record_sid;

    private int record_question_id;
    private int record_status;
    private Timestamp record_time;
    private String record_code;
    private String record_code_type;

    public Record( int record_sid, int record_question_id, int record_status, String record_code
    ,String record_code_type) {
        this.record_sid = record_sid;
        this.record_question_id = record_question_id;
        this.record_status = record_status;
        this.record_time = new Timestamp(System.currentTimeMillis());
        this.record_code = record_code;
        this.record_code_type = record_code_type;
    }
}
