package com.example.jpegscaler;

import org.apache.commons.cli.*;

import java.awt.*;
import java.io.File;
import java.io.IOException;

/**
 * Command line interface for the JPEG Scaler application.
 */
public class JpegScalerCLI {
    
    private static final String PROGRAM_NAME = "jpeg-scaler";
    private static final float DEFAULT_QUALITY = 0.8f;
    
    public static void main(String[] args) {
        Options options = createOptions();
        CommandLineParser parser = new DefaultParser();
        
        try {
            CommandLine cmd = parser.parse(options, args);
            
            if (cmd.hasOption("help")) {
                printHelp(options);
                return;
            }
            
            if (cmd.hasOption("version")) {
                printVersion();
                return;
            }
            
            // Validate required arguments
            if (!cmd.hasOption("input") || !cmd.hasOption("output")) {
                System.err.println("Error: Both input and output files are required.");
                printHelp(options);
                System.exit(1);
            }
            
            if (!cmd.hasOption("width") && !cmd.hasOption("height") && !cmd.hasOption("max-width") && !cmd.hasOption("max-height")) {
                System.err.println("Error: At least one dimension parameter is required (width, height, max-width, or max-height).");
                printHelp(options);
                System.exit(1);
            }
            
            // Parse arguments
            String inputPath = cmd.getOptionValue("input");
            String outputPath = cmd.getOptionValue("output");
            float quality = Float.parseFloat(cmd.getOptionValue("quality", String.valueOf(DEFAULT_QUALITY)));
            boolean verbose = cmd.hasOption("verbose");
            
            File inputFile = new File(inputPath);
            File outputFile = new File(outputPath);
            
            JpegScaler scaler = new JpegScaler();
            
            if (verbose) {
                System.out.println("Input file: " + inputFile.getAbsolutePath());
                System.out.println("Output file: " + outputFile.getAbsolutePath());
                System.out.println("Quality: " + quality);
                
                try {
                    Dimension originalDimensions = scaler.getImageDimensions(inputFile);
                    System.out.println("Original dimensions: " + originalDimensions.width + "x" + originalDimensions.height);
                } catch (IOException e) {
                    System.err.println("Warning: Could not read original image dimensions: " + e.getMessage());
                }
            }
            
            // Determine scaling mode
            if (cmd.hasOption("max-width") || cmd.hasOption("max-height")) {
                // Aspect ratio preserving mode
                int maxWidth = Integer.parseInt(cmd.getOptionValue("max-width", "10000"));
                int maxHeight = Integer.parseInt(cmd.getOptionValue("max-height", "10000"));
                
                if (verbose) {
                    System.out.println("Scaling with aspect ratio preservation. Max dimensions: " + maxWidth + "x" + maxHeight);
                }
                
                scaler.scaleImageMaintainAspectRatio(inputFile, outputFile, maxWidth, maxHeight, quality);
            } else {
                // Exact dimensions mode
                int width = Integer.parseInt(cmd.getOptionValue("width", "0"));
                int height = Integer.parseInt(cmd.getOptionValue("height", "0"));
                
                // If only one dimension is specified, maintain aspect ratio
                if (width == 0 || height == 0) {
                    try {
                        Dimension originalDimensions = scaler.getImageDimensions(inputFile);
                        if (width == 0) {
                            width = (int) ((double) height * originalDimensions.width / originalDimensions.height);
                        } else {
                            height = (int) ((double) width * originalDimensions.height / originalDimensions.width);
                        }
                    } catch (IOException e) {
                        System.err.println("Error: Could not read original image dimensions to calculate missing dimension: " + e.getMessage());
                        System.exit(1);
                    }
                }
                
                if (verbose) {
                    System.out.println("Scaling to exact dimensions: " + width + "x" + height);
                }
                
                scaler.scaleImage(inputFile, outputFile, width, height, quality);
            }
            
            if (verbose) {
                try {
                    Dimension scaledDimensions = scaler.getImageDimensions(outputFile);
                    System.out.println("Scaled dimensions: " + scaledDimensions.width + "x" + scaledDimensions.height);
                } catch (IOException e) {
                    System.err.println("Warning: Could not read scaled image dimensions: " + e.getMessage());
                }
            }
            
            System.out.println("Image scaling completed successfully!");
            
        } catch (ParseException e) {
            System.err.println("Error parsing command line arguments: " + e.getMessage());
            printHelp(options);
            System.exit(1);
        } catch (NumberFormatException e) {
            System.err.println("Error: Invalid number format: " + e.getMessage());
            System.exit(1);
        } catch (IllegalArgumentException e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Error processing image: " + e.getMessage());
            System.exit(1);
        }
    }
    
    private static Options createOptions() {
        Options options = new Options();
        
        options.addOption(Option.builder("i")
                .longOpt("input")
                .hasArg()
                .desc("Input JPEG file path")
                .build());
        
        options.addOption(Option.builder("o")
                .longOpt("output")
                .hasArg()
                .desc("Output JPEG file path")
                .build());
        
        options.addOption(Option.builder("w")
                .longOpt("width")
                .hasArg()
                .desc("Target width in pixels")
                .build());
        
        options.addOption(Option.builder("h")
                .longOpt("height")
                .hasArg()
                .desc("Target height in pixels")
                .build());
        
        options.addOption(Option.builder("mw")
                .longOpt("max-width")
                .hasArg()
                .desc("Maximum width in pixels (maintains aspect ratio)")
                .build());
        
        options.addOption(Option.builder("mh")
                .longOpt("max-height")
                .hasArg()
                .desc("Maximum height in pixels (maintains aspect ratio)")
                .build());
        
        options.addOption(Option.builder("q")
                .longOpt("quality")
                .hasArg()
                .desc("JPEG quality (0.0 to 1.0, default: " + DEFAULT_QUALITY + ")")
                .build());
        
        options.addOption(Option.builder("v")
                .longOpt("verbose")
                .desc("Enable verbose output")
                .build());
        
        options.addOption(Option.builder()
                .longOpt("help")
                .desc("Show help message")
                .build());
        
        options.addOption(Option.builder()
                .longOpt("version")
                .desc("Show version information")
                .build());
        
        return options;
    }
    
    private static void printHelp(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(PROGRAM_NAME, 
                "A command line tool for scaling JPEG images\n\n", 
                options, 
                "\nExamples:\n" +
                "  " + PROGRAM_NAME + " -i input.jpg -o output.jpg -w 800 -h 600\n" +
                "  " + PROGRAM_NAME + " -i input.jpg -o output.jpg --max-width 1024\n" +
                "  " + PROGRAM_NAME + " -i input.jpg -o output.jpg -w 800 -q 0.9 -v\n");
    }
    
    private static void printVersion() {
        System.out.println(PROGRAM_NAME + " version 1.0.0");
    }
}