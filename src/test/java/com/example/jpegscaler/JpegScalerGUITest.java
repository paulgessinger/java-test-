package com.example.jpegscaler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.io.TempDir;
import static org.assertj.core.api.Assertions.*;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Path;
import javax.imageio.ImageIO;

/**
 * Test class for JpegScalerGUI.
 * These tests work with GUI components in CI environments using virtual displays (Xvfb).
 */
public class JpegScalerGUITest {

    private JpegScalerGUI gui;
    private File testImageFile;
    private File outputFile;
    
    @TempDir
    Path tempDir;
    
    @BeforeEach
    void setUp() throws Exception {
        // Create a test JPEG image
        testImageFile = tempDir.resolve("test-image.jpg").toFile();
        createTestImage(testImageFile, 800, 600);
        
        outputFile = tempDir.resolve("output.jpg").toFile();
        
        // Create GUI instance in EDT and wait for completion
        SwingUtilities.invokeAndWait(() -> {
            gui = new JpegScalerGUI();
            // Don't show the window, just create the components
            gui.setVisible(false);
        });
    }
    
    @AfterEach
    void tearDown() throws Exception {
        if (gui != null) {
            SwingUtilities.invokeAndWait(() -> {
                gui.dispose();
            });
        }
    }
    
    /**
     * Test that the GUI can be instantiated properly.
     */
    @Test
    void testGUIInstantiation() {
        assertThat(gui).isNotNull();
        assertThat(gui.getTitle()).contains("JPEG Scaler GUI");
        // GUI components are created but window is not displayed
        assertThat(gui.isVisible()).isFalse();
    }
    
    /**
     * Test the file validation logic.
     */
    @Test
    void testFileValidation() throws Exception {
        // Use reflection to access private method
        Method isJpegFileMethod = JpegScalerGUI.class.getDeclaredMethod("isJpegFile", File.class);
        isJpegFileMethod.setAccessible(true);
        
        // Test valid JPEG files
        File jpgFile = tempDir.resolve("test.jpg").toFile();
        jpgFile.createNewFile();
        assertThat((Boolean) isJpegFileMethod.invoke(gui, jpgFile)).isTrue();
        
        File jpegFile = tempDir.resolve("test.jpeg").toFile();
        jpegFile.createNewFile();
        assertThat((Boolean) isJpegFileMethod.invoke(gui, jpegFile)).isTrue();
        
        // Test invalid files
        File txtFile = tempDir.resolve("test.txt").toFile();
        txtFile.createNewFile();
        assertThat((Boolean) isJpegFileMethod.invoke(gui, txtFile)).isFalse();
        
        File pngFile = tempDir.resolve("test.png").toFile();
        pngFile.createNewFile();
        assertThat((Boolean) isJpegFileMethod.invoke(gui, pngFile)).isFalse();
    }
    
