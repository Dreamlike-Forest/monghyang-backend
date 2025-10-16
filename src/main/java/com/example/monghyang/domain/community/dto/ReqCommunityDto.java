package com.example.monghyang.domain.community.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
public class ReqCommunityDto {
    private String title;
    private String category;
    private String subCategory;
    private String productName;
    private String breweryName;
    private Double star;
    private String detail;
    private String tags;
    private List<MultipartFile> images;
}
