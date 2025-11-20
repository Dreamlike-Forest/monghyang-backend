package com.example.monghyang.domain.joy.service;

import com.example.monghyang.domain.joy.repository.JoySlotRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;

@Service
@Slf4j
public class JoySlotService {
    // Spring AOP: Transactional 적용을 위해 클래스를 의도적으로 분리합니다.
    private final JoySlotRepository joySlotRepository;
    @Autowired
    public JoySlotService(JoySlotRepository joySlotRepository) {
        this.joySlotRepository = joySlotRepository;
    }

    /**
     * JoySlot Insert: 새 트랜잭션 생성(REQUIRES_NEW) 전략 적용 -> JDBC 예외 대응
     * @param joyId
     * @param date
     * @param time
     * @param count
     * @return
     * @throws DataIntegrityViolationException
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int createJoySlot(Long joyId, LocalDate date, LocalTime time, Integer count) throws DataIntegrityViolationException {
        // JDBC 예외 발생으로 인한 트랜잭션 강제 롤백을 insert 수행에 대해서만 적용되도록 하기 위해 트랜잭션 분리

        return joySlotRepository.insertJoySlot(joyId, date, time, count);
    }

    @Transactional
    public int incrementJoySlotCount(Long joyId, LocalDate date, LocalTime time, Integer count) {
        return joySlotRepository.incrementJoySlotCount(joyId, date, time, count);
    }

    /**
     * joy slot 키운트 감소(예약 취소 등)
     * @param joyId
     * @param date
     * @param time
     * @param count
     */
    @Transactional
    public void decrementJoySlotCount(Long joyId, LocalDate date, LocalTime time, Integer count) {
        int ret = joySlotRepository.decrementJoySlotCount(joyId, date, time, count);
        if(ret == 0) {
            // 카운트 감소 후 카운트가 0이 되는 경우 감소되지 않는다. (ret == 0)
            // 이 경우 해당 슬롯을 아예 제거한다.
            joySlotRepository.deleteJoySlot(joyId, date, time);
        }
    }
}
