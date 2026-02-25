package com.example.monghyang.domain.qna.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
public class ReqQnaDto {
    private String qnaTitle;
    private String content;
    private List<MultipartFile> images;
}
