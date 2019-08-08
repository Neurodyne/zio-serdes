package zio.serdes

import java.io.{ ByteArrayInputStream, ByteArrayOutputStream, ObjectInputStream, ObjectOutputStream }
import zio.{ Chunk }

sealed abstract class Serdes[F[_], G[_]] {

  def serialize[A](din: F[A]): G[Byte]
  def deserialize[A](din: G[Byte]): F[A]

}

object Serdes {

  def apply[F[_], G[_]](implicit srd: Serdes[F, G]) = srd

  def scatter[F[_], A](value: F[A]): ByteArrayOutputStream = {
    val stream: ByteArrayOutputStream = new ByteArrayOutputStream()
    val oos                           = new ObjectOutputStream(stream)
    try {
      oos.writeObject(value)
    } finally {
      oos.close()
    }
    stream
  }

  def gather[F[_], A](bytes: BArr): F[A] = {
    val ois = new ObjectInputStream(new ByteArrayInputStream(bytes))
    try {
      (ois.readObject()).asInstanceOf[F[A]]
    } finally {
      ois.close()
    }
  }

  // Serdes for ZIO Chunk
  implicit val chunkSerdes = new Serdes[Chunk, Chunk] {

    def serialize[A](din: Chunk[A]): Chunk[Byte] =
      Chunk.fromArray(scatter[Array, A](din.toArray).toByteArray)

    def deserialize[A](din: Chunk[Byte]): Chunk[A] =
      Chunk.fromArray(gather[Array, A](din.toArray))

  }

  // Serdes for Apache Arrow
  implicit val arrowSerdes = new Serdes[ByteArrow, Array] {

    def serialize[A](din: ByteArrow[A]): BArr = {
      val bytes = Array(din.readableBytes.toByte)
      din.readBytes(bytes)
      bytes
    }

    def deserialize[A](din: BArr): ByteArrow[Byte] = {
      import org.apache.arrow.memory.RootAllocator

      val allocator = new RootAllocator(Long.MaxValue)

      val buffer = allocator.buffer(din.length)
      buffer.writeBytes(din)
      buffer
    }

  }
}
