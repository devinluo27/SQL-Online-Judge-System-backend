package ooad.demo.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SysPermission implements Serializable {
    private int id;
    private String permission_code;
    private  String permission_name;

    public int getId() {
        return id;
    }

    public String getPermission_code() {
        return permission_code;
    }

    public String getPermission_name() {
        return permission_name;
    }
}
