package ooad.demo.judge;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;

@Slf4j
public class ManageDockersPool {

    private static volatile ManageDockersPool INSTANCE;
    private static HashMap<String, DockerPool> dockersHashMap;
    private static final Object createDockLock = new Object();

    private ManageDockersPool() {
        dockersHashMap = new HashMap<>();
    }

    public static ManageDockersPool getInstance() {
        if (null == INSTANCE) {
            synchronized (ManageDockersPool.class) {
                if (null == INSTANCE) {
                    INSTANCE = new ManageDockersPool();
                }
            }
        }
        return INSTANCE;
    }

    public  HashMap<String, DockerPool> getDockersHashMap() {
        return dockersHashMap;
    }

}
