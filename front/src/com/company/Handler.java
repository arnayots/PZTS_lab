package com.company;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.*;

public class Handler extends Thread{

    boolean debug = false;

    private static final Map<String, String> CONTENT_TYPES = new HashMap<>() {{
        put("jpg", "image/jpeg");
        put("png", "image/png");
        put("html", "text/html");
        put("json", "application/json");
        put("txt", "text/plain");
        put("css", "text/css");
        put("js", "text/javascript");
        put("", "text/plain");
    }};

    private static final Map<Integer, String> CODE_DESCR = new HashMap<>() {{
        put(100, "Continue");
        put(200, "OK");
        put(400, "Bad Request");
        put(401, "Unauthorized");
        //put(402, "");
        put(403, "Forbidden");
        put(404, "Not Found");
        put(405, "Method Not Allowed");
        put(500, "Internal Server Error");
        put(501, "Not Implemented");
        put(502, "Bad Gateway");
        put(503, "Service Unavailable");
        put(504, "Gateway Timeout");
    }};
    private String python_link = "python ..\\back\\";

    private static final String NOT_FOUND_MESSAGE = "NOT FOUND";
    private String directory;
    private Socket socket;

    private HashSet<String> errors;

    public Handler(Socket socket){
        this.socket = socket;
//        directory = "D:\\DATA\\Arsen\\Курсова\\membrane_dynamics\\front_end\\index";
        directory = Path.of("").toAbsolutePath().toString() + "\\index";
        errors = new HashSet<>();
    }

