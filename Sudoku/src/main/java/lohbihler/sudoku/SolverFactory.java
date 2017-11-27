package lohbihler.sudoku;

/**
 * @author mlohbihler
 */
public class SolverFactory {
    private static Solver[] firstLevelSolvers = { new BoxEliminator(), new HorizontalEliminator(),
            new VerticalEliminator(), new BoxSingleValueSeeker(), new HorizontalSingleValueSeeker(),
            new VerticalSingleValueSeeker(), };

    private static Solver[] secondLevelSolvers = { new PotentialValueElimination(), };

    private static Validator[] validators = { new HorizontalValidator(), new VerticalValidator(), new BoxValidator(), };

    public static boolean solveFirstLevel(final PuzzleModel<?> model) {
        boolean changed = false;
        model.reset();
        while (true) {
            for (int i = 0; i < firstLevelSolvers.length; i++)
                firstLevelSolvers[i].process(model);
            if (!model.hasChanged())
                break;
            changed = true;
            model.reset();
        }
        return changed;
    }

    public static boolean solveSecondLevel(final PuzzleModel<?> model) {
        boolean changed = false;
        model.reset();
        while (true) {
            for (int i = 0; i < secondLevelSolvers.length; i++)
                secondLevelSolvers[i].process(model);
            if (!model.hasChanged())
                break;
            changed = true;
            model.reset();
        }
        return changed;
    }

    public static void solve(final PuzzleModel<?> model) {
        // First validate the model.
        for (int i = 0; i < validators.length; i++) {
            validators[i].validate(model);
        }

        // Then try to solve it.
        solveValidated(model);
    }

    static void solveValidated(final PuzzleModel<?> model) {
        boolean changed = false;
        while (true) {
            System.out.println("First level...");
            changed = solveFirstLevel(model);

            if (!model.isSolved()) {
                System.out.println("Second level...");
                if (solveSecondLevel(model))
                    changed = true;
            }

            if (model.isSolved() || !changed)
                return;

            changed = false;
        }
    }
}
