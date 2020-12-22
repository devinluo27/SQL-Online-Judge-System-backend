package ooad.demo.pojo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Repository;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Database implements Serializable {

    private Integer id;
    private Integer database_id;
    private Integer user_file_id;
    private String database_name;
    private String database_description;
    private String database_remote_path;
    private String database_remote_name;
    private Boolean is_enabled;

    public Database(int database_id, String database_remote_path, String database_remote_name) {
        this.database_id = database_id;
        this.database_remote_path = database_remote_path;
        this.database_remote_name = database_remote_name;
    }

    public Database(Integer user_file_id, String database_name, String database_description, String database_remote_path, String database_remote_name) {
        this.user_file_id = user_file_id;
        this.database_name = database_name;
        this.database_description = database_description;
        this.database_remote_path = database_remote_path;
        this.database_remote_name = database_remote_name;
    }
}
