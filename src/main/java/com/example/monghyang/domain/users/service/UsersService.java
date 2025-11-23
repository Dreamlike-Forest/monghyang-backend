package com.example.monghyang.domain.users.service;

import com.example.monghyang.domain.brewery.entity.Brewery;
import com.example.monghyang.domain.brewery.repository.BreweryRepository;
import com.example.monghyang.domain.global.advice.ApplicationError;
import com.example.monghyang.domain.global.advice.ApplicationException;
import com.example.monghyang.domain.seller.entity.Seller;
import com.example.monghyang.domain.seller.repository.SellerRepository;
import com.example.monghyang.domain.users.dto.ReqUsersDto;
import com.example.monghyang.domain.users.dto.ResUsersDto;
import com.example.monghyang.domain.users.dto.ResUsersPrivateInfoDto;
import com.example.monghyang.domain.users.entity.RoleType;
import com.example.monghyang.domain.users.entity.Users;
import com.example.monghyang.domain.users.repository.UsersRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class UsersService {
    private final UsersRepository usersRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder; // 패스워드 암호화 모듈
    private final SellerRepository sellerRepository;
    private final BreweryRepository breweryRepository;

    @Autowired
    public UsersService(UsersRepository usersRepository, BCryptPasswordEncoder bCryptPasswordEncoder, SellerRepository sellerRepository, BreweryRepository breweryRepository) {
        this.usersRepository = usersRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.sellerRepository = sellerRepository;
        this.breweryRepository = breweryRepository;
    }

    public List<ResUsersDto> getUsersByEmail(String email) {
        List<Users> users = usersRepository.findByEmailJoinedRole(email);
        if(users.isEmpty()) {
            throw new ApplicationException(ApplicationError.USER_NOT_FOUND);
        }
        return users.stream().map(ResUsersDto::usersJoinedWithRoleToDto).toList();
    }

    public ResUsersPrivateInfoDto getMyUserInfo(Long userId) {
        Users users = usersRepository.findByIdJoinedRole(userId).orElseThrow(() ->
                new ApplicationException(ApplicationError.USER_NOT_FOUND));
        return ResUsersPrivateInfoDto.usersJoinedWithRoleToDto(users);
    }

    public ResUsersDto getUsersById(Long userId) {
        Users users = usersRepository.findByIdJoinedRole(userId).orElseThrow(() ->
                new ApplicationException(ApplicationError.USER_NOT_FOUND));
        return ResUsersDto.usersJoinedWithRoleToDto(users);
    }

    @Transactional
    public void updateUsers(Long userId, ReqUsersDto reqUsersDto, String userRole) {
        if(reqUsersDto == null) {
            throw new ApplicationException(ApplicationError.DTO_NULL_ERROR);
        }

        Users users = usersRepository.findById(userId).orElseThrow(() ->
                new ApplicationException(ApplicationError.USER_NOT_FOUND));

        // 비밀번호 변경 시 인증 및 새 비밀번호 암호화
        if(reqUsersDto.getCurPassword() != null) {
            // 클라이언트가 보내온 '기존 비밀번호' 값과 DB에 저장된 값이 일치해야 한다.
            if(bCryptPasswordEncoder.matches(reqUsersDto.getCurPassword(), users.getPassword())) {
                // 비밀번호 변경 요청이 포함되어 있어야 한다.
                if(reqUsersDto.getNewPassword() != null && !reqUsersDto.getNewPassword().isBlank()) {
                    String newPassword = bCryptPasswordEncoder.encode(reqUsersDto.getNewPassword());
                    users.updatePassword(newPassword);
                } else {
                    // 새 비밀번호 필드 비어있음을 알리는 예외
                    throw new ApplicationException(ApplicationError.NEW_PASSWORD_NULL);
                }
            } else {
                // 사용자가 입력한 '현재 비밀번호'가 DB와 일치하지 않음을 알리는 예외
                throw new ApplicationException(ApplicationError.NOT_MATCH_CUR_PASSWORD);
            }
        }
//        users.updateUsers(reqUsersDto);
        // 회원 테이블 수정
        if(reqUsersDto.getEmail() != null) {
            users.updateEmail(reqUsersDto.getEmail());
        }
        if(reqUsersDto.getNickname() != null) {
            users.updateNickname(reqUsersDto.getNickname());
        }
        if(reqUsersDto.getName() != null) {
            users.updateName(reqUsersDto.getName());
        }
        if(reqUsersDto.getPhone() != null) {
            users.updatePhone(reqUsersDto.getPhone());
        }
        if(reqUsersDto.getBirth() != null) {
            users.updateBirth(reqUsersDto.getBirth());
        }
        if(reqUsersDto.getGender() != null) {
            users.updateGender(reqUsersDto.getGender());
        }
        if(reqUsersDto.getAddress() != null) {
            users.updateAddress(reqUsersDto.getAddress());
        }
        if(reqUsersDto.getAddress_detail() != null) {
            users.updateAddressDetail(reqUsersDto.getAddress_detail());
        }

        // 판매자/양조장 테이블을 수정해야 하는 경우인지 검증하는 플래그 변수
        boolean needsOtherEntity = reqUsersDto.getNickname() != null
                || reqUsersDto.getAddress() != null
                || reqUsersDto.getAddress_detail() != null;

        // 판매자/양조장에 관련된 컬럼에 대한 수정사항 발생 시 판매자/양조장 테이블에 똑같이 반영
        if(needsOtherEntity) {
            if(userRole.equals(RoleType.ROLE_SELLER.getRoleName())) {
                Seller seller = sellerRepository.findByUserId(users.getId()).orElseThrow(() ->
                        new ApplicationException(ApplicationError.SELLER_NOT_FOUND));
                if(reqUsersDto.getNickname() != null) {
                    seller.updateSellerName(reqUsersDto.getNickname());
                }
                if(reqUsersDto.getAddress() != null) {
                    seller.updateSellerAddress(reqUsersDto.getAddress());
                }
                if(reqUsersDto.getAddress_detail() != null) {
                    seller.updateSellerAddressDetail(reqUsersDto.getAddress_detail());
                }
            } else if(userRole.equals(RoleType.ROLE_BREWERY.getRoleName())) {
                Brewery brewery = breweryRepository.findByUserId(users.getId()).orElseThrow(() ->
                        new ApplicationException(ApplicationError.BREWERY_NOT_FOUND));

                if(reqUsersDto.getNickname() != null) {
                    brewery.updateBreweryName(reqUsersDto.getNickname());
                }
                if(reqUsersDto.getAddress() != null) {
                    brewery.updateBreweryAddress(reqUsersDto.getAddress());
                }
                if(reqUsersDto.getAddress_detail() != null) {
                    brewery.updateBreweryAddressDetail(reqUsersDto.getAddress_detail());
                }
            }
        }
    }

    // 회원 탈퇴(soft delete)
    public void withdrawalUser(Long userId, String userRole) {
        Users users = usersRepository.findById(userId).orElseThrow(() ->
                new ApplicationException(ApplicationError.USER_NOT_FOUND));
        users.setDeleted();
        if(userRole.equals(RoleType.ROLE_SELLER.getRoleName())) {
            // 탈퇴 유형이 판매자인 경우 판매자 테이블에서도 삭제 처리
            Seller seller = sellerRepository.findByUserId(users.getId()).orElseThrow(() ->
                    new ApplicationException(ApplicationError.SELLER_NOT_FOUND));
            seller.setDeleted();
        } else if(userRole.equals(RoleType.ROLE_BREWERY.getRoleName())) {
            // 탈퇴 유형이 양조장인 경우 양조장 테이블에서도 삭제 처리
            Brewery brewery = breweryRepository.findByUserId(users.getId()).orElseThrow(() ->
                    new ApplicationException(ApplicationError.BREWERY_NOT_FOUND));
            brewery.setDeleted();
        }
        usersRepository.save(users);
    }
}
