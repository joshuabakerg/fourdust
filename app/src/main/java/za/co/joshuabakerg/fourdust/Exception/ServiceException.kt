package za.co.joshuabakerg.fourdust.Exception

class ServiceException: Exception {

    constructor(message: String) : super(message)

    constructor(message: String, throwable: Throwable) : super(message, throwable)

}