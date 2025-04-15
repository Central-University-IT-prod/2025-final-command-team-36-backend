package de.teamnoco.books.data.instance.exception

import de.teamnoco.books.web.response.WebErrorException

class BookInstanceNotAccessibleException(message: String = "Book instance not accessible") :
    WebErrorException(message, 403)