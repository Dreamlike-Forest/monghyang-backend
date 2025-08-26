package com.example.monghyang.domain.tag.repository;

import com.example.monghyang.domain.tag.entity.Tags;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TagsRepository extends JpaRepository<Tags, Integer> {
}
