package de.teamnoco.books.web.exception.base

import de.teamnoco.books.web.response.WebErrorException

class ConflictException(message: String = "Conflict") : WebErrorException(message, 409)