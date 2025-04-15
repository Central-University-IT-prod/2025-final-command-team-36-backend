package de.teamnoco.books.data.reservation.exception

import de.teamnoco.books.web.response.WebErrorException

class ReservationForbiddenException(message: String = "You haven't rights to this reservation") :
    WebErrorException(message, 403)