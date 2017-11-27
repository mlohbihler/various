package lohbihler.sudoku;

import java.util.Arrays;
import java.util.List;

/*
 * Created on 4-Jul-2005
 */

/**
 * @author mlohbihler
 */
public abstract class PuzzleModel<T extends PuzzleModel<?>> {
    protected PossibleCellValues[][] possibleCellValues;
    protected boolean changed;

    public T copy() {
        final T copy = copyImpl();

        final int size = getSize();
        copy.possibleCellValues = new PossibleCellValues[size][size];
        copy.changed = changed;

        XYStreams.traverse(possibleCellValues,
                (x, y) -> copy.possibleCellValues[x][y] = possibleCellValues[x][y].copy());

        return copy;
    }

    protected abstract T copyImpl();

    public abstract int getBoxSize();

    public abstract Character getValueChar(int i);

    public int getSize() {
        return getBoxSize() * getBoxSize();
    }

    public void set(final PuzzleModel<?> that) {
        possibleCellValues = that.possibleCellValues;
        changed = that.changed;
    }

    public boolean hasChanged() {
        return changed;
    }

    public void reset() {
        changed = false;
    }

    public void init() {
        final int size = getSize();
        possibleCellValues = new PossibleCellValues[size][size];
        XYStreams.traverse(possibleCellValues, (x, y) -> possibleCellValues[x][y] = new PossibleCellValues(this));
    }

    protected PossibleCellValues[][] getPossibleCellValues() {
        return possibleCellValues;
    }

    public boolean isSolved(final int x, final int y) {
        return possibleCellValues[x][y].isSolved();
    }

    public Character getSolvedValue(final int x, final int y) {
        try {
            return possibleCellValues[x][y].getSolvedValue();
        } catch (final RuntimeException e) {
            throw new RuntimeException("" + x + "," + y, e);
        }
    }

    public PossibleCellValues getCell(final int x, final int y) {
        return possibleCellValues[x][y];
    }

    public void removeValues(final int x, final int y, final List<Character> values) {
        if (possibleCellValues[x][y].removeValues(values))
            changed = true;
    }

    public void removeValue(final int x, final int y, final Character value) {
        if (possibleCellValues[x][y].removeValue(value))
            changed = true;
    }

    public void setValue(final int x, final int y, final char value) {
        setValue(x, y, new Character(value));
    }

    public void setValue(final int x, final int y, final Character value) {
        try {
            possibleCellValues[x][y].setValue(value);
        } catch (final RuntimeException e) {
            throw new RuntimeException("" + x + "," + y, e);
        }
    }

    public boolean isEmpty(final int x, final int y) {
        return possibleCellValues[x][y].size() == 0;
    }

    public boolean isSolved() {
        return !XYStreams.toStream(possibleCellValues).anyMatch(pcv -> !pcv.isSolved());
    }

    @Override
    public String toString() {
        final int maxSize = XYStreams.toStream(possibleCellValues) //
                .max((pcv1, pcv2) -> pcv1.size() - pcv2.size()) //
                .get().size();

        final StringBuilder sb = new StringBuilder();

        Arrays.stream(possibleCellValues) //
                .forEach(arr -> {
                    Arrays.stream(arr) //
                            .forEach(pcv -> {
                                int counter = maxSize;
                                // Write out the remaining values
                                for (int i = 0; i < pcv.size(); i++) {
                                    sb.append(pcv.get(i));
                                    counter--;
                                }
                                // Fill in with spaces.
                                while (counter > 0) {
                                    sb.append(' ');
                                    counter--;
                                }
                                sb.append(" | ");
                            });
                    sb.append("\r\n");
                });
        return sb.toString();
    }

    public void dump() {
        System.out.println(toString());
    }
}
