import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;

// 1. DATA MODEL
class Person {
    int id;
    String firstName;
    String lastName;

    public Person(int id, String firstName, String lastName) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    @Override
    public String toString() {
        return String.format("%-10d | %-15s | %-15s", id, firstName, lastName);
    }
}

// 2. MAIN APPLICATION
public class SortingAlgorithm extends JFrame {
    private JTextArea textArea;
    private JLabel statusLabel;
    private JScrollPane scrollPane;
    private JComboBox<String> algoBox, columnBox;
    private JTextField nInput;
    
    private List<Person> originalData = new ArrayList<>();
    private List<Person> currentData = new ArrayList<>();

    public SortingAlgorithm() {
        // Updated Title to PrelimExam
        setTitle("PrelimExam");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 800);
        setLayout(new BorderLayout());

        // --- CENTERED CONTROL PANEL ---
        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));
        topContainer.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));

        // Large Buttons (Matching your previous request)
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        
        JButton loadButton = new JButton("Load CSV File");
        JButton runButton = new JButton("Run Sort");
        JButton resetButton = new JButton("Reset Data");
        JButton clearButton = new JButton("Clear Screen");

        Dimension btnSize = new Dimension(170, 50);
        Font btnFont = new Font("Arial", Font.BOLD, 14);
        for (JButton b : new JButton[]{loadButton, runButton, resetButton, clearButton}) {
            b.setPreferredSize(btnSize);
            b.setFont(btnFont);
        }

        buttonPanel.add(loadButton);
        buttonPanel.add(runButton);
        buttonPanel.add(resetButton);
        buttonPanel.add(clearButton);

        // Parameters Row
        JPanel paramPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        algoBox = new JComboBox<>(new String[]{"Bubble Sort", "Insertion Sort", "Merge Sort"});
        columnBox = new JComboBox<>(new String[]{"ID", "FirstName", "LastName"});
        nInput = new JTextField("1000", 8);

        paramPanel.add(new JLabel("Algorithm:")); paramPanel.add(algoBox);
        paramPanel.add(new JLabel("Sort By:")); paramPanel.add(columnBox);
        paramPanel.add(new JLabel("N Rows:")); paramPanel.add(nInput);

        topContainer.add(buttonPanel);
        topContainer.add(paramPanel);

        textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        scrollPane = new JScrollPane(textArea);

        statusLabel = new JLabel("No file loaded");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));

        add(topContainer, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);

        loadButton.addActionListener(e -> loadFile());
        runButton.addActionListener(e -> runSortWithLoading());
        resetButton.addActionListener(e -> resetData());
        clearButton.addActionListener(e -> clearScreen());
    }

    private void loadFile() {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            readCSV(fileChooser.getSelectedFile());
        }
    }

    private void readCSV(File file) {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            originalData.clear();
            br.readLine(); 
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split(",");
                if (p.length >= 3) {
                    originalData.add(new Person(Integer.parseInt(p[0].trim()), p[1].trim(), p[2].trim()));
                }
            }
            currentData = new ArrayList<>(originalData);
            textArea.setText("");
            appendToBottom("=== Reading file: " + file.getName() + " ===\n");
            appendToBottom("Total records loaded: " + originalData.size() + "\n\n");
            statusLabel.setText("Loaded " + originalData.size() + " records");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void runSortWithLoading() {
        if (currentData.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please load data first!");
            return;
        }

        int n;
        try {
            n = Math.min(currentData.size(), Integer.parseInt(nInput.getText()));
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid N value.");
            return;
        }

        String algo = (String) algoBox.getSelectedItem();
        String col = (String) columnBox.getSelectedItem();
        List<Person> toSort = new ArrayList<>(currentData.subList(0, n));

        // Loading Screen
        JDialog loadingDialog = new JDialog(this, "Processing", true);
        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        loadingDialog.setLayout(new BorderLayout());
        loadingDialog.add(new JLabel("  Sorting data, please wait...  "), BorderLayout.NORTH);
        loadingDialog.add(progressBar, BorderLayout.CENTER);
        loadingDialog.setSize(300, 100);
        loadingDialog.setLocationRelativeTo(this);

        SwingWorker<Double, Void> worker = new SwingWorker<>() {
            @Override
            protected Double doInBackground() {
                long start = System.nanoTime();
                if (algo.equals("Bubble Sort")) bubbleSort(toSort, col);
                else if (algo.equals("Insertion Sort")) insertionSort(toSort, col);
                else mergeSort(toSort, col);
                long end = System.nanoTime();
                return (end - start) / 1_000_000_000.0;
            }

            @Override
            protected void done() {
                try {
                    double time = get();
                    loadingDialog.dispose(); 
                    
                    appendToBottom("=== Starting " + algo + " ===\n");
                    for (Person p : toSort) appendToBottom(p.toString() + "\n");
                    appendToBottom("\n=== Results ===\n");
                    appendToBottom(String.format("Time taken: %.6f seconds\n\n", time));
                    statusLabel.setText(algo + " finished in " + String.format("%.4f", time) + "s");
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        };

        worker.execute();
        loadingDialog.setVisible(true); 
    }

    private void resetData() {
        if (!originalData.isEmpty()) {
            currentData = new ArrayList<>(originalData);
            appendToBottom("\n=== Data Reset ===\nRestored original order.\n");
            statusLabel.setText("Data reset to original");
        }
    }

    private void clearScreen() {
        textArea.setText("");
        appendToBottom("=== Screen Cleared ===\n");
        statusLabel.setText("Screen cleared");
    }

    // --- SORTING METHODS ---
    private int compare(Person p1, Person p2, String col) {
        if (col.equals("ID")) return Integer.compare(p1.id, p2.id);
        if (col.equals("FirstName")) return p1.firstName.compareToIgnoreCase(p2.firstName);
        return p1.lastName.compareToIgnoreCase(p2.lastName);
    }

    private void bubbleSort(List<Person> list, String col) {
        for (int i = 0; i < list.size() - 1; i++) {
            for (int j = 0; j < list.size() - i - 1; j++) {
                if (compare(list.get(j), list.get(j + 1), col) > 0) Collections.swap(list, j, j + 1);
            }
        }
    }

    private void insertionSort(List<Person> list, String col) {
        for (int i = 1; i < list.size(); i++) {
            Person key = list.get(i);
            int j = i - 1;
            while (j >= 0 && compare(list.get(j), key, col) > 0) {
                list.set(j + 1, list.get(j));
                j--;
            }
            list.set(j + 1, key);
        }
    }

    private void mergeSort(List<Person> list, String col) {
        if (list.size() <= 1) return;
        int mid = list.size() / 2;
        List<Person> left = new ArrayList<>(list.subList(0, mid));
        List<Person> right = new ArrayList<>(list.subList(mid, list.size()));
        mergeSort(left, col); mergeSort(right, col);
        int i = 0, j = 0, k = 0;
        while (i < left.size() && j < right.size()) {
            if (compare(left.get(i), right.get(j), col) <= 0) list.set(k++, left.get(i++));
            else list.set(k++, right.get(j++));
        }
        while (i < left.size()) list.set(k++, left.get(i++));
        while (j < right.size()) list.set(k++, right.get(j++));
    }

    private void appendToBottom(String text) {
        textArea.append(text);
        SwingUtilities.invokeLater(() -> {
            JScrollBar v = scrollPane.getVerticalScrollBar();
            v.setValue(v.getMaximum());
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SortingAlgorithm().setVisible(true));
    }
}