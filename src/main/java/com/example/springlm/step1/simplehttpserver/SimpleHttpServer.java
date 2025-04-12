package com.example.springlm.step1.simplehttpserver;

import java.io.*;
import java.net.*;
import java.nio.file.*;

public class SimpleHttpServer {
    public static void main(String[] args) throws IOException {

        /* 서버 시작
         * - 포트 바인딩
         * - 네트워크 리스닝(8080 포트에서 들어오는 연결 요청을 대기)
         */
        ServerSocket serverSocket = new ServerSocket(8080);

        while (true) {
            // 소켓에서 응답답 받으면 바이트 -> 문자로 변경하고, 버퍼링 기능을 추가
            try (Socket clientSocket = serverSocket.accept();

                 /*
                  * 1. 버퍼링으로 I/O 작업의 효율성 높임
                  * 2. 문자 처리
                  *   - 소켓에서 응답을 받으면, clientSocker으로부터 입력 스트림을 얻고, raw 바이트 데이터 -> inputStream 객체로 변환
                  *   - inputStream(바이트 스트림)을 Reader(문자 스트림)로 변환
                  *   - 문자 입력 스트림에 버퍼링 기능 추
                  */

                 BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                 OutputStream out = clientSocket.getOutputStream()) {

                // HTTP 요청 파싱
                String requestLine = in.readLine();
                if (requestLine == null) continue;

                String[] requestParts = requestLine.split(" ");
                if (requestParts.length != 3) continue;

                String method = requestParts[0];
                String path = requestParts[1];

                // QueryString 파싱
                String queryString = null;
                String originalPath = path; // 원본 경로 보존
                
                // QueryString 파라미터를 저장할 Map
                java.util.Map<String, String> parameters = new java.util.HashMap<>();
                
                if (path.contains("?")) {
                    String[] pathAndQuery = path.split("\\?", 2);
                    path = pathAndQuery[0];           // 파일 경로 (QueryString 제거)
                    queryString = pathAndQuery[1];    // QueryString 부분
                    
                    // QueryString 파라미터 파싱 및 로그 출력
                    if (queryString != null && !queryString.isEmpty()) {
                        
                        String[] params = queryString.split("&");
                        for (String param : params) {
                            if (param.contains("=")) {
                                String[] keyValue = param.split("=", 2);
                                String key = keyValue[0];
                                String value = keyValue.length > 1 ? keyValue[1] : "";
                                System.out.println("  " + key + " = " + value);
                                
                                // 파라미터를 Map에 저장
                                parameters.put(key, value);
                            } else {
                                System.out.println("  " + param + " = (값 없음)");
                            }
                        }
                    }
                }

                // 간단한 라우팅
                if ("/".equals(path)) {
                    path = "/simpleserver.html";
                }

                // 정적 파일 서비스
                try {
                    byte[] fileContent = Files.readAllBytes(Paths.get("public" + path));
                    String contentType = getContentType(path);

                    // HTTP 응답 헤더
                    String headers = "HTTP/1.1 200 OK\r\n" +
                            "Content-Type: " + contentType + "\r\n" +
                            "Content-Length: " + fileContent.length + "\r\n" +
                            "X-Request-Method: " + method + "\r\n" +
                            "X-Request-Path: " + originalPath + "\r\n";
                    
                    // QueryString이 있는 경우 헤더에 추가
                    if (queryString != null && !queryString.isEmpty()) {
                        headers += "X-Query-String: " + queryString + "\r\n";
                        
                        // 개별 파라미터들을 헤더에 추가
                        for (java.util.Map.Entry<String, String> entry : parameters.entrySet()) {
                            String key = entry.getKey();
                            String value = entry.getValue();
                            headers += "X-" + key.substring(0, 1).toUpperCase() + key.substring(1) + ": " + value + "\r\n";
                        }
                    }
                    
                    headers += "\r\n";

                    out.write(headers.getBytes());
                    out.write(fileContent);

                } catch (IOException e) {
                    // 파일을 찾을 수 없을 경우 404 응답
                    System.out.println("파일을 찾을 수 없음: public" + path);
                    System.out.println("에러: " + e.getMessage());
                    
                    String response = "HTTP/1.1 404 Not Found\r\n\r\n";
                    out.write(response.getBytes());
                }
            } catch (IOException e) {
                System.out.println("Error handling client request: " + e.getMessage());
            }
        }
    }

    private static String getContentType(String path) {
        if (path.endsWith(".html")) return "text/html";
        if (path.endsWith(".css")) return "text/css";
        if (path.endsWith(".js")) return "application/javascript";
        return "text/plain";
    }
}
