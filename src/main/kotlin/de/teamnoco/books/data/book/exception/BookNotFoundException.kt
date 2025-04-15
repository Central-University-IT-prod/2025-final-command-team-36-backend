package de.teamnoco.books.data.book.exception

import de.teamnoco.books.web.response.WebErrorException

class BookNotFoundException(message: String = "Book not found") : WebErrorException(message, 404)