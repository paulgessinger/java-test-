# GUI Screenshot Generation Setup

## Overview

This document explains how GUI screenshot generation works in the CI pipeline and how to troubleshoot issues when screenshots are not being generated.

## How It Works

### Virtual Display Setup
1. **Xvfb (X Virtual Framebuffer)**: Creates a virtual display `:99` with resolution 1024x768x24
2. **Fluxbox Window Manager**: Provides window management for proper GUI rendering
3. **ImageMagick**: Used for screenshot capture and image manipulation

### Screenshot Generation Process
1. Start virtual display and window manager
2. Run GUI tests to ensure functionality
3. Build GUI JAR file
4. Create test images for demonstration
5. Launch GUI application in background
6. Capture screenshots at different stages
7. Create annotated and composite images
8. Upload as CI artifacts

## Common Issues and Solutions

### Issue 1: "No files were found with the provided path: gui-*.png"

**Cause**: Screenshot files are not being created with the expected naming pattern.

**Solutions**:
1. Check if the GUI application is actually starting
2. Verify virtual display is working
3. Ensure ImageMagick `import` command is available
4. Check file permissions and disk space

### Issue 2: GUI Application Not Starting

**Possible Causes**:
- Missing DISPLAY environment variable
- Virtual display not running
- GUI JAR file not found
- Java AWT/Swing issues in headless environment

**Debugging Steps**:
```bash
# Check if display is available
echo $DISPLAY
xdpyinfo

# Check if GUI JAR exists
ls -la target/jpeg-scaler-gui-1.0.0.jar

# Test GUI startup manually
java -jar target/jpeg-scaler-gui-1.0.0.jar &
```

### Issue 3: Screenshots Are Black/Empty

**Possible Causes**:
- Window manager not running
- GUI window not visible
- Timing issues (GUI not fully loaded)

**Solutions**:
- Increase wait times after GUI startup
- Ensure window manager is running
- Take multiple screenshots at different intervals

## Testing Locally

Use the provided test script to debug screenshot generation:

```bash
# Make script executable
chmod +x test-gui-screenshot.sh

# Run the test
./test-gui-screenshot.sh
```

The script will:
1. Set up virtual display if needed
2. Build GUI JAR if missing
3. Create test images
4. Launch GUI and capture screenshots
5. List all created files with sizes

## CI Workflow Configuration

### Environment Setup
```yaml
- name: Set up virtual display for GUI testing
  run: |
    sudo apt-get update
    sudo apt-get install -y xvfb imagemagick fluxbox
    export DISPLAY=:99
    Xvfb :99 -screen 0 1024x768x24 > /dev/null 2>&1 &
    DISPLAY=:99 fluxbox > /dev/null 2>&1 &
    sleep 5
    DISPLAY=:99 xdpyinfo
```

### Screenshot Generation
```yaml
- name: Create GUI screenshots
  run: |
    export DISPLAY=:99
    # Create test image
    convert -size 800x600 xc:lightblue -fill darkblue -draw "circle 400,300 400,100" gui-demo-input.jpg
    
    # Start GUI and capture screenshots
    DISPLAY=:99 java -jar target/jpeg-scaler-gui-1.0.0.jar &
    GUI_PID=$!
    sleep 8
    DISPLAY=:99 import -window root gui-screenshot-1-with-gui.png
    
    # Cleanup
    kill $GUI_PID 2>/dev/null || true
```

### Artifact Upload
```yaml
- name: Upload GUI screenshots and test artifacts
  uses: actions/upload-artifact@v4
  if: always()
  with:
    name: gui-screenshots-java-${{ matrix.java }}
    path: |
      gui-*.png
      gui-*.jpg
      screenshot-*.png
      test-input.jpg
      target/*.jar
```

## File Naming Conventions

The CI workflow creates files with these patterns:
- `gui-screenshot-*.png` - Main GUI screenshots
- `gui-demo-input.jpg` - Test input image
- `screenshot-*.png` - Additional screenshots from test script
- `gui-screenshots-*.png` - Composite/processed images

## Troubleshooting Commands

### Check Virtual Display
```bash
echo $DISPLAY
ps aux | grep Xvfb
ps aux | grep fluxbox
xdpyinfo
```

### Check GUI Process
```bash
ps aux | grep java
ps aux | grep jpeg-scaler
```

### Check Screenshots
```bash
ls -la *.png *.jpg
find . -name "gui-*" -type f
```

### Manual Screenshot Test
```bash
export DISPLAY=:99
import -window root test-screenshot.png
ls -la test-screenshot.png
```

## Expected Artifacts

After successful CI run, you should see artifacts named:
- `gui-screenshots-java-11`
- `gui-screenshots-java-17` 
- `gui-screenshots-java-21`

Each artifact should contain:
- GUI screenshot files (PNG format)
- Test input images (JPG format)
- Built JAR files
- Composite/annotated images

## Performance Considerations

- Screenshot generation adds ~30-60 seconds to CI runtime
- Virtual display uses minimal resources
- Screenshot files are typically 50-500KB each
- Artifacts are retained for 14 days

## Security Notes

- Virtual display runs in isolated container
- No actual display server access required
- Screenshot files contain no sensitive information
- GUI application runs with minimal permissions

## Future Improvements

1. **Interactive Screenshots**: Simulate user interactions before capturing
2. **Multi-Resolution**: Test GUI at different screen resolutions
3. **Animated GIFs**: Create animated demonstrations
4. **Visual Regression**: Compare screenshots across versions
5. **Mobile Simulation**: Test GUI scaling on different DPI settings

## Support

If screenshot generation continues to fail:
1. Check the "Debug screenshot files" step in CI logs
2. Run the test script locally to reproduce issues
3. Verify all dependencies are installed
4. Check for Java/AWT compatibility issues
5. Review virtual display configuration