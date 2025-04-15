package de.teamnoco.books.data.report.exception

import de.teamnoco.books.web.response.WebErrorException

class ReportForbiddenException(message: String = "Report forbidden") : WebErrorException(message, 403)