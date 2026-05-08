package de.consansri.unterhaltsrechner

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform