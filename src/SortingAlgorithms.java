public class SortingAlgorithms {

    public static void insertionSort(double[] arr) {
        for (int i = 1; i < arr.length; i++) {
            double key = arr[i];
            int j = i - 1;
            while (j >= 0 && arr[j] > key) {
                arr[j + 1] = arr[j];
                j--;
            }
            arr[j + 1] = key;
        }
    }

    public static void shellSort(double[] arr) {
        int n = arr.length;
        for (int gap = n / 2; gap > 0; gap /= 2) {
            for (int i = gap; i < n; i++) {
                double temp = arr[i];
                int j = i;
                while (j >= gap && arr[j - gap] > temp) {
                    arr[j] = arr[j - gap];
                    j -= gap;
                }
                arr[j] = temp;
            }
        }
    }

    public static void mergeSort(double[] arr) {
        if (arr.length <= 1)
            return;
        mergeSortHelper(arr, 0, arr.length - 1, new double[arr.length]);
    }

    private static void mergeSortHelper(double[] arr, int l, int r, double[] aux) {
        if (l >= r)
            return;
        int m = (l + r) / 2;
        mergeSortHelper(arr, l, m, aux);
        mergeSortHelper(arr, m + 1, r, aux);
        merge(arr, l, m, r, aux);
    }

    private static void merge(double[] arr, int l, int m, int r, double[] aux) {
        int i = l, j = m + 1, k = l;
        while (i <= m && j <= r)
            aux[k++] = (arr[i] <= arr[j]) ? arr[i++] : arr[j++];
        while (i <= m)
            aux[k++] = arr[i++];
        while (j <= r)
            aux[k++] = arr[j++];
        for (k = l; k <= r; k++)
            arr[k] = aux[k];
    }

    public static void quickSort(double[] arr) {
        quickSortHelper(arr, 0, arr.length - 1);
    }

    private static void quickSortHelper(double[] a, int lo, int hi) {
        if (lo >= hi)
            return;
        int p = partition(a, lo, hi);
        quickSortHelper(a, lo, p - 1);
        quickSortHelper(a, p + 1, hi);
    }

    private static int partition(double[] a, int lo, int hi) {
        double pivot = a[hi];
        int i = lo;
        for (int j = lo; j < hi; j++) {
            if (a[j] <= pivot) {
                double tmp = a[i];
                a[i] = a[j];
                a[j] = tmp;
                i++;
            }
        }
        double tmp = a[i];
        a[i] = a[hi];
        a[hi] = tmp;
        return i;
    }

}
