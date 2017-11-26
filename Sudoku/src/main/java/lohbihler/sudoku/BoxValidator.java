package lohbihler.sudoku;

import java.util.ArrayList;
import java.util.List;

/*
 * Created on 3-Jul-2005
 */

/**
 * @author mlohbihler
 */
public class BoxValidator implements Validator {
    @Override
    public void validate(final PuzzleModel<?> model) {
        for (int x = 0; x < model.getBoxSize(); x++) {
            for (int y = 0; y < model.getBoxSize(); y++)
                doBox(x * model.getBoxSize(), y * model.getBoxSize(), model);
        }
    }

    private static void doBox(final int xStart, final int yStart, final PuzzleModel<?> model) {
        final List<Character> values = new ArrayList<>();
        Character value;

        for (int x = xStart; x < model.getBoxSize() + xStart; x++) {
            for (int y = yStart; y < model.getBoxSize() + yStart; y++) {
                if (model.isSolved(x, y)) {
                    value = model.getSolvedValue(x, y);
                    // Check that the value doesn't already exist.
                    if (values.contains(value))
                        throw new RuntimeException("Duplicate box value at x=" + x + ", y=" + y);
                    values.add(value);
                }
            }
        }
    }
}
