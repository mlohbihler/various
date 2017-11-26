package lohbihler.sudoku;

/**
 * @author mlohbihler
 */
public class Puzzle16x16 extends PuzzleModel<Puzzle16x16> {
    private static final Character[] CHARS = { //
            new Character('0'), new Character('1'), new Character('2'), new Character('3'), //
            new Character('4'), new Character('5'), new Character('6'), new Character('7'), //
            new Character('8'), new Character('9'), new Character('a'), new Character('b'), //
            new Character('c'), new Character('d'), new Character('e'), new Character('f') //
    };

    @Override
    protected Puzzle16x16 copyImpl() {
        return new Puzzle16x16();
    }

    @Override
    public int getBoxSize() {
        return 4;
    }

    @Override
    public Character getValueChar(final int value) {
        return CHARS[value];
    }
}
