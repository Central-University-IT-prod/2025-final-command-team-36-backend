package de.teamnoco.books.data.reservation.exception

import de.teamnoco.books.web.response.WebErrorException

class ReservationLimitException(message: String = "You have reached the reservation limit!") :
    WebErrorException(message, 400)