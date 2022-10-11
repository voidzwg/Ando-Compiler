package Pass;

import Backend.MCModule;
import IR.IRModule;

public interface Pass {
    String getName();

    interface IRPass extends Pass{
        void run(IRModule module);
    }

    interface MCPass extends Pass{
        void run(MCModule mcModule);
    }
}
