package ooad.demo.judge;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;

@Slf4j
public class ManageDockersPool {

    private static volatile ManageDockersPool INSTANCE;
    private static HashMap<String, DockerPool> dockersPoolHashMap;
    private static ArrayList<Integer> dockerPoolIDList;
    private static final Object createDockerPoolLock = new Object();

    private ManageDockersPool() {
        dockersPoolHashMap = new HashMap<>();
        dockerPoolIDList = new ArrayList<>();
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

    public  HashMap<String, DockerPool> getDockersPoolHashMap() {
        return dockersPoolHashMap;
    }

    public Object getCreateDockerPoolLock() {
        return createDockerPoolLock;
    }

    public ArrayList<Integer> getDockerPoolIDList() {
        return dockerPoolIDList;
    }
}
