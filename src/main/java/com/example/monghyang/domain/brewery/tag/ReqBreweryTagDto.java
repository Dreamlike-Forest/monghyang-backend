package com.example.monghyang.domain.brewery.tag;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ReqBreweryTagDto {
    private List<Integer> add_tag_list = new ArrayList<>();
    private List<Integer> delete_tag_list = new ArrayList<>();
}
