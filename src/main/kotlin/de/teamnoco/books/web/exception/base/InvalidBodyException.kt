package de.teamnoco.books.web.exception.base

import de.teamnoco.books.web.response.WebErrorException

class InvalidBodyException(message: String = "Invalid body") : WebErrorException(message, 400)