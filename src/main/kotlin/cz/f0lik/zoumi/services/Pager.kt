package cz.f0lik.zoumi.services

class Pager(totalPages: Int, currentPage: Int, private var buttonsToShow: Int) {
    var startPage: Int = 0
    var endPage: Int = 0

    init {
        val halfPagesToShow = this.buttonsToShow / 2
        when {
            totalPages <= this.buttonsToShow -> {
                startPage = 1
                endPage = totalPages
            }
            currentPage - halfPagesToShow <= 0 -> {
                startPage = 1
                endPage = this.buttonsToShow

            }
            currentPage + halfPagesToShow == totalPages -> {
                startPage = currentPage - halfPagesToShow
                endPage = totalPages
            }
            currentPage + halfPagesToShow > totalPages -> {
                startPage = totalPages - this.buttonsToShow + 1
                endPage = totalPages
            }
            else -> {
                startPage = currentPage - halfPagesToShow
                endPage = currentPage + halfPagesToShow
            }
        }
    }
}