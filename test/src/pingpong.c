include <rocc.h>
#include <stdio.h>

int main() {
    int ping = 1;
    int pong = 0;
    int result;
    
    printf("Starting pingpong test with VCode ROCC accelerator...\n");
    
    // Send ping value to accelerator (funct=0 for ADD operation)
    ROCC_INSTRUCTION_DSS(0, result, &ping, &pong, 0);
    
    printf("Ping: %d, Pong: %d, Result: %d\n", ping, pong, result);
    
    // Send pong value back
    ping = 0;
    pong = 1;
    ROCC_INSTRUCTION_DSS(0, result, &ping, &pong, 0);
    
    printf("Ping: %d, Pong: %d, Result: %d\n", ping, pong, result);
    
    // Test with different values
    ping = 10;
    pong = 20;
    ROCC_INSTRUCTION_DSS(0, result, &ping, &pong, 0);
    
    printf("Ping: %d, Pong: %d, Result: %d\n", ping, pong, result);
    
    printf("Pingpong test completed successfully!\n");
    
    return 0;
} 