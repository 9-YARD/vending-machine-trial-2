#!/bin/zsh
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
export PATH="$JAVA_HOME/bin:$PATH"
cd /Users/ai/Documents/GitHub/vending-machine-trial-2
./gradlew assembleDebug --no-daemon "$@"
