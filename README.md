# JPEG Scaler CLI

A command-line tool for scaling JPEG images with configurable output dimensions and quality settings.

## Features

- Scale JPEG images to exact dimensions
- Scale images while maintaining aspect ratio
- Configurable JPEG quality settings
- Support for both upscaling and downscaling
- Verbose output mode for detailed information
- Comprehensive error handling
- Cross-platform compatibility

## Requirements

- Java 11 or later
- Maven 3.6+ (for building from source)

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

### Basic Usage

```bash
java -jar jpeg-scaler-1.0.0.jar [OPTIONS]
```

### Command Line Options

| Option | Short | Description |
|--------|-------|-------------|
| `--input` | `-i` | Input JPEG file path (required) |
| `--output` | `-o` | Output JPEG file path (required) |
| `--width` | `-w` | Target width in pixels |
| `--height` | `-h` | Target height in pixels |
| `--max-width` | `-mw` | Maximum width in pixels (maintains aspect ratio) |
| `--max-height` | `-mh` | Maximum height in pixels (maintains aspect ratio) |
| `--quality` | `-q` | JPEG quality (0.0 to 1.0, default: 0.8) |
| `--verbose` | `-v` | Enable verbose output |
| `--help` | | Show help message |
| `--version` | | Show version information |

### Examples

#### Scale to exact dimensions:
```bash
java -jar jpeg-scaler-1.0.0.jar -i input.jpg -o output.jpg -w 800 -h 600
```

#### Scale maintaining aspect ratio (fit within bounds):
```bash
java -jar jpeg-scaler-1.0.0.jar -i input.jpg -o output.jpg --max-width 1024 --max-height 768
```

#### Scale with only width specified (maintains aspect ratio):
```bash
java -jar jpeg-scaler-1.0.0.jar -i input.jpg -o output.jpg -w 800
```

#### Scale with only height specified (maintains aspect ratio):
```bash
java -jar jpeg-scaler-1.0.0.jar -i input.jpg -o output.jpg -h 600
```

#### Scale with custom quality:
```bash
java -jar jpeg-scaler-1.0.0.jar -i input.jpg -o output.jpg -w 800 -h 600 -q 0.9
```

#### Scale with verbose output:
```bash
java -jar jpeg-scaler-1.0.0.jar -i input.jpg -o output.jpg -w 800 -h 600 -v
```

## Scaling Modes

### Exact Dimensions Mode
When both `--width` and `--height` are specified, the image is scaled to the exact dimensions, potentially changing the aspect ratio.

### Aspect Ratio Preserving Mode
- When only `--width` is specified, height is calculated to maintain aspect ratio
- When only `--height` is specified, width is calculated to maintain aspect ratio
- When `--max-width` and/or `--max-height` are specified, the image is scaled to fit within the bounds while maintaining aspect ratio

### Quality Settings
The `--quality` parameter controls JPEG compression quality:
- `1.0` = Maximum quality (larger file size)
- `0.8` = Default quality (good balance)
- `0.1` = Minimum quality (smaller file size)

## Error Handling

The tool provides comprehensive error handling for:
- Missing or invalid input files
- Invalid dimension parameters
- Invalid quality values
- File I/O errors
- Unsupported image formats

## Development

### Building the Project

```bash
mvn clean compile
```

### Running Tests

```bash
mvn test
```

### Building the Executable JAR

```bash
mvn clean package
```

### Running Integration Tests

The project includes both unit tests and integration tests:

- **Unit Tests**: Test the `JpegScaler` service class functionality
- **Integration Tests**: Test the CLI interface and command-line argument parsing

## CI/CD

The project uses GitHub Actions for continuous integration, testing on multiple Java versions (11, 17, 21) and validating:

- Unit test execution
- Integration test execution  
- CLI functionality with real images
- Error handling scenarios
- Cross-platform compatibility

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Ensure all tests pass
6. Submit a pull request

## Support

For issues and questions, please open an issue on the GitHub repository.