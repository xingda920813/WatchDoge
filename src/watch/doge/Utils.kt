package watch.doge

import java.awt.*
import java.io.IOException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import kotlin.system.exitProcess

var lastPrice: Double? = null

private var client: HttpClient? = null
private var trayIcon: TrayIcon? = null

fun fetchPrice(): Double {
    if (client == null) client = HttpClient.newHttpClient()
    val req = HttpRequest.newBuilder(URI.create("https://www.okex.com/api/index/v3/ETH-USDT/constituents")).build()
    val res = try {
        client!!.send(req, HttpResponse.BodyHandlers.ofString())
    } catch (ex: IOException) {
        ex.printStackTrace()
        return Double.NaN
    }
    val body =  res.body()
    val last = extractPrice(body)
    return last.toDouble()
}

private fun extractPrice(res: String): String {
    val key = """"last":""""
    val start = res.indexOf(key) + key.length
    val end = res.indexOf('"', start)
    return res.substring(start, end)
}

fun notifyOnce(price: Double) {
    val title = formatPrice(price)
    var level = TrayIcon.MessageType.INFO
    val desc: String
    if (lastPrice != null) {
        val changeInPercentage = (price - lastPrice!!) / lastPrice!!
        if (changeInPercentage >= 0.02 || changeInPercentage <= -0.02) {
            level = TrayIcon.MessageType.WARNING
        }
        val changeInDesc = (if (changeInPercentage >= 0) " +" else " ") + formatPrice(changeInPercentage * 100) + '%'
        desc = "ETH/USDT 在 15 分钟内$changeInDesc, 现报 $title, 前值 ${formatPrice(lastPrice!!)}" +
                if (level == TrayIcon.MessageType.WARNING) ". 请注意控制风险." else ""
    } else {
        desc = "ETH/USDT 现报 $title"
    }
    notifyOnce(title, desc, level)
}

private fun formatPrice(price: Double) = String.format("%.2f", price)

@Synchronized
private fun notifyOnce(title: String, desc: String, level: TrayIcon.MessageType) {
    if (trayIcon == null) {
        val tray = SystemTray.getSystemTray()
        val image = Toolkit.getDefaultToolkit().createImage(Images.ETH)
        trayIcon = TrayIcon(image)
        trayIcon!!.isImageAutoSize = true
        val popupMenu = PopupMenu()
        val menuItem = MenuItem("Exit")
        menuItem.addActionListener { exitProcess(0) }
        popupMenu.add(menuItem)
        trayIcon!!.popupMenu = popupMenu
        tray.add(trayIcon)
    }
    trayIcon!!.displayMessage(title, desc, level)
}
