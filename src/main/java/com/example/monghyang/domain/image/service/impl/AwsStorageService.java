package com.example.monghyang.domain.image.service.impl;

import com.example.monghyang.domain.global.advice.ApplicationError;
import com.example.monghyang.domain.global.advice.ApplicationException;
import com.example.monghyang.domain.image.service.ImageType;
import com.example.monghyang.domain.image.service.StorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.time.Duration;
import java.util.UUID;

@Service
@Slf4j
@Profile({"prod", "test"}) // 운영 환경에서 사용되는 스토리지 서비스
public class AwsStorageService implements StorageService {
    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final String bucket;
    private final Region region;
    private final Duration presignTtl;

    @Autowired
    public AwsStorageService(S3Client s3Client, S3Presigner s3Presigner,
                             @Value("${app.s3.bucket}") String bucket, @Value("${app.s3.region}") Region region,
                             @Value("${app.s3.presign-exp-minutes}") long expMinutes) {
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
        this.bucket = bucket;
        this.region = region;
        this.presignTtl = Duration.ofMinutes(expMinutes);
    }

    @Override
    public String upload(MultipartFile file, ImageType imageType) {
        if(file.isEmpty()) {
            throw new ApplicationException(ApplicationError.IMAGE_REQUEST_NULL);
        }
        if(file.getOriginalFilename() == null || !file.getOriginalFilename().contains(".")) {
            // 이미지 이름이나 확장자 형식이 잘못되었으면 예외 발생
            throw new ApplicationException(ApplicationError.IMAGE_FORMAT_ERROR);
        }
        if(file.getSize() > MAX_FILE_SIZE) {
            // 이미지 용량이 10MB를 초과하거나 잘못된 용량일 때 예외 발생
            throw new ApplicationException(ApplicationError.IMAGE_SIZE_OVER);
        } else if(file.getSize() <= 0) {
            throw new ApplicationException(ApplicationError.IMAGE_SIZE_ERROR);
        }

        String ext = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".")).toLowerCase();
        String key = UUID.randomUUID() + ext;

        try {
            PutObjectRequest put = PutObjectRequest.builder().bucket(bucket).key(key).contentType(file.getContentType()).build();
            s3Client.putObject(put, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
            return key;
        } catch (IOException e) {
            log.error("AWS S3 PUT IOExcption! {}", e.getMessage());
            throw new ApplicationException(ApplicationError.IMAGE_UPLOAD_ERROR);
        } catch (S3Exception e) {
            log.error("AWS S3 PUT S3Excption! {}", e.getMessage());
            throw new ApplicationException(ApplicationError.IMAGE_UPLOAD_ERROR);
        }
    }

    @Override
    public Resource load(String imageFullName) {
        try {
            HeadObjectRequest head = HeadObjectRequest.builder().bucket(bucket).key(imageFullName).build();
            s3Client.headObject(head);

            GetObjectRequest getReq = GetObjectRequest.builder().bucket(bucket).key(imageFullName).build();
            PresignedGetObjectRequest presigned = s3Presigner.presignGetObject(
                    GetObjectPresignRequest.builder()
                            .signatureDuration(presignTtl)
                            .getObjectRequest(getReq).build()
            );

            URI uri = presigned.url().toURI();
            return new UrlResource(uri);
        } catch (NoSuchKeyException e) {
            throw new ApplicationException(ApplicationError.IMAGE_NOT_FOUND);
        } catch (S3Exception e) {
            if(e.statusCode() == 404) {
                throw new ApplicationException(ApplicationError.IMAGE_NOT_FOUND);
            }
            log.error("AWS S3 Load S3Excption! {}", e.getMessage());
            throw new ApplicationException(ApplicationError.IMAGE_LOAD_ERROR);
        } catch (MalformedURLException e) {
            log.error("AWS S3 Load MalformedURLException! {}", e.getMessage());
            throw new ApplicationException(ApplicationError.IMAGE_LOAD_ERROR);
        } catch (Exception e) {
            log.error("AWS S3 Load Exception! {}", e.getMessage());
            throw new ApplicationException(ApplicationError.IMAGE_LOAD_ERROR);
        }
    }

    @Override
    public void remove(String imageFullName) {
        try {
            DeleteObjectRequest delete = DeleteObjectRequest.builder().bucket(bucket).key(imageFullName).build();
            s3Client.deleteObject(delete); // 존재하지 않는 파일을 삭제해도 204/200 리턴됨.
        } catch (S3Exception e) {
            if(e.statusCode() == 404) {
                throw new ApplicationException(ApplicationError.IMAGE_NOT_FOUND);
            }
            log.error("AWS S3 Delete S3Excption! {}", e.getMessage());
            throw new ApplicationException(ApplicationError.IMAGE_REMOVE_ERROR);
        }
    }
}
