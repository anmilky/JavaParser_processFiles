import java.lang.String;

public class Worker extends Thread{
    Task task=new Task();

    public Worker(Task task) {
        this.task = task;
    }

    @Override
    public void run() {
        while (!isInterrupted()) {
            try {
                String s = this.task.getTask();
                System.out.println(currentThread().getName()+s);
            } catch (InterruptedException e) {
                break;
            }
        }

    }

    public static void main(String[] args) throws InterruptedException {
        Task task=new Task();
        Worker worker=new Worker(task);
        worker.start();
        task.addTask("Bod");
        Thread.sleep(1000);
        task.addTask("Alice");
        Thread.sleep(1000);
        task.addTask("SAM");
        Thread.sleep(1000);
        worker.interrupt();
        System.out.println(currentThread().getName());
        worker.join();
        System.out.println("end");





    }
}
