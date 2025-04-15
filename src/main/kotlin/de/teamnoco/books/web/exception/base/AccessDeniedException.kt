package de.teamnoco.books.web.exception.base

import de.teamnoco.books.web.response.WebErrorException

class AccessDeniedException(message: String = "Forbidden") : WebErrorException(message, 403)