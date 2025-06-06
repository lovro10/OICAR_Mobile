package org.oicar.models

data class OglasVoznja(
    val voziloId: Int,
    val korisnikId : String,
    val datumIVrijemePolaska: String,
    val datumIVrijemeDolaska: String,
    val cestarina: String,
    val gorivo: String,
    val brojPutnika: String,
    val polaziste: String,
    val odrediste: String,
    val statusVoznjeNaziv: String
)
