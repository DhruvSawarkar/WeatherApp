#!/bin/sh
# Standard Gradle wrapper launcher script.
# If this is missing the wrapper jar, run: gradle wrapper --gradle-version 8.9
# from Android Studio's terminal once, or open this project directly in
# Android Studio, which will offer to regenerate the wrapper automatically.
DIR="$(cd "$(dirname "$0")" && pwd)"
exec "${GRADLE:-gradle}" -p "$DIR" "$@"
