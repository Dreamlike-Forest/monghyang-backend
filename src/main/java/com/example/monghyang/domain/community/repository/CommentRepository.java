package com.example.monghyang.domain.community.repository;

import com.example.monghyang.domain.community.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByCommunityIdAndIsDeletedFalseOrderByCreatedAtAsc(Long communityId);

    List<Comment> findByParentCommentIdAndIsDeletedFalseOrderByCreatedAtAsc(Long parentCommentId);

    Optional<Comment> findByIdAndIsDeletedFalse(Long id);

    Long countByCommunityIdAndIsDeletedFalse(Long communityId);
}
