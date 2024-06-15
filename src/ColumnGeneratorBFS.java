import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

public class ColumnGeneratorBFS extends Thread {
    private SharedDataElement sharedData;
    private final ReadWriteLock rwLock;
    private final int umpire;
    private final double v_u;
    private final double[][] w;

//    private ??? solution;

    public ColumnGeneratorBFS(SharedDataElement sharedData, ReadWriteLock rwLock, int umpire, double v_u, double[][] w) {
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
        //TODO uiteindelijk de beste oplossing leesbaar maken (via getter)
        //      --> zorg dat deze getter direct een Column object returnt
    }
}
