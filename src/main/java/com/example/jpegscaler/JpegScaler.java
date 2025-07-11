package com.example.jpegscaler;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 * Service class for scaling JPEG images.
 */
public class JpegScaler {
    
    /**
     * Scales a JPEG image to the specified dimensions.
     *
     * @param inputFile  the input JPEG file
     * @param outputFile the output JPEG file
     * @param width      the target width
     * @param height     the target height
     * @param quality    the JPEG quality (0.0f to 1.0f)
     * @throws IOException if an I/O error occurs
     */
    public void scaleImage(File inputFile, File outputFile, int width, int height, float quality) throws IOException {
        if (!inputFile.exists()) {
            throw new IOException("Input file does not exist: " + inputFile.getPath());
        }
        
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Width and height must be positive integers");
        }
        
        if (quality < 0.0f || quality > 1.0f) {
            throw new IllegalArgumentException("Quality must be between 0.0 and 1.0");
        }
        
        BufferedImage originalImage = ImageIO.read(inputFile);
        if (originalImage == null) {
            throw new IOException("Could not read image from file: " + inputFile.getPath());
        }
        
        BufferedImage scaledImage = scaleImage(originalImage, width, height);
        
        // Create output directory if it doesn't exist
        File outputDir = outputFile.getParentFile();
        if (outputDir != null && !outputDir.exists()) {
            outputDir.mkdirs();
        }
        
        // Write the scaled image
        if (!ImageIO.write(scaledImage, "JPEG", outputFile)) {
            throw new IOException("Could not write JPEG image to file: " + outputFile.getPath());
        }
    }
    
    /**
     * Scales a JPEG image maintaining aspect ratio.
     *
     * @param inputFile  the input JPEG file
     * @param outputFile the output JPEG file
     * @param maxWidth   the maximum width
     * @param maxHeight  the maximum height
     * @param quality    the JPEG quality (0.0f to 1.0f)
     * @throws IOException if an I/O error occurs
     */
    public void scaleImageMaintainAspectRatio(File inputFile, File outputFile, int maxWidth, int maxHeight, float quality) throws IOException {
        if (!inputFile.exists()) {
            throw new IOException("Input file does not exist: " + inputFile.getPath());
        }
        
        BufferedImage originalImage = ImageIO.read(inputFile);
        if (originalImage == null) {
            throw new IOException("Could not read image from file: " + inputFile.getPath());
        }
        
        Dimension scaledDimension = calculateScaledDimension(
            originalImage.getWidth(), 
            originalImage.getHeight(), 
            maxWidth, 
            maxHeight
        );
        
        scaleImage(inputFile, outputFile, scaledDimension.width, scaledDimension.height, quality);
    }
    
    /**
     * Scales a BufferedImage to the specified dimensions using high-quality scaling.
     *
     * @param originalImage the original image
     * @param width         the target width
     * @param height        the target height
     * @return the scaled image
     */
    private BufferedImage scaleImage(BufferedImage originalImage, int width, int height) {
        BufferedImage scaledImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = scaledImage.createGraphics();
        
        // Set high-quality rendering hints
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        g2d.drawImage(originalImage, 0, 0, width, height, null);
        g2d.dispose();
        
        return scaledImage;
    }
    
    /**
     * Calculates the scaled dimensions while maintaining aspect ratio.
     *
     * @param originalWidth  the original width
     * @param originalHeight the original height
     * @param maxWidth       the maximum width
     * @param maxHeight      the maximum height
     * @return the scaled dimensions
     */
    private Dimension calculateScaledDimension(int originalWidth, int originalHeight, int maxWidth, int maxHeight) {
        double widthRatio = (double) maxWidth / originalWidth;
        double heightRatio = (double) maxHeight / originalHeight;
        double ratio = Math.min(widthRatio, heightRatio);
        
        int scaledWidth = (int) (originalWidth * ratio);
        int scaledHeight = (int) (originalHeight * ratio);
        
        return new Dimension(scaledWidth, scaledHeight);
    }
    
    /**
     * Gets the dimensions of an image file.
     *
     * @param imageFile the image file
     * @return the dimensions of the image
     * @throws IOException if an I/O error occurs
     */
    public Dimension getImageDimensions(File imageFile) throws IOException {
        if (!imageFile.exists()) {
            throw new IOException("Image file does not exist: " + imageFile.getPath());
        }
        
        BufferedImage image = ImageIO.read(imageFile);
        if (image == null) {
            throw new IOException("Could not read image from file: " + imageFile.getPath());
        }
        
        return new Dimension(image.getWidth(), image.getHeight());
    }
}