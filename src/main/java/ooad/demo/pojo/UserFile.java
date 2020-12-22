package ooad.demo.pojo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.sql.Timestamp;


@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Accessors(chain=true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserFile implements Serializable {
    private Integer id;
    private String old_file_name;
    private String new_file_name;
    private String ext;
    private String relative_path;
    private String file_size;
    private String file_type;
    private String is_img;
    private Integer down_counts = 0;
    private Timestamp upload_time = new Timestamp(System.currentTimeMillis());
    private Integer user_id;
    private Integer question_id;
    private Integer assignment_id;
    private Boolean is_database;
    private Boolean is_in_remote;
    private String file_description;
    private String file_show_place;
    private String remote_full_path;
    private Boolean is_exist = true;
}