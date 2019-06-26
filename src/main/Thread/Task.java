import java.lang.String;

import java.util.LinkedList;
import java.util.Queue;

public class Task {
    Queue<String> task =new LinkedList<>();

    public synchronized void addTask(String s){
        this.task.add(s);
        this.notifyAll();
    }

    public synchronized String getTask() throws InterruptedException {
        while (task.isEmpty()){
            this.wait();
        }
        return task.remove();
    }


}