    /**
     * Test input validation logic for exact dimensions mode.
     */
    @Test
    void testExactDimensionsValidation() throws Exception {
        // Use reflection to access private fields and methods
        Field exactRadioField = JpegScalerGUI.class.getDeclaredField("exactDimensionsRadio");
        exactRadioField.setAccessible(true);
        JRadioButton exactRadio = (JRadioButton) exactRadioField.get(gui);
        
        Field widthFieldField = JpegScalerGUI.class.getDeclaredField("widthField");
        widthFieldField.setAccessible(true);
        JTextField widthField = (JTextField) widthFieldField.get(gui);
        
        Field heightFieldField = JpegScalerGUI.class.getDeclaredField("heightField");
        heightFieldField.setAccessible(true);
        JTextField heightField = (JTextField) heightFieldField.get(gui);
        
        Method validateInputsMethod = JpegScalerGUI.class.getDeclaredMethod("validateInputs");
        validateInputsMethod.setAccessible(true);
        
        // Test exact dimensions mode with valid input
        SwingUtilities.invokeAndWait(() -> {
            exactRadio.setSelected(true);
            widthField.setText("800");
            heightField.setText("600");
        });
        
        assertThatNoException().isThrownBy(() -> {
            validateInputsMethod.invoke(gui);
        });
        
        // Test exact dimensions mode with missing width
        SwingUtilities.invokeAndWait(() -> {
            widthField.setText("");
            heightField.setText("600");
        });
        
        assertThatThrownBy(() -> {
            validateInputsMethod.invoke(gui);
        }).isInstanceOf(java.lang.reflect.InvocationTargetException.class)
          .hasRootCauseInstanceOf(IllegalArgumentException.class)
          .hasRootCauseMessage("Both width and height are required for exact dimensions mode.");
        
        // Test exact dimensions mode with invalid width
        SwingUtilities.invokeAndWait(() -> {
            widthField.setText("invalid");
            heightField.setText("600");
        });
        
        assertThatThrownBy(() -> {
            validateInputsMethod.invoke(gui);
        }).isInstanceOf(java.lang.reflect.InvocationTargetException.class)
          .hasRootCauseInstanceOf(IllegalArgumentException.class)
          .hasRootCauseMessage("Width and height must be valid integers.");
        
        // Test exact dimensions mode with negative width
        SwingUtilities.invokeAndWait(() -> {
            widthField.setText("-100");
            heightField.setText("600");
        });
        
        assertThatThrownBy(() -> {
            validateInputsMethod.invoke(gui);
        }).isInstanceOf(java.lang.reflect.InvocationTargetException.class)
          .hasRootCauseInstanceOf(IllegalArgumentException.class)
          .hasRootCauseMessage("Width and height must be positive integers.");
    }
    
    /**
     * Test single dimension validation.
     */
    @Test
    void testSingleDimensionValidation() throws Exception {
        // Use reflection to access private fields and methods
        Field singleRadioField = JpegScalerGUI.class.getDeclaredField("singleDimensionRadio");
        singleRadioField.setAccessible(true);
        JRadioButton singleRadio = (JRadioButton) singleRadioField.get(gui);
        
        Field widthFieldField = JpegScalerGUI.class.getDeclaredField("widthField");
        widthFieldField.setAccessible(true);
        JTextField widthField = (JTextField) widthFieldField.get(gui);
        
        Field heightFieldField = JpegScalerGUI.class.getDeclaredField("heightField");
        heightFieldField.setAccessible(true);
        JTextField heightField = (JTextField) heightFieldField.get(gui);
        
        Method validateInputsMethod = JpegScalerGUI.class.getDeclaredMethod("validateInputs");
        validateInputsMethod.setAccessible(true);
        
        // Test single dimension mode with width only
        SwingUtilities.invokeAndWait(() -> {
            singleRadio.setSelected(true);
            widthField.setText("800");
            heightField.setText("");
        });
        
        assertThatNoException().isThrownBy(() -> {
            validateInputsMethod.invoke(gui);
        });
        
        // Test single dimension mode with height only
        SwingUtilities.invokeAndWait(() -> {
            widthField.setText("");
            heightField.setText("600");
        });
        
        assertThatNoException().isThrownBy(() -> {
            validateInputsMethod.invoke(gui);
        });
        
        // Test single dimension mode with no dimensions
        SwingUtilities.invokeAndWait(() -> {
            widthField.setText("");
            heightField.setText("");
        });
        
        assertThatThrownBy(() -> {
            validateInputsMethod.invoke(gui);
        }).isInstanceOf(java.lang.reflect.InvocationTargetException.class)
          .hasRootCauseInstanceOf(IllegalArgumentException.class)
          .hasRootCauseMessage("At least one dimension (width or height) is required for single dimension mode.");
    }
    
