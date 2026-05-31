-- 양조장 요일별 정기 운영시간/휴게시간 테이블, 양조장 별도 휴무일 테이블 생성

/* =========================================================
 * 1) 양조장 요일별 정기 운영 시간대
 * ========================================================= */
CREATE TABLE brewery_weekly_open_time (
    id            BIGINT(20) NOT NULL AUTO_INCREMENT,
    brewery_id    BIGINT(20) NOT NULL,
    day_of_week   ENUM('Mon','Tue','Wed','Thu','Fri','Sat','Sun') NOT NULL,
    open_time     TIME(6) NOT NULL,
    close_time    TIME(6) NOT NULL,
    effective_date DATE NOT NULL,

    PRIMARY KEY (id),
    CONSTRAINT uk_brewery_open_week UNIQUE (brewery_id, effective_date, day_of_week),
    CONSTRAINT ck_brewery_open_time_range CHECK (open_time < close_time),
    CONSTRAINT fk_brewery_open_time_brewery FOREIGN KEY (brewery_id) REFERENCES brewery(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

/* =========================================================
 * 2) 양조장 요일별 정기 휴게시간
 *    - 휴게시간이 없는 요일/버전은 레코드를 생성하지 않음
 * ========================================================= */
CREATE TABLE brewery_weekly_break_time (
    id            BIGINT(20) NOT NULL AUTO_INCREMENT,
    brewery_id    BIGINT(20) NOT NULL,
    day_of_week   ENUM('Mon','Tue','Wed','Thu','Fri','Sat','Sun') NOT NULL,
    break_start   TIME(6) NOT NULL,
    break_end     TIME(6) NOT NULL,
    effective_date DATE NOT NULL,

    PRIMARY KEY (id),
    CONSTRAINT uk_brewery_break_week UNIQUE (brewery_id, effective_date, day_of_week),
    CONSTRAINT ck_brewery_break_time_range CHECK (break_start < break_end),
    CONSTRAINT fk_brewery_break_time_brewery FOREIGN KEY (brewery_id) REFERENCES brewery(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

/* =========================================================
 * 3) 양조장 별도 휴무일
 * ========================================================= */
CREATE TABLE brewery_closed_date (
     id          BIGINT(20) NOT NULL AUTO_INCREMENT,
     brewery_id  BIGINT(20) NOT NULL,
     closed_date DATE NOT NULL,
     reason      varchar(255) NULL,

     PRIMARY KEY (id),
     CONSTRAINT uk_brewery_closed_date UNIQUE (brewery_id, closed_date),
     CONSTRAINT fk_brewery_closed_date_brewery FOREIGN KEY (brewery_id) REFERENCES brewery(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;