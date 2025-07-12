package com.example.jpegscaler;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.swing.SwingWorker;

/**
 * Cross-platform GUI for the JPEG Scaler application using Java Swing.
 */
public class JpegScalerGUI extends JFrame {
    
    private static final String TITLE = "JPEG Scaler GUI";
    private static final String VERSION = "1.0.0";
    
    // GUI Components
    private JLabel inputFileLabel;
    private JLabel outputFileLabel;
    private JLabel imagePreview;
    private JTextField widthField;
    private JTextField heightField;
    private JTextField maxWidthField;
    private JTextField maxHeightField;
    private JSlider qualitySlider;
    private JLabel qualityLabel;
    private JButton selectInputButton;
    private JButton selectOutputButton;
    private JButton processButton;
    private JProgressBar progressBar;
    private JTextArea logArea;
    private JRadioButton exactDimensionsRadio;
    private JRadioButton maxDimensionsRadio;
    private JRadioButton singleDimensionRadio;
    private JCheckBox maintainAspectRatioCheck;
    
    // Data
    private File inputFile;
    private File outputFile;
    private JpegScaler scaler;
    
    public JpegScalerGUI() {
        scaler = new JpegScaler();
        initializeGUI();
        setupDragAndDrop();
    }
    
    private void initializeGUI() {
        setTitle(TITLE + " v" + VERSION);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        // Create main panel
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Create file selection panel
        JPanel filePanel = createFileSelectionPanel();
        mainPanel.add(filePanel, BorderLayout.NORTH);
        
        // Create center panel with options and preview
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(createOptionsPanel(), BorderLayout.WEST);
        centerPanel.add(createPreviewPanel(), BorderLayout.CENTER);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        
        // Create bottom panel with process button and progress
        JPanel bottomPanel = createBottomPanel();
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
        
        // Set window properties
        setSize(900, 700);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(800, 600));
        
