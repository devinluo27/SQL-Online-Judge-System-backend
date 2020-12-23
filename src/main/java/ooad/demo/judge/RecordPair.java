package ooad.demo.judge;

import lombok.Data;
import ooad.demo.controller.StatisticsController;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
@Data
public class RecordPair implements Comparable<RecordPair>, Serializable {
    Integer sid_1 = 0;
    Integer sid_2 = 0;
    Double checkResult = 0.0;

    public RecordPair(Integer sid_1, Integer sid_2, Double checkResult) {
        this.sid_1 = sid_1;
        this.sid_2 = sid_2;
        this.checkResult = checkResult;
    }

    public RecordPair() {
    }

    @Override
    public int compareTo(RecordPair o) {
        return (int)(this.checkResult - o.checkResult);
    }


}
