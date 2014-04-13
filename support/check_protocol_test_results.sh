#!/bin/bash

file=target/sockjs-protocol-output.txt

function show_failures() {
  sed -e "/FAIL: test_headersSanity/,+6d" "$file"
  exit 1
}

numFailures=`grep -cE "^ERROR:|FAIL:" "$file"`
# Ignore the Content-Length check on upgrade responses - Undertow
# returns 0 but the tests want the header unset
isContentLength=`grep -A 5 "FAIL: test_headersSanity" "$file" | grep -c "content-length"`

if [ "$numFailures" -gt 1 ]; then
  show_failures;
elif [ "$numFailures" -eq 1 ]; then
  if [ ! "$isContentLength" -eq 1 ]; then
    show_failures;
  fi
fi
