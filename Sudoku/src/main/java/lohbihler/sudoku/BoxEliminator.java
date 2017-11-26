package lohbihler.sudoku;

import java.util.ArrayList;
import java.util.List;

/*
 * Created on 3-Jul-2005
 */

/**
 * @author mlohbihler
 */
public class BoxEliminator implements Solver {
    @Override
    public void process(final PuzzleModel<?> model) {
        for (int x = 0; x < model.getBoxSize(); x++) {
            for (int y = 0; y < model.getBoxSize(); y++)
                doBox(x * model.getBoxSize(), y * model.getBoxSize(), model);
        }
    }

    private static void doBox(final int xStart, final int yStart, final PuzzleModel<?> model) {
        // Collect a list of all solved cells.
        final List<Character> solved = new ArrayList<>();
        for (int x = xStart; x < model.getBoxSize() + xStart; x++) {
            for (int y = yStart; y < model.getBoxSize() + yStart; y++) {
                if (model.isSolved(x, y))
                    solved.add(model.getSolvedValue(x, y));
            }
        }

        // Remove these solved values from unsolved cells.
        for (int x = xStart; x < model.getBoxSize() + xStart; x++) {
            for (int y = yStart; y < model.getBoxSize() + yStart; y++) {
                if (!model.isSolved(x, y))
                    model.removeValues(x, y, solved);
            }
        }
    }
}
