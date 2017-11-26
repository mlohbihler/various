package lohbihler.sudoku;

/*
 * Created on 3-Jul-2005
 */

/**
 * A second-level solver. Uses a "trial"ing algorithm to test potential
 * values by creating a clone of the model, and inserting a potential
 * value to see if the first-level solvers discover a problem (i.e.
 * throw a runtime exception). If only one value does not throw an
 * exception, that value is set in the original model.
 *
 * @author mlohbihler
 */
public class PotentialValueElimination implements Solver {
    @Override
    public void process(final PuzzleModel<?> model) {
        // Create a clone of the model.
        PuzzleModel<?> modelClone;

        // Iterate through the cells looking for those with multiple potential values.
        for (int y = 0; y < model.getSize(); y++) {
            for (int x = 0; x < model.getSize(); x++) {
                if (!model.isSolved(x, y)) {
                    final PossibleCellValues pcv = model.getCell(x, y).copy();

                    // Test the values in the clone.
                    for (int i = 0; i < pcv.size(); i++) {
                        modelClone = model.copy();
                        modelClone.setValue(x, y, pcv.get(i));

                        try {
                            SolverFactory.solveFirstLevel(modelClone);
                        } catch (final RuntimeException e) {
                            // The value didn't work, so remove it as a potential value.
                            model.removeValue(x, y, pcv.get(i));
                        }
                    }
                }
            }
        }
    }
}
