package lohbihler.sudoku;

/**
 * @author mlohbihler
 */
public class Puzzle25x25 extends PuzzleModel<Puzzle25x25> {
    private static final Character[] CHARS = { //
            new Character('a'), new Character('b'), new Character('c'), new Character('d'), new Character('e'), //
            new Character('f'), new Character('g'), new Character('h'), new Character('i'), new Character('j'), //
            new Character('k'), new Character('l'), new Character('m'), new Character('n'), new Character('o'), //
            new Character('p'), new Character('q'), new Character('r'), new Character('s'), new Character('t'), //
            new Character('u'), new Character('v'), new Character('w'), new Character('x'), new Character('y') //
    };

    @Override
    protected Puzzle25x25 copyImpl() {
        return new Puzzle25x25();
    }

    @Override
    public int getBoxSize() {
        return 5;
    }

    @Override
    public Character getValueChar(final int value) {
        return CHARS[value];
    }
}
