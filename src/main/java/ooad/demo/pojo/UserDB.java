package ooad.demo.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Data
@NoArgsConstructor
//@AllArgsConstructor
public class UserDB implements Serializable {

    private int id;
    private int sid;
    private String user_name;
    private String user_password;

    public String getUser_name() {
        return user_name;
    }

    public UserDB(int i, int sid, String user_name, String password) {
        this.id = i;
        this.sid = sid;
        this.user_name = user_name;
        this.user_password = password;
    }
}
