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

good-no-prefixes() {
    run_test good-no-prefixes 0
}

good-prefixes() {
    run_test good-prefixes 0
}

good-refer-all() {
    run_test good-refer-all 0
}

ignore-unused() {
    run_test ignore-unused 0
}

ignore-paths() {
    run_test ignore-paths 0
}

bad-unsorted() {
    run_test bad-unsorted 1
}

bad-unused() {
    run_test bad-unused 1
}

bad-prefixes() {
    run_test bad-prefixes 1
}

bad-invalid-files() {
    run_test bad-invalid-files 1
}

all() {
    good-no-prefixes
    good-prefixes
    good-refer-all
    ignore-unused
    ignore-paths
    bad-unsorted
    bad-unused
    bad-prefixes
    bad-invalid-files
}

# Install locally & switch to test project dir
echo 'Installing locally...'
lein install

cd test-project

# Run specific tests like ./test.sh good-refer-all
# Run all tests like ./test.sh

if [ "$1" ]; then
    for test_fn in "$@"; do
        "$test_fn"
    done
else
    all
fi

echo -e "$GREEN""All tests completed successfully.""$RESET"
