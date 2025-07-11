package com.example.jpegscaler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import javax.imageio.ImageIO;

import static org.assertj.core.api.Assertions.*;

class JpegScalerCLITest {
    
    @TempDir
    Path tempDir;
    
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;
    
    @BeforeEach
    void setUp() {
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
    }
    
    @Test
    void testCLIHelp() {
        String[] args = {"--help"};
        
        assertThatCode(() -> JpegScalerCLI.main(args))
                .doesNotThrowAnyException();
        
        String output = outContent.toString();
        assertThat(output).contains("usage:");
        assertThat(output).contains("jpeg-scaler");
        assertThat(output).contains("input");
        assertThat(output).contains("output");
        assertThat(output).contains("width");
        assertThat(output).contains("height");
    }
    
    @Test
    void testCLIVersion() {
        String[] args = {"--version"};
        
        assertThatCode(() -> JpegScalerCLI.main(args))
                .doesNotThrowAnyException();
        
        String output = outContent.toString();
        assertThat(output).contains("jpeg-scaler version 1.0.0");
    }
    
    @Test
    void testCLIWithMissingInputFile() {
        String[] args = {"--output", "output.jpg", "--width", "100", "--height", "100"};
        
        assertThatCode(() -> JpegScalerCLI.main(args))
                .doesNotThrowAnyException(); // main() handles exceptions internally
        
        String errorOutput = errContent.toString();
        assertThat(errorOutput).contains("Both input and output files are required");
    }
    
    @Test
    void testCLIWithMissingOutputFile() {
        String[] args = {"--input", "input.jpg", "--width", "100", "--height", "100"};
        
        assertThatCode(() -> JpegScalerCLI.main(args))
                .doesNotThrowAnyException(); // main() handles exceptions internally
        
        String errorOutput = errContent.toString();
        assertThat(errorOutput).contains("Both input and output files are required");
    }
    
    @Test
    void testCLIWithMissingDimensions() throws IOException {
        File inputFile = createTestImage(100, 100);
        File outputFile = tempDir.resolve("output.jpg").toFile();
        
        String[] args = {"--input", inputFile.getAbsolutePath(), "--output", outputFile.getAbsolutePath()};
        
        assertThatCode(() -> JpegScalerCLI.main(args))
                .doesNotThrowAnyException(); // main() handles exceptions internally
        
        String errorOutput = errContent.toString();
        assertThat(errorOutput).contains("At least one dimension parameter is required");
    }
    
    @Test
    void testCLIWithExactDimensions() throws IOException {
        File inputFile = createTestImage(100, 100);
        File outputFile = tempDir.resolve("output.jpg").toFile();
        
        String[] args = {
            "--input", inputFile.getAbsolutePath(),
            "--output", outputFile.getAbsolutePath(),
            "--width", "50",
            "--height", "50"
        };
        
        assertThatCode(() -> JpegScalerCLI.main(args))
                .doesNotThrowAnyException();
        
        assertThat(outputFile).exists();
        
        JpegScaler scaler = new JpegScaler();
        Dimension dimensions = scaler.getImageDimensions(outputFile);
        assertThat(dimensions.width).isEqualTo(50);
        assertThat(dimensions.height).isEqualTo(50);
        
        String output = outContent.toString();
        assertThat(output).contains("Image scaling completed successfully!");
    }
    
    @Test
    void testCLIWithMaxDimensions() throws IOException {
        File inputFile = createTestImage(200, 100); // 2:1 aspect ratio
        File outputFile = tempDir.resolve("output.jpg").toFile();
        
        String[] args = {
            "--input", inputFile.getAbsolutePath(),
            "--output", outputFile.getAbsolutePath(),
            "--max-width", "100",
            "--max-height", "100"
        };
        
        assertThatCode(() -> JpegScalerCLI.main(args))
                .doesNotThrowAnyException();
        
        assertThat(outputFile).exists();
        
        JpegScaler scaler = new JpegScaler();
        Dimension dimensions = scaler.getImageDimensions(outputFile);
        assertThat(dimensions.width).isEqualTo(100);
        assertThat(dimensions.height).isEqualTo(50); // Maintains 2:1 aspect ratio
        
        String output = outContent.toString();
        assertThat(output).contains("Image scaling completed successfully!");
    }
    
    @Test
    void testCLIWithOnlyWidth() throws IOException {
        File inputFile = createTestImage(100, 200); // 1:2 aspect ratio
        File outputFile = tempDir.resolve("output.jpg").toFile();
        
        String[] args = {
            "--input", inputFile.getAbsolutePath(),
            "--output", outputFile.getAbsolutePath(),
            "--width", "50"
        };
        
        assertThatCode(() -> JpegScalerCLI.main(args))
                .doesNotThrowAnyException();
        
        assertThat(outputFile).exists();
        
        JpegScaler scaler = new JpegScaler();
        Dimension dimensions = scaler.getImageDimensions(outputFile);
        assertThat(dimensions.width).isEqualTo(50);
        assertThat(dimensions.height).isEqualTo(100); // Maintains 1:2 aspect ratio
        
        String output = outContent.toString();
        assertThat(output).contains("Image scaling completed successfully!");
    }
    
