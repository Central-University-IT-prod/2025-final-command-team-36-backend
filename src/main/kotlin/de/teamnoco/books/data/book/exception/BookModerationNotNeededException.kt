package de.teamnoco.books.data.book.exception

import de.teamnoco.books.web.response.WebErrorException

class BookModerationNotNeededException(message: String = "Book moderation not needed!") :
    WebErrorException(message, 400)