package xyz.suey;

import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Preoxy {

    public static void main(String[] args) {
        checkProxyConnectionFromTxt();
    }

    private static void checkProxyConnectionFromTxt() {
        List<String> proxyList = new ArrayList<>();
        try {
            BufferedReader proxyReader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(Preoxy.class.getClassLoader().getResourceAsStream("proxies.txt"))));
            String proxy;
            while ((proxy = proxyReader.readLine()) != null) {
                proxyList.add(proxy);
            }
            proxyReader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        int count = proxyList.size();
        ExecutorService executorService = Executors.newFixedThreadPool(count);
        CountDownLatch countDownLatch = new CountDownLatch(count);
        for (String proxy : proxyList) {
            executorService.execute(() -> {
                try {
                    String[] proxyArr = proxy.split(":");
                    String host = proxyArr[0];
                    int port = Integer.parseInt(proxyArr[1]);
                    Proxy p = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(host, port));
                    HttpURLConnection connection = (HttpURLConnection) new URL("http://www.baidu.com").openConnection(p);
                    connection.setConnectTimeout(1000);
                    connection.setReadTimeout(1000);
                    connection.connect();
                    if (connection.getResponseCode() == 200) {
                        System.out.println(Ansi.ansi().eraseScreen().fg(Ansi.Color.GREEN).a(proxy).reset());
                    } else {
                        System.out.println(Ansi.ansi().eraseScreen().fg(Ansi.Color.RED).a(proxy).reset());
                    }
                } catch (Exception e) {
                    System.out.println(Ansi.ansi().eraseScreen().fg(Ansi.Color.RED).a(proxy).reset());
                } finally {
                    countDownLatch.countDown();
                }
            });
        }
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        executorService.shutdown();
    }

}
