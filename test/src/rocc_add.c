// Trying to do a simple addition command using the coprocessor
#include <rocc.h>

// Use global variables to avoid stack memory access issues
int a = 1;
int b = 2;
int c = 0;

int main() {
    ROCC_INSTRUCTION_DSS(0, c, &a, &b, 0);
    ROCC_INSTRUCTION_DSS(0, c, &a, &b, 0);
    return 0;
}
