-- 문의사항 테이블
DROP TABLE IF EXISTS `qna`;
CREATE TABLE `qna` (
  `qna_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL,
  `qna_title` varchar(255) NOT NULL,
  `content` varchar(255) NOT NULL,
  `is_complete` tinyint(1) NOT NULL DEFAULT 0,
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`qna_id`),
  KEY `FK_qna_user` (`user_id`),
  CONSTRAINT `FK_qna_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- 문의사항 첨부 이미지 테이블
DROP TABLE IF EXISTS `qna_image`;
CREATE TABLE `qna_image` (
  `qna_image_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `qna_id` bigint(20) NOT NULL,
  `image_key` varchar(255) NOT NULL,
  `volume` int(11) NOT NULL,
  PRIMARY KEY (`qna_image_id`),
  KEY `FK_qna_image_qna` (`qna_id`),
  CONSTRAINT `FK_qna_image_qna` FOREIGN KEY (`qna_id`) REFERENCES `qna` (`qna_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- 문의 답변 테이블
DROP TABLE IF EXISTS `qna_answer`;
CREATE TABLE `qna_answer` (
  `qna_answer_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `qna_id` bigint(20) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  `content` varchar(255) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  PRIMARY KEY (`qna_answer_id`),
  KEY `FK_qna_answer_qna` (`qna_id`),
  KEY `FK_qna_answer_user` (`user_id`),
  CONSTRAINT `FK_qna_answer_qna` FOREIGN KEY (`qna_id`) REFERENCES `qna` (`qna_id`),
  CONSTRAINT `FK_qna_answer_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
