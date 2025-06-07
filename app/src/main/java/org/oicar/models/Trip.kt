package org.oicar.models

data class Trip(

    val idOglasVoznja: Int,
    val marka: String,
    val model: String,
    val registracija: String,
    val datumIVrijemePolaska: String,
    val datumIVrijemeDolaska: String,
    val cestarina: Double,
    val gorivo: Double,
    val cijenaPoPutniku: Double,
    val brojPutnika: Int,
    val statusVoznjeNaziv: String?,
    val polaziste: String,
    val odrediste: String,
    val ime: String,
    val prezime: String,
    val username: String
)
