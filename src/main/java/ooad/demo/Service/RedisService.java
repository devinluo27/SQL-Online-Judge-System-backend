package ooad.demo.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RedisService {

    public final static Logger logger = LoggerFactory.getLogger(RedisService.class);

    @Autowired
    private RedisTemplate redisTemplate;

    public boolean setKeyLifeTime(String key, long lifetime, TimeUnit unit){
        try {
            return redisTemplate.expire(key, lifetime, unit);
        }catch (Exception e){
            return true;
        }
    }

    //放入redis，更新也使用这个方法，前提key要存在
    public void setHashValue(String hashName, Object key, Object value) {
        redisTemplate.opsForHash().put(hashName, key, value);
    }

    public Object getHashValue(String hashName, Object key) {
        return redisTemplate.opsForHash().get(hashName, key);
    }

    public boolean incr(final String key, String keyInHashMap) {
        Integer count = (Integer) getHashValue(key, keyInHashMap);
        if (getHashValue(key, keyInHashMap) == null){
            return true;
        }
        else {
            setHashValue(key, keyInHashMap, ++count);
        }
        return false;
    }




}
