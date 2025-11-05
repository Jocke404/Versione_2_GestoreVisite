package src.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadPoolController {

    private static ThreadPoolController instance;  
    private static final List<ExecutorService> threadPools = new ArrayList<>();

    private ThreadPoolController() {}

     
    public ExecutorService createThreadPool(int poolSize) {
        ExecutorService executorService = Executors.newFixedThreadPool(poolSize);
        threadPools.add(executorService);
        return executorService;
    }

     
    public ExecutorService createSingleThreadExecutor() {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        threadPools.add(executorService);
        return executorService;
    }

     
    public void shutdownAll() {
        for (ExecutorService executorService : threadPools) {
            executorService.shutdown();
        }
        threadPools.clear();
    }
    
    public static synchronized ThreadPoolController getInstance() {
        if (instance == null) {
            instance = new ThreadPoolController();
        }
        return instance;
    }
}
