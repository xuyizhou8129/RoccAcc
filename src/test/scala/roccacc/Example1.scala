import chisel3._
import chisel3.simulator.scalatest.ChiselSim
import chisel3.simulator.stimulus.{RunUntilFinished, RunUntilSuccess}
import chisel3.util.Counter
import org.scalatest.funspec.AnyFunSpec

class ChiselSimExample extends AnyFunSpec with ChiselSim {

  class Foo extends Module {
    val a, b = IO(Input(UInt(8.W)))
    val c = IO(Output(chiselTypeOf(a)))

    private val r = Reg(chiselTypeOf(a))

    r :<= a +% b
    c :<= r
  }

  describe("Baz") {

    it("adds two numbers") {

      simulate(new Foo) { foo =>
        // Poke different values on the two input ports.
        foo.a.poke(1)
        foo.b.poke(2)

        // Step the clock by one cycle.
        foo.clock.step(1)

        // Expect that the sum of the two inputs is on the output port.
        foo.c.expect(3)
      }

    }

  }

  class Bar extends Module {

    val (_, done) = Counter(true.B, 10)

    when (done) {
      stop()
    }

  }

  describe("Bar") {

    it("terminates cleanly before 11 cycles have elapsed") {

      simulate(new Bar)(RunUntilFinished(11))

    }

  }

  class Baz extends Module {

    val success = IO(Output(Bool()))

    val (_, done) = Counter(true.B, 20)

    success :<= done

  }

  describe("Baz") {

    it("asserts success before 21 cycles have elapsed") {

      simulate(new Baz)(RunUntilSuccess(21, _.success))

    }

  }

}
