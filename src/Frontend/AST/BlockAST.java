package Frontend.AST;

import java.util.ArrayList;

public class BlockAST {
    ArrayList<BlockItemAST> BlockItems;

    public BlockAST() {
        this.BlockItems = new ArrayList<>();
    }

    public void addBlockItem(BlockItemAST blockItemAST){
        this.BlockItems.add(blockItemAST);
    }

}
