import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

public class StudentGradeTrackerGUI extends JFrame {
    private JTextField nameField, gradeField;
    private DefaultTableModel tableModel;
    private JLabel avgLabel, highLabel, lowLabel;
    private ArrayList<Integer> grades = new ArrayList<>();
    private ArrayList<String> names = new ArrayList<>();
    private final String FILE_NAME = "students.txt"; // save file

    public StudentGradeTrackerGUI() {
        setTitle("🎓 Student Grade Tracker");
        setSize(800, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // ==== Input Panel ====
        JPanel inputPanel = new JPanel(new FlowLayout());
        inputPanel.setBackground(new Color(200, 230, 250));

        nameField = new JTextField(10);
        gradeField = new JTextField(5);
        JButton addBtn = new JButton("➕ Add Student");
        addBtn.setBackground(new Color(100, 180, 100));
        addBtn.setForeground(Color.WHITE);

        inputPanel.add(new JLabel("Name:"));
        inputPanel.add(nameField);
        inputPanel.add(new JLabel("Grade:"));
        inputPanel.add(gradeField);
        inputPanel.add(addBtn);

        // ==== Table ====
        String[] columns = {"Student Name", "Grade", "Category"};
        tableModel = new DefaultTableModel(columns, 0);
        JTable table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);

        // ==== Stats Panel ====
        JPanel statsPanel = new JPanel(new GridLayout(1, 3));
        statsPanel.setBackground(new Color(245, 245, 245));
        avgLabel = new JLabel("Average: N/A", SwingConstants.CENTER);
        highLabel = new JLabel("Highest: N/A", SwingConstants.CENTER);
        lowLabel = new JLabel("Lowest: N/A", SwingConstants.CENTER);

        statsPanel.add(avgLabel);
        statsPanel.add(highLabel);
        statsPanel.add(lowLabel);

        // ==== Chart Panel ====
        JPanel chartPanel = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (grades.isEmpty()) return;

                int width = getWidth();
                int height = getHeight();
                int barWidth = width / grades.size();

                int maxGrade = 100;
                for (int i = 0; i < grades.size(); i++) {
                    int grade = grades.get(i);
                    int barHeight = (int) ((grade / (double) maxGrade) * (height - 50));

                    // Set bar color by category
                    if (grade >= 80) g.setColor(new Color(46, 204, 113)); // Green (A)
                    else if (grade >= 60) g.setColor(new Color(52, 152, 219)); // Blue (B)
                    else if (grade >= 40) g.setColor(new Color(243, 156, 18)); // Orange (C)
                    else g.setColor(new Color(231, 76, 60)); // Red (Fail)

                    g.fillRect(i * barWidth + 10, height - barHeight - 30, barWidth - 20, barHeight);

                    g.setColor(Color.BLACK);
                    g.drawString(names.get(i), i * barWidth + 15, height - 10);
                    g.drawString(String.valueOf(grade), i * barWidth + 15, height - barHeight - 35);
                }
            }
        };
        chartPanel.setPreferredSize(new Dimension(350, 250));
        chartPanel.setBackground(new Color(230, 240, 255));

        // ==== Add Student Action ====
        addBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            String gradeText = gradeField.getText().trim();

            if (name.isEmpty() || gradeText.isEmpty()) {
                JOptionPane.showMessageDialog(this, "⚠️ Enter both name and grade!");
                return;
            }

            try {
                int grade = Integer.parseInt(gradeText);
                if (grade < 0 || grade > 100) {
                    JOptionPane.showMessageDialog(this, "⚠️ Grade must be 0-100!");
                    return;
                }

                // Determine category
                String category;
                if (grade >= 80) category = "A";
                else if (grade >= 60) category = "B";
                else if (grade >= 40) category = "C";
                else category = "Fail";

                names.add(name);
                grades.add(grade);
                tableModel.addRow(new Object[]{name, grade, category});

                updateStats();
                chartPanel.repaint();
                saveToFile(); // save every time a new student is added

                nameField.setText("");
                gradeField.setText("");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "❌ Grade must be a number!");
            }
        });

        // ==== Load previous data on startup ====
        loadFromFile();

        // ==== Layout ====
        add(inputPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(statsPanel, BorderLayout.SOUTH);
        add(chartPanel, BorderLayout.EAST);
    }

    private void updateStats() {
        if (grades.isEmpty()) return;

        int total = 0, highest = grades.get(0), lowest = grades.get(0);
        for (int g : grades) {
            total += g;
            if (g > highest) highest = g;
            if (g < lowest) lowest = g;
        }
        double average = (double) total / grades.size();

        avgLabel.setText("Average: " + String.format("%.2f", average));
        highLabel.setText("Highest: " + highest);
        lowLabel.setText("Lowest: " + lowest);
    }

    private void saveToFile() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(FILE_NAME))) {
            for (int i = 0; i < names.size(); i++) {
                writer.println(names.get(i) + "," + grades.get(i));
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "❌ Error saving file: " + e.getMessage());
        }
    }

    private void loadFromFile() {
        File file = new File(FILE_NAME);
        if (!file.exists()) return;

        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String[] parts = scanner.nextLine().split(",");
                if (parts.length == 2) {
                    String name = parts[0];
                    int grade = Integer.parseInt(parts[1]);

                    String category;
                    if (grade >= 80) category = "A";
                    else if (grade >= 60) category = "B";
                    else if (grade >= 40) category = "C";
                    else category = "Fail";

                    names.add(name);
                    grades.add(grade);
                    tableModel.addRow(new Object[]{name, grade, category});
                }
            }
            updateStats();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "❌ Error loading file: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new StudentGradeTrackerGUI().setVisible(true);
        });
    }
}
