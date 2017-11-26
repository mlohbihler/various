package lohbihler.sudoku;

import java.util.ArrayList;
import java.util.List;

/*
 * Created on 3-Jul-2005
 */

/**
 * @author mlohbihler
 */
public class HorizontalEliminator implements Solver {
    @Override
    public void process(final PuzzleModel<?> model) {
        for (int i = 0; i < model.getSize(); i++)
            doRow(i, model);
    }

    private static void doRow(final int row, final PuzzleModel<?> model) {
        // Collect a list of all solved cells.
        final List<Character> solved = new ArrayList<>();
        for (int i = 0; i < model.getSize(); i++) {
            if (model.isSolved(i, row))
                solved.add(model.getSolvedValue(i, row));
        }

        // Remove these solved values from unsolved cells.
        for (int i = 0; i < model.getSize(); i++) {
            if (!model.isSolved(i, row))
                model.removeValues(i, row, solved);
        }
    }
}
