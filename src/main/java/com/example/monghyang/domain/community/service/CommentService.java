package com.example.monghyang.domain.community.service;

import com.example.monghyang.domain.community.dto.ReqCommentDto;
import com.example.monghyang.domain.community.dto.ResCommentDto;
import com.example.monghyang.domain.community.entity.Comment;
import com.example.monghyang.domain.community.entity.Community;
import com.example.monghyang.domain.community.repository.CommentRepository;
import com.example.monghyang.domain.community.repository.CommunityRepository;
import com.example.monghyang.domain.global.advice.ApplicationError;
import com.example.monghyang.domain.global.advice.ApplicationException;
import com.example.monghyang.domain.users.entity.Users;
import com.example.monghyang.domain.users.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {
    private final CommentRepository commentRepository;
    private final CommunityRepository communityRepository;
    private final UsersRepository usersRepository;

    @Transactional
    public ResCommentDto createComment(Long userId, ReqCommentDto dto) {
        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new ApplicationException(ApplicationError.USER_NOT_FOUND));

        Community community = communityRepository.findByIdAndIsDeletedFalse(dto.getCommunityId())
                .orElseThrow(() -> new ApplicationException(ApplicationError.COMMUNITY_NOT_FOUND));

        Comment parentComment = null;
        if (dto.getParentCommentId() != null) {
            parentComment = commentRepository.findByIdAndIsDeletedFalse(dto.getParentCommentId())
                    .orElseThrow(() -> new ApplicationException(ApplicationError.COMMENT_NOT_FOUND));
        }

        Comment comment = Comment.builder()
                .community(community)
                .user(user)
                .parentComment(parentComment)
                .content(dto.getContent())
                .build();

        Comment saved = commentRepository.save(comment);
        community.increaseComments();

        return ResCommentDto.from(saved);
    }

    public List<ResCommentDto> getCommentsByCommunity(Long communityId) {
        return commentRepository.findByCommunityIdAndIsDeletedFalseOrderByCreatedAtAsc(communityId)
                .stream()
                .map(ResCommentDto::from)
                .collect(Collectors.toList());
    }

    public List<ResCommentDto> getRepliesByComment(Long parentCommentId) {
        return commentRepository.findByParentCommentIdAndIsDeletedFalseOrderByCreatedAtAsc(parentCommentId)
                .stream()
                .map(ResCommentDto::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public ResCommentDto updateComment(Long userId, Long commentId, String content) {
        Comment comment = commentRepository.findByIdAndIsDeletedFalse(commentId)
                .orElseThrow(() -> new ApplicationException(ApplicationError.COMMENT_NOT_FOUND));

        if (!comment.getUser().getId().equals(userId)) {
            throw new ApplicationException(ApplicationError.FORBIDDEN);
        }

        comment.updateContent(content);
        return ResCommentDto.from(comment);
    }

    @Transactional
    public void deleteComment(Long userId, Long commentId) {
        Comment comment = commentRepository.findByIdAndIsDeletedFalse(commentId)
                .orElseThrow(() -> new ApplicationException(ApplicationError.COMMENT_NOT_FOUND));

        if (!comment.getUser().getId().equals(userId)) {
            throw new ApplicationException(ApplicationError.FORBIDDEN);
        }

        comment.setDeleted();
        comment.getCommunity().decreaseComments();
    }
}
