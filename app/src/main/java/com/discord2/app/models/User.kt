
package com.discord2.app.models

data class User(
    val uid: String = "",
    val handle: String = "",   // ex: "user2282" - fixo, gerado automaticamente, usado pra adicionar
    val nick: String = "",     // ex: "Edudh" - nome de exibição, editável a qualquer momento
    val email: String = ""
)
