package lohbihler.sudoku;

import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class XYStreams {
    private XYStreams() {
        // No instances for you.
    }

    public static <T> Stream<T> toStream(final T[][] ta) {
        final Spliterator<T> spliterator = new Spliterator<T>() {
            int x = 0;
            int y = 0;
            long size = -1;

            @Override
            public int characteristics() {
                return 0;
            }

            @Override
            public Spliterator<T> trySplit() {
                return null;
            }

            @Override
            public boolean tryAdvance(Consumer<? super T> action) {
                if (x >= ta.length)
                    return false;
                if (y >= ta[x].length) {
                    x++;
                    y = 0;
                    if (x >= ta.length)
                        return false;
                }

                action.accept(ta[x][y]);

                y++;
                return true;
            }

            @Override
            public long estimateSize() {
                if (size == -1) {
                    size = 0;
                    for (int i = 0; i < ta.length; i++)
                        size += ta[i].length;
                }
                return size;
            }
        };

        return StreamSupport.stream(new Supplier<Spliterator<T>>() {
            @Override
            public Spliterator<T> get() {
                return spliterator;
            }
        }, 0, false);
    }

    public static <T> void traverse(T[][] ta, XYConsumer consumer) {
        for (int y = 0; y < ta.length; y++) {
            for (int x = 0; x < ta[y].length; x++) {
                consumer.apply(x, y);
            }
        }
    }

    @FunctionalInterface
    public static interface XYConsumer {
        void apply(int x, int y);
    }
}
