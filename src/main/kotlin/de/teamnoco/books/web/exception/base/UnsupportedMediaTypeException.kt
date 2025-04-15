package de.teamnoco.books.web.exception.base

import de.teamnoco.books.web.response.WebErrorException

/**
 * @author <a href="https://github.com/Neruxov">Neruxov</a>
 */
class UnsupportedMediaTypeException(message: String = "Unsupported media type") : WebErrorException(message, 409)