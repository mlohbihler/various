package lohbihler.sudoku;

import java.util.ArrayList;
import java.util.List;

/*
 * Created on 3-Jul-2005
 */

public class VerticalValidator implements Validator {
    @Override
    public void validate(final PuzzleModel<?> model) {
        for (int i = 0; i < model.getSize(); i++)
            doColumn(i, model);
    }

    public void doColumn(final int column, final PuzzleModel<?> model) {
        final List<Character> values = new ArrayList<>();
        Character value;

        for (int i = 0; i < model.getSize(); i++) {
            if (model.isSolved(column, i)) {
                value = model.getSolvedValue(column, i);
                // Check that the value doesn't already exist.
                if (values.contains(value))
                    throw new RuntimeException("Duplicate column value at x=" + column + ", y=" + i);
                values.add(value);
            }
        }
    }
}
