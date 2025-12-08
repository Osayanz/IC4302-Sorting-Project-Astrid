import java.io.*;
import java.util.*;

public class CsvLoader {
    // Return header names (first line split by comma)
    public static List<String> getHeaders(File csvFile) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            String headerLine = br.readLine();
            if (headerLine == null)
                throw new IOException("Empty file");
            String[] headers = headerLine.split(",");
            List<String> out = new ArrayList<>();
            for (String h : headers)
                out.add(h.trim());
            return out;
        }
    }

    // Load all rows including all columns (except header)
    public static List<String[]> loadAllRows(File csvFile) throws IOException {
        List<String[]> rows = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            br.readLine(); // skip header
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",", -1);
                rows.add(parts);
            }
        }
        return rows;
    }
}
