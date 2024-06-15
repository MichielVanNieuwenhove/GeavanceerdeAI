import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

public class ColumnGeneratorDFS extends Thread {
    private SharedDataElement sharedData;
    private ReadWriteLock rwLock;
    private int umpire;
    private double v_u;
    private double[][] w;

    public ColumnGeneratorDFS(SharedDataElement sharedData, ReadWriteLock rwLock, int umpire, double v_u, double[][] w) {
        this.sharedData = sharedData;
        this.rwLock = rwLock;
        this.umpire = umpire;
        this.v_u = v_u;
        this.w = w;
    }

    @Override
    public void run() {
        Lock readLock = rwLock.readLock();
        Lock writeLock = rwLock.writeLock();
    }
}
