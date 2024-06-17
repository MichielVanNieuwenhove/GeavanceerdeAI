//import java.util.LinkedList;
//import java.util.List;
//import java.util.concurrent.locks.Lock;
//import java.util.concurrent.locks.ReadWriteLock;
//
//public class ColumnGeneratorBFS extends Thread {
//    private SharedDataElement sharedData;
//    private final ReadWriteLock rwLock;
//    private final int umpire;
//    private final double v_u;
//    private final double[][] w;
//
//    private ??? solution;
//
//    public ColumnGeneratorBFS(SharedDataElement sharedData, ReadWriteLock rwLock, int umpire, double v_u, double[][] w) {
//        this.sharedData = sharedData;
//        this.rwLock = rwLock;
//        this.umpire = umpire;
//        this.v_u = v_u;
//        this.w = w;
//    }
//
//    @Override
//    public void run() {
//        Lock readLock = rwLock.readLock();
//        Lock writeLock = rwLock.writeLock();
//
//        //list met volgende geldige nodes
//        List<ColumnGenNode> nextNodes = new LinkedList<>();  //moet node een leaf node van een tree voorstellen???
//        //TODO add root node to nextNodes
//        while(!nextNodes.isEmpty()) {
//            //TODO
//            //  currentNode = nextNodes.remove(0);
//            //  calc stuff for current node;
//            //  look for possible next Nodes and add to list;
//            //  sort nextNodes so the lowest cost is first in the list;
//        }
//
//        //TODO uiteindelijk de beste oplossing leesbaar maken (via getter)
//        //      --> zorg dat deze getter direct een Column object returnt
//    }
//
//    public Column getSol() {
//        //TODO
//        return null;
//    }
//}
