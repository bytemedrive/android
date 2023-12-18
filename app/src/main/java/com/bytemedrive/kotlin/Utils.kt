package com.bytemedrive.kotlin

fun <T> List<T>.updateIf(predicate: (T) -> Boolean, creator: (T) -> T): List<T> {
    val mutable = this.toMutableList()
    val index = this.indexOfFirst(predicate)
    mutable[index] = creator.invoke(this[index])

    return mutable.toList()
}