package com.example.monghyang.domain.tag.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ReqTagDto {
    private List<Integer> add_tag_list = new ArrayList<>();
    private List<Integer> delete_tag_list = new ArrayList<>();
}
