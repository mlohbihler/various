package lohbihler.sudoku;

import java.awt.Point;

/*
 * Created on 3-Jul-2005
 */

/**
 * @author mlohbihler
 */
public class BoxSingleValueSeeker implements Solver {
    private static final Point FOUND_POINT = new Point(-2, -2);

    @Override
    public void process(final PuzzleModel<?> model) {
        for (int x = 0; x < model.getBoxSize(); x++) {
            for (int y = 0; y < model.getBoxSize(); y++)
                doBox(x * model.getBoxSize(), y * model.getBoxSize(), model);
        }
    }

    private void doBox(final int xStart, final int yStart, final PuzzleModel<?> model) {
        for (int i = 0; i < model.getSize(); i++)
            doBoxValue(xStart, yStart, model.getValueChar(i), model);
    }

    private static void doBoxValue(final int xStart, final int yStart, final Character value,
            final PuzzleModel<?> model) {
        Point point = null;
        for (int x = xStart; x < model.getBoxSize() + xStart; x++) {
            for (int y = yStart; y < model.getBoxSize() + yStart; y++) {
                if (model.getCell(x, y).contains(value)) {
                    if (point == null)
                        point = new Point(x, y);
                    else {
                        point = FOUND_POINT;
                        break;
                    }
                }
            }
        }

        if (point == null)
            throw new RuntimeException("No cell can take the value " + value + ", box at " + xStart + "," + yStart);

        if (point != FOUND_POINT && !model.isSolved(point.x, point.y))
            // Only one cell can take the value, so set the cell to that value.
            model.setValue(point.x, point.y, value);
    }
}
