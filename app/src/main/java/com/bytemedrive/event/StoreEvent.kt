package com.bytemedrive.event

@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS, AnnotationTarget.TYPE)
annotation class StoreEvent(val name: String)
