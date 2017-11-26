package lohbihler.sudoku;

/**
 * Looks for cells in rows where they are the only cells that can take on
 * given value, and if found, sets the cell to that value.
 *
 * @author mlohbihler
 */
public class HorizontalSingleValueSeeker implements Solver {
    @Override
    public void process(final PuzzleModel<?> model) {
        for (int i = 0; i < model.getSize(); i++)
            doRow(i, model);
    }

    public void doRow(final int row, final PuzzleModel<?> model) {
        for (int i = 0; i < model.getSize(); i++)
            doRowValue(row, model.getValueChar(i), model);
    }

    private static void doRowValue(final int row, final Character value, final PuzzleModel<?> model) {
        int column = -1;
        for (int i = 0; i < model.getSize(); i++) {
            if (model.getCell(i, row).contains(value)) {
                if (column == -1)
                    column = i;
                else {
                    column = -2;
                    break;
                }
            }
        }

        if (column == -1)
            throw new RuntimeException("No cell can take the value " + value);

        if (column > -1 && !model.isSolved(column, row))
            // Only one cell can take the value, so set the cell to that value.
            model.setValue(column, row, value);
    }
}
