package com.example.monghyang;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.Rollback;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // application.yml에 명시된 데이터베이스 설정을 따라감(기본값은 H2기 때문에 이러한 별도의 설정 필요)
@Rollback(false)
public class InitialSetting {
    // 데이터베이스에 기본값(role type 등) 삽입하는 스크립트

    @Test
    void contextLoads() {

    }
}
