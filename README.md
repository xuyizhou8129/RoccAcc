# Reed-Solomon Encoding/Decoding Hardware Accelerator
This is a repository that holds the implementation for a Reed-Solomon hardware accelerator integrated with the Rocket Core of Chipyard. The structure of the accelerator incoporates a Memory SM that is connected to Cache 1, a control unit that determine Elaborational Parameters Galois Field bases, length of message, and tolerance of corruption, a execution context which handles the math work undelying decoding and encoding. Optimization of the accelerator working efficiency such as pipelining will be implemented after the main structure is established.
# Prerequisites
1. SBT
2. Scala
