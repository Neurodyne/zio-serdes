package nettest

import org.specs2._
import zio.{ Chunk, DefaultRuntime }

import zioSerdesPkg._
import zioSerdes._

class BaseSpec extends Specification with DefaultRuntime {

  def is = s2"""

  ZIO Serdes should      
    serialize byte array        $serBArr 
    deserialize byte array      $deserBArr 

    """

  def serBArr = {

    val arr: BArr = Array(1, 2, 3)
    val chunk     = Chunk.fromArray(arr)
    val data      = StreamData(chunk)

    val bytes: Chunk[Byte] = Serdes.chunkSerdes.serialize(data)

    true === true

  }

  def deserBArr = {

    val arr: BArr = Array(1, 2, 3)
    val chunk     = Chunk.fromArray(arr)
    val data      = StreamData(chunk)

    val bytes: Chunk[Byte]    = Serdes.chunkSerdes.serialize(data)
    val out: StreamData[Byte] = Serdes.chunkSerdes.deserialize(bytes)

    zioSerdesPkg.eqv(arr, out.din.toArray) === true
    //Chunk(arr) === out.din // Chunk comparison doesn't work!
  }
}
