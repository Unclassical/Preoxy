package xyz.suey;

import org.fusesource.jansi.Ansi;

import java.io.*;
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
        checkAndSaveWorkingProxies();
    }

    private static void checkAndSaveWorkingProxies() {
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
        List<String> workingProxies = new ArrayList<>();

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
                        workingProxies.add(proxy);
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

        // Warten, bis alle Threads abgeschlossen sind
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        executorService.shutdown();

        // Speichern der funktionierenden Proxies in einer Datei
        saveWorkingProxiesToFile(workingProxies);
    }

    private static void saveWorkingProxiesToFile(List<String> workingProxies) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("working_proxies.txt", true))) {
            for (String proxy : workingProxies) {
                writer.write(proxy);
                writer.newLine();
            }
            System.out.println("Working proxies appended to 'working_proxies.txt'");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Deprecated
    private static List<String> getProxies(String URL) {
        List<String> proxyList = new ArrayList<>();
        try {
            BufferedReader proxyReader = new BufferedReader(new InputStreamReader(new URL(URL).openStream()));
            String proxy;
            while ((proxy = proxyReader.readLine()) != null) {
                proxyList.add(proxy);
            }
            proxyReader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return proxyList;

    }

}
