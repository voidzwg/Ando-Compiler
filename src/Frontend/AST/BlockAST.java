package Frontend.AST;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class BlockAST {
    ArrayList<BlockItemAST> BlockItems;
    private int line;

    public BlockAST(ArrayList<BlockItemAST> blockItems, int line) {
        this.BlockItems = blockItems;
        this.line = line;
    }

    public void addBlockItem(BlockItemAST blockItemAST){
        this.BlockItems.add(blockItemAST);
    }

    public ArrayList<BlockItemAST> getBlockItems() {
        return BlockItems;
    }

    public int getLine() {
        return line;
    }
}
