package org.oicar.models

data class KorisnikDetails(
    val idKorisnik: Int,
    val ime: String,
    val email: String,
    val telefon: String,
    val datumRodjenja: String,
    val prezime: String,
    val username: String,
    val isconfirmed: Boolean
)
