package lohbihler.sudoku;

/*
 * Created on 3-Jul-2005
 */

/**
 * @author mlohbihler
 */
public class VerticalSingleValueSeeker implements Solver {
    @Override
    public void process(final PuzzleModel<?> model) {
        for (int i = 0; i < model.getSize(); i++)
            doColumn(i, model);
    }

    public void doColumn(final int column, final PuzzleModel<?> model) {
        for (int i = 0; i < model.getSize(); i++)
            doColumnValue(column, model.getValueChar(i), model);
    }

    private static void doColumnValue(final int column, final Character value, final PuzzleModel<?> model) {
        int row = -1;
        for (int i = 0; i < model.getSize(); i++) {
            if (model.getCell(column, i).contains(value)) {
                if (row == -1)
                    row = i;
                else {
                    row = -2;
                    break;
                }
            }
        }

        if (row == -1)
            throw new RuntimeException("No cell can take the value " + value + ", column " + column);

        if (row > -1 && !model.isSolved(column, row))
            // Only one cell can take the value, so set the cell to that value.
            model.setValue(column, row, value);
    }
}
