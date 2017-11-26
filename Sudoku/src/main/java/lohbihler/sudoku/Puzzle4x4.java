package lohbihler.sudoku;

/**
 * @author mlohbihler
 */
public class Puzzle4x4 extends PuzzleModel<Puzzle4x4> {
    private static final Character[] CHARS = { //
            new Character('1'), new Character('2'), //
            new Character('3'), new Character('4'), //
    };

    @Override
    protected Puzzle4x4 copyImpl() {
        return new Puzzle4x4();
    }

    @Override
    public int getBoxSize() {
        return 2;
    }

    @Override
    public Character getValueChar(final int value) {
        return CHARS[value];
    }
}
