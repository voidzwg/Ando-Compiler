package IR;

import IR.Value.Function;
import IR.Value.GlobalVars;

import java.util.ArrayList;

public class IRModule {
    private ArrayList<Function> functions;
    private ArrayList<GlobalVars> globalVars;

    public IRModule(ArrayList<Function> functions, ArrayList<GlobalVars> globalVars){
        this.functions = functions;
        this.globalVars = globalVars;
    }

    public ArrayList<Function> getFunctions() {
        return functions;
    }

    public ArrayList<GlobalVars> getGlobalVars() {
        return globalVars;
    }
}
