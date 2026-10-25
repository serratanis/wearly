package com.kampplus.hava.model

import java.text.Normalizer
import java.util.Locale

/** Türkiye sekmesinde kabul edilen 81 resmî il adı. */
object TurkeyProvinces {
    val names = setOf(
        "Adana", "Adıyaman", "Afyonkarahisar", "Ağrı", "Amasya", "Ankara", "Antalya", "Artvin", "Aydın",
        "Balıkesir", "Bilecik", "Bingöl", "Bitlis", "Bolu", "Burdur", "Bursa", "Çanakkale", "Çankırı",
        "Çorum", "Denizli", "Diyarbakır", "Edirne", "Elazığ", "Erzincan", "Erzurum", "Eskişehir", "Gaziantep",
        "Giresun", "Gümüşhane", "Hakkâri", "Hatay", "Isparta", "Mersin", "İstanbul", "İzmir", "Kars",
        "Kastamonu", "Kayseri", "Kırklareli", "Kırşehir", "Kocaeli", "Konya", "Kütahya", "Malatya", "Manisa",
        "Kahramanmaraş", "Mardin", "Muğla", "Muş", "Nevşehir", "Niğde", "Ordu", "Rize", "Sakarya", "Samsun",
        "Siirt", "Sinop", "Sivas", "Tekirdağ", "Tokat", "Trabzon", "Tunceli", "Şanlıurfa", "Uşak", "Van",
        "Yozgat", "Zonguldak", "Aksaray", "Bayburt", "Karaman", "Kırıkkale", "Batman", "Şırnak", "Bartın",
        "Ardahan", "Iğdır", "Yalova", "Karabük", "Kilis", "Osmaniye", "Düzce"
    )

    private val normalizedNames = names.map(String::searchKey).toSet()

    fun contains(name: String): Boolean = name.searchKey() in normalizedNames
}

private fun String.searchKey(): String = Normalizer.normalize(
    lowercase(Locale.forLanguageTag("tr")),
    Normalizer.Form.NFD
).replace(Regex("\\p{Mn}+"), "")
