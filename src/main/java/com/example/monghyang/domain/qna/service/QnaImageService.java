package com.example.monghyang.domain.qna.service;

import com.example.monghyang.domain.global.advice.ApplicationError;
import com.example.monghyang.domain.global.advice.ApplicationException;
import com.example.monghyang.domain.image.service.ImageType;
import com.example.monghyang.domain.image.service.StorageService;
import com.example.monghyang.domain.qna.dto.ResQnaImageDto;
import com.example.monghyang.domain.qna.entity.Qna;
import com.example.monghyang.domain.qna.entity.QnaImage;
import com.example.monghyang.domain.qna.repository.QnaImageRepository;
import com.example.monghyang.domain.qna.repository.QnaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
// transactional 삭제
public class QnaImageService {
    private final QnaImageRepository qnaImageRepository;
    private final QnaRepository qnaRepository;
    private final StorageService storageService;

    @Transactional
    public ResQnaImageDto uploadImage(Long userId, Long qnaId, MultipartFile file) {
        Qna qna = qnaRepository.findByIdAndIsDeletedFalse(qnaId)
                .orElseThrow(() -> new ApplicationException(ApplicationError.QNA_NOT_FOUND));

        // 권한 검증: 본인이 작성한 문의인지 확인
        if (!qna.getUser().getId().equals(userId)) {
            throw new ApplicationException(ApplicationError.FORBIDDEN);
        }

        // 이미지 파일 저장 (AWS S3 or Local)
        String imageKey = storageService.upload(file, ImageType.QNA_IMAGE);

        // 파일 용량 (bytes)
        Integer volume = (int) file.getSize();

        // QnaImage 엔티티 생성
        QnaImage qnaImage = QnaImage.of(qna, imageKey, volume);

        QnaImage saved = qnaImageRepository.save(qnaImage);
        return ResQnaImageDto.from(saved);
    }

    public List<ResQnaImageDto> getImagesByQna(Long qnaId) {
        return qnaImageRepository.findByQnaId(qnaId)
                .stream()
                .map(ResQnaImageDto::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteImage(Long userId, Long imageId) {
        QnaImage qnaImage = qnaImageRepository.findById(imageId)
                .orElseThrow(() -> new ApplicationException(ApplicationError.IMAGE_NOT_FOUND));

        // 권한 검증: 본인이 작성한 문의의 이미지인지 확인
        if (!qnaImage.getQna().getUser().getId().equals(userId)) {
            throw new ApplicationException(ApplicationError.FORBIDDEN);
        }

        // 스토리지에서 이미지 삭제
        storageService.remove(qnaImage.getImageKey());

        // DB에서 삭제
        qnaImageRepository.delete(qnaImage);
    }

    @Transactional
    public void deleteAllImagesByQna(Long qnaId) {
        List<QnaImage> images = qnaImageRepository.findByQnaId(qnaId);

        for (QnaImage image : images) {
            storageService.remove(image.getImageKey());
        }

        qnaImageRepository.deleteByQnaId(qnaId);
    }
}
