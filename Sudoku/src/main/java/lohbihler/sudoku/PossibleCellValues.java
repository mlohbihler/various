package lohbihler.sudoku;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class PossibleCellValues {
    private List<Character> remaining;

    private PossibleCellValues() {
        // For copying.
    }

    public PossibleCellValues(final PuzzleModel<?> model) {
        remaining = new ArrayList<>(model.getSize());
        IntStream.range(0, model.getSize()).forEach(index -> remaining.add(model.getValueChar(index)));
    }

    public boolean isSolved() {
        return remaining.size() == 1;
    }

    public Character getSolvedValue() {
        if (!isSolved())
            throw new RuntimeException("Cell is not solved");
        return remaining.get(0);
    }

    public boolean removeValues(final List<Character> values) {
        return remaining.removeAll(values);
    }

    public boolean removeValue(final Character value) {
        return remaining.remove(value);
    }

    public boolean setValue(final Character value) {
        if (!remaining.contains(value))
            throw new RuntimeException("Attempt to set cell to a value that has already been eliminated: " + value);

        // Check if the cell will actually change.
        if (remaining.size() == 1 && remaining.get(0).equals(value))
            return false;

        remaining.clear();
        remaining.add(value);
        return true;
    }

    public int size() {
        return remaining.size();
    }

    public Character get(final int index) {
        return remaining.get(index);
    }

    public PossibleCellValues copy() {
        final PossibleCellValues copy = new PossibleCellValues();
        copy.remaining = new ArrayList<>(remaining);
        return copy;
    }

    public boolean contains(final Character c) {
        return remaining.contains(c);
    }
}
