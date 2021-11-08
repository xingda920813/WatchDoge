package watch.doge;

import java.awt.*;
import java.io.*;
import java.net.*;
import java.net.http.*;

public class Utils {

    public static double lastPrice = Double.NaN;

    private static HttpClient sClient;
    private static TrayIcon sTrayIcon;

    public static double fetchPrice() {
        if (sClient == null) sClient = HttpClient.newHttpClient();
        final HttpRequest req = HttpRequest.newBuilder(URI.create("https://www.okex.com/api/index/v3/ETH-USDT/constituents")).build();
        final HttpResponse<String> res;
        try {
            res = sClient.send(req, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException ex) {
            ex.printStackTrace();
            return Double.NaN;
        }
        final String body =  res.body();
        final String last = extractPrice(body);
        return Double.parseDouble(last);
    }

    private static String extractPrice(String res) {
        final String key = "\"last\":\"";
        final int start = res.indexOf(key) + key.length();
        final int end = res.indexOf('"', start);
        return res.substring(start, end);
    }

    public static void notifyOnce(double price) {
        final String title = formatPrice(price);
        TrayIcon.MessageType level = TrayIcon.MessageType.INFO;
        final String desc;
        if (!Double.isNaN(lastPrice)) {
            final double changeInPercentage = (price - lastPrice) / lastPrice;
            if (changeInPercentage >= 0.01 || changeInPercentage <= -0.01) {
                level = TrayIcon.MessageType.WARNING;
            }
            final String changeInDesc = (changeInPercentage >= 0 ? "+" : "") + formatPrice(changeInPercentage * 100) + '%';
            desc = "ETH/USDT 在 15 分钟内 " + changeInDesc + ", 现报 " + title + ", 前值 " + formatPrice(lastPrice)
                    + (level == TrayIcon.MessageType.WARNING ? ". 请注意控制风险." : "");
        } else {
            desc = "ETH/USDT 现报 " + title;
        }
        notifyOnce(title, desc, level);
    }

    private static String formatPrice(double price) {
        return String.format("%.2f", price);
    }

    private static synchronized void notifyOnce(String title, String desc, TrayIcon.MessageType level) {
        if (sTrayIcon == null) {
            final SystemTray tray = SystemTray.getSystemTray();
            final Image image = Toolkit.getDefaultToolkit().createImage(Images.ETH);
            sTrayIcon = new TrayIcon(image);
            sTrayIcon.setImageAutoSize(true);
            final PopupMenu popupMenu = new PopupMenu();
            final MenuItem menuItem = new MenuItem("Exit");
            menuItem.addActionListener(e -> System.exit(0));
            popupMenu.add(menuItem);
            sTrayIcon.setPopupMenu(popupMenu);
            try {
                tray.add(sTrayIcon);
            } catch (AWTException ex) {
                throw new RuntimeException(ex);
            }
        }
        sTrayIcon.displayMessage(title, desc, level);
    }
}
