package com.example.monghyang;

import com.example.monghyang.domain.users.entity.Role;
import com.example.monghyang.domain.users.entity.RoleType;
import com.example.monghyang.domain.users.repository.RoleRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // application.yml에 명시된 데이터베이스 설정을 따라감(기본값은 H2기 때문에 이러한 별도의 설정 필요)
@Rollback(false)
public class InitialSetting {
    @Autowired
    private RoleRepository roleRepository;
    // 데이터베이스에 기본값(role type 등) 삽입하는 스크립트

    @Test
    @Transactional
    void contextLoads() {
        RoleType[] roles = {RoleType.ROLE_ADMIN, RoleType.ROLE_BREWERY, RoleType.ROLE_SELLER, RoleType.ROLE_USER};
        for(int i = 0; i < roles.length; i++) {
            Role role = new Role();
            role.setId(i+1);
            role.setName(roles[i]);
            roleRepository.save(role);
        }
    }
}
