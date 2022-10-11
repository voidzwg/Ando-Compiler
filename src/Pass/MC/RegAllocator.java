package Pass.MC;

import Backend.MCModule;
import Pass.Pass;

public class RegAllocator implements Pass.MCPass {
    @Override
    public String getName() {
        return "RegAlloc";
    }

    @Override
    public void run(MCModule mcModule) {

    }
}
