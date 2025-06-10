package org.example.gui;

import org.example.cipher.CaesarCipher;
import org.example.io.FileManager;
import org.example.validation.Validator;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.*;
import java.nio.file.Path;

public class CipherSwingApp {
    private final CaesarCipher cipher = new CaesarCipher();
    private final Validator validator = new Validator(org.example.cipher.Alphabet.length());

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new CipherSwingApp().createAndShow());
    }

    private void createAndShow() {
        JFrame frame = new JFrame("Caesar Cipher");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Файлы", createFilePanel());
        tabs.addTab("Текст", createTextPanel());

        frame.getContentPane().add(tabs);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private JPanel createFilePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5,5,5,5);
        c.fill = GridBagConstraints.HORIZONTAL;

        JTextField inPath = new JTextField();
        JButton inBtn = new JButton("Выбрать");
        inBtn.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            fc.setFileFilter(new FileNameExtensionFilter("Text files", "txt"));
            if (fc.showOpenDialog(null)==JFileChooser.APPROVE_OPTION)
                inPath.setText(fc.getSelectedFile().getAbsolutePath());
        });

        JTextField outPath = new JTextField();
        JButton outBtn = new JButton("Сохранить в");
        outBtn.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            fc.setFileFilter(new FileNameExtensionFilter("Text files", "txt"));
            if (fc.showSaveDialog(null)==JFileChooser.APPROVE_OPTION)
                outPath.setText(fc.getSelectedFile().getAbsolutePath());
        });

        JTextField keyField = new JTextField();
        JButton encBtn = new JButton("Шифровать");
        JButton decBtn = new JButton("Расшифровать");

        encBtn.addActionListener(e -> runFile(inPath, outPath, keyField, true));
        decBtn.addActionListener(e -> runFile(inPath, outPath, keyField, false));

        c.gridx=0; c.gridy=0; panel.add(new JLabel("Исходный файл:"),c);
        c.gridx=1; panel.add(inPath,c);
        c.gridx=2; panel.add(inBtn,c);

        c.gridx=0; c.gridy=1; panel.add(new JLabel("Результат в:"),c);
        c.gridx=1; panel.add(outPath,c);
        c.gridx=2; panel.add(outBtn,c);

        c.gridx=0; c.gridy=2; panel.add(new JLabel("Ключ:"),c);
        c.gridx=1; panel.add(keyField,c);

        c.gridx=0; c.gridy=3; panel.add(encBtn,c);
        c.gridx=1; panel.add(decBtn,c);

        return panel;
    }

    private JPanel createTextPanel() {
        JPanel panel = new JPanel(new BorderLayout(5,5));
        JTextArea input = new JTextArea();
        JTextArea output = new JTextArea();
        output.setEditable(false);

        JPanel north = new JPanel();
        JTextField keyField = new JTextField(5);
        JButton encBtn = new JButton("Шифровать");
        JButton decBtn = new JButton("Расшифровать");
        north.add(new JLabel("Ключ:"));
        north.add(keyField);
        north.add(encBtn);
        north.add(decBtn);

        encBtn.addActionListener(e -> {
            try {
                int key = Integer.parseInt(keyField.getText().trim());
                StringWriter sw = new StringWriter();
                cipher.transformStream(new StringReader(input.getText()), sw, key, true);
                output.setText(sw.toString());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, ex.getMessage());
            }
        });

        decBtn.addActionListener(e -> {
            try {
                int key = Integer.parseInt(keyField.getText().trim());
                StringWriter sw = new StringWriter();
                cipher.transformStream(new StringReader(input.getText()), sw, key, false);
                output.setText(sw.toString());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, ex.getMessage());
            }
        });

        panel.add(north, BorderLayout.NORTH);
        panel.add(new JScrollPane(input), BorderLayout.CENTER);
        panel.add(new JScrollPane(output), BorderLayout.SOUTH);
        return panel;
    }

    private void runFile(JTextField inPath, JTextField outPath, JTextField keyField, boolean encrypt) {
        try {
            Path in = Path.of(inPath.getText().trim());
            Path out = Path.of(outPath.getText().trim());
            int key = Integer.parseInt(keyField.getText().trim());
            validator.ensureFileReadable(in);
            validator.ensureParentWritable(out);
            validator.ensureKeyInRange(key);
            try (Reader r = FileManager.newReader(in);
                 Writer w = FileManager.newWriter(out)) {
                cipher.transformStream(r,w,key,encrypt);
            }
            JOptionPane.showMessageDialog(null, (encrypt?"Зашифровано":"Расшифровано")+" в "+out);
        } catch (IOException | NumberFormatException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }
}