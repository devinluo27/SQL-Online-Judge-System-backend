package ooad.demo.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VerifyCode implements Serializable {
    private int cid;
    private int user_id;
    private int v_code;
    private Timestamp created_time;
    private String dest_addr;

    public Timestamp getCreated_time() {
        return created_time;
    }

}

