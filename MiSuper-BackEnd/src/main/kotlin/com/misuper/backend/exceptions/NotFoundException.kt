package com.misuper.backend.exceptions

class NotFoundException(override val message: String) : RuntimeException(message)
