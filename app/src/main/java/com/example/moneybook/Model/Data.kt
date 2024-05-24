package com.example.moneybook.Model

class Data {
    var amount: Int = 0
    var type: String = ""
    var note: String = ""
    var id: String = ""
    var date: String = ""
    var anno: String = ""
    var mese: String = ""

    constructor()

    constructor(amount: Int, type: String, note: String, id: String, date: String, anno: String, mese: String) {
        this.amount = amount
        this.type = type
        this.note = note
        this.id = id
        this.date = date
        this.anno = anno
        this.mese = mese
    }
}