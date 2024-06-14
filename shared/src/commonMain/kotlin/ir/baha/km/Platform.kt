package ir.baha.km

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform