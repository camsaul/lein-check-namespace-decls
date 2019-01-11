#! /usr/bin/env bash

GREEN='\033[0;32m'
RED='\033[0;31m'
BLUE_ON_GRAY='\033[1;34m\033[47m'
RESET='\033[0m'

run_test() {
    test_profile="$1"
    expected_status_code="$2"

    echo -e "$BLUE_ON_GRAY""DEBUG=true lein with-profile +$test_profile check-namespace-decls""$RESET"
    DEBUG=true lein with-profile +"$test_profile" check-namespace-decls

    actual_status_code="$?"
    echo "Expected status code $expected_status_code, got $actual_status_code"

    if [ "$actual_status_code" -eq "$expected_status_code" ]; then
        echo -e "$GREEN""[OK]\n""$RESET"
    else
        echo -e "$RED""Not all tests completed successfully.""$RESET"
        exit 1
    fi
}

cd test-project

run_test good 0
run_test good-prefixes 0
run_test ignore-unused 0
run_test ignore-paths 0
run_test bad-unsorted 1
run_test bad-unused 1
run_test bad-prefixes 1
run_test bad-invalid-files 1

echo -e "$GREEN""All tests completed successfully.""$RESET"
