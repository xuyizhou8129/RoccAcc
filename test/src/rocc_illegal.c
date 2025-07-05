#include <rocc.h>

int main() {
    int a,b,c;
    a = 1;
    b = 2;
    ROCC_INSTRUCTION_DSS(0, c, &a, &b, ILLEGAL_ROCC_FUNCT7);
    return 0;
}
