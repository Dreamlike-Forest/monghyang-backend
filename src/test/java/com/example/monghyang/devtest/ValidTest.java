package com.example.monghyang.devtest;

import java.util.regex.Pattern;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class ValidTest {
    public static void main(String[] args) {
        Pattern P = Pattern.compile("^[^\\s].*\\S.*$");
        assertThat(P.matcher("").matches()).isFalse();
        assertThat(P.matcher("   ").matches()).isFalse();        // 공백만 → 실패
        assertThat(P.matcher("asdf").matches()).isTrue();
        assertThat(P.matcher(" asdf").matches()).isFalse();       // <- 여기
        assertThat(P.matcher("asdf ").matches()).isTrue();       // <- 여기
        assertThat(P.matcher("asdf asdf").matches()).isTrue();   // <- 여기
    }
}
