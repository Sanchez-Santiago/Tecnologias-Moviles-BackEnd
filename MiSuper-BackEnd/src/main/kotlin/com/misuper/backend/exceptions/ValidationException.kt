package com.misuper.backend.exceptions

class ValidationException(override val message: String) : RuntimeException(message)