    @Test
    void testCLIWithOnlyHeight() throws IOException {
        File inputFile = createTestImage(200, 100); // 2:1 aspect ratio
        File outputFile = tempDir.resolve("output.jpg").toFile();
        
        String[] args = {
            "--input", inputFile.getAbsolutePath(),
            "--output", outputFile.getAbsolutePath(),
            "--height", "50"
        };
        
        assertThatCode(() -> JpegScalerCLI.main(args))
                .doesNotThrowAnyException();
        
        assertThat(outputFile).exists();
        
        JpegScaler scaler = new JpegScaler();
        Dimension dimensions = scaler.getImageDimensions(outputFile);
        assertThat(dimensions.width).isEqualTo(100); // Maintains 2:1 aspect ratio
        assertThat(dimensions.height).isEqualTo(50);
        
        String output = outContent.toString();
        assertThat(output).contains("Image scaling completed successfully!");
    }
    
    @Test
    void testCLIWithQuality() throws IOException {
        File inputFile = createTestImage(100, 100);
        File outputFile = tempDir.resolve("output.jpg").toFile();
        
        String[] args = {
            "--input", inputFile.getAbsolutePath(),
            "--output", outputFile.getAbsolutePath(),
            "--width", "50",
            "--height", "50",
            "--quality", "0.9"
        };
        
        assertThatCode(() -> JpegScalerCLI.main(args))
                .doesNotThrowAnyException();
        
        assertThat(outputFile).exists();
        
        String output = outContent.toString();
        assertThat(output).contains("Image scaling completed successfully!");
    }
    
    @Test
    void testCLIWithVerbose() throws IOException {
        File inputFile = createTestImage(100, 100);
        File outputFile = tempDir.resolve("output.jpg").toFile();
        
        String[] args = {
            "--input", inputFile.getAbsolutePath(),
            "--output", outputFile.getAbsolutePath(),
            "--width", "50",
            "--height", "50",
            "--verbose"
        };
        
        assertThatCode(() -> JpegScalerCLI.main(args))
                .doesNotThrowAnyException();
        
        assertThat(outputFile).exists();
        
        String output = outContent.toString();
        assertThat(output).contains("Input file:");
        assertThat(output).contains("Output file:");
        assertThat(output).contains("Quality:");
        assertThat(output).contains("Original dimensions:");
        assertThat(output).contains("Scaled dimensions:");
        assertThat(output).contains("Image scaling completed successfully!");
    }
    
    @Test
    void testCLIWithNonExistentInputFile() {
        File outputFile = tempDir.resolve("output.jpg").toFile();
        
        String[] args = {
            "--input", "non-existent-file.jpg",
            "--output", outputFile.getAbsolutePath(),
            "--width", "50",
            "--height", "50"
        };
        
        assertThatCode(() -> JpegScalerCLI.main(args))
                .doesNotThrowAnyException(); // main() handles exceptions internally
        
        String errorOutput = errContent.toString();
        assertThat(errorOutput).contains("Error processing image:");
        assertThat(errorOutput).contains("Input file does not exist");
    }
    
    @Test
    void testCLIWithInvalidQuality() throws IOException {
        File inputFile = createTestImage(100, 100);
        File outputFile = tempDir.resolve("output.jpg").toFile();
        
        String[] args = {
            "--input", inputFile.getAbsolutePath(),
            "--output", outputFile.getAbsolutePath(),
            "--width", "50",
            "--height", "50",
            "--quality", "1.5"
        };
        
        assertThatCode(() -> JpegScalerCLI.main(args))
                .doesNotThrowAnyException(); // main() handles exceptions internally
        
        String errorOutput = errContent.toString();
        assertThat(errorOutput).contains("Quality must be between 0.0 and 1.0");
    }
    
    @Test
    void testCLIWithInvalidDimensions() throws IOException {
        File inputFile = createTestImage(100, 100);
        File outputFile = tempDir.resolve("output.jpg").toFile();
        
        String[] args = {
            "--input", inputFile.getAbsolutePath(),
            "--output", outputFile.getAbsolutePath(),
            "--width", "-50",
            "--height", "50"
        };
        
        assertThatCode(() -> JpegScalerCLI.main(args))
                .doesNotThrowAnyException(); // main() handles exceptions internally
        
        String errorOutput = errContent.toString();
        assertThat(errorOutput).contains("Width and height must be positive integers");
    }
    
    @Test
    void testCLIShortOptions() throws IOException {
        File inputFile = createTestImage(100, 100);
        File outputFile = tempDir.resolve("output.jpg").toFile();
        
        String[] args = {
            "-i", inputFile.getAbsolutePath(),
            "-o", outputFile.getAbsolutePath(),
            "-w", "50",
            "-h", "50",
            "-q", "0.9",
            "-v"
        };
        
        assertThatCode(() -> JpegScalerCLI.main(args))
                .doesNotThrowAnyException();
        
        assertThat(outputFile).exists();
        
        String output = outContent.toString();
        assertThat(output).contains("Image scaling completed successfully!");
        assertThat(output).contains("Input file:");
        assertThat(output).contains("Output file:");
    }
    
    private File createTestImage(int width, int height) throws IOException {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        
        // Fill with a simple color pattern
        g2d.setColor(Color.RED);
        g2d.fillRect(0, 0, width/2, height/2);
        g2d.setColor(Color.GREEN);
        g2d.fillRect(width/2, 0, width/2, height/2);
        g2d.setColor(Color.BLUE);
        g2d.fillRect(0, height/2, width/2, height/2);
        g2d.setColor(Color.YELLOW);
        g2d.fillRect(width/2, height/2, width/2, height/2);
        
        g2d.dispose();
        
        File testFile = tempDir.resolve("test-input-" + width + "x" + height + ".jpg").toFile();
        ImageIO.write(image, "JPEG", testFile);
        
        return testFile;
    }
}