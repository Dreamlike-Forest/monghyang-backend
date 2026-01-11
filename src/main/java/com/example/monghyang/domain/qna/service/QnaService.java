package com.example.monghyang.domain.qna.service;

import com.example.monghyang.domain.global.advice.ApplicationError;
import com.example.monghyang.domain.global.advice.ApplicationException;
import com.example.monghyang.domain.qna.dto.*;
import com.example.monghyang.domain.qna.entity.Qna;
import com.example.monghyang.domain.qna.entity.QnaAnswer;
import com.example.monghyang.domain.qna.repository.QnaAnswerRepository;
import com.example.monghyang.domain.qna.repository.QnaRepository;
import com.example.monghyang.domain.users.entity.Users;
import com.example.monghyang.domain.users.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QnaService {
    private final QnaRepository qnaRepository;
    private final QnaAnswerRepository qnaAnswerRepository;
    private final UsersRepository usersRepository;
    private final QnaImageService qnaImageService;

    private static final int PAGE_SIZE = 10;

    @Transactional
    public ResQnaDto createQna(Long userId, ReqQnaDto dto) {
        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new ApplicationException(ApplicationError.USER_NOT_FOUND));

        Qna qna = Qna.builder()
                .user(user)
                .qnaTitle(dto.getQnaTitle())
                .content(dto.getContent())
                .build();

        Qna saved = qnaRepository.save(qna);

        // 이미지 업로드 처리
        if (dto.getImages() != null && !dto.getImages().isEmpty()) {
            for (int i = 0; i < dto.getImages().size(); i++) {
                if (!dto.getImages().get(i).isEmpty()) {
                    qnaImageService.uploadImage(userId, saved.getId(), dto.getImages().get(i));
                }
            }
        }

        List<ResQnaImageDto> images = qnaImageService.getImagesByQna(saved.getId());
        return ResQnaDto.from(saved, images, null);
    }

    public PageResponseDto<ResQnaListDto> getMyQnas(Long userId, Integer page) {
        if (page == null || page < 0) {
            page = 0;
        }
        Pageable pageable = PageRequest.of(page, PAGE_SIZE);
        Page<Qna> qnaPage = qnaRepository.findByUserIdAndIsDeletedFalseOrderByIdDesc(userId, pageable);
        Page<ResQnaListDto> dtoPage = qnaPage.map(ResQnaListDto::from);
        return PageResponseDto.from(dtoPage);
    }

    public ResQnaDto getQnaById(Long userId, Long qnaId) {
        Qna qna = qnaRepository.findByIdAndIsDeletedFalse(qnaId)
                .orElseThrow(() -> new ApplicationException(ApplicationError.QNA_NOT_FOUND));

        // 본인 문의만 조회 가능
        if (!qna.getUser().getId().equals(userId)) {
            throw new ApplicationException(ApplicationError.FORBIDDEN);
        }

        List<ResQnaImageDto> images = qnaImageService.getImagesByQna(qnaId);
        QnaAnswer answer = qnaAnswerRepository.findByQnaId(qnaId).orElse(null);
        return ResQnaDto.from(qna, images, answer);
    }

    @Transactional
    public ResQnaDto updateQna(Long userId, Long qnaId, ReqQnaDto dto) {
        Qna qna = qnaRepository.findByIdAndIsDeletedFalse(qnaId)
                .orElseThrow(() -> new ApplicationException(ApplicationError.QNA_NOT_FOUND));

        // 본인 문의만 수정 가능
        if (!qna.getUser().getId().equals(userId)) {
            throw new ApplicationException(ApplicationError.FORBIDDEN);
        }

        if (dto.getQnaTitle() != null) qna.updateQnaTitle(dto.getQnaTitle());
        if (dto.getContent() != null) qna.updateContent(dto.getContent());

        // 이미지 업로드 처리 (새로운 이미지가 있는 경우)
        if (dto.getImages() != null && !dto.getImages().isEmpty()) {
            for (int i = 0; i < dto.getImages().size(); i++) {
                if (!dto.getImages().get(i).isEmpty()) {
                    qnaImageService.uploadImage(userId, qnaId, dto.getImages().get(i));
                }
            }
        }

        List<ResQnaImageDto> images = qnaImageService.getImagesByQna(qnaId);
        QnaAnswer answer = qnaAnswerRepository.findByQnaId(qnaId).orElse(null);
        return ResQnaDto.from(qna, images, answer);
    }

    @Transactional
    public void deleteQna(Long userId, Long qnaId) {
        Qna qna = qnaRepository.findByIdAndIsDeletedFalse(qnaId)
                .orElseThrow(() -> new ApplicationException(ApplicationError.QNA_NOT_FOUND));

        // 본인 문의만 삭제 가능
        if (!qna.getUser().getId().equals(userId)) {
            throw new ApplicationException(ApplicationError.FORBIDDEN);
        }

        // 문의에 연결된 이미지 모두 삭제
        qnaImageService.deleteAllImagesByQna(qnaId);

        qna.setDeleted();
    }

    // ===== 관리자용 메서드 =====

    public PageResponseDto<ResQnaListDto> getAllQnas(Integer page) {
        if (page == null || page < 0) {
            page = 0;
        }
        Pageable pageable = PageRequest.of(page, PAGE_SIZE);
        Page<Qna> qnaPage = qnaRepository.findByIsDeletedFalseOrderByIdDesc(pageable);
        Page<ResQnaListDto> dtoPage = qnaPage.map(ResQnaListDto::from);
        return PageResponseDto.from(dtoPage);
    }

    public ResQnaDto getQnaByIdForAdmin(Long qnaId) {
        Qna qna = qnaRepository.findByIdAndIsDeletedFalse(qnaId)
                .orElseThrow(() -> new ApplicationException(ApplicationError.QNA_NOT_FOUND));

        List<ResQnaImageDto> images = qnaImageService.getImagesByQna(qnaId);
        QnaAnswer answer = qnaAnswerRepository.findByQnaId(qnaId).orElse(null);
        return ResQnaDto.from(qna, images, answer);
    }

    @Transactional
    public ResQnaDto answerQna(Long adminUserId, Long qnaId, ReqQnaAnswerDto dto) {
        Qna qna = qnaRepository.findByIdAndIsDeletedFalse(qnaId)
                .orElseThrow(() -> new ApplicationException(ApplicationError.QNA_NOT_FOUND));

        Users adminUser = usersRepository.findById(adminUserId)
                .orElseThrow(() -> new ApplicationException(ApplicationError.USER_NOT_FOUND));

        // 기존 답변이 있는지 확인
        QnaAnswer existingAnswer = qnaAnswerRepository.findByQnaId(qnaId).orElse(null);

        QnaAnswer answer;
        if (existingAnswer != null) {
            // 기존 답변 수정
            existingAnswer.updateContent(dto.getContent());
            answer = existingAnswer;
        } else {
            // 새 답변 생성
            answer = QnaAnswer.builder()
                    .qna(qna)
                    .user(adminUser)
                    .content(dto.getContent())
                    .build();
            qnaAnswerRepository.save(answer);
        }

        // 문의 상태를 완료로 변경
        qna.setComplete();

        List<ResQnaImageDto> images = qnaImageService.getImagesByQna(qnaId);
        return ResQnaDto.from(qna, images, answer);
    }
}
