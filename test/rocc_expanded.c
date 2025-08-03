# 0 "src/rocc_add.c"
# 0 "<built-in>"
# 0 "<command-line>"
# 1 "/usr/include/stdc-predef.h" 1 3 4
# 0 "<command-line>" 2
# 1 "src/rocc_add.c"

# 1 "include/rocc.h" 1
# 3 "src/rocc_add.c" 2

int main() {
    int a,b,c;
    a = 1;
    b = 2;
    do { __asm__ __volatile__ ( ".insn r CUSTOM_" "0" ", %3, %4, %0, %1, %2\n\t" : "=r" (c) : "r" (a), "r" (b), "i" (0x4 | 0x2 | 0x1), "i" (0)); } while (0);
    return 0;
}
