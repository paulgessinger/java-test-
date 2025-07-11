#!/bin/bash

# Test script for GUI screenshot generation
# This script can be used to test screenshot generation locally or in CI

set -e

echo "=== GUI Screenshot Test Script ==="

# Check if we have a display
if [ -z "$DISPLAY" ]; then
    echo "No DISPLAY set. Setting up virtual display..."
    export DISPLAY=:99
    
    # Start Xvfb if not already running
    if ! pgrep -x "Xvfb" > /dev/null; then
        echo "Starting Xvfb..."
        Xvfb :99 -screen 0 1024x768x24 > /dev/null 2>&1 &
        XVFB_PID=$!
        echo "Xvfb started with PID: $XVFB_PID"
        sleep 3
    fi
    
    # Start window manager if not already running
    if ! pgrep -x "fluxbox" > /dev/null; then
        echo "Starting fluxbox window manager..."
        fluxbox > /dev/null 2>&1 &
        WM_PID=$!
        echo "Fluxbox started with PID: $WM_PID"
        sleep 2
    fi
fi

echo "Using DISPLAY: $DISPLAY"

# Verify display is working
echo "Testing display..."
xdpyinfo | head -5

# Check if GUI JAR exists
GUI_JAR="target/jpeg-scaler-gui-1.0.0.jar"
if [ ! -f "$GUI_JAR" ]; then
    echo "GUI JAR not found at $GUI_JAR"
    echo "Building GUI JAR..."
    mvn clean package -DskipTests
fi

if [ ! -f "$GUI_JAR" ]; then
    echo "ERROR: GUI JAR still not found after build!"
    exit 1
fi

echo "GUI JAR found: $GUI_JAR"

# Create a test image
echo "Creating test image..."
convert -size 800x600 xc:lightblue -fill darkblue -draw "circle 400,300 400,100" -pointsize 48 -annotate +200+320 "Test Image for GUI" test-input.jpg

# Take initial desktop screenshot
echo "Taking initial desktop screenshot..."
import -window root screenshot-0-initial.png

# Start GUI application
echo "Starting GUI application..."
java -jar "$GUI_JAR" &
GUI_PID=$!
echo "GUI started with PID: $GUI_PID"

# Wait for GUI to load
echo "Waiting for GUI to load..."
sleep 5

# Take screenshot with GUI
echo "Taking screenshot with GUI..."
import -window root screenshot-1-with-gui.png

# Try to find the GUI window specifically
echo "Looking for GUI window..."
GUI_WINDOW=$(xwininfo -root -tree | grep -i "jpeg\|scaler" | head -1 | awk '{print $1}' || echo "")
if [ -n "$GUI_WINDOW" ]; then
    echo "Found GUI window: $GUI_WINDOW"
    import -window "$GUI_WINDOW" screenshot-2-gui-window.png
else
    echo "GUI window not found in window list"
    echo "Available windows:"
    xwininfo -root -tree | grep -E "^\s+0x[0-9a-f]+" | head -10
fi

# Wait a bit more
sleep 3

# Take final screenshot
echo "Taking final screenshot..."
import -window root screenshot-3-final.png

# Create annotated screenshot
echo "Creating annotated screenshot..."
convert screenshot-1-with-gui.png -pointsize 24 -fill red -annotate +10+30 "JPEG Scaler GUI Test" screenshot-annotated.png

# Clean up GUI
echo "Cleaning up GUI process..."
kill $GUI_PID 2>/dev/null || true
sleep 2

# List created files
echo "=== Created Files ==="
ls -la screenshot-*.png test-input.jpg 2>/dev/null || echo "No files created"

# Show file sizes to verify they're not empty
echo "=== File Sizes ==="
for file in screenshot-*.png; do
    if [ -f "$file" ]; then
        size=$(stat -f%z "$file" 2>/dev/null || stat -c%s "$file" 2>/dev/null || echo "unknown")
        echo "$file: $size bytes"
    fi
done

echo "=== Screenshot test completed ==="

# Cleanup virtual display if we started it
if [ -n "$XVFB_PID" ]; then
    echo "Cleaning up Xvfb (PID: $XVFB_PID)..."
    kill $XVFB_PID 2>/dev/null || true
fi

if [ -n "$WM_PID" ]; then
    echo "Cleaning up window manager (PID: $WM_PID)..."
    kill $WM_PID 2>/dev/null || true
fi