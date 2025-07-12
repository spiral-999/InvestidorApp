package com.example.investidorapp.model

// classe modelo de dados para os investimentos, vai ter um nome string e um valor em double
data class Investimento(
    val nome: String = "",
    val valor: Double = 0.0
)