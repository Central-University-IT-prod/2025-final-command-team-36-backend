package de.teamnoco.books.data.auth.exception

import de.teamnoco.books.web.response.WebErrorException

class BadPasswordException(message: String = "Bad password") : WebErrorException(message, 400)
