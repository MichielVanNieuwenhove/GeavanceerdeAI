public class FixedColumnTreeNode {
    int[] fixedColumn;//format: {umpireNr, columnNr, 0 of 1}
    FixedColumnTreeNode previous = null;

    public FixedColumnTreeNode(int[] toFix, FixedColumnTreeNode prev) {
        fixedColumn = toFix;
        previous = prev;
    }

    public int[] getFixedColumn() {
        return fixedColumn;
    }

    public FixedColumnTreeNode getPrevious() {
        return previous;
    }
}
