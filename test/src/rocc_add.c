// Trying to do a simple addition command using the coprocessor
#include <rocc.h>

int main() {
    int a,b,c;
    a = 1;
    b = 2;
    ROCC_INSTRUCTION_DSS(0, c, &a, &b, 0);
    return 0;
}
