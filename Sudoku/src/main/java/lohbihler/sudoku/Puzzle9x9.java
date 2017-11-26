package lohbihler.sudoku;

/**
 * @author mlohbihler
 */
public class Puzzle9x9 extends PuzzleModel<Puzzle9x9> {
    private static final Character[] CHARS = { //
            new Character('1'), new Character('2'), new Character('3'), //
            new Character('4'), new Character('5'), new Character('6'), //
            new Character('7'), new Character('8'), new Character('9') //
    };

    @Override
    protected Puzzle9x9 copyImpl() {
        return new Puzzle9x9();
    }

    @Override
    public int getBoxSize() {
        return 3;
    }

    @Override
    public Character getValueChar(final int value) {
        return CHARS[value];
    }
}
