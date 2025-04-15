package de.teamnoco.books.web.exception.base

import de.teamnoco.books.web.response.WebErrorException

class NotFoundException(message: String = "Not found") : WebErrorException(message, 404)