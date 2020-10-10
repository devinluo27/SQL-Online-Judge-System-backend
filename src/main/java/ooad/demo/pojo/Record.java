package ooad.demo.pojo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
public class Record {
    private int record_id;
    private int record_sid;
    private int record_question_id;
    private int record_status;
    private Timestamp record_time;
    private String record_code;

    public Record(int record_id, int record_sid, int record_question_id, int record_status, Timestamp record_time, String record_code) {
        this.record_id = record_id;
        this.record_sid = record_sid;
        this.record_question_id = record_question_id;
        this.record_status = record_status;
        this.record_time = record_time;
        this.record_code = record_code;
    }
}
