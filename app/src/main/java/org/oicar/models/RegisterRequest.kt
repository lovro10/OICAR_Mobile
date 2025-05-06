package org.oicar.models

data class RegisterRequest(
    val username : String,
    val password : String,
    val ime : String,
    val prezime : String,
    val email : String,
    val telefon : String,
    val datumrodjenja : String
)
