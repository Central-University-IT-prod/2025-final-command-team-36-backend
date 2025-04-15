package de.teamnoco.books.data.instance.exception

import de.teamnoco.books.web.response.WebErrorException

class BookInstanceModerationNotNeeded(message: String = "Book instance moderation not needed") :
    WebErrorException(message, 400)