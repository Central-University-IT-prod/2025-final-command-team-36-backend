package de.teamnoco.books.data.reservation.exception

import de.teamnoco.books.web.response.WebErrorException

class ReservationAlreadyExistsException(message: String = "Reservation already exists") :
    WebErrorException(message, 409)