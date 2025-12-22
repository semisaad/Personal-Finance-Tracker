import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.*;
import com.google.auth.oauth2.GoogleCredentials;
import java.io.FileInputStream;

public class PersonalFinanceTracker extends JFrame {
    private DefaultTableModel transactionModel;
    private JTable transactionTable;
    private JTextField amountField, descriptionField;
    private JComboBox<String> categoryCombo, typeCombo;
    private JLabel balanceLabel, incomeLabel, expenseLabel;
    private List<Transaction> transactions;
    private DatabaseReference database;
    
    public PersonalFinanceTracker() {
        transactions = new ArrayList<>();
        initializeFirebase();
        initComponents();
        loadTransactionsFromFirebase();
        setTitle("Personal Finance Tracker");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }
    
    private void initializeFirebase() {
        try {
            // IMPORTANT: Replace with your Firebase JSON file path
            FileInputStream serviceAccount = new FileInputStream("path/to/your/firebase-key.json");
            
            FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .setDatabaseUrl("https://your-project-id.firebaseio.com") // Replace with your URL
                .build();
            
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
            }
            
            database = FirebaseDatabase.getInstance().getReference("transactions");
            
            JOptionPane.showMessageDialog(this, "Connected to Firebase successfully!", 
                "Success", JOptionPane.INFORMATION_MESSAGE);
                
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Failed to connect to Firebase: " + e.getMessage(), 
                "Firebase Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    private void loadTransactionsFromFirebase() {
        if (database == null) return;
        
        database.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                transactions.clear();
                transactionModel.setRowCount(0);
                
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Transaction transaction = snapshot.getValue(Transaction.class);
                    if (transaction != null) {
                        transaction.firebaseKey = snapshot.getKey();
                        transactions.add(transaction);
                        addTransactionToTable(transaction);
                    }
                }
                updateSummary();
            }
            
            @Override
            public void onCancelled(DatabaseError error) {
                JOptionPane.showMessageDialog(PersonalFinanceTracker.this,
                    "Failed to load data: " + error.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
    
    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JPanel summaryPanel = createSummaryPanel();
        mainPanel.add(summaryPanel, BorderLayout.NORTH);
        
        JPanel centerPanel = createTablePanel();
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        
        JPanel inputPanel = createInputPanel();
        mainPanel.add(inputPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
    }
    
    private JPanel createSummaryPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 3, 10, 0));
        panel.setBorder(BorderFactory.createTitledBorder("Financial Summary"));
        panel.setPreferredSize(new Dimension(0, 80));
        
        balanceLabel = new JLabel("Balance: $0.00", SwingConstants.CENTER);
        incomeLabel = new JLabel("Income: $0.00", SwingConstants.CENTER);
        expenseLabel = new JLabel("Expenses: $0.00", SwingConstants.CENTER);
        
        Font labelFont = new Font("Arial", Font.BOLD, 16);
        balanceLabel.setFont(labelFont);
        incomeLabel.setFont(labelFont);
        expenseLabel.setFont(labelFont);
        
        balanceLabel.setForeground(new Color(0, 100, 0));
        incomeLabel.setForeground(new Color(0, 150, 0));
        expenseLabel.setForeground(new Color(200, 0, 0));
        
        panel.add(balanceLabel);
        panel.add(incomeLabel);
        panel.add(expenseLabel);
        
        return panel;
    }
    
    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Transaction History"));
        
        String[] columns = {"Date", "Type", "Category", "Description", "Amount"};
        transactionModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        transactionTable = new JTable(transactionModel);
        transactionTable.setRowHeight(25);
        transactionTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Make table opaque to show colors properly
        transactionTable.setOpaque(true);
        transactionTable.setFillsViewportHeight(true);
        
        // Color rows based on transaction type
        transactionTable.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                if (!isSelected) {
                    String type = (String) table.getValueAt(row, 1); // Type column (index 1)
                    if ("Income".equals(type)) {
                        setBackground(new Color(200, 255, 200)); // Light green for income
                        setForeground(Color.BLACK);
                    } else if ("Expense".equals(type)) {
                        setBackground(new Color(255, 200, 200)); // Light red for expense
                        setForeground(Color.BLACK);
                    }
                } else {
                    setBackground(table.getSelectionBackground());
                    setForeground(table.getSelectionForeground());
                }
                return this;
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(transactionTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        JButton deleteBtn = new JButton("Delete Selected");
        deleteBtn.addActionListener(e -> deleteTransaction());
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.add(deleteBtn);
        panel.add(btnPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createInputPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Add New Transaction"));
        panel.setPreferredSize(new Dimension(0, 120));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Type:"), gbc);
        gbc.gridx = 1;
        typeCombo = new JComboBox<>(new String[]{"Income", "Expense"});
        panel.add(typeCombo, gbc);
        
        gbc.gridx = 2; gbc.gridy = 0;
        panel.add(new JLabel("Category:"), gbc);
        gbc.gridx = 3;
        categoryCombo = new JComboBox<>(new String[]{
            "Salary", "Freelance", "Investment", "Other Income",
            "Food", "Transport", "Bills", "Entertainment", 
            "Shopping", "Healthcare", "Other Expense"
        });
        panel.add(categoryCombo, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Amount:"), gbc);
        gbc.gridx = 1;
        amountField = new JTextField(10);
        panel.add(amountField, gbc);
        
        gbc.gridx = 2; gbc.gridy = 1;
        panel.add(new JLabel("Description:"), gbc);
        gbc.gridx = 3;
        descriptionField = new JTextField(20);
        panel.add(descriptionField, gbc);
        
        gbc.gridx = 4; gbc.gridy = 0; gbc.gridheight = 2;
        JButton addBtn = new JButton("Add Transaction");
        addBtn.setPreferredSize(new Dimension(140, 50));
        addBtn.addActionListener(e -> addTransaction());
        panel.add(addBtn, gbc);
        
        return panel;
    }
    
    private void addTransaction() {
        if (database == null) {
            JOptionPane.showMessageDialog(this, "Not connected to Firebase!",
                "Connection Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            double amount = Double.parseDouble(amountField.getText().trim());
            if (amount <= 0) {
                JOptionPane.showMessageDialog(this, "Amount must be positive!", 
                    "Invalid Input", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            String type = (String) typeCombo.getSelectedItem();
            String category = (String) categoryCombo.getSelectedItem();
            String description = descriptionField.getText().trim();
            
            if (description.isEmpty()) {
                description = category;
            }
            
            Transaction transaction = new Transaction(type, category, description, amount);
            
            // Save to Firebase
            DatabaseReference newRef = database.push();
            transaction.firebaseKey = newRef.getKey();
            newRef.setValue(transaction, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError error, DatabaseReference ref) {
                    if (error == null) {
                        transactions.add(transaction);
                        addTransactionToTable(transaction);
                        updateSummary();
                        clearInputFields();
                    } else {
                        JOptionPane.showMessageDialog(PersonalFinanceTracker.this,
                            "Failed to save: " + error.getMessage(),
                            "Database Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });
            
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid amount!", 
                "Invalid Input", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void addTransactionToTable(Transaction transaction) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        transactionModel.addRow(new Object[]{
            sdf.format(new Date(transaction.timestamp)),
            transaction.type,
            transaction.category,
            transaction.description,
            String.format("$%.2f", transaction.amount)
        });
    }
    
    private void deleteTransaction() {
        int selectedRow = transactionTable.getSelectedRow();
        if (selectedRow >= 0) {
            int confirm = JOptionPane.showConfirmDialog(this, 
                "Are you sure you want to delete this transaction?", 
                "Confirm Delete", JOptionPane.YES_NO_OPTION);
            
            if (confirm == JOptionPane.YES_OPTION) {
                Transaction transaction = transactions.get(selectedRow);
                
                // Delete from Firebase
                if (database != null && transaction.firebaseKey != null) {
                    database.child(transaction.firebaseKey).removeValue(new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError error, DatabaseReference ref) {
                            if (error == null) {
                                transactions.remove(selectedRow);
                                transactionModel.removeRow(selectedRow);
                                updateSummary();
                            } else {
                                JOptionPane.showMessageDialog(PersonalFinanceTracker.this,
                                    "Failed to delete: " + error.getMessage(),
                                    "Database Error", JOptionPane.ERROR_MESSAGE);
                            }
                        }
                    });
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a transaction to delete!", 
                "No Selection", JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private void updateSummary() {
        double totalIncome = 0;
        double totalExpense = 0;
        
        for (Transaction t : transactions) {
            if (t.type.equals("Income")) {
                totalIncome += t.amount;
            } else {
                totalExpense += t.amount;
            }
        }
        
        double balance = totalIncome - totalExpense;
        
        balanceLabel.setText(String.format("Balance: $%.2f", balance));
        incomeLabel.setText(String.format("Income: $%.2f", totalIncome));
        expenseLabel.setText(String.format("Expenses: $%.2f", totalExpense));
        
        balanceLabel.setForeground(balance >= 0 ? new Color(0, 100, 0) : new Color(200, 0, 0));
    }
    
    private void clearInputFields() {
        amountField.setText("");
        descriptionField.setText("");
        typeCombo.setSelectedIndex(0);
        categoryCombo.setSelectedIndex(0);
        amountField.requestFocus();
    }
    
    // Transaction class - must be public for Firebase
    public static class Transaction {
        public long timestamp;
        public String type;
        public String category;
        public String description;
        public double amount;
        public String firebaseKey;
        
        // Default constructor required for Firebase
        public Transaction() {}
        
        public Transaction(String type, String category, String description, double amount) {
            this.timestamp = System.currentTimeMillis();
            this.type = type;
            this.category = category;
            this.description = description;
            this.amount = amount;
        }
    }
    
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> {
            PersonalFinanceTracker tracker = new PersonalFinanceTracker();
            tracker.setVisible(true);
        });
    }
}