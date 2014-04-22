#!/bin/bash

file=target/sockjs-protocol-output.log

function show_failures() {
  sed -e "/FAIL: test_headersSanity/,+6d" "$file" |
  exit 1
}

numFailures=`grep -cE "^ERROR:|FAIL:" "$file"`

declare -a expectedFailures=(
  # Ignore the Content-Length check on upgrade responses - Undertow
  # returns 0 but the tests want the header unset
  `grep -A 5 "FAIL: test_headersSanity" "$file" | grep -c "content-length"`
)

expectedFailureCount=0;
for failure in "${expectedFailures[@]}"; do
  expectedFailureCount=$(($expectedFailureCount + $failure))
done

echo "Test failures:     $numFailures"
echo "Expected failures: $expectedFailureCount"

if [ "$numFailures" -gt "$expectedFailureCount" ]; then
  show_failures;
fi
