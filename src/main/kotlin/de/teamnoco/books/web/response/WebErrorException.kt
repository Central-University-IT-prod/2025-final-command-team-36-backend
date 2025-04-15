package de.teamnoco.books.web.response

open class WebErrorException(
    val error: StatusResponse.Error
) : RuntimeException() {
    constructor(message: String, httpStatus: Int) : this(StatusResponse.Error(message, httpStatus))
}
