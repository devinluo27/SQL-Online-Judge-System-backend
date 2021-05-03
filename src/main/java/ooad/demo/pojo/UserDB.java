package ooad.demo.pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
public class UserDB implements Serializable {

    @NotNull
    private Integer sid;

    private String user_name;

    @JsonIgnore
    private String user_password;

    private String authority = null;
    private Integer enabled = 1;
    private Timestamp last_login_time;
    private Timestamp created_time;
    private Integer lab_num;

    private String email_addr;

    public String getUser_name() {
        return user_name;
    }

    public UserDB(int sid, String user_name, String password, String authority) {
        this.sid = sid;
        this.user_name = user_name;
        this.user_password = password;
        this.authority = authority;
    }

    public UserDB(int sid, String user_name, String password) {
        this.sid = sid;
        this.user_name = user_name;
        this.user_password = password;
    }

    public int getSid() {
        return sid;
    }
}
