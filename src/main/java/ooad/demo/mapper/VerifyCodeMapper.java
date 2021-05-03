package ooad.demo.mapper;

import ooad.demo.pojo.VerifyCode;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;


@Mapper
@Repository
public interface VerifyCodeMapper{
    VerifyCode getVerifyCode(int user_id);
    int insertVerifyCode(int user_id, int v_code, String dest_addr);
}
