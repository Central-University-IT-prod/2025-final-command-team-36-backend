package de.teamnoco.books.web.exception.base

import de.teamnoco.books.web.response.WebErrorException

/**
 * @author <a href="https://github.com/Neruxov">Neruxov</a>
 */
class PayloadTooLargeException(message: String = "Payload is too large") : WebErrorException(message, 413)