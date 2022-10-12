package Pass.MC;

import Backend.MCModule;
import Backend.MachineValue.MCBlock;
import Backend.MachineValue.MCFunction;
import Pass.MC.Utils.LiveAnalysis;
import Pass.Pass;
import Pass.MC.Utils.LiveAnalysis.BlockLiveInfo;

import java.util.ArrayList;
import java.util.HashMap;

public class RegAllocator implements Pass.MCPass {

    @Override
    public void run(MCModule mcModule) {
        ArrayList<MCFunction> mcFunctions = mcModule.getMcFunctions();
        for(MCFunction mf : mcFunctions){
            LiveAnalysis liveAnalysis = new LiveAnalysis(mf);
            HashMap<MCBlock, BlockLiveInfo> liveAnalysisRes = liveAnalysis.getRes();


        }
    }




    @Override
    public String getName() {
        return "RegAlloc";
    }
}
