package com.ghuba.taprux

fun List<UByte>.toByteArray(): ByteArray {
  return ByteArray(size) { index -> get(index).toByte() }
}
