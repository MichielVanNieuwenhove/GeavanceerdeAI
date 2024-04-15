import gurobi.*;

import java.util.List;
import java.util.ArrayList;

public class masterProblemSolver {
    private final List<Column>[] columns = new List[InputManager.getnUmpires()];

    public void init(){
        for(int u = 0; u < InputManager.getnUmpires(); u++){
            columns[u] = new ArrayList<>();
            //TODO generate initial columns
        }
    }

}
