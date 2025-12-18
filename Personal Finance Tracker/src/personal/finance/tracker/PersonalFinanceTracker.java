/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package personal.finance.tracker;

/**
 * @author abdullah
 * @author saad
 */
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class PersonalFinanceTracker extends JFrame {
    private DefaultTableModel transactionModel;
    private JTable transactionTable;
    private JTextField amountField, descriptionField;
    private JComboBox<String> categoryCombo, typeCombo;
    private JLabel balanceLabel, incomeLabel, expenseLabel;
    private List<Transaction> transactions;
    
    public PersonalFinanceTracker() {
        transactions = new ArrayList<>();
        initComponents();
        setTitle("Personal Finance Tracker");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }
    
    private void initComponents() {
        // Main panel with border layout
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Top panel - Summary
        JPanel summaryPanel = createSummaryPanel();
        mainPanel.add(summaryPanel, BorderLayout.NORTH);
        
        // Center panel - Transaction table
        JPanel centerPanel = createTablePanel();
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        
        // Bottom panel - Input form
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
        
        balanceLabel.setForeground(new Color(0, 255, 255));
        incomeLabel.setForeground(new Color(0, 255, 0));
        expenseLabel.setForeground(new Color(255, 0, 0));
        
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
        
        JScrollPane scrollPane = new JScrollPane(transactionTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Delete button
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
        
        // Type
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Type:"), gbc);
        gbc.gridx = 1;
        typeCombo = new JComboBox<>(new String[]{"Income", "Expense"});
        panel.add(typeCombo, gbc);
        
        // Category
        gbc.gridx = 2; gbc.gridy = 0;
        panel.add(new JLabel("Category:"), gbc);
        gbc.gridx = 3;
        categoryCombo = new JComboBox<>(new String[]{
            "Salary", "Freelance", "Investment", "Other Income",
            "Food", "Transport", "Bills", "Entertainment", 
            "Shopping", "Healthcare", "Other Expense"
        });
        panel.add(categoryCombo, gbc);
        
        // Amount
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Amount:"), gbc);
        gbc.gridx = 1;
        amountField = new JTextField(10);
        panel.add(amountField, gbc);
        
        // Description
        gbc.gridx = 2; gbc.gridy = 1;
        panel.add(new JLabel("Description:"), gbc);
        gbc.gridx = 3;
        descriptionField = new JTextField(20);
        panel.add(descriptionField, gbc);
        
        // Add button
        gbc.gridx = 4; gbc.gridy = 0; gbc.gridheight = 2;
        JButton addBtn = new JButton("Add Transaction");
        addBtn.setPreferredSize(new Dimension(140, 50));
        addBtn.addActionListener(e -> addTransaction());
        panel.add(addBtn, gbc);
        
        return panel;
    }
    
    private void addTransaction() {
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
            transactions.add(transaction);
            
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            transactionModel.addRow(new Object[]{
                sdf.format(transaction.date),
                transaction.type,
                transaction.category,
                transaction.description,
                String.format("$%.2f", transaction.amount)
            });
            
            updateSummary();
            clearInputFields();
            
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid amount!", 
                "Invalid Input", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void deleteTransaction() {
        int selectedRow = transactionTable.getSelectedRow();
        if (selectedRow >= 0) {
            int confirm = JOptionPane.showConfirmDialog(this, 
                "Are you sure you want to delete this transaction?", 
                "Confirm Delete", JOptionPane.YES_NO_OPTION);
            
            if (confirm == JOptionPane.YES_OPTION) {
                transactions.remove(selectedRow);
                transactionModel.removeRow(selectedRow);
                updateSummary();
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
    
    // Transaction class
    static class Transaction {
        Date date;
        String type;
        String category;
        String description;
        double amount;
        
        public Transaction(String type, String category, String description, double amount) {
            this.date = new Date();
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
