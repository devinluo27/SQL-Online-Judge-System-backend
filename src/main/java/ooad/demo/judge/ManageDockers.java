package ooad.demo.judge;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;

@Slf4j
public class ManageDockers {

    private static volatile ManageDockers INSTANCE;
    private static HashMap<String, DockerPool> dockersHashMap;
    // 加上 ForTool 后缀来和之前两种方式创建的对象作区分。
//    private UserService userServiceForTool;

    private ManageDockers() {
        dockersHashMap = new HashMap<>();
//        userServiceForTool = SpringContextUtils.getBean(UserService.class);
    }

    public static ManageDockers getInstance() {
        if (null == INSTANCE) {
            synchronized (ManageDockers.class) {
                if (null == INSTANCE) {
                    INSTANCE = new ManageDockers();
                }
            }
        }
        return INSTANCE;
    }

    public  HashMap<String, DockerPool> getDockersHashMap() {
        return dockersHashMap;
    }
    /**
     * 使用 SpringContextUtils 获取的 UserService 对象，并从 UserDao 中获取数据
     * @return
     */
//    public String getUserForToolFromDao() {
//        if (null == userServiceForTool) {
//            log.debug("UserSingleton userService is null");
//            return "UserSingleton Exception: userService is null";
//        }
//        return userServiceForTool.getUserForDao();
//    }
}
