package org.oicar.models

data class Vehicle(
    val idvozilo: Int,
    val naziv: String,
    val marka: String,
    val model: String,
    val registracija: String,
    val vozacId: String
)