    public void run(){
        try (var input = this.socket.getInputStream(); var output = this.socket.getOutputStream()) {
            var url = this.getRequestUrl(input);
            System.out.println("url: "+url);
            if(url.equals("/"))
                sendFile("/index.html", output);
            else{
                int delim_id = url.indexOf('?');
                String url_sub;
                String param_sub = "";
                if(delim_id != -1){
                    url_sub = url.substring(0, delim_id);
                    param_sub = url.substring(delim_id + 1);
                    System.out.println("url :"+url_sub);
                    System.out.println("par :"+param_sub);
                } else
                    url_sub = url;
                switch(url_sub.toLowerCase(Locale.ROOT)){
                    case "/calculate":
                        handleCalculate(param_sub, output); break;
                    case "/extras/request_animation_frame.js":
                        sendFile("/js/extras/request_animation_frame.js", output);
                    default:
                        sendFile(url, output);
                }
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public void sendFile(String url, OutputStream output) throws IOException {
        sendFile(url, output, 200);
    }

    public void sendFile(String url, OutputStream output, int code) throws IOException {
        try {
            var filePath = Path.of(this.directory, url);
            if (Files.exists(filePath) && !Files.isDirectory(filePath)) {
                var extension = this.getFileExtension(filePath);
                var type = CONTENT_TYPES.get(extension);
                var fileBytes = Files.readAllBytes(filePath);
                if(code < 300)
                    this.sendHeader(output, code, "OK", type, fileBytes.length);
                else
                    this.sendHeader(output, code, "Error", type, fileBytes.length);
                output.write(fileBytes);
            } else {
                var type = CONTENT_TYPES.get("text");
                this.sendHeader(output, 404, "Not Found", type, NOT_FOUND_MESSAGE.length());
                output.write(NOT_FOUND_MESSAGE.getBytes());
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
        catch (InvalidPathException | NoSuchElementException e){
            sendCustomError(404, output, "No such file");
        }
    }

    public void sendError(int code, OutputStream output) throws IOException {
        //outdated/ use sendCustonError
        switch (code){
            case 404: sendFile("/notFound.html", output, 404); break;
            case 503: sendFile("/error_503.html", output, 503); break;
            default: sendFile("/notFound.html", output, 404); break;
        }
    }

    private void handleCalculate(String param, OutputStream output) throws IOException {
        //handles /calculate request
        //checks data from request and calls python application
        double sygma_top=0, sygma_bottom=1, ro_top=0, ro_bottom=1, beta_top=0, beta_bottom=1, x0=0, y0=0, z0=0, t_final=0;
        int n_pts = 1;

        boolean raised_error = false;
        try {
            HashMap<String, String> par_list = parseParams(param);

            for (var el : par_list.keySet()) {
                System.out.println(" " + el + " : " + par_list.get(el));
            }
            sygma_top = Double.parseDouble(par_list.get("sygma_top"));
            sygma_bottom = Double.parseDouble(par_list.get("sygma_bottom"));
            ro_top = Double.parseDouble(par_list.get("ro_top"));
            ro_bottom = Double.parseDouble(par_list.get("ro_bottom"));
            beta_top = Double.parseDouble(par_list.get("beta_top"));
            beta_bottom = Double.parseDouble(par_list.get("beta_bottom"));
            x0 = Double.parseDouble(par_list.get("x0"));
            y0 = Double.parseDouble(par_list.get("y0"));
            z0 = Double.parseDouble(par_list.get("z0"));
            t_final = Double.parseDouble(par_list.get("t_final"));
            n_pts = Integer.parseInt(par_list.get("n_pts"));

            if(Dequal(sygma_bottom, 0))
                claimError("sygma_bottom is zero");
            if(Dequal(ro_bottom, 0))
                claimError("ro_bottom is zero");
            if(Dequal(beta_bottom, 0))
                claimError("beta_bottom is zero");
            if(sygma_top > 1000 || sygma_top < -1000)
                claimError("sygma_top is out of range");
            if(ro_top > 1000 || ro_top < -1000)
                claimError("ro_top is out of range");
            if(beta_top > 1000 || beta_top < -1000)
                claimError("beta_top is out of range");
            if(x0 > 1000 || x0 < -1000)
                claimError("x0 is out of range");
            if(y0 > 1000 || y0 < -1000)
                claimError("y0 is out of range");
            if(z0 > 1000 || z0 < -1000)
                claimError("z0 is out of range");
            if(t_final <= 0)
                claimError("t_final is out of range");
            if(n_pts < 1)
                claimError("n_pts is out of range");

            checkErrors();
        }
        catch(NumberFormatException | NullPointerException | StringIndexOutOfBoundsException | IOException e){
            if(debug)
                e.printStackTrace();
            raised_error = true;
            sendCustomError(404, output, errors);
            e.printStackTrace();
        }
        if(!raised_error){
            try{
                String cmd = python_link + "main.py" +
                        " --sygma_top " + String.valueOf(sygma_top) +
                        " --sygma_bottom " + String.valueOf(sygma_bottom) +
                        " --ro_top " + String.valueOf(ro_top) +
                        " --ro_bottom " + String.valueOf(ro_bottom) +
                        " --beta_top " + String.valueOf(beta_top) +
                        " --beta_bottom " + String.valueOf(beta_bottom) +
                        " --x0 " + String.valueOf(x0) +
                        " --y0 " + String.valueOf(y0) +
                        " --z0 " + String.valueOf(z0) +
                        " --t_final " + String.valueOf(t_final) +
                        " --n_pts " + String.valueOf(n_pts);

                System.out.println(cmd);

                Runtime r = Runtime.getRuntime();
                Process p = r.exec(cmd);
                BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));

                String tmp, msg="";
                while((tmp = br.readLine()) != null){
                    msg += tmp;
                }

                System.out.println( p.exitValue() + " plotData.lenght() = " + msg.length());
                if(p.exitValue() == -2)
                    throw new IOException(msg);
                if(p.exitValue() != 0)
                    throw new RuntimeException(msg);

                sendHeader(output, 200, "OK", "text", msg.length());
                var ps = new PrintStream(output);
                ps.print(msg);

            }
            catch (IOException | RuntimeException e){
                e.printStackTrace();
                sendCustomError(503, output, e.getMessage());
            }
        }
    }

    private void claimError(String msg){
        errors.add(msg);
    }
    //claim your requests data errors
    //user will get this messages as error

    private void checkErrors() throws IOException {
        //check all void claimError errors and throw exception with complete error text
        if(!errors.isEmpty()){
            String msg = "";
            for(String el : errors)
                msg += el + "; ";
            throw new IOException(msg);
        }
    }

    private void sendCustomError(int code, OutputStream output, HashSet<String> errors) throws IOException {
        //returns to user page with all errors from void checkErrors()
        String msg = "";
        var itr = errors.iterator();
        while(itr.hasNext()){
            msg += itr.next() + ";\n";
        }

        sendCustomError(code, output, msg);
    }

    private void sendCustomError(int code, OutputStream output, String msg) throws IOException {
        //returns to user page with custom message
        String text = "<!doctype html>\n" +
                "\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "    <meta charset=\"utf-8\">\n" +
                "\n" +
                "    <title>Error</title>\n" +
                "    <meta name=\"description\" content=\"Error page\">\n" +
                "    <meta name=\"author\" content=\"Arsen Stoian\">\n" +
                "\n" +
                "    <link rel=\"preconnect\" href=\"https://fonts.gstatic.com\">\n" +
                "    <link href=\"https://fonts.googleapis.com/css2?family=Open+Sans:ital,wght@0,400;0,700;1,400;1,700&display=swap\" rel=\"stylesheet\">\n" +
                "</head>\n" +
                "<style>\n" +
                "    *{\n" +
                "        font-family: 'Open Sans', sans-serif;\n" +
                "    }\n" +
                "    body{\n" +
                "        background: #eee;\n" +
                "    }\n" +
                "    .code{\n" +
                "        font-weight: bold;\n" +
                "        font-size: 30vh;\n" +
                "        text-align: center;\n" +
                "        margin-top: 5rem;\n" +
                "        margin-bottom: 1rem;\n" +
                "    }\n" +
                "    .short_descr{\n" +
                "        font-size: 2rem;\n" +
                "        text-align: center;\n" +
                "        margin-bottom: 7rem;\n" +
                "    }\n" +
                "    .error_div{\n" +
                "        display: flex;\n" +
                "        justify-content: space-around;\n" +
                "        font-size: 2rem;\n" +
                "        margin-bottom: 3rem;\n" +
                "    }\n" +
                "    summary{\n" +
                "        max-width: 800px;\n" +
                "        margin: 0 auto;\n" +
                "    }\n" +
                "    .content{\n" +
                "        max-width: 800px;\n" +
                "        margin: 1rem auto;\n" +
                "    }\n" +
                "</style>\n" +
                "<body>\n" +
                "    <div class=\"main\">\n" +
                "        <div class=\"container\">\n" +
                "            <div class=\"code\">" + Integer.toString(code) + "</div>\n" +
                "            <div class=\"short_descr\">" + CODE_DESCR.get(code) + "</div>\n" +
                "            <div class=\"error_div\">\n" +
                "                <a href=\"/\">Main page</a>\n" +
                "            </div>\n" +
                "\n" +
                "            <details>\n" +
                "                <summary>Details</summary>\n" +
                "                <div class=\"content\">" + msg + "</div>\n" +
                "\n" +
                "            </details>\n" +
                "        </div>\n" +
                "    </div>\n" +
                "\n" +
                "</body>\n" +
                "</html>\n";

        sendHeader(output, code, CODE_DESCR.get(code), "html", text.length());
        var ps = new PrintStream(output);
        ps.print(text);
    }

    public HashMap<String, String> parseParams(String params){
        //parses parameters from request
        //returns HashSet, where key is name of parameter, value is value of parameter
        HashMap<String, String> res = new HashMap<>();
        String[] pairs = params.split("&");
        for (String pair : pairs){
            int ind = pair.indexOf("=");
            res.put(pair.substring(0, ind), pair.substring(ind + 1));
        }
        return res;
    }

    private String getRequestUrl(InputStream input) {
        //returns relative url of reques
        var reader = new Scanner(input).useDelimiter("\r\n");
        var line = reader.next();
        System.out.println(line);
        return line.split(" ")[1];
    }

    private String getFileExtension(Path path) {
        //returns extension of file located at "path"
        var name = path.getFileName().toString();
        var extensionStart = name.lastIndexOf(".");
        return extensionStart == -1 ? "" : name.substring(extensionStart + 1);
    }

    private void sendHeader(OutputStream output, int statusCode, String statusText, String type, long lenght) {
        //sends http header to "output"
        var ps = new PrintStream(output);
        ps.printf("HTTP/1.1 %s %s%n", statusCode, statusText);
        ps.printf("Content-Type: %s%n", type);
        ps.printf("Content-Length: %s%n%n", lenght);
    }

    private boolean Dequal(double x, double y){
        return Math.abs(x - y) < 1E-8;
    }


}
