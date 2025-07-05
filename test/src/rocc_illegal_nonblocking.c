#include <rocc.h>

int main() {
    int a,b;
    a = 1;
    b = 2;
    // Currently, 0x7F is an undefined funct7 code.
    // funct7 is 7 bits, in this case, all of them are 1.
    ROCC_INSTRUCTION_SS(0, &a, &b, ILLEGAL_ROCC_FUNCT7);
    return 0;
}
