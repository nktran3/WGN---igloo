package com.example.wgn_igloo.database


class Friend {
    var name: String? = null
    var email: String? = null // add more fields as necessary

    constructor()
    constructor(name: String?, email: String?) {
        this.name = name
        this.email = email
    } // Getters and setters (if needed)
}