package ooad.demo.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.sql.Timestamp;

@Data
@NoArgsConstructor
//@AllArgsConstructor
public class UserDB implements Serializable {

    @NotNull
    private int sid;

    private String user_name;
    private String user_password;
    private String authority;
    private int enabled = 1;
    private Timestamp last_login_time;
    private Timestamp created_time;

    public String getUser_name() {
        return user_name;
    }

    public UserDB(int sid, String user_name, String password, String authority) {
        this.sid = sid;
        this.user_name = user_name;
        this.user_password = password;
        this.authority = authority;
    }

    public int getSid() {
        return sid;
    }
}
