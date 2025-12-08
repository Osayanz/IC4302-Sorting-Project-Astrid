import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;

public class SortAppGUI {

    private JFrame frame;

    private JButton btnUpload, btnSort;
    private JComboBox<String> comboColumns;

    private JTable table;
    private DefaultTableModel tableModel;

    private JTextArea txtTimings;
    private JLabel lblStatus;

    private File currentFile;

    private String[] headers;
    private List<String[]> rows = new ArrayList<>();

    public SortAppGUI() {
        frame = new JFrame("Sorting Performance Evaluator - Astrid");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1100, 650);
        frame.setLayout(new BorderLayout(10, 10));

        buildTopPanel();
        buildTablePanel();
        buildRightPanel();
        buildStatusBar();

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    // --------------------- TOP PANEL -----------------------
    private void buildTopPanel() {
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));

        btnUpload = new JButton("Upload CSV");
        comboColumns = new JComboBox<>();
        btnSort = new JButton("Run Sorting");

        comboColumns.setPreferredSize(new Dimension(160, 28));
        btnSort.setEnabled(false);

        top.add(btnUpload);
        top.add(new JLabel("Column:"));
        top.add(comboColumns);
        top.add(btnSort);

        btnUpload.addActionListener(e -> doUpload());
        btnSort.addActionListener(e -> doSort());

        frame.add(top, BorderLayout.NORTH);
    }

    // --------------------- TABLE PANEL -----------------------
    private void buildTablePanel() {
        tableModel = new DefaultTableModel();
        table = new JTable(tableModel);

        JScrollPane tableScroll = new JScrollPane(table);
        frame.add(tableScroll, BorderLayout.CENTER);
    }

    // --------------------- RIGHT PANEL (TIMINGS) -----------------------
    private void buildRightPanel() {
        JPanel right = new JPanel(new BorderLayout());
        right.setPreferredSize(new Dimension(300, 0));

        JLabel label = new JLabel("Execution Times (ms)");
        label.setFont(new Font("Arial", Font.BOLD, 14));
        txtTimings = new JTextArea();
        txtTimings.setFont(new Font("Monospaced", Font.PLAIN, 13));
        txtTimings.setEditable(false);

        right.add(label, BorderLayout.NORTH);
        right.add(new JScrollPane(txtTimings), BorderLayout.CENTER);

        frame.add(right, BorderLayout.EAST);
    }

    // --------------------- STATUS BAR -----------------------
    private void buildStatusBar() {
        lblStatus = new JLabel("Ready.");
        lblStatus.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 0));
        frame.add(lblStatus, BorderLayout.SOUTH);
    }

    // --------------------- UPLOAD CSV -----------------------
    private void doUpload() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("CSV Files", "csv"));

        if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
            currentFile = chooser.getSelectedFile();

            try {
                List<String> headerList = CsvLoader.getHeaders(currentFile);
                if (headerList == null || headerList.isEmpty()) {
                    showError("Invalid or empty CSV file.");
                    return;
                }

                comboColumns.removeAllItems();
                headers = headerList.toArray(new String[0]);
                for (String h : headers) comboColumns.addItem(h);

                rows = CsvLoader.loadAllRows(currentFile);

                refreshTable();

                btnSort.setEnabled(true);
                lblStatus.setText("Loaded: " + currentFile.getName());

            } catch (Exception ex) {
                showError("Error reading CSV: " + ex.getMessage());
            }
        }
    }

    // --------------------- REFRESH TABLE -----------------------
    private void refreshTable() {
        tableModel.setRowCount(0);
        tableModel.setColumnCount(0);

        for (String h : headers) tableModel.addColumn(h);
        for (String[] row : rows) tableModel.addRow(row);
    }

    // --------------------- RUN SORTING -----------------------
    private void doSort() {
        if (currentFile == null) {
            showError("Upload a CSV first.");
            return;
        }

        int col = comboColumns.getSelectedIndex();
        if (col < 0) {
            showError("Choose a column.");
            return;
        }

        double[] arr;

        try {
            arr = CsvLoader.loadNumericColumn(currentFile, col);
        } catch (NumberFormatException e) {
            showError("Non-numeric values found in selected column.");
            return;
        } catch (Exception ex) {
            showError("CSV Error: " + ex.getMessage());
            return;
        }

        btnSort.setEnabled(false);
        btnUpload.setEnabled(false);
        lblStatus.setText("Sorting...");

        runSortingWorker(arr);
    }

    // --------------------- SWINGWORKER FOR SORTING -----------------------
    private void runSortingWorker(double[] arr) {
        SwingWorker<List<SortRunner.Result>, Void> worker = new SwingWorker<>() {

            @Override
            protected List<SortRunner.Result> doInBackground() {
                List<SortRunner.Result> results = new ArrayList<>();

                results.add(SortRunner.runAndTime("Insertion", arr, SortingAlgorithms::insertionSort));
                results.add(SortRunner.runAndTime("Shell", arr, SortingAlgorithms::shellSort));
                results.add(SortRunner.runAndTime("Merge", arr, SortingAlgorithms::mergeSort));
                results.add(SortRunner.runAndTime("Quick", arr, SortingAlgorithms::quickSort));
                results.add(SortRunner.runAndTime("Heap", arr, SortingAlgorithms::heapSort));

                return results;
            }

            @Override
            protected void done() {
                try {
                    List<SortRunner.Result> results = get();
                    showTimings(results);

                    SortRunner.Result best = results.stream()
                            .min(Comparator.comparingDouble(r -> r.timeMs))
                            .orElse(null);

                    lblStatus.setText("Fastest: " + best.name + " (" + String.format("%.2f ms", best.timeMs) + ")");

                } catch (Exception ex) {
                    showError("Sorting failed: " + ex.getMessage());
                }

                btnSort.setEnabled(true);
                btnUpload.setEnabled(true);
            }
        };

        worker.execute();
    }

    // --------------------- SHOW TIMINGS -----------------------
    private void showTimings(List<SortRunner.Result> results) {
        StringBuilder sb = new StringBuilder();
        sb.append("Column: ").append(comboColumns.getSelectedItem()).append("\n\n");

        results.forEach(r -> sb.append(String.format("%-12s : %8.3f ms\n", r.name, r.timeMs)));

        // Highlight fastest
        SortRunner.Result best = results.stream().min(Comparator.comparingDouble(r -> r.timeMs)).get();
        sb.append("\nFastest: ").append(best.name);

        txtTimings.setText(sb.toString());
    }

    // --------------------- ERROR POPUP -----------------------
    private void showError(String msg) {
        JOptionPane.showMessageDialog(frame, msg, "Error", JOptionPane.ERROR_MESSAGE);
        lblStatus.setText("Error: " + msg);
    }

    // --------------------- MAIN -----------------------
    public static void main(String[] args) {
        SwingUtilities.invokeLater(SortAppGUI::new);
    }
}