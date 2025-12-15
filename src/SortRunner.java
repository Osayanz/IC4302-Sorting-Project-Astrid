public class SortRunner {
    public static class Result {
        public final String name;
        public final double timeMs;

        public Result(String name, double timeMs) {
            this.name = name;
            this.timeMs = timeMs;
        }
    }

    public static Result runAndTime(String name, double[] data, RunnableSort sorter) {
        double[] copy = SortingAlgorithms.copy(data);
        long t0 = System.nanoTime();
        sorter.sort(copy);
        long t1 = System.nanoTime();
        double ms = (t1 - t0) / 1_000_000.0;
        return new Result(name, ms);
    }

    @FunctionalInterface
    public interface RunnableSort {
        void sort(double[] arr);
    }
}
