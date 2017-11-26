package lohbihler.sudoku;

import java.util.ArrayList;
import java.util.List;

/*
 * Created on 3-Jul-2005
 */

/**
 * @author mlohbihler
 */
public class HorizontalValidator implements Validator {
    @Override
    public void validate(final PuzzleModel<?> model) {
        // Ensure that each row has no duplicate solved cells.
        for (int i = 0; i < model.getSize(); i++)
            doRow(i, model);
    }

    private static void doRow(final int row, final PuzzleModel<?> model) {
        final List<Character> values = new ArrayList<>();
        Character value;

        // Ensure that there are no duplicate solved cells.
        for (int x = 0; x < model.getSize(); x++) {
            if (model.isSolved(x, row)) {
                value = model.getSolvedValue(x, row);
                // Check that the value doesn't already exist.
                if (values.contains(value))
                    throw new RuntimeException("Duplicate row value at x=" + x + ", y=" + row);
                values.add(value);
            }
        }
    }
}
