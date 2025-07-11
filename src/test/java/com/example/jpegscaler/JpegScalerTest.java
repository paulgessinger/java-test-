package com.example.jpegscaler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import javax.imageio.ImageIO;

import static org.assertj.core.api.Assertions.*;

class JpegScalerTest {
    
    private JpegScaler jpegScaler;
    
    @TempDir
    Path tempDir;
    
    @BeforeEach
    void setUp() {
        jpegScaler = new JpegScaler();
    }
    
    @Test
    void testScaleImageWithValidInput() throws IOException {
        // Create a test image
        File inputFile = createTestImage(100, 100);
        File outputFile = tempDir.resolve("output.jpg").toFile();
        
        jpegScaler.scaleImage(inputFile, outputFile, 50, 50, 0.8f);
        
        assertThat(outputFile).exists();
        
        Dimension outputDimensions = jpegScaler.getImageDimensions(outputFile);
        assertThat(outputDimensions.width).isEqualTo(50);
        assertThat(outputDimensions.height).isEqualTo(50);
    }
    
    @Test
    void testScaleImageWithNonExistentInput() {
        File inputFile = new File("non-existent-file.jpg");
        File outputFile = tempDir.resolve("output.jpg").toFile();
        
        assertThatThrownBy(() -> jpegScaler.scaleImage(inputFile, outputFile, 50, 50, 0.8f))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("Input file does not exist");
    }
    
    @Test
    void testScaleImageWithInvalidDimensions() throws IOException {
        File inputFile = createTestImage(100, 100);
        File outputFile = tempDir.resolve("output.jpg").toFile();
        
        assertThatThrownBy(() -> jpegScaler.scaleImage(inputFile, outputFile, -1, 50, 0.8f))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Width and height must be positive integers");
        
        assertThatThrownBy(() -> jpegScaler.scaleImage(inputFile, outputFile, 50, 0, 0.8f))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Width and height must be positive integers");
    }
    
    @Test
    void testScaleImageWithInvalidQuality() throws IOException {
        File inputFile = createTestImage(100, 100);
        File outputFile = tempDir.resolve("output.jpg").toFile();
        
        assertThatThrownBy(() -> jpegScaler.scaleImage(inputFile, outputFile, 50, 50, -0.1f))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Quality must be between 0.0 and 1.0");
        
        assertThatThrownBy(() -> jpegScaler.scaleImage(inputFile, outputFile, 50, 50, 1.1f))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Quality must be between 0.0 and 1.0");
    }
    
    @Test
    void testScaleImageMaintainAspectRatio() throws IOException {
        // Create a 200x100 test image (2:1 aspect ratio)
        File inputFile = createTestImage(200, 100);
        File outputFile = tempDir.resolve("output.jpg").toFile();
        
        jpegScaler.scaleImageMaintainAspectRatio(inputFile, outputFile, 100, 100, 0.8f);
        
        assertThat(outputFile).exists();
        
        Dimension outputDimensions = jpegScaler.getImageDimensions(outputFile);
        // Should be scaled to 100x50 to maintain 2:1 aspect ratio within 100x100 bounds
        assertThat(outputDimensions.width).isEqualTo(100);
        assertThat(outputDimensions.height).isEqualTo(50);
    }
    
    @Test
    void testScaleImageMaintainAspectRatioTallImage() throws IOException {
        // Create a 100x200 test image (1:2 aspect ratio)
        File inputFile = createTestImage(100, 200);
        File outputFile = tempDir.resolve("output.jpg").toFile();
        
        jpegScaler.scaleImageMaintainAspectRatio(inputFile, outputFile, 100, 100, 0.8f);
        
        assertThat(outputFile).exists();
        
        Dimension outputDimensions = jpegScaler.getImageDimensions(outputFile);
        // Should be scaled to 50x100 to maintain 1:2 aspect ratio within 100x100 bounds
        assertThat(outputDimensions.width).isEqualTo(50);
        assertThat(outputDimensions.height).isEqualTo(100);
    }
    
    @Test
    void testGetImageDimensions() throws IOException {
        File testFile = createTestImage(150, 75);
        
        Dimension dimensions = jpegScaler.getImageDimensions(testFile);
        
        assertThat(dimensions.width).isEqualTo(150);
        assertThat(dimensions.height).isEqualTo(75);
    }
    
    @Test
    void testGetImageDimensionsWithNonExistentFile() {
        File nonExistentFile = new File("non-existent-file.jpg");
        
        assertThatThrownBy(() -> jpegScaler.getImageDimensions(nonExistentFile))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("Image file does not exist");
    }
    
    @Test
    void testScaleImageCreatesOutputDirectory() throws IOException {
        File inputFile = createTestImage(100, 100);
        File outputDir = tempDir.resolve("new-directory").toFile();
        File outputFile = new File(outputDir, "output.jpg");
        
        jpegScaler.scaleImage(inputFile, outputFile, 50, 50, 0.8f);
        
        assertThat(outputDir).exists();
        assertThat(outputFile).exists();
    }
    
    @Test
    void testScaleImageWithDifferentQualitySettings() throws IOException {
        File inputFile = createTestImage(100, 100);
        File outputFileHighQuality = tempDir.resolve("output-high.jpg").toFile();
        File outputFileLowQuality = tempDir.resolve("output-low.jpg").toFile();
        
        jpegScaler.scaleImage(inputFile, outputFileHighQuality, 50, 50, 1.0f);
        jpegScaler.scaleImage(inputFile, outputFileLowQuality, 50, 50, 0.1f);
        
        assertThat(outputFileHighQuality).exists();
        assertThat(outputFileLowQuality).exists();
        
        // High quality file should generally be larger than low quality file
        assertThat(outputFileHighQuality.length()).isGreaterThan(outputFileLowQuality.length());
    }
    
    @Test
    void testScaleImageUpscaling() throws IOException {
        File inputFile = createTestImage(50, 50);
        File outputFile = tempDir.resolve("output.jpg").toFile();
        
        jpegScaler.scaleImage(inputFile, outputFile, 100, 100, 0.8f);
        
        assertThat(outputFile).exists();
        
        Dimension outputDimensions = jpegScaler.getImageDimensions(outputFile);
        assertThat(outputDimensions.width).isEqualTo(100);
        assertThat(outputDimensions.height).isEqualTo(100);
    }
    
    @Test
    void testScaleImageDownscaling() throws IOException {
        File inputFile = createTestImage(200, 200);
        File outputFile = tempDir.resolve("output.jpg").toFile();
        
        jpegScaler.scaleImage(inputFile, outputFile, 50, 50, 0.8f);
        
        assertThat(outputFile).exists();
        
        Dimension outputDimensions = jpegScaler.getImageDimensions(outputFile);
        assertThat(outputDimensions.width).isEqualTo(50);
        assertThat(outputDimensions.height).isEqualTo(50);
    }
    
    private File createTestImage(int width, int height) throws IOException {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        
        // Fill with a gradient for visual testing
        GradientPaint gradient = new GradientPaint(0, 0, Color.RED, width, height, Color.BLUE);
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, width, height);
        
        // Add some geometric shapes for detail
        g2d.setColor(Color.WHITE);
        g2d.fillOval(width/4, height/4, width/2, height/2);
        g2d.setColor(Color.BLACK);
        g2d.drawOval(width/4, height/4, width/2, height/2);
        
        g2d.dispose();
        
        File testFile = tempDir.resolve("test-" + width + "x" + height + ".jpg").toFile();
        ImageIO.write(image, "JPEG", testFile);
        
        return testFile;
    }
}