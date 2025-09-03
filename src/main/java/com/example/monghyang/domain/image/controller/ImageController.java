package com.example.monghyang.domain.image.controller;

import com.example.monghyang.domain.global.advice.ApplicationError;
import com.example.monghyang.domain.global.advice.ApplicationException;
import com.example.monghyang.domain.image.service.StorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;

@RestController
@RequestMapping("/api/image")
@Tag(name = "이미지 파일 요청 API")
@Slf4j
public class ImageController {
    private final StorageService storageService;
    @Autowired
    public ImageController(StorageService storageService) {
        this.storageService = storageService;
    }


    /**
     * 이미지 파일 응답 API (stream 방식)
     * - S3 presigned URL을 담은 UrlResource면: 서버가 URL을 열어 InputStream을 얻고, StreamingResponseBody로 그대로 흘려보냄
     * - 로컬(파일시스템) Resource면: Resource 자체를 바디로 반환 (getFile() 호출 금지!)
     */
    @GetMapping("/{imageFullName}")
    @Operation(summary = "실제 이미지 파일 요청 API", description = "확장자명을 포함한 이미지 전체 이름을 파라메터에 넣어주세요.")
    public ResponseEntity<?> loadImage(@PathVariable String imageFullName, HttpServletRequest request) {
        try {
            Resource image = storageService.load(imageFullName);

            // (1) UrlResource인 경우: presigned URL(원격)을 서버가 직접 열어 스트리밍
            if (image instanceof UrlResource urlRes) {
                var url = urlRes.getURL();
                var conn = url.openConnection(); // 서버에서 s3와의 통로를 연다.
                conn.setConnectTimeout(3000); // 연결 대기 시간 timeout
                conn.setReadTimeout(15000); // 읽기 대기 시간 timeout

                String contentType = Optional.ofNullable(conn.getContentType())
                        .orElseGet(() -> guessContentType(request, imageFullName));
                long contentLength = getSafeContentLength(conn.getContentLengthLong());

                InputStream in = conn.getInputStream(); // try-with-resources는 StreamingResponseBody에서 닫음
                StreamingResponseBody body = out -> {
                    try (in) { in.transferTo(out); }
                };

                var builder = ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, inlineFilename(imageFullName))
                        .cacheControl(CacheControl.maxAge(Duration.ofMinutes(10)).cachePublic()); // 브라우저에서 이미지를 캐싱하도록 설정

                if (contentLength >= 0) builder = builder.contentLength(contentLength);
                return builder.body(body);
            }



            // (2) UrlResource가 아닌 경우(로컬 파일 등): Resource 자체를 바디에 싣는다.
            // 주의: 여기서도 getFile()을 호출하지 않고, Content-Type만 추정해서 헤더에 넣는다.
            String contentType = guessContentType(request, imageFullName);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, inlineFilename(imageFullName))
                    // 로컬은 매 요청 최신을 원하면 no-cache 권장. 필요시 max-age로 조정.
                    .cacheControl(CacheControl.noCache())
                    .body(image);

        } catch (IOException e) {
            log.error("이미지 로드 에러: 반환하려는 파일의 타입을 결정할 수 없습니다. {}", e.getMessage());
            throw new ApplicationException(ApplicationError.IMAGE_LOAD_ERROR);
        }
    }

    private String guessContentType(HttpServletRequest request, String filename) {
        String ct = Optional.ofNullable(request.getServletContext().getMimeType(filename))
                .orElseGet(() -> Optional.ofNullable(URLConnection.guessContentTypeFromName(filename)).orElse(null));
        return (ct != null) ? ct : "application/octet-stream";
    }

    /**
     * Content-Length가 음수면 미지정으로 간주 (헤더 세팅 생략)
     * - HTTP 청크 전송이 자동으로 사용돼 진행률 표시가 어려울 수 있으나, 스트리밍은 정상 동작
     */
    private long getSafeContentLength(long len) {
        return (len >= 0 ? len : -1);
    }

    /**
     * Content-Disposition 헤더 값을 inline(브라우저 미리보기)로 구성.
     * - RFC 5987 형식: filename* 로 UTF-8 안전하게 파일명을 표기
     * - 다운로드로 강제하려면 "attachment; ..." 로 변경 가능
     */
    private String inlineFilename(String filename) {
        String quoted = filename.replace("\\", "\\\\").replace("\"", "\\\"");
        String encoded = java.net.URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20");
        return "inline; filename=\"" + quoted + "\"; filename*=UTF-8''" + encoded;
    }
}
