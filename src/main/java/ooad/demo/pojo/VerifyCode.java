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
    private Timestamp expired_time;

    public int getUser_id() {
        return user_id;
    }

    public int getV_code() {
        return v_code;
    }

    public int getCid() {
        return cid;
    }

    public Timestamp getExpired_time() {
        return expired_time;
    }
}

