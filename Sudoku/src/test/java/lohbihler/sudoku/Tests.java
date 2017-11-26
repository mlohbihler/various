package lohbihler.sudoku;

import java.util.Spliterator.OfInt;
import java.util.function.IntConsumer;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

import org.junit.Test;

public class Tests {
    @Test
    public void objStreamTest() {
        final Integer[][] ia = { //
                { 11, 12, 13 }, //
                { 14, 15, 16 }, //
                { 17, 18, 19, 110 }, //
                { 111, 112 }, //
        };

        XYStreams.toStream(ia).forEach(i -> System.out.println(i));
    }

    //    @Test
    public void intStreamTest() {
        final int[][] ia = { //
                { 1, 2, 3 }, //
                { 4, 5, 6 }, //
                { 7, 8, 9, 10 }, //
                { 11, 12 }, //
        };

        toIntStream(ia).forEach(i -> System.out.println(i));
    }

    private static IntStream toIntStream(final int[][] ia) {
        final OfInt ofInt = new OfInt() {
            int x = 0;
            int y = 0;

            @Override
            public int characteristics() {
                return 0;
            }

            @Override
            public OfInt trySplit() {
                return null;
            }

            @Override
            public boolean tryAdvance(final IntConsumer action) {
                if (x >= ia.length)
                    return false;
                if (y >= ia[x].length) {
                    x++;
                    y = 0;
                    if (x >= ia.length)
                        return false;
                }

                action.accept(ia[x][y]);
                y++;
                return true;
            }

            @Override
            public long estimateSize() {
                long size = 0;
                for (int i = 0; i < ia.length; i++)
                    size += ia[i].length;
                return size;
            }
        };

        return StreamSupport.intStream(new Supplier<OfInt>() {
            @Override
            public OfInt get() {
                return ofInt;
            }
        }, 0, false);
    }
}
