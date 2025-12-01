package com.vikas.news;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import org.json.*;

import java.io.*;
import java.net.*;
import java.time.LocalDate;
import java.util.Properties;

public class NewsServlet extends HttpServlet {

    private String apiKey;
    private String baseUrl;

    @Override
    public void init() throws ServletException {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            if (in == null) throw new ServletException("config.properties missing");
            Properties p = new Properties();
            p.load(in);

            apiKey = p.getProperty("NEWS_API_KEY");
            baseUrl = p.getProperty("NEWS_URL", "https://newsapi.org/v2/everything?");

            if (apiKey == null || apiKey.isEmpty()) {
                throw new ServletException("NEWS_API_KEY is empty");
            }

        } catch (IOException e) {
            throw new ServletException("Failed to load config.properties", e);
        }
    }

    private JSONObject callNewsApi(String q, String from, String to, int page, int size) throws IOException {

        StringBuilder sb = new StringBuilder(baseUrl);
        sb.append("q=").append(URLEncoder.encode(q, "UTF-8"));
        sb.append("&from=").append(from);
        sb.append("&to=").append(to);
        sb.append("&page=").append(page);
        sb.append("&pageSize=").append(size);
        sb.append("&sortBy=publishedAt");
        sb.append("&apiKey=").append(apiKey);

        HttpURLConnection conn = (HttpURLConnection) new URL(sb.toString()).openConnection();
        conn.setRequestMethod("GET");

        BufferedReader br = new BufferedReader(new InputStreamReader(
                conn.getResponseCode() >= 200 && conn.getResponseCode() < 300
                        ? conn.getInputStream()
                        : conn.getErrorStream()
        ));

        StringBuilder resp = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) resp.append(line);

        return new JSONObject(resp.toString());
    }

    private JSONArray fetchWithFallback(String query, int page, int size) throws IOException {

        // Try today's news first
        LocalDate today = LocalDate.now();
        JSONObject todayRes = callNewsApi(query, today.toString(), today.toString(), page, size);

        JSONArray todayArticles = todayRes.optJSONArray("articles");
        if (todayArticles != null && todayArticles.length() > 0) {
            return todayArticles;
        }

        // If empty → last 7 days
        LocalDate weekAgo = today.minusDays(7);
        JSONObject weekRes = callNewsApi(query, weekAgo.toString(), today.toString(), page, size);

        return weekRes.optJSONArray("articles") == null
                ? new JSONArray()
                : weekRes.getJSONArray("articles");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {

        String path = req.getServletPath();

        if ("/news-data".equals(path)) {
            handleNewsData(req, resp);
            return;
        }

        req.getRequestDispatcher("/index.jsp").forward(req, resp);
    }

    private void handleNewsData(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String q = req.getParameter("q");
        String category = req.getParameter("category");

        String query = (q != null && !q.isEmpty())
                ? q
                : (category != null && !category.isEmpty() ? category : "india");

        int page = Integer.parseInt(req.getParameter("page") == null ? "1" : req.getParameter("page"));
        int size = Integer.parseInt(req.getParameter("pageSize") == null ? "10" : req.getParameter("pageSize"));

        JSONArray articles;
        try {
            articles = fetchWithFallback(query, page, size);
        } catch (Exception ex) {
            JSONObject err = new JSONObject();
            err.put("status", "error");
            err.put("message", ex.getMessage());
            err.put("articles", new JSONArray());
            writeJson(resp, err);
            return;
        }

        JSONObject out = new JSONObject();
        out.put("status", "ok");
        out.put("count", articles.length());
        out.put("articles", articles);

        writeJson(resp, out);
    }

    private void writeJson(HttpServletResponse resp, JSONObject obj) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        resp.getWriter().write(obj.toString());
    }
}
