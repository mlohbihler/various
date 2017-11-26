package lohbihler.sudoku;

import java.util.ArrayList;
import java.util.List;

/*
 * Created on 3-Jul-2005
 */

/**
 * @author mlohbihler
 */
public class VerticalEliminator implements Solver {
    @Override
    public void process(final PuzzleModel<?> model) {
        for (int i = 0; i < model.getSize(); i++)
            doColumn(i, model);
    }

    private static void doColumn(final int column, final PuzzleModel<?> model) {
        // Collect a list of all solved cells.
        final List<Character> solved = new ArrayList<>();
        for (int i = 0; i < model.getSize(); i++) {
            if (model.isSolved(column, i))
                solved.add(model.getSolvedValue(column, i));
        }

        // Remove these solved values from unsolved cells.
        for (int i = 0; i < model.getSize(); i++) {
            if (!model.isSolved(column, i))
                model.removeValues(column, i, solved);
        }
    }
}
