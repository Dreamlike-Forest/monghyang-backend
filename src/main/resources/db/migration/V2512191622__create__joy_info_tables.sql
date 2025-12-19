-- 체험 요일별 정기 운영 시간 리스트, 체험 별도 휴무일, 체험 별도 휴무 시간대 테이블 생성

/* =========================================================
 * 4) 요일별 정기 체험 일정 시작 시간대(예약 가능 시작 시각 리스트)
 *    - 주간 스냅샷 정책을 따를 경우: 어떤 요일 변경이든 모든 요일 슬롯을 동일 effective_date로 재생성
 * ========================================================= */
CREATE TABLE joy_weekly_start_time (
    id            BIGINT(20) NOT NULL AUTO_INCREMENT,
    joy_id        BIGINT(20) NOT NULL,
    day_of_week   ENUM('Mon','Tue','Wed','Thu','Fri','Sat','Sun') NOT NULL,
    start_time    TIME(6) NOT NULL,
    effective_date DATE NOT NULL,

    PRIMARY KEY (id),
    CONSTRAINT uk_joy_weekly_start_time UNIQUE (joy_id, effective_date, day_of_week, start_time),
    CONSTRAINT fk_joy_weekly_start_time_joy FOREIGN KEY (joy_id) REFERENCES joy(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

/* =========================================================
 * 5) 체험 별도 휴무일
 * ========================================================= */
CREATE TABLE joy_closed_date (
     id          BIGINT(20) NOT NULL AUTO_INCREMENT,
     joy_id      BIGINT(20) NOT NULL,
     closed_date DATE NOT NULL,
     is_all_day  TINYINT(1) NOT NULL,
     reason      varchar(255) NULL,

     PRIMARY KEY (id),
     CONSTRAINT uk_joy_closed_date UNIQUE (joy_id, closed_date),
     CONSTRAINT fk_joy_closed_date_joy FOREIGN KEY (joy_id) REFERENCES joy(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

/* =========================================================
 * 6) 특정 날짜의 체험 별도 휴무 시작 시간대
 * ========================================================= */
CREATE TABLE joy_closed_start_time (
    id                BIGINT(20) NOT NULL AUTO_INCREMENT,
    joy_closed_date_id BIGINT(20) NOT NULL,
    closed_start_time TIME(6) NOT NULL,
    reason            varchar(255) NULL,

    PRIMARY KEY (id),
    CONSTRAINT uk_joy_closed_start_time UNIQUE (joy_closed_date_id, closed_start_time),
    CONSTRAINT fk_joy_closed_start_time_closed_date FOREIGN KEY (joy_closed_date_id) REFERENCES joy_closed_date(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;