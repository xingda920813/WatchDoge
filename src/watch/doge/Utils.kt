package watch.doge

import java.awt.SystemTray
import java.awt.Toolkit
import java.awt.TrayIcon
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

private var trayIcon: TrayIcon? = null

fun fetchLast(): String {
    val client = HttpClient.newHttpClient()
    val req = HttpRequest.newBuilder(URI.create("https://www.okex.com/api/index/v3/ETH-USDT/constituents")).build()
    val res = client.send(req, HttpResponse.BodyHandlers.ofString())
    val body =  res.body()
    val last = extractLast(body)
    return String.format("%.2f", last.toDouble())
}

private fun extractLast(res: String): String {
    val key = """"last":""""
    val start = res.indexOf(key) + key.length
    val end = res.indexOf('"', start)
    return res.substring(start, end)
}

@Synchronized
private fun notify(title: String, desc: String, level: TrayIcon.MessageType) {
    if (trayIcon == null) {
        if (!SystemTray.isSupported()) return
        val tray = SystemTray.getSystemTray()
        val image = Toolkit.getDefaultToolkit().createImage(Images.ETH)
        trayIcon = TrayIcon(image)
        trayIcon!!.isImageAutoSize = true
        tray.add(trayIcon)
    }
    trayIcon!!.displayMessage(title, desc, level)
}