    /**
     * Test max dimensions validation.
     */
    @Test
    void testMaxDimensionsValidation() throws Exception {
        // Use reflection to access private fields and methods
        Field maxRadioField = JpegScalerGUI.class.getDeclaredField("maxDimensionsRadio");
        maxRadioField.setAccessible(true);
        JRadioButton maxRadio = (JRadioButton) maxRadioField.get(gui);
        
        Field maxWidthFieldField = JpegScalerGUI.class.getDeclaredField("maxWidthField");
        maxWidthFieldField.setAccessible(true);
        JTextField maxWidthField = (JTextField) maxWidthFieldField.get(gui);
        
        Field maxHeightFieldField = JpegScalerGUI.class.getDeclaredField("maxHeightField");
        maxHeightFieldField.setAccessible(true);
        JTextField maxHeightField = (JTextField) maxHeightFieldField.get(gui);
        
        Method validateInputsMethod = JpegScalerGUI.class.getDeclaredMethod("validateInputs");
        validateInputsMethod.setAccessible(true);
        
        // Test max dimensions mode with valid input
        SwingUtilities.invokeAndWait(() -> {
            maxRadio.setSelected(true);
            maxWidthField.setText("1024");
            maxHeightField.setText("768");
        });
        
        assertThatNoException().isThrownBy(() -> {
            validateInputsMethod.invoke(gui);
        });
        
        // Test max dimensions mode with missing max width
        SwingUtilities.invokeAndWait(() -> {
            maxWidthField.setText("");
            maxHeightField.setText("768");
        });
        
        assertThatThrownBy(() -> {
            validateInputsMethod.invoke(gui);
        }).isInstanceOf(java.lang.reflect.InvocationTargetException.class)
          .hasRootCauseInstanceOf(IllegalArgumentException.class)
          .hasRootCauseMessage("Both maximum width and height are required for maximum dimensions mode.");
    }
    
    /**
     * Test the quality slider functionality.
     */
    @Test
    void testQualitySlider() throws Exception {
        Field qualitySliderField = JpegScalerGUI.class.getDeclaredField("qualitySlider");
        qualitySliderField.setAccessible(true);
        JSlider qualitySlider = (JSlider) qualitySliderField.get(gui);
        
        Field qualityLabelField = JpegScalerGUI.class.getDeclaredField("qualityLabel");
        qualityLabelField.setAccessible(true);
        JLabel qualityLabel = (JLabel) qualityLabelField.get(gui);
        
        // Test default quality
        assertThat(qualitySlider.getValue()).isEqualTo(80);
        
        // Test quality label update
        SwingUtilities.invokeAndWait(() -> {
            qualitySlider.setValue(90);
            // Trigger change event
            qualitySlider.getChangeListeners()[0].stateChanged(
                new javax.swing.event.ChangeEvent(qualitySlider)
            );
        });
        
        assertThat(qualityLabel.getText()).contains("0.90");
        
        // Test minimum quality
        SwingUtilities.invokeAndWait(() -> {
            qualitySlider.setValue(0);
            qualitySlider.getChangeListeners()[0].stateChanged(
                new javax.swing.event.ChangeEvent(qualitySlider)
            );
        });
        
        assertThat(qualityLabel.getText()).contains("0.00");
        
        // Test maximum quality
        SwingUtilities.invokeAndWait(() -> {
            qualitySlider.setValue(100);
            qualitySlider.getChangeListeners()[0].stateChanged(
                new javax.swing.event.ChangeEvent(qualitySlider)
            );
        });
        
        assertThat(qualityLabel.getText()).contains("1.00");
    }
    
    /**
     * Test field state updates based on radio button selection.
     */
    @Test
    void testFieldStateUpdates() throws Exception {
        // Access private fields using reflection
        Field exactRadioField = JpegScalerGUI.class.getDeclaredField("exactDimensionsRadio");
        exactRadioField.setAccessible(true);
        JRadioButton exactRadio = (JRadioButton) exactRadioField.get(gui);
        
        Field singleRadioField = JpegScalerGUI.class.getDeclaredField("singleDimensionRadio");
        singleRadioField.setAccessible(true);
        JRadioButton singleRadio = (JRadioButton) singleRadioField.get(gui);
        
        Field maxRadioField = JpegScalerGUI.class.getDeclaredField("maxDimensionsRadio");
        maxRadioField.setAccessible(true);
        JRadioButton maxRadio = (JRadioButton) maxRadioField.get(gui);
        
        Field widthFieldField = JpegScalerGUI.class.getDeclaredField("widthField");
        widthFieldField.setAccessible(true);
        JTextField widthField = (JTextField) widthFieldField.get(gui);
        
        Field heightFieldField = JpegScalerGUI.class.getDeclaredField("heightField");
        heightFieldField.setAccessible(true);
        JTextField heightField = (JTextField) heightFieldField.get(gui);
        
        Field maxWidthFieldField = JpegScalerGUI.class.getDeclaredField("maxWidthField");
        maxWidthFieldField.setAccessible(true);
        JTextField maxWidthField = (JTextField) maxWidthFieldField.get(gui);
        
        Field maxHeightFieldField = JpegScalerGUI.class.getDeclaredField("maxHeightField");
        maxHeightFieldField.setAccessible(true);
        JTextField maxHeightField = (JTextField) maxHeightFieldField.get(gui);
        
        Method updateFieldStatesMethod = JpegScalerGUI.class.getDeclaredMethod("updateFieldStates");
        updateFieldStatesMethod.setAccessible(true);
        
        // Test exact dimensions mode
        SwingUtilities.invokeAndWait(() -> {
            exactRadio.setSelected(true);
            try {
                updateFieldStatesMethod.invoke(gui);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        
        assertThat(widthField.isEnabled()).isTrue();
        assertThat(heightField.isEnabled()).isTrue();
        assertThat(maxWidthField.isEnabled()).isFalse();
        assertThat(maxHeightField.isEnabled()).isFalse();
        
        // Test single dimension mode
        SwingUtilities.invokeAndWait(() -> {
            singleRadio.setSelected(true);
            try {
                updateFieldStatesMethod.invoke(gui);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        
        assertThat(widthField.isEnabled()).isTrue();
        assertThat(heightField.isEnabled()).isTrue();
        assertThat(maxWidthField.isEnabled()).isFalse();
        assertThat(maxHeightField.isEnabled()).isFalse();
        
        // Test max dimensions mode
        SwingUtilities.invokeAndWait(() -> {
            maxRadio.setSelected(true);
            try {
                updateFieldStatesMethod.invoke(gui);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        
        assertThat(widthField.isEnabled()).isFalse();
        assertThat(heightField.isEnabled()).isFalse();
        assertThat(maxWidthField.isEnabled()).isTrue();
        assertThat(maxHeightField.isEnabled()).isTrue();
    }
    
    /**
     * Test the logging functionality.
     */
    @Test
    void testLogging() throws Exception {
        Field logAreaField = JpegScalerGUI.class.getDeclaredField("logArea");
        logAreaField.setAccessible(true);
        JTextArea logArea = (JTextArea) logAreaField.get(gui);
        
        Method logMethod = JpegScalerGUI.class.getDeclaredMethod("log", String.class);
        logMethod.setAccessible(true);
        
        // Test logging a message
        String testMessage = "Test log message";
        logMethod.invoke(gui, testMessage);
        
        // Wait for SwingUtilities.invokeLater to complete
        SwingUtilities.invokeAndWait(() -> {});
        
        assertThat(logArea.getText()).contains(testMessage);
        
        // Test logging multiple messages
        String testMessage2 = "Second test message";
        logMethod.invoke(gui, testMessage2);
        
        SwingUtilities.invokeAndWait(() -> {});
        
        assertThat(logArea.getText()).contains(testMessage);
        assertThat(logArea.getText()).contains(testMessage2);
    }
    
    /**
     * Test GUI component accessibility and properties.
     */
    @Test
    void testGUIComponentProperties() throws Exception {
        // Test that all major components are accessible
        Field[] fields = JpegScalerGUI.class.getDeclaredFields();
        int componentCount = 0;
        
        for (Field field : fields) {
            field.setAccessible(true);
            Object value = field.get(gui);
            if (value instanceof JComponent) {
                componentCount++;
                JComponent component = (JComponent) value;
                assertThat(component).isNotNull();
                // Components should be properly initialized
                assertThat(component.getClass().getName()).isNotEmpty();
            }
        }
        
        // Verify we have a reasonable number of components
        assertThat(componentCount).isGreaterThan(10);
    }
    
    /**
     * Helper method to create a test JPEG image.
     */
    private void createTestImage(File file, int width, int height) throws IOException {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        
        // Create a simple test pattern
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, width, height);
        
        g2d.setColor(Color.RED);
        g2d.fillOval(width/4, height/4, width/2, height/2);
        
        g2d.setColor(Color.BLUE);
        g2d.drawString("Test Image " + width + "x" + height, 10, 20);
        
        g2d.dispose();
        
        ImageIO.write(image, "JPEG", file);
    }
}