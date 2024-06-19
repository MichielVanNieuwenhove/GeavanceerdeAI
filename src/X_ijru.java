public class X_ijru {
    private final int i;
    private final int j;
    private final int r;
    private final int u;

    public X_ijru(int i, int j, int r, int u) {
        this.i = i;
        this.j = j;
        this.r = r;
        this.u = u;
    }

    public static boolean compare(X_ijru x1, X_ijru x2){
        return x1.getI() == x2.getI() && x1.getJ() == x2.getJ() && x1.getR() == x2.getR();
    }

    public int getI() {
        return i;
    }

    public int getJ() {
        return j;
    }

    public int getR() {
        return r;
    }

    public int getU(){
        return u;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        X_ijru other = (X_ijru) o;
        return i == other.i && j == other.j && r == other.r && u == other.u;
    }
}
