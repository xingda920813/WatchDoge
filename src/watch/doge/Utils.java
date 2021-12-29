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
        try {
            return fetchPriceCore();
        } catch (IOException | InterruptedException ignored) {}
        try {
            return fetchPriceCore();
        } catch (IOException | InterruptedException ignored) {}
        return Double.NaN;
    }

    private static double fetchPriceCore() throws IOException, InterruptedException {
        if (sClient == null) sClient = HttpClient.newHttpClient();
        final HttpRequest req = HttpRequest.newBuilder(URI.create("https://capi.bitgetapi.com/api/swap/v3/market/mark_price?symbol=cmt_ethusdt")).build();
        final HttpResponse<String> res = sClient.send(req, HttpResponse.BodyHandlers.ofString());
        final String body =  res.body();
        final String last = extractPrice(body);
        return Double.parseDouble(last);
    }

    private static String extractPrice(String res) {
        final String key = "\"mark_price\":\"";
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
            if (changeInPercentage >= 0.008 || changeInPercentage <= -0.008) {
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
            sTrayIcon = new TrayIcon(Toolkit.getDefaultToolkit().createImage(Images.ETH));
            sTrayIcon.setImageAutoSize(true);
            final PopupMenu popupMenu = new PopupMenu();
            final MenuItem menuItem = new MenuItem("Exit");
            final Font defaultFont = Font.decode(null);
            menuItem.setFont(defaultFont.deriveFont(defaultFont.getSize() * Toolkit.getDefaultToolkit().getScreenResolution() / 96F));
            menuItem.addActionListener(ev -> System.exit(0));
            popupMenu.add(menuItem);
            sTrayIcon.setPopupMenu(popupMenu);
            try {
                SystemTray.getSystemTray().add(sTrayIcon);
            } catch (AWTException ex) {
                throw new RuntimeException(ex);
            }
        }
        sTrayIcon.displayMessage(title, desc, level);
    }
}