        // Set look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            SwingUtilities.updateComponentTreeUI(this);
        } catch (Exception e) {
            // Fallback to default look and feel
        }
    }
    
    private JPanel createFileSelectionPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("File Selection"));
        
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.add(new JLabel("Input JPEG:"), BorderLayout.WEST);
        inputFileLabel = new JLabel("No file selected (drag & drop or click to select)");
        inputFileLabel.setBorder(new EmptyBorder(5, 10, 5, 10));
        inputFileLabel.setBackground(Color.LIGHT_GRAY);
        inputFileLabel.setOpaque(true);
        inputPanel.add(inputFileLabel, BorderLayout.CENTER);
        
        selectInputButton = new JButton("Browse...");
        selectInputButton.addActionListener(e -> selectInputFile());
        inputPanel.add(selectInputButton, BorderLayout.EAST);
        
        JPanel outputPanel = new JPanel(new BorderLayout());
        outputPanel.add(new JLabel("Output JPEG:"), BorderLayout.WEST);
        outputFileLabel = new JLabel("No file selected");
        outputFileLabel.setBorder(new EmptyBorder(5, 10, 5, 10));
        outputFileLabel.setBackground(Color.LIGHT_GRAY);
        outputFileLabel.setOpaque(true);
        outputPanel.add(outputFileLabel, BorderLayout.CENTER);
        
        selectOutputButton = new JButton("Browse...");
        selectOutputButton.addActionListener(e -> selectOutputFile());
        outputPanel.add(selectOutputButton, BorderLayout.EAST);
        
        panel.add(inputPanel, BorderLayout.NORTH);
        panel.add(outputPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createOptionsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Scaling Options"));
        panel.setPreferredSize(new Dimension(300, 0));
        
        JPanel optionsContent = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Scaling mode selection
        ButtonGroup scalingModeGroup = new ButtonGroup();
        exactDimensionsRadio = new JRadioButton("Exact Dimensions", true);
        singleDimensionRadio = new JRadioButton("Single Dimension (Maintain Aspect Ratio)");
        maxDimensionsRadio = new JRadioButton("Maximum Dimensions (Maintain Aspect Ratio)");
        
        scalingModeGroup.add(exactDimensionsRadio);
        scalingModeGroup.add(singleDimensionRadio);
        scalingModeGroup.add(maxDimensionsRadio);
        
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        optionsContent.add(exactDimensionsRadio, gbc);
        gbc.gridy = 1;
        optionsContent.add(singleDimensionRadio, gbc);
        gbc.gridy = 2;
        optionsContent.add(maxDimensionsRadio, gbc);
        
        // Dimension fields
        gbc.gridwidth = 1;
        gbc.gridy = 3;
        gbc.gridx = 0;
        optionsContent.add(new JLabel("Width:"), gbc);
        gbc.gridx = 1;
        widthField = new JTextField(10);
        optionsContent.add(widthField, gbc);
        
        gbc.gridy = 4;
        gbc.gridx = 0;
        optionsContent.add(new JLabel("Height:"), gbc);
        gbc.gridx = 1;
        heightField = new JTextField(10);
        optionsContent.add(heightField, gbc);
        
        gbc.gridy = 5;
        gbc.gridx = 0;
        optionsContent.add(new JLabel("Max Width:"), gbc);
        gbc.gridx = 1;
        maxWidthField = new JTextField(10);
        optionsContent.add(maxWidthField, gbc);
        
        gbc.gridy = 6;
        gbc.gridx = 0;
        optionsContent.add(new JLabel("Max Height:"), gbc);
        gbc.gridx = 1;
        maxHeightField = new JTextField(10);
        optionsContent.add(maxHeightField, gbc);
        
        // Quality slider
        gbc.gridy = 7;
        gbc.gridx = 0;
        optionsContent.add(new JLabel("Quality:"), gbc);
        gbc.gridx = 1;
        qualitySlider = new JSlider(0, 100, 80);
        qualitySlider.setMajorTickSpacing(20);
        qualitySlider.setMinorTickSpacing(10);
        qualitySlider.setPaintTicks(true);
        qualitySlider.setPaintLabels(true);
        qualitySlider.addChangeListener(e -> updateQualityLabel());
        optionsContent.add(qualitySlider, gbc);
        
        gbc.gridy = 8;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        qualityLabel = new JLabel("Quality: 0.80");
        optionsContent.add(qualityLabel, gbc);
        
        // Add action listeners for radio buttons
        ActionListener radioListener = e -> updateFieldStates();
        exactDimensionsRadio.addActionListener(radioListener);
        singleDimensionRadio.addActionListener(radioListener);
        maxDimensionsRadio.addActionListener(radioListener);
        
        panel.add(optionsContent, BorderLayout.NORTH);
        
        // Add log area
        logArea = new JTextArea(10, 25);
        logArea.setEditable(false);
        logArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane logScrollPane = new JScrollPane(logArea);
        logScrollPane.setBorder(BorderFactory.createTitledBorder("Log"));
        panel.add(logScrollPane, BorderLayout.CENTER);
        
        updateFieldStates();
        
        return panel;
    }
    
    private JPanel createPreviewPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Preview"));
        
        imagePreview = new JLabel("No image selected", SwingConstants.CENTER);
        imagePreview.setBackground(Color.WHITE);
        imagePreview.setOpaque(true);
        imagePreview.setPreferredSize(new Dimension(400, 300));
        
        JScrollPane scrollPane = new JScrollPane(imagePreview);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        processButton = new JButton("Process Image");
        processButton.setEnabled(false);
        processButton.addActionListener(e -> processImage());
        
        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        progressBar.setString("Ready");
        
        panel.add(processButton, BorderLayout.WEST);
        panel.add(progressBar, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void setupDragAndDrop() {
        new DropTarget(inputFileLabel, new DropTargetListener() {
            @Override
            public void dragEnter(DropTargetDragEvent dtde) {
                if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                    dtde.acceptDrag(DnDConstants.ACTION_COPY);
                    inputFileLabel.setBackground(Color.YELLOW);
                } else {
                    dtde.rejectDrag();
                }
            }
            
            @Override
            public void dragOver(DropTargetDragEvent dtde) {}
            
            @Override
            public void dropActionChanged(DropTargetDragEvent dtde) {}
            
            @Override
            public void dragExit(DropTargetEvent dte) {
                inputFileLabel.setBackground(Color.LIGHT_GRAY);
            }
            
            @Override
            public void drop(DropTargetDropEvent dtde) {
                try {
                    dtde.acceptDrop(DnDConstants.ACTION_COPY);
                    Transferable transferable = dtde.getTransferable();
                    
                    if (transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                        @SuppressWarnings("unchecked")
                        List<File> files = (List<File>) transferable.getTransferData(DataFlavor.javaFileListFlavor);
                        
                        if (!files.isEmpty()) {
                            File file = files.get(0);
                            if (isJpegFile(file)) {
                                setInputFile(file);
                            } else {
                                JOptionPane.showMessageDialog(JpegScalerGUI.this, 
                                    "Please drop a JPEG file (.jpg or .jpeg)", 
                                    "Invalid File Type", 
                                    JOptionPane.ERROR_MESSAGE);
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(JpegScalerGUI.this, 
                        "Error handling dropped file: " + e.getMessage(), 
                        "Error", 
                        JOptionPane.ERROR_MESSAGE);
                } finally {
                    inputFileLabel.setBackground(Color.LIGHT_GRAY);
                }
            }
        });
    }
    
    private void selectInputFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("JPEG Images", "jpg", "jpeg"));
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            setInputFile(fileChooser.getSelectedFile());
        }
    }
    
    private void selectOutputFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("JPEG Images", "jpg", "jpeg"));
        
        if (inputFile != null) {
            String inputName = inputFile.getName();
            String baseName = inputName.substring(0, inputName.lastIndexOf('.'));
            fileChooser.setSelectedFile(new File(inputFile.getParent(), baseName + "_scaled.jpg"));
        }
        
        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            outputFile = fileChooser.getSelectedFile();
            if (!outputFile.getName().toLowerCase().endsWith(".jpg") && 
                !outputFile.getName().toLowerCase().endsWith(".jpeg")) {
                outputFile = new File(outputFile.getParent(), outputFile.getName() + ".jpg");
            }
            outputFileLabel.setText(outputFile.getName());
            updateProcessButtonState();
        }
    }
    
    private void setInputFile(File file) {
        inputFile = file;
        inputFileLabel.setText(file.getName());
        loadImagePreview(file);
        updateProcessButtonState();
        
        // Auto-suggest output file
        if (outputFile == null) {
            String inputName = file.getName();
            String baseName = inputName.substring(0, inputName.lastIndexOf('.'));
            outputFile = new File(file.getParent(), baseName + "_scaled.jpg");
            outputFileLabel.setText(outputFile.getName());
        }
    }
    
    private void loadImagePreview(File file) {
        try {
            Dimension imageDimensions = scaler.getImageDimensions(file);
            
            // Load and scale image for preview
            ImageIcon originalIcon = new ImageIcon(file.getPath());
            Image originalImage = originalIcon.getImage();
            
            // Calculate preview size (max 400x300)
            int previewWidth = 400;
            int previewHeight = 300;
            double ratio = Math.min((double) previewWidth / imageDimensions.width, 
                                   (double) previewHeight / imageDimensions.height);
            
            int scaledWidth = (int) (imageDimensions.width * ratio);
            int scaledHeight = (int) (imageDimensions.height * ratio);
            
            Image scaledImage = originalImage.getScaledInstance(scaledWidth, scaledHeight, Image.SCALE_SMOOTH);
            imagePreview.setIcon(new ImageIcon(scaledImage));
            imagePreview.setText("");
            
            log("Loaded image: " + imageDimensions.width + "x" + imageDimensions.height + " pixels");
            
        } catch (IOException e) {
            imagePreview.setIcon(null);
            imagePreview.setText("Error loading image");
            log("Error loading image: " + e.getMessage());
        }
    }
    
    private void updateFieldStates() {
        boolean exactMode = exactDimensionsRadio.isSelected();
        boolean singleMode = singleDimensionRadio.isSelected();
        boolean maxMode = maxDimensionsRadio.isSelected();
        
        widthField.setEnabled(exactMode || singleMode);
        heightField.setEnabled(exactMode || singleMode);
        maxWidthField.setEnabled(maxMode);
        maxHeightField.setEnabled(maxMode);
    }
    
    private void updateQualityLabel() {
        float quality = qualitySlider.getValue() / 100.0f;
        qualityLabel.setText(String.format("Quality: %.2f", quality));
    }
    
    private void updateProcessButtonState() {
        processButton.setEnabled(inputFile != null && outputFile != null);
    }
    
    private void processImage() {
        if (inputFile == null || outputFile == null) {
            JOptionPane.showMessageDialog(this, "Please select both input and output files.", 
                "Missing Files", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            validateInputs();
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), 
                "Invalid Input", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Process image in background thread
        SwingWorker<Void, String> worker = new SwingWorker<Void, String>() {
            @Override
            protected Void doInBackground() throws Exception {
                processButton.setEnabled(false);
                progressBar.setIndeterminate(true);
                progressBar.setString("Processing...");
                
                publish("Starting image processing...");
                
                float quality = qualitySlider.getValue() / 100.0f;
                
                if (maxDimensionsRadio.isSelected()) {
                    int maxWidth = Integer.parseInt(maxWidthField.getText().trim());
                    int maxHeight = Integer.parseInt(maxHeightField.getText().trim());
                    
                    publish("Scaling with aspect ratio preservation. Max dimensions: " + maxWidth + "x" + maxHeight);
                    scaler.scaleImageMaintainAspectRatio(inputFile, outputFile, maxWidth, maxHeight, quality);
                    
                } else {
                    int width = 0, height = 0;
                    
                    if (!widthField.getText().trim().isEmpty()) {
                        width = Integer.parseInt(widthField.getText().trim());
                    }
                    if (!heightField.getText().trim().isEmpty()) {
                        height = Integer.parseInt(heightField.getText().trim());
                    }
                    
                    // Handle single dimension mode
                    if (singleDimensionRadio.isSelected() && (width == 0 || height == 0)) {
                        Dimension originalDimensions = scaler.getImageDimensions(inputFile);
                        if (width == 0) {
                            width = (int) ((double) height * originalDimensions.width / originalDimensions.height);
                        } else {
                            height = (int) ((double) width * originalDimensions.height / originalDimensions.width);
                        }
                    }
                    
                    publish("Scaling to exact dimensions: " + width + "x" + height);
                    scaler.scaleImage(inputFile, outputFile, width, height, quality);
                }
                
                return null;
            }
            
            @Override
            protected void process(List<String> chunks) {
                for (String message : chunks) {
                    log(message);
                }
            }
            
            @Override
            protected void done() {
                try {
                    get(); // This will throw any exceptions from doInBackground
                    
                    Dimension scaledDimensions = scaler.getImageDimensions(outputFile);
                    log("Scaled dimensions: " + scaledDimensions.width + "x" + scaledDimensions.height);
                    log("Image scaling completed successfully!");
                    
                    progressBar.setString("Completed");
                    
                    int result = JOptionPane.showConfirmDialog(JpegScalerGUI.this, 
                        "Image processed successfully!\nDo you want to open the output folder?", 
                        "Success", 
                        JOptionPane.YES_NO_OPTION);
                    
                    if (result == JOptionPane.YES_OPTION) {
                        try {
                            Desktop.getDesktop().open(outputFile.getParentFile());
                        } catch (IOException e) {
                            log("Could not open output folder: " + e.getMessage());
                        }
                    }
                    
                } catch (Exception e) {
                    log("Error processing image: " + e.getMessage());
                    JOptionPane.showMessageDialog(JpegScalerGUI.this, 
                        "Error processing image: " + e.getMessage(), 
                        "Error", 
                        JOptionPane.ERROR_MESSAGE);
                    progressBar.setString("Error");
                } finally {
                    progressBar.setIndeterminate(false);
                    processButton.setEnabled(true);
                }
            }
        };
        
        worker.execute();
    }
    
    private void validateInputs() throws IllegalArgumentException {
        if (exactDimensionsRadio.isSelected()) {
            if (widthField.getText().trim().isEmpty() || heightField.getText().trim().isEmpty()) {
                throw new IllegalArgumentException("Both width and height are required for exact dimensions mode.");
            }
            try {
                int width = Integer.parseInt(widthField.getText().trim());
                int height = Integer.parseInt(heightField.getText().trim());
                if (width <= 0 || height <= 0) {
                    throw new IllegalArgumentException("Width and height must be positive integers.");
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Width and height must be valid integers.");
            }
        } else if (singleDimensionRadio.isSelected()) {
            boolean hasWidth = !widthField.getText().trim().isEmpty();
            boolean hasHeight = !heightField.getText().trim().isEmpty();
            
            if (!hasWidth && !hasHeight) {
                throw new IllegalArgumentException("At least one dimension (width or height) is required for single dimension mode.");
            }
            
            try {
                if (hasWidth) {
                    int width = Integer.parseInt(widthField.getText().trim());
                    if (width <= 0) {
                        throw new IllegalArgumentException("Width must be a positive integer.");
                    }
                }
                if (hasHeight) {
                    int height = Integer.parseInt(heightField.getText().trim());
                    if (height <= 0) {
                        throw new IllegalArgumentException("Height must be a positive integer.");
                    }
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Width and height must be valid integers.");
            }
        } else if (maxDimensionsRadio.isSelected()) {
            if (maxWidthField.getText().trim().isEmpty() || maxHeightField.getText().trim().isEmpty()) {
                throw new IllegalArgumentException("Both maximum width and height are required for maximum dimensions mode.");
            }
            try {
                int maxWidth = Integer.parseInt(maxWidthField.getText().trim());
                int maxHeight = Integer.parseInt(maxHeightField.getText().trim());
                if (maxWidth <= 0 || maxHeight <= 0) {
                    throw new IllegalArgumentException("Maximum width and height must be positive integers.");
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Maximum width and height must be valid integers.");
            }
        }
    }
    
    private boolean isJpegFile(File file) {
        String name = file.getName().toLowerCase();
        return name.endsWith(".jpg") || name.endsWith(".jpeg");
    }
    
    private void log(String message) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                // Fallback to default look and feel
            }
            
            new JpegScalerGUI().setVisible(true);
        });
    }
}