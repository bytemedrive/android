package com.bytemedrive.file.root

import com.fasterxml.jackson.annotation.JsonValue

enum class Resolution(@JsonValue val value: Int) {

    P1280(1280),

    P720(720),

    P360(360)
}