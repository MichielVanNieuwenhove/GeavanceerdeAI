public class BranchAndBound {
    int upperbound = Integer.MAX_VALUE;

    public void start(){
        for(int i = 0; i < InputManager.getnUmpires(); i++){
            BranchAndBound_DFS DFS = new BranchAndBound_DFS(i, this);
            DFS.start();
            BranchAndBound_BFS BFS = new BranchAndBound_BFS(i, this);
            BFS.start();
        }
    }
}
