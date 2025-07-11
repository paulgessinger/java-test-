# CI Screenshot Generation Troubleshooting Guide

## Common Issues and Fixes Applied

### Issue 1: DISPLAY Environment Variable Not Persisting

**Problem**: The `DISPLAY=:99` environment variable was being set within individual step scripts but not persisting across GitHub Actions steps.

**Fix Applied**:
```yaml
# OLD (doesn't persist):
- name: Set up virtual display
  run: |
    export DISPLAY=:99
    Xvfb :99 &

# NEW (persists across steps):
- name: Set up virtual display
  run: |
    export DISPLAY=:99
    echo "DISPLAY=:99" >> $GITHUB_ENV  # ✅ Persists to subsequent steps
    Xvfb :99 &
```

### Issue 2: Process Management Problems

**Problem**: Xvfb and window manager processes were starting without proper error checking and PID tracking.

**Fix Applied**:
```yaml
# Store PIDs in environment for cleanup
Xvfb :99 -screen 0 1024x768x24 -ac +extension GLX +render -noreset &
XVFB_PID=$!
echo "XVFB_PID=$XVFB_PID" >> $GITHUB_ENV

# Verify process started successfully
if ! ps -p $XVFB_PID > /dev/null; then
  echo "ERROR: Xvfb failed to start"
  exit 1
fi
```

### Issue 3: Missing Error Handling

**Problem**: Steps were failing silently without proper error detection.

**Fix Applied**:
```yaml
# Verify display connection before proceeding
if ! DISPLAY=:99 xdpyinfo > /dev/null 2>&1; then
  echo "ERROR: Cannot connect to display :99"
  ps aux | grep Xvfb
  exit 1
fi

# Check tools are available
which convert > /dev/null || (echo "ERROR: ImageMagick convert not found" && exit 1)
which import > /dev/null || (echo "ERROR: ImageMagick import not found" && exit 1)
```

### Issue 4: Timing and Synchronization Issues

**Problem**: Not enough time for processes to start and GUI to become ready.

**Fix Applied**:
```yaml
# Increased wait times
sleep 10  # Instead of 5-8 seconds

# Added process verification
if ps -p $GUI_PID > /dev/null; then
  echo "GUI process is running"
else
  echo "WARNING: GUI process may have exited"
fi
```

### Issue 5: Xvfb Configuration Problems

**Problem**: Basic Xvfb configuration was missing important flags for GUI applications.

**Fix Applied**:
```bash
# OLD (minimal config):
Xvfb :99 -screen 0 1024x768x24 > /dev/null 2>&1 &

# NEW (comprehensive config):
Xvfb :99 -screen 0 1024x768x24 -ac +extension GLX +render -noreset &
```

**Flags explained**:
- `-ac`: Disable access control (allows connections)
- `+extension GLX`: Enable OpenGL extension
- `+render`: Enable RENDER extension
- `-noreset`: Don't reset when last client disconnects

### Issue 6: Dependency Installation Problems

**Problem**: Installing all dependencies in one step could cause conflicts or timeouts.

**Fix Applied**:
```yaml
# Separate dependency installation from setup
- name: Install dependencies for GUI testing
  run: |
    sudo apt-get update
    sudo apt-get install -y xvfb imagemagick fluxbox xauth

- name: Set up virtual display for GUI testing
  run: |
    # Setup logic here
```

## Alternative Solution: Proven GitHub Action

Created `ci-alternative.yml` using the proven `pyvista/setup-headless-display-action`:

```yaml
- name: Setup headless display
  uses: pyvista/setup-headless-display-action@v3
  with:
    qt: false
    pyvista: false
```

**Benefits**:
- ✅ Tested and maintained by the community
- ✅ Handles all the complex setup automatically
- ✅ Works across different OS versions
- ✅ Includes proper error handling

## Debugging Steps Added

### 1. Process Verification
```bash
# Check if processes are running
ps aux | grep Xvfb
ps aux | grep fluxbox
ps -p $GUI_PID
```

### 2. Display Testing
```bash
# Test display connection
xdpyinfo | head -5
echo "DISPLAY: $DISPLAY"
```

### 3. Window Enumeration
```bash
# List all windows to find GUI
xwininfo -root -tree | grep -E "^\s+0x[0-9a-f]+" | head -10
```

### 4. File Size Verification
```bash
# Check screenshot files aren't empty
for file in gui-*.png; do
  if [ -f "$file" ]; then
    size=$(stat -c%s "$file" 2>/dev/null || echo "unknown")
    echo "$file: $size bytes"
  fi
done
```

## Process Cleanup

Added proper cleanup to prevent interference:

```yaml
- name: Cleanup virtual display processes
  if: always()
  run: |
    # Kill tracked processes
    kill $WM_PID 2>/dev/null || true
    kill $XVFB_PID 2>/dev/null || true
    
    # Clean up any remaining processes
    pkill -f "Xvfb :99" 2>/dev/null || true
    pkill -f "fluxbox" 2>/dev/null || true
```

## Expected Behavior After Fixes

### Successful Run Should Show:
1. ✅ Virtual display starts without errors
2. ✅ DISPLAY environment variable persists across steps
3. ✅ GUI tests pass without HeadlessException
4. ✅ GUI application starts successfully
5. ✅ Screenshots are captured and have non-zero file sizes
6. ✅ Artifacts are uploaded with screenshot files

### Log Output Should Include:
```
Virtual display :99 started successfully
Using DISPLAY: :99
GUI started with PID: 1234
GUI process is running
Captured GUI screenshot
Created annotated screenshot
gui-screenshot-with-gui.png: 45678 bytes
```

## Common Error Patterns and Solutions

### "Cannot connect to display"
- **Cause**: Xvfb not started or crashed
- **Solution**: Check Xvfb process status and logs
- **Prevention**: Added process verification steps

### "GUI started and exited as expected" but no screenshots
- **Cause**: GUI window not visible or ImageMagick issues
- **Solution**: Verify window manager is running
- **Prevention**: Added window enumeration and tool verification

### "No files matching gui-* pattern"
- **Cause**: Screenshot commands failing silently
- **Solution**: Added error checking for ImageMagick commands
- **Prevention**: Verify tools exist before using them

## Testing Locally

To test the setup locally:

```bash
# Use the test script
chmod +x test-gui-screenshot.sh
./test-gui-screenshot.sh

# Or manually test virtual display
export DISPLAY=:99
Xvfb :99 -screen 0 1024x768x24 -ac +extension GLX +render -noreset &
xdpyinfo
```

## Monitoring CI Runs

Check these sections in GitHub Actions logs:
1. **"Set up virtual display"** - Should show successful Xvfb start
2. **"Run GUI tests"** - Should show all tests passing
3. **"Create GUI screenshots"** - Should show file creation with sizes
4. **"Debug screenshot files"** - Should list created PNG files
5. **Artifacts** - Should contain `gui-screenshots-java-*` with files

## Performance Impact

The fixes add approximately:
- **30-60 seconds** to CI runtime (mostly wait times)
- **50-500KB** per screenshot file
- **Minimal CPU/memory** overhead from virtual display

## Next Steps

1. **Monitor CI runs** with the improved configuration
2. **Use alternative workflow** (`ci-alternative.yml`) if issues persist
3. **Check artifacts** for screenshot files
4. **Review logs** for any remaining error patterns
5. **Iterate on timing** if GUI startup is still inconsistent