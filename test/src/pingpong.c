#include <rocc.h>

int main() {
    int ping = 1;
    int pong = 0;
    int result;
    
    // Send ping value to accelerator (funct=0 for ADD operation)
    ROCC_INSTRUCTION_DSS(0, result, &ping, &pong, 0);
    
    // Send pong value back
    //ping = 0;
    //pong = 1;
    ROCC_INSTRUCTION_DSS(0, result, &ping, &pong, 0);
    
    // Test with different values
    //ping = 10;
    //pong = 20;
    ROCC_INSTRUCTION_DSS(0, result, &ping, &pong, 0);
    
    // Return success if we get here (no exceptions)
    return 0;
} 