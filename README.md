# JPEG Scaler

A high-performance Java command-line tool for scaling JPEG images with configurable output dimensions and quality settings.

## Features

- **Flexible Scaling Options**: Scale to exact dimensions or maintain aspect ratio
- **Quality Control**: Configurable JPEG compression quality (0.0 to 1.0)
- **Aspect Ratio Preservation**: Automatic aspect ratio calculation when only one dimension is specified
- **High-Quality Rendering**: Uses advanced graphics rendering for smooth scaling
- **Command Line Interface**: Easy-to-use CLI with comprehensive options
- **Cross-Platform**: Runs on any system with Java 11+
- **Zero Dependencies**: Self-contained executable JAR

## Requirements

- Java 11 or higher
- Input files must be valid JPEG images

## Installation

### Download Pre-built JAR

Download the latest `jpeg-scaler-1.0.0.jar` from the releases page.

### Build from Source

```bash
git clone <repository-url>
cd jpeg-scaler
mvn clean package
```

The executable JAR will be created at `target/jpeg-scaler-1.0.0.jar`.

## Usage

### Basic Syntax

```bash
java -jar jpeg-scaler-1.0.0.jar [OPTIONS]
```

### Required Options

- `-i, --input <file>`: Input JPEG file path
- `-o, --output <file>`: Output JPEG file path
- At least one dimension parameter (see Scaling Options below)

### Scaling Options

Choose one of the following scaling modes:

#### 1. Exact Dimensions
Scale to specific width and height (may distort aspect ratio):
```bash
java -jar jpeg-scaler-1.0.0.jar -i input.jpg -o output.jpg -w 800 -h 600
```

#### 2. Single Dimension (Maintains Aspect Ratio)
Specify only width or height, the other dimension is calculated automatically:
```bash
# Scale to width of 800px, height calculated automatically
java -jar jpeg-scaler-1.0.0.jar -i input.jpg -o output.jpg -w 800

# Scale to height of 600px, width calculated automatically
java -jar jpeg-scaler-1.0.0.jar -i input.jpg -o output.jpg -h 600
```

#### 3. Maximum Dimensions (Maintains Aspect Ratio)
Scale within maximum bounds while preserving aspect ratio:
```bash
java -jar jpeg-scaler-1.0.0.jar -i input.jpg -o output.jpg --max-width 1024 --max-height 768
```

### Additional Options

- `-q, --quality <0.0-1.0>`: JPEG quality (default: 0.8)
- `-v, --verbose`: Enable verbose output showing dimensions and processing info
- `--help`: Show help message
- `--version`: Show version information

### Examples

#### Basic scaling with exact dimensions:
```bash
java -jar jpeg-scaler-1.0.0.jar -i photo.jpg -o photo_800x600.jpg -w 800 -h 600
```

#### Scale maintaining aspect ratio within bounds:
```bash
java -jar jpeg-scaler-1.0.0.jar -i photo.jpg -o photo_thumbnail.jpg --max-width 200 --max-height 200
```

#### High-quality scaling with verbose output:
```bash
java -jar jpeg-scaler-1.0.0.jar -i photo.jpg -o photo_hq.jpg -w 1920 -h 1080 -q 0.95 -v
```

#### Batch processing with shell script:
```bash
#!/bin/bash
for file in *.jpg; do
    java -jar jpeg-scaler-1.0.0.jar -i "$file" -o "scaled_$file" --max-width 800 --max-height 600 -q 0.9
done
```

## Output

### Verbose Mode Output
When using `-v` or `--verbose`, the tool provides detailed information:

```
Input file: /path/to/input.jpg
Output file: /path/to/output.jpg
Quality: 0.8
Original dimensions: 1920x1080
Scaling to exact dimensions: 800x600
Scaled dimensions: 800x600
Image scaling completed successfully!
```

### Error Handling
The tool provides clear error messages for common issues:

- Invalid file paths
- Unsupported image formats
- Invalid quality values
- Missing required parameters
- File permission issues

## Technical Details

### Image Processing
- **Algorithm**: Bilinear interpolation with high-quality rendering hints
- **Color Space**: RGB color space preservation
- **Quality Control**: Explicit JPEG compression quality control using ImageIO
- **Memory Efficient**: Processes images without loading entire file into memory unnecessarily

### Performance
- **Scaling**: Optimized for both upscaling and downscaling operations
- **Quality**: Maintains image quality through advanced rendering techniques
- **Speed**: Fast processing suitable for batch operations

### Supported Formats
- **Input**: JPEG/JPG files
- **Output**: JPEG files with configurable quality

## Development

### Building
```bash
mvn clean compile
```

### Testing
```bash
mvn test
```

### Packaging
```bash
mvn clean package
```

### Running Tests with Coverage
```bash
mvn clean test jacoco:report
```

## CI/CD

The project includes GitHub Actions workflow for:
- Multi-version Java testing (Java 11, 17, 21)
- Automated testing with real images
- End-to-end CLI validation
- Performance testing with large images
- Error condition verification

## Dependencies

### Runtime Dependencies
- Apache Commons CLI 1.5.0 (command line parsing)
- Apache Commons Imaging 1.0.0-alpha6 (enhanced image processing)

### Build Dependencies
- Maven 3.6+
- JUnit 5 (testing)
- AssertJ (test assertions)

## License

[Add your license information here]

## Contributing

1. Fork the repository
2. Create a feature branch
3. Add tests for new functionality
4. Ensure all tests pass
5. Submit a pull request

## Troubleshooting

### Common Issues

#### "Input file does not exist"
- Verify the input file path is correct
- Ensure the file has proper read permissions

#### "Quality must be between 0.0 and 1.0"
- Quality parameter must be a decimal between 0.0 (lowest) and 1.0 (highest)

#### "At least one dimension parameter is required"
- Specify at least one of: `-w`, `-h`, `--max-width`, or `--max-height`

#### Out of Memory Errors
- For very large images, increase JVM heap size:
```bash
java -Xmx2g -jar jpeg-scaler-1.0.0.jar [options]
```

### Performance Tips

1. **Batch Processing**: Process multiple files in a single script
2. **Quality Settings**: Use quality 0.8-0.9 for good balance of size/quality
3. **Memory**: For large images, ensure adequate heap space
4. **Disk Space**: Ensure sufficient disk space for output files

## Version History

### 1.0.0
- Initial release
- Basic scaling functionality
- Quality control
- Aspect ratio preservation
- Comprehensive CLI interface
- Full test coverage