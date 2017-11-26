package lohbihler.sudoku;

/**
 * @author mlohbihler
 */
public class SolverFactory {
    public static Solver[] firstLevelSolvers() {
        return new Solver[] { new BoxEliminator(), new HorizontalEliminator(), new VerticalEliminator(),
                new BoxSingleValueSeeker(), new HorizontalSingleValueSeeker(), new VerticalSingleValueSeeker(), };
    }

    public static Solver[] secondLevelSolvers() {
        return new Solver[] { new PotentialValueElimination(), };
    }

    public static boolean solveFirstLevel(final PuzzleModel<?> model) {
        boolean changed = false;
        model.reset();
        final Solver[] solvers = firstLevelSolvers();
        while (true) {
            for (int i = 0; i < solvers.length; i++)
                solvers[i].process(model);
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
        final Solver[] solvers = secondLevelSolvers();
        while (true) {
            for (int i = 0; i < solvers.length; i++)
                solvers[i].process(model);
            if (!model.hasChanged())
                break;
            changed = true;
            model.reset();
        }
        return changed;
    }

    public static void solve(final PuzzleModel<?> model) {
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
