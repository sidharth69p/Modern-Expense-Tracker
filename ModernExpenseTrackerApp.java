import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;

public class ModernExpenseTrackerApp extends JFrame {
    // Colors
    private static final Color PRIMARY_COLOR = new Color(75, 0, 130);  // Deep Purple
    private static final Color ACCENT_COLOR = new Color(255, 127, 80); // Coral
    private static final Color BG_COLOR = new Color(245, 245, 250);    // Light Gray/Purple
    private static final Color TEXT_COLOR = new Color(50, 50, 50);     // Dark Gray
    private static final Color SUCCESS_COLOR = new Color(46, 204, 113); // Green
    private static final Color WARNING_COLOR = new Color(241, 196, 15); // Yellow
    private static final Color DANGER_COLOR = new Color(231, 76, 60);   // Red
    
    // Fonts
    private static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 20);
    private static final Font HEADING_FONT = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font REGULAR_FONT = new Font("Segoe UI", Font.PLAIN, 13);
    
    private JTextField descField, amountField, dateField, budgetField;
    private JComboBox<String> categoryBox;
    private JLabel totalLabel, budgetUsedLabel, titleLabel;
    private JTable table;
    private DefaultTableModel tableModel;
    private ArrayList<Expense> expenses;
    private double totalExpense = 0;
    private double budget = 0;
    private JProgressBar budgetProgressBar;
    private JPanel cardPanel;
    private CardLayout cardLayout;
    private JPanel dashboardPanel, expensePanel;
    private JButton addExpenseViewBtn, dashboardViewBtn;
    private JPanel statusPanel;
    private JLabel statusLabel;

    public ModernExpenseTrackerApp() {
        setTitle("Personal Expense Tracker");
        setSize(900, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(BG_COLOR);
        
        expenses = new ArrayList<>();
        
        // Create main components
        createNavBar();
        createCardPanel();
        createDashboardPanel();
        createExpensePanel();
        createStatusBar();
        
        // Add main panels to card layout
        cardPanel.add(dashboardPanel, "dashboard");
        cardPanel.add(expensePanel, "expense");
        cardLayout.show(cardPanel, "dashboard");
        
        // Load saved data
        loadExpensesFromFile();
        loadBudgetFromFile();
        updateTotals();
        updateCategoryChart();
        
        setVisible(true);
        setLocationRelativeTo(null);
    }
    
    private void createNavBar() {
        JPanel navBar = new JPanel(new BorderLayout());
        navBar.setBackground(PRIMARY_COLOR);
        navBar.setPreferredSize(new Dimension(getWidth(), 60));
        
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        titlePanel.setOpaque(false);
        titleLabel = new JLabel("Personal Expense Tracker");
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setForeground(Color.WHITE);
        titlePanel.add(titleLabel);
        
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonsPanel.setOpaque(false);
        
        dashboardViewBtn = createNavButton("Dashboard", "dashboard");
        addExpenseViewBtn = createNavButton("Add Expense", "expense");
        
        buttonsPanel.add(dashboardViewBtn);
        buttonsPanel.add(addExpenseViewBtn);
        
        navBar.add(titlePanel, BorderLayout.WEST);
        navBar.add(buttonsPanel, BorderLayout.EAST);
        
        add(navBar, BorderLayout.NORTH);
    }
    
    private JButton createNavButton(String text, String viewName) {
        JButton button = new JButton(text);
        button.setFont(HEADING_FONT);
        button.setForeground(Color.WHITE);
        button.setBackground(PRIMARY_COLOR);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(new Color(95, 20, 150));
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(PRIMARY_COLOR);
            }
        });
        
        button.addActionListener(e -> cardLayout.show(cardPanel, viewName));
        
        return button;
    }
    
    private void createCardPanel() {
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        cardPanel.setBackground(BG_COLOR);
        add(cardPanel, BorderLayout.CENTER);
    }
    
    private void createDashboardPanel() {
        dashboardPanel = new JPanel(new BorderLayout(10, 10));
        dashboardPanel.setBackground(BG_COLOR);
        dashboardPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Top section with summary and budget setting
        JPanel topPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        topPanel.setOpaque(false);
        
        // Budget panel
        JPanel budgetPanel = createRoundedPanel(new BorderLayout(10, 10));
        budgetPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        JLabel budgetTitle = new JLabel("Monthly Budget");
        budgetTitle.setFont(HEADING_FONT);
        budgetTitle.setForeground(TEXT_COLOR);
        
        JPanel budgetInputPanel = new JPanel(new BorderLayout(10, 0));
        budgetInputPanel.setOpaque(false);
        
        budgetField = new JTextField();
        stylizeTextField(budgetField);
        budgetField.setFont(new Font("Segoe UI", Font.BOLD, 18));
        
        JLabel currencyLabel = new JLabel("₹");
        // Use Unicode escape sequence for Rupee symbol or fallback to "Rs."
        currencyLabel.setText("Rs.");  // Safer option than using ₹ directly
        currencyLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        currencyLabel.setForeground(TEXT_COLOR);
        
        JButton setBudgetButton = new JButton("Set Budget");
        stylizeButton(setBudgetButton, ACCENT_COLOR);
        setBudgetButton.addActionListener(e -> setBudget());
        
        budgetInputPanel.add(currencyLabel, BorderLayout.WEST);
        budgetInputPanel.add(budgetField, BorderLayout.CENTER);
        budgetInputPanel.add(setBudgetButton, BorderLayout.EAST);
        
        budgetProgressBar = new JProgressBar(0, 100);
        budgetProgressBar.setStringPainted(true);
        budgetProgressBar.setFont(REGULAR_FONT);
        budgetProgressBar.setValue(0);
        budgetProgressBar.setForeground(SUCCESS_COLOR);
        budgetProgressBar.setBackground(new Color(220, 220, 220));
        budgetProgressBar.setPreferredSize(new Dimension(budgetProgressBar.getWidth(), 25));
        
        JPanel budgetLabelsPanel = new JPanel(new GridLayout(2, 1, 0, 5));
        budgetLabelsPanel.setOpaque(false);
        
        totalLabel = new JLabel("Total Spent: ₹0.00");
        totalLabel.setFont(REGULAR_FONT);
        totalLabel.setForeground(TEXT_COLOR);
        
        budgetUsedLabel = new JLabel("Budget Used: ₹0.00 / ₹0.00");
        budgetUsedLabel.setFont(REGULAR_FONT);
        budgetUsedLabel.setForeground(TEXT_COLOR);
        
        budgetLabelsPanel.add(totalLabel);
        budgetLabelsPanel.add(budgetUsedLabel);
        
        JPanel budgetStatusPanel = new JPanel(new BorderLayout(0, 10));
        budgetStatusPanel.setOpaque(false);
        budgetStatusPanel.add(budgetLabelsPanel, BorderLayout.NORTH);
        budgetStatusPanel.add(budgetProgressBar, BorderLayout.SOUTH);
        
        budgetPanel.add(budgetTitle, BorderLayout.NORTH);
        budgetPanel.add(budgetInputPanel, BorderLayout.CENTER);
        budgetPanel.add(budgetStatusPanel, BorderLayout.SOUTH);
        
        // Chart panel (placeholder for category distribution)
        JPanel chartPanel = createRoundedPanel(new BorderLayout());
        chartPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        JLabel chartTitle = new JLabel("Expense Categories");
        chartTitle.setFont(HEADING_FONT);
        chartTitle.setForeground(TEXT_COLOR);
        
        JPanel categoryChartPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawCategoryPieChart(g);
            }
        };
        categoryChartPanel.setOpaque(false);
        
        chartPanel.add(chartTitle, BorderLayout.NORTH);
        chartPanel.add(categoryChartPanel, BorderLayout.CENTER);
        
        // Add both panels to top section
        topPanel.add(budgetPanel);
        topPanel.add(chartPanel);
        
        // Expense table with heading
        JPanel tableContainer = createRoundedPanel(new BorderLayout(0, 10));
        tableContainer.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        JPanel tableHeaderPanel = new JPanel(new BorderLayout());
        tableHeaderPanel.setOpaque(false);
        
        JLabel tableTitle = new JLabel("Recent Expenses");
        tableTitle.setFont(HEADING_FONT);
        tableTitle.setForeground(TEXT_COLOR);
        
        JButton reportButton = new JButton("Generate Report");
        stylizeButton(reportButton, PRIMARY_COLOR);
        reportButton.addActionListener(e -> generateReport());
        
        tableHeaderPanel.add(tableTitle, BorderLayout.WEST);
        tableHeaderPanel.add(reportButton, BorderLayout.EAST);
        
        // Create table
        tableModel = new DefaultTableModel(new String[]{"Description", "Amount (Rs.)", "Category", "Date"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setFont(REGULAR_FONT);
        table.setRowHeight(30);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.getTableHeader().setFont(REGULAR_FONT);
        table.getTableHeader().setBackground(new Color(240, 240, 240));
        table.getTableHeader().setForeground(TEXT_COLOR);
        table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(200, 200, 200)));
        table.setSelectionBackground(new Color(230, 230, 245));
        
        // Center align amount column
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        table.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);
        
        tableContainer.add(tableHeaderPanel, BorderLayout.NORTH);
        tableContainer.add(scrollPane, BorderLayout.CENTER);
        
        // Add components to dashboard
        dashboardPanel.add(topPanel, BorderLayout.NORTH);
        dashboardPanel.add(tableContainer, BorderLayout.CENTER);
    }
    
    private void createExpensePanel() {
        expensePanel = new JPanel(new BorderLayout(0, 20));
        expensePanel.setBackground(BG_COLOR);
        expensePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JPanel formPanel = createRoundedPanel(new BorderLayout(0, 15));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel formTitle = new JLabel("Add New Expense");
        formTitle.setFont(HEADING_FONT);
        formTitle.setForeground(TEXT_COLOR);
        
        JPanel inputsPanel = new JPanel(new GridLayout(4, 2, 15, 15));
        inputsPanel.setOpaque(false);
        
        // Description field
        JPanel descPanel = new JPanel(new BorderLayout());
        descPanel.setOpaque(false);
        JLabel descLabel = new JLabel("Description");
        descLabel.setFont(REGULAR_FONT);
        descLabel.setForeground(TEXT_COLOR);
        descField = new JTextField();
        stylizeTextField(descField);
        descPanel.add(descLabel, BorderLayout.NORTH);
        descPanel.add(descField, BorderLayout.CENTER);
        
        // Amount field
        JPanel amountPanel = new JPanel(new BorderLayout());
        amountPanel.setOpaque(false);
        JLabel amountLabel = new JLabel("Amount (Rs.)");
        amountLabel.setFont(REGULAR_FONT);
        amountLabel.setForeground(TEXT_COLOR);
        amountField = new JTextField();
        stylizeTextField(amountField);
        amountPanel.add(amountLabel, BorderLayout.NORTH);
        amountPanel.add(amountField, BorderLayout.CENTER);
        
        // Category field
        JPanel categoryPanel = new JPanel(new BorderLayout());
        categoryPanel.setOpaque(false);
        JLabel categoryLabel = new JLabel("Category");
        categoryLabel.setFont(REGULAR_FONT);
        categoryLabel.setForeground(TEXT_COLOR);
        categoryBox = new JComboBox<>(new String[]{"Food", "Travel", "Bills", "Entertainment", "Shopping", "Health", "Education", "Other"});
        stylizeComboBox(categoryBox);
        categoryPanel.add(categoryLabel, BorderLayout.NORTH);
        categoryPanel.add(categoryBox, BorderLayout.CENTER);
        
        // Date field
        JPanel datePanel = new JPanel(new BorderLayout());
        datePanel.setOpaque(false);
        JLabel dateLabel = new JLabel("Date (YYYY-MM-DD)");
        dateLabel.setFont(REGULAR_FONT);
        dateLabel.setForeground(TEXT_COLOR);
        dateField = new JTextField(LocalDate.now().toString());
        stylizeTextField(dateField);
        datePanel.add(dateLabel, BorderLayout.NORTH);
        datePanel.add(dateField, BorderLayout.CENTER);
        
        inputsPanel.add(descPanel);
        inputsPanel.add(amountPanel);
        inputsPanel.add(categoryPanel);
        inputsPanel.add(datePanel);
        
        JButton addButton = new JButton("Add Expense");
        stylizeButton(addButton, ACCENT_COLOR);
        addButton.setPreferredSize(new Dimension(addButton.getWidth(), 50));
        addButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        addButton.addActionListener(e -> addExpense());
        
        JPanel buttonPanel = new JPanel(new BorderLayout());
        buttonPanel.setOpaque(false);
        buttonPanel.add(addButton, BorderLayout.CENTER);
        
        formPanel.add(formTitle, BorderLayout.NORTH);
        formPanel.add(inputsPanel, BorderLayout.CENTER);
        formPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        // Add padding
        JPanel paddingPanel = new JPanel(new BorderLayout());
        paddingPanel.setOpaque(false);
        paddingPanel.add(formPanel, BorderLayout.NORTH);
        
        expensePanel.add(paddingPanel, BorderLayout.CENTER);
    }
    
    private void createStatusBar() {
        statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBackground(new Color(240, 240, 240));
        statusPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        statusPanel.setPreferredSize(new Dimension(getWidth(), 30));
        
        statusLabel = new JLabel("Ready");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(TEXT_COLOR);
        
        statusPanel.add(statusLabel, BorderLayout.WEST);
        
        add(statusPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createRoundedPanel(LayoutManager layout) {
        JPanel panel = new JPanel(layout) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(getBackground());
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2d.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setBackground(Color.WHITE);
        return panel;
    }
    
    private void stylizeTextField(JTextField field) {
        field.setFont(REGULAR_FONT);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        field.setBackground(Color.WHITE);
        field.setForeground(TEXT_COLOR);
    }
    
    private void stylizeComboBox(JComboBox<String> comboBox) {
        comboBox.setFont(REGULAR_FONT);
        comboBox.setBackground(Color.WHITE);
        comboBox.setForeground(TEXT_COLOR);
        comboBox.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
            BorderFactory.createEmptyBorder(8, 5, 8, 5)
        ));
        
        comboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                label.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
                return label;
            }
        });
    }
    
    private void stylizeButton(JButton button, Color bgColor) {
        button.setFont(REGULAR_FONT);
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(bgColor.darker());
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(bgColor);
            }
        });
    }
    
    private void addExpense() {
        try {
            String desc = descField.getText().trim();
            if (desc.isEmpty()) {
                showStatus("Description cannot be empty", DANGER_COLOR);
                return;
            }
            
            double amount = Double.parseDouble(amountField.getText());
            if (amount <= 0) {
                showStatus("Amount must be greater than zero", DANGER_COLOR);
                return;
            }
            
            String category = categoryBox.getSelectedItem().toString();
            LocalDate date = LocalDate.parse(dateField.getText());
            
            Expense exp = new Expense(desc, amount, category, date);
            expenses.add(exp);
            tableModel.addRow(new Object[]{desc, String.format("%.2f", amount), category, date});
            
            totalExpense += amount;
            updateTotals();
            updateCategoryChart();
            saveExpensesToFile();
            
            descField.setText("");
            amountField.setText("");
            dateField.setText(LocalDate.now().toString());
            
            showStatus("Expense added successfully", SUCCESS_COLOR);
            cardLayout.show(cardPanel, "dashboard");
            
        } catch (NumberFormatException ex) {
            showStatus("Please enter a valid amount", DANGER_COLOR);
        } catch (Exception ex) {
            showStatus("Invalid input. Please check all fields", DANGER_COLOR);
        }
    }
    
    private void updateTotals() {
        totalLabel.setText("Total Spent: Rs." + String.format("%.2f", totalExpense));
        budgetUsedLabel.setText("Budget Used: Rs." + String.format("%.2f", totalExpense) + " / Rs." + String.format("%.2f", budget));
        
        if (budget > 0) {
            int percentUsed = (int) ((totalExpense / budget) * 100);
            budgetProgressBar.setValue(Math.min(percentUsed, 100));
            budgetProgressBar.setString(percentUsed + "% Used");
            
            if (percentUsed < 70) {
                budgetProgressBar.setForeground(SUCCESS_COLOR);
                budgetProgressBar.setToolTipText("Budget usage under control");
            } else if (percentUsed >= 70 && percentUsed < 90) {
                budgetProgressBar.setForeground(WARNING_COLOR);
                budgetProgressBar.setToolTipText("Warning: Budget usage over 70%!");
            } else if (percentUsed >= 90) {
                budgetProgressBar.setForeground(DANGER_COLOR);
                budgetProgressBar.setToolTipText("Critical: Budget usage over 90%!");
                
                if (totalExpense > budget && budget > 0) {
                    showStatus("Warning: You have exceeded your budget!", DANGER_COLOR);
                }
            }
        }
    }
    
    private void setBudget() {
        try {
            budget = Double.parseDouble(budgetField.getText());
            if (budget <= 0) {
                showStatus("Budget must be greater than zero", DANGER_COLOR);
                return;
            }
            
            updateTotals();
            showStatus("Budget set successfully", SUCCESS_COLOR);
            
            // Save budget to file
            try (PrintWriter writer = new PrintWriter("budget.txt")) {
                writer.println(budget);
            }
            
        } catch (NumberFormatException e) {
            showStatus("Please enter a valid budget amount", DANGER_COLOR);
        } catch (Exception e) {
            showStatus("Failed to set budget", DANGER_COLOR);
        }
    }
    
    private void generateReport() {
        try {
            // Get current date for filename
            String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String fileName = "expense_report_" + dateStr + ".txt";
            
            try (PrintWriter writer = new PrintWriter(fileName)) {
                writer.println("===== EXPENSE REPORT =====");
                writer.println("Generated on: " + LocalDate.now());
                writer.println("---------------------------");
                writer.println();
                writer.println("SUMMARY");
                writer.println("---------------------------");
                writer.println("Total Spent: Rs." + String.format("%.2f", totalExpense));
                writer.println("Budget: Rs." + String.format("%.2f", budget));
                writer.println("Remaining: Rs." + String.format("%.2f", budget - totalExpense));
                writer.println();
                
                HashMap<String, Double> categoryTotals = new HashMap<>();
                for (Expense exp : expenses) {
                    categoryTotals.put(exp.getCategory(), categoryTotals.getOrDefault(exp.getCategory(), 0.0) + exp.getAmount());
                }
                
                writer.println("CATEGORY BREAKDOWN");
                writer.println("---------------------------");
                for (String cat : categoryTotals.keySet()) {
                    double amount = categoryTotals.get(cat);
                    double percentage = (amount / totalExpense) * 100;
                    writer.println(cat + ": Rs." + String.format("%.2f", amount) + 
                                  " (" + String.format("%.1f", percentage) + "%)");
                }
                writer.println();
                
                writer.println("EXPENSE DETAILS");
                writer.println("---------------------------");
                for (Expense exp : expenses) {
                    writer.println(exp.getDate() + " | " + 
                                  exp.getCategory() + " | Rs." + 
                                  String.format("%.2f", exp.getAmount()) + " | " + 
                                  exp.getDescription());
                }
            }
            
            showStatus("Report generated as '" + fileName + "'", SUCCESS_COLOR);
        } catch (IOException e) {
            showStatus("Failed to generate report", DANGER_COLOR);
        }
    }
    
    private void saveExpensesToFile() {
        try (PrintWriter writer = new PrintWriter("expenses.csv")) {
            for (Expense exp : expenses) {
                writer.println(exp.getDescription() + "," + exp.getAmount() + "," + exp.getCategory() + "," + exp.getDate());
            }
            showStatus("Expenses saved successfully", SUCCESS_COLOR);
        } catch (IOException e) {
            showStatus("Failed to save expenses", DANGER_COLOR);
        }
    }
    
    private void loadExpensesFromFile() {
        File file = new File("expenses.csv");
        if (!file.exists()) return;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 4) {
                    Expense exp = new Expense(parts[0], Double.parseDouble(parts[1]), parts[2], LocalDate.parse(parts[3]));
                    expenses.add(exp);
                    tableModel.addRow(new Object[]{parts[0], String.format("%.2f", exp.getAmount()), parts[2], parts[3]});
                    totalExpense += exp.getAmount();
                }
            }
            showStatus("Previous expenses loaded", SUCCESS_COLOR);
        } catch (IOException e) {
            showStatus("Failed to load previous expenses", DANGER_COLOR);
        }
    }
    
    private void loadBudgetFromFile() {
        File file = new File("budget.txt");
        if (!file.exists()) return;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine();
            if (line != null) {
                budget = Double.parseDouble(line);
                budgetField.setText(String.format("%.2f", budget));
                showStatus("Budget loaded successfully", SUCCESS_COLOR);
            }
        } catch (IOException | NumberFormatException e) {
            showStatus("Failed to load budget", DANGER_COLOR);
        }
    }
    
    private void showStatus(String message, Color color) {
        statusLabel.setText(message);
        statusLabel.setForeground(color);
        
        // Reset the status color after a delay
        new Timer(3000, e -> {
            statusLabel.setText("Ready");
            statusLabel.setForeground(TEXT_COLOR);
        }).start();
    }
    
    private void updateCategoryChart() {
        // This will trigger a repaint of the chart panel
        SwingUtilities.invokeLater(() -> dashboardPanel.repaint());
    }
    
    private void drawCategoryPieChart(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        int width = g.getClipBounds().width;
        int height = g.getClipBounds().height;
        
        // Calculate center and radius
        int centerX = width / 2;
        int centerY = height / 2;
        int radius = Math.min(width, height) / 2 - 30;
        
        // Create category map
        HashMap<String, Double> categoryMap = new HashMap<>();
        
        // Return if no expenses
        if (expenses.isEmpty() || totalExpense == 0) {
            g2d.setColor(new Color(220, 220, 220));
            g2d.fillOval(centerX - radius, centerY - radius, radius * 2, radius * 2);
            
            g2d.setColor(TEXT_COLOR);
            g2d.setFont(new Font("Segoe UI", Font.BOLD, 14));
            String noDataMsg = "No expense data";
            FontMetrics fm = g2d.getFontMetrics();
            int msgWidth = fm.stringWidth(noDataMsg);
            g2d.drawString(noDataMsg, centerX - msgWidth / 2, centerY + 5);
            return;
        }
        
        // Calculate category totals
        for (Expense exp : expenses) {
            String category = exp.getCategory();
            double amount = exp.getAmount();
            categoryMap.put(category, categoryMap.getOrDefault(category, 0.0) + amount);
        }
        
        // Define colors for categories
        Color[] colors = {
            new Color(52, 152, 219),  // Blue
            new Color(155, 89, 182),  // Purple
            new Color(52, 73, 94),    // Dark Blue
            new Color(231, 76, 60),   // Red
            new Color(241, 196, 15),  // Yellow
            new Color(46, 204, 113),  // Green
            new Color(230, 126, 34),  // Orange
            new Color(149, 165, 166)  // Gray
        };
        
        // Draw pie chart
        int colorIndex = 0;
        int startAngle = 0;
        
        // Create a map to store legends
        HashMap<String, Color> legendMap = new HashMap<>();
        
        for (String category : categoryMap.keySet()) {
            // Get category amount and calculate percentage
            double amount = categoryMap.get(category);
            double percentage = (amount / totalExpense) * 100;
            int arcAngle = (int) Math.round(360 * (amount / totalExpense));
            
            // Set color (cycle through the color array)
            Color color = colors[colorIndex % colors.length];
            legendMap.put(category, color);
            
            // Draw arc
            g2d.setColor(color);
            g2d.fillArc(centerX - radius, centerY - radius, radius * 2, radius * 2, startAngle, arcAngle);
            
            // Move to next position and color
            startAngle += arcAngle;
            colorIndex++;
        }
        
        // Draw legends
        int legendY = 20;
        int legendX = width - 120;
        int boxSize = 15;
        
        g2d.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        FontMetrics fm = g2d.getFontMetrics();
        
        for (String category : legendMap.keySet()) {
            double amount = categoryMap.get(category);
            double percentage = (amount / totalExpense) * 100;
            
            // Only show in legend if significant (>1%)
            if (percentage > 1.0) {
                Color color = legendMap.get(category);
                
                // Draw color box
                g2d.setColor(color);
                g2d.fillRect(legendX, legendY, boxSize, boxSize);
                
                // Draw category name and percentage
                g2d.setColor(TEXT_COLOR);
                String text = category + " (" + String.format("%.0f", percentage) + "%)";
                g2d.drawString(text, legendX + boxSize + 5, legendY + boxSize - 2);
                
                legendY += boxSize + 10;
            }
        }
    }
    
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        SwingUtilities.invokeLater(() -> new ModernExpenseTrackerApp());
    }
}

class Expense {
    private String description;
    private double amount;
    private String category;
    private LocalDate date;

    public Expense(String description, double amount, String category, LocalDate date) {
        this.description = description;
        this.amount = amount;
        this.category = category;
        this.date = date;
    }

    public String getDescription() { return description; }
    public double getAmount() { return amount; }
    public String getCategory() { return category; }
    public LocalDate getDate() { return date; }
}