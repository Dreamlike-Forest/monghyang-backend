/*M!999999\- enable the sandbox mode */ 
-- MariaDB dump 10.19  Distrib 10.11.13-MariaDB, for debian-linux-gnu (aarch64)
--
-- Host: monghyang-dev-db.cdy2icsem0tc.ap-northeast-2.rds.amazonaws.com    Database: monghyang
-- ------------------------------------------------------
-- Server version	10.11.8-MariaDB

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Sequence structure for `comment_seq`
--

DROP SEQUENCE IF EXISTS `comment_seq`;
CREATE SEQUENCE `comment_seq` start with 1 minvalue 1 maxvalue 9223372036854775806 increment by 50 nocache nocycle ENGINE=InnoDB;
DO SETVAL(`comment_seq`, 1, 0);

--
-- Sequence structure for `community_seq`
--

DROP SEQUENCE IF EXISTS `community_seq`;
CREATE SEQUENCE `community_seq` start with 1 minvalue 1 maxvalue 9223372036854775806 increment by 50 nocache nocycle ENGINE=InnoDB;
DO SETVAL(`community_seq`, 1, 0);

--
-- Sequence structure for `image_community_seq`
--

DROP SEQUENCE IF EXISTS `image_community_seq`;
CREATE SEQUENCE `image_community_seq` start with 1 minvalue 1 maxvalue 9223372036854775806 increment by 50 nocache nocycle ENGINE=InnoDB;
DO SETVAL(`image_community_seq`, 1, 0);

--
-- Table structure for table `brewery`
--

DROP TABLE IF EXISTS `brewery`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `brewery` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `brewery_account_number` varchar(255) NOT NULL,
  `brewery_address` varchar(255) NOT NULL,
  `brewery_address_detail` varchar(255) NOT NULL,
  `brewery_bank_name` varchar(255) NOT NULL,
  `brewery_depositor` varchar(255) NOT NULL,
  `brewery_name` varchar(255) NOT NULL,
  `brewery_website` varchar(255) DEFAULT NULL,
  `business_registration_number` varchar(255) NOT NULL,
  `end_time` time(6) NOT NULL,
  `introduction` text DEFAULT NULL,
  `is_agreed_brewery` tinyint(1) NOT NULL,
  `is_deleted` tinyint(1) NOT NULL,
  `is_regular_visit` tinyint(1) NOT NULL,
  `is_visiting_brewery` tinyint(1) NOT NULL,
  `joy_count` int(11) NOT NULL,
  `min_joy_price` decimal(38,2) NOT NULL,
  `registered_at` date NOT NULL,
  `start_time` time(6) NOT NULL,
  `region_type_id` int(11) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKfvofyfjrwfjynin3e8sosytvn` (`user_id`),
  KEY `FKpcpy15pc481fvcw7ot79w9vxk` (`region_type_id`),
  CONSTRAINT `FK3wwj69ca2soeigq4j4poy7ht1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKpcpy15pc481fvcw7ot79w9vxk` FOREIGN KEY (`region_type_id`) REFERENCES `region_type` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=114 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `brewery_image`
--

DROP TABLE IF EXISTS `brewery_image`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `brewery_image` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `image_key` varchar(255) NOT NULL,
  `seq` int(11) NOT NULL,
  `volume` bigint(20) NOT NULL,
  `brewery_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKhw87t0gjpmfbm6b9019j98cou` (`image_key`),
  UNIQUE KEY `uk_brewery_image_seq` (`brewery_id`,`seq`),
  CONSTRAINT `FKq3wkb1wd3pxvj7djqmms8qadj` FOREIGN KEY (`brewery_id`) REFERENCES `brewery` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=107 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `brewery_tag`
--

DROP TABLE IF EXISTS `brewery_tag`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `brewery_tag` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `brewery_id` bigint(20) NOT NULL,
  `tag_id` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_brewery_tag` (`brewery_id`,`tag_id`),
  KEY `FKtbwcoy1vm9rfsmvetrcmn01sj` (`tag_id`),
  CONSTRAINT `FKedijgxiidalm6igllb4k7ndpk` FOREIGN KEY (`brewery_id`) REFERENCES `brewery` (`id`),
  CONSTRAINT `FKtbwcoy1vm9rfsmvetrcmn01sj` FOREIGN KEY (`tag_id`) REFERENCES `tags` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `cart`
--

DROP TABLE IF EXISTS `cart`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `cart` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `quantity` int(11) NOT NULL CHECK (`quantity` <= 99 and `quantity` >= 1),
  `product_id` bigint(20) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK3d704slv66tw6x5hmbm6p2x3u` (`product_id`),
  KEY `FKg5uhi8vpsuy0lgloxk2h4w5o6` (`user_id`),
  CONSTRAINT `FK3d704slv66tw6x5hmbm6p2x3u` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`),
  CONSTRAINT `FKg5uhi8vpsuy0lgloxk2h4w5o6` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  CONSTRAINT `CONSTRAINT_1` CHECK (`quantity` between 1 and 99)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `comment`
--

DROP TABLE IF EXISTS `comment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `comment` (
  `comment_id` bigint(20) NOT NULL,
  `content` mediumtext NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `is_deleted` tinyint(1) NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `community_id` bigint(20) NOT NULL,
  `parent_comment_id` bigint(20) DEFAULT NULL,
  `user_id` bigint(20) NOT NULL,
  PRIMARY KEY (`comment_id`),
  KEY `FKbwy7a8hwdjqbm26qadw82ikqg` (`community_id`),
  KEY `FKhvh0e2ybgg16bpu229a5teje7` (`parent_comment_id`),
  KEY `FKqm52p1v3o13hy268he0wcngr5` (`user_id`),
  CONSTRAINT `FKbwy7a8hwdjqbm26qadw82ikqg` FOREIGN KEY (`community_id`) REFERENCES `community` (`community_id`),
  CONSTRAINT `FKhvh0e2ybgg16bpu229a5teje7` FOREIGN KEY (`parent_comment_id`) REFERENCES `comment` (`comment_id`),
  CONSTRAINT `FKqm52p1v3o13hy268he0wcngr5` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `community`
--

DROP TABLE IF EXISTS `community`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `community` (
  `community_id` bigint(20) NOT NULL,
  `brewery_name` varchar(100) DEFAULT NULL,
  `category` varchar(50) NOT NULL,
  `comments` int(11) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `detail` mediumtext NOT NULL,
  `is_deleted` tinyint(1) NOT NULL,
  `likes` int(11) NOT NULL,
  `product_name` varchar(100) DEFAULT NULL,
  `star` double DEFAULT NULL,
  `sub_category` varchar(50) NOT NULL,
  `tags` varchar(255) DEFAULT NULL,
  `title` varchar(255) NOT NULL,
  `view_count` int(11) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  PRIMARY KEY (`community_id`),
  KEY `FKj2rho9053if429pagqkx3bmaa` (`user_id`),
  CONSTRAINT `FKj2rho9053if429pagqkx3bmaa` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `image_community`
--

DROP TABLE IF EXISTS `image_community`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `image_community` (
  `image_community_id` bigint(20) NOT NULL,
  `image_num` int(11) NOT NULL,
  `image_url` varchar(255) NOT NULL,
  `community_id` bigint(20) NOT NULL,
  PRIMARY KEY (`image_community_id`),
  KEY `FKmdb4dkln74gl7jlmnk3psoj83` (`community_id`),
  CONSTRAINT `FKmdb4dkln74gl7jlmnk3psoj83` FOREIGN KEY (`community_id`) REFERENCES `community` (`community_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `joy`
--

DROP TABLE IF EXISTS `joy`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `joy` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `detail` varchar(255) NOT NULL,
  `discount_rate` decimal(3,1) NOT NULL,
  `final_price` decimal(8,0) NOT NULL,
  `image_key` varchar(255) DEFAULT NULL,
  `is_deleted` tinyint(1) NOT NULL,
  `is_soldout` tinyint(1) NOT NULL,
  `name` varchar(255) NOT NULL,
  `origin_price` decimal(8,0) NOT NULL,
  `place` varchar(255) NOT NULL,
  `sales_volume` int(11) NOT NULL,
  `time_unit` int(11) NOT NULL,
  `brewery_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKsn7np8dpt9fyb9nd5apxne11j` (`brewery_id`),
  CONSTRAINT `FKsn7np8dpt9fyb9nd5apxne11j` FOREIGN KEY (`brewery_id`) REFERENCES `brewery` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `joy_order`
--

DROP TABLE IF EXISTS `joy_order`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `joy_order` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `count` int(11) DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `is_deleted` tinyint(1) NOT NULL,
  `payer_name` varchar(255) NOT NULL,
  `payer_phone` varchar(255) NOT NULL,
  `pg_order_id` uuid NOT NULL,
  `pg_payment_key` varchar(255) DEFAULT NULL,
  `reservation` datetime(6) NOT NULL,
  `joy_id` bigint(20) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  `joy_payment_status` enum('CANCELED','FAILED','PAID','PENDING') NOT NULL,
  `total_amount` decimal(16,2) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKreucw2s5sklupoxo0lyktc0wj` (`pg_order_id`),
  UNIQUE KEY `UKmkkhla3vdrk3do04g4fmwevv4` (`pg_payment_key`),
  KEY `FK8j0prxxq9o13icwgv4t5x75pn` (`joy_id`),
  KEY `FKdtttqnnb3xdqra5d8bd8m3ldh` (`user_id`),
  CONSTRAINT `FK8j0prxxq9o13icwgv4t5x75pn` FOREIGN KEY (`joy_id`) REFERENCES `joy` (`id`),
  CONSTRAINT `FKdtttqnnb3xdqra5d8bd8m3ldh` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `joy_review`
--

DROP TABLE IF EXISTS `joy_review`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `joy_review` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `content` varchar(255) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `is_deleted` tinyint(1) NOT NULL,
  `likes` int(11) NOT NULL,
  `star` double NOT NULL,
  `view` int(11) NOT NULL,
  `joy_id` bigint(20) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK65l0rh4c5flky5o98f6ybu796` (`joy_id`),
  KEY `FKgvgdxccrndas3ch3ogkbiq7o7` (`user_id`),
  CONSTRAINT `FK65l0rh4c5flky5o98f6ybu796` FOREIGN KEY (`joy_id`) REFERENCES `joy` (`id`),
  CONSTRAINT `FKgvgdxccrndas3ch3ogkbiq7o7` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `joy_slot`
--

DROP TABLE IF EXISTS `joy_slot`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `joy_slot` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `reservation` datetime(6) NOT NULL,
  `joy_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `joy_reservation_uk` (`joy_id`,`reservation`),
  CONSTRAINT `FKg214f4u9by3khhymaipjvfsmg` FOREIGN KEY (`joy_id`) REFERENCES `joy` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `joy_status_history`
--

DROP TABLE IF EXISTS `joy_status_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `joy_status_history` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `reason_code` varchar(255) NOT NULL,
  `to_status` enum('CANCELED','FAILED','PAID','PENDING') NOT NULL,
  `joy_order_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK94xunj7c3q6tc9m4wdckp5fkw` (`joy_order_id`),
  CONSTRAINT `FK94xunj7c3q6tc9m4wdckp5fkw` FOREIGN KEY (`joy_order_id`) REFERENCES `joy_order` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `order_item`
--

DROP TABLE IF EXISTS `order_item`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `order_item` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `carrier_code` varchar(255) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  `delivered_at` datetime(6) DEFAULT NULL,
  `fulfillment_status` enum('ALLOCATED','CANCELED','CREATED','DELIVERED','FAILED','IN_TRANSIT','PACKED') NOT NULL,
  `is_deleted` tinyint(1) NOT NULL,
  `quantity` int(11) NOT NULL,
  `refund_status` enum('NONE','REFUNDED','REQUESTED','RETURNED') NOT NULL,
  `shipped_at` datetime(6) DEFAULT NULL,
  `tracking_no` varchar(255) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `order_id` bigint(20) NOT NULL,
  `product_id` bigint(20) NOT NULL,
  `amount` decimal(12,2) NOT NULL,
  `provider_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKt4dc2r9nbvbujrljv3e23iibt` (`order_id`),
  KEY `FK551losx9j75ss5d6bfsqvijna` (`product_id`),
  KEY `FK4isqg9g0i9mp1koa8buwgjdif` (`provider_id`),
  CONSTRAINT `FK4isqg9g0i9mp1koa8buwgjdif` FOREIGN KEY (`provider_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FK551losx9j75ss5d6bfsqvijna` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`),
  CONSTRAINT `FKt4dc2r9nbvbujrljv3e23iibt` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `order_item_fulfillment_history`
--

DROP TABLE IF EXISTS `order_item_fulfillment_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `order_item_fulfillment_history` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `reason_code` varchar(255) NOT NULL,
  `to_status` enum('ALLOCATED','CANCELED','CREATED','DELIVERED','FAILED','IN_TRANSIT','PACKED') NOT NULL,
  `order_item_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKhqv9kolt3ijq9kvuqsn0o6cp` (`order_item_id`),
  CONSTRAINT `FKhqv9kolt3ijq9kvuqsn0o6cp` FOREIGN KEY (`order_item_id`) REFERENCES `order_item` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `order_item_refund_history`
--

DROP TABLE IF EXISTS `order_item_refund_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `order_item_refund_history` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `to_status` enum('NONE','REFUNDED','REQUESTED','RETURNED') NOT NULL,
  `order_item_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKero8v2ftb311s4akflj54n1r6` (`order_item_id`),
  CONSTRAINT `FKero8v2ftb311s4akflj54n1r6` FOREIGN KEY (`order_item_id`) REFERENCES `order_item` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `order_status_history`
--

DROP TABLE IF EXISTS `order_status_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `order_status_history` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `reason_code` varchar(255) NOT NULL,
  `to_status` enum('FAILED','PAID','PENDING') NOT NULL,
  `order_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKnmcbg3mmbt8wfva97ra40nmp3` (`order_id`),
  CONSTRAINT `FKnmcbg3mmbt8wfva97ra40nmp3` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `orders`
--

DROP TABLE IF EXISTS `orders`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `orders` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `address` varchar(255) NOT NULL,
  `address_detail` varchar(255) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `currency` varchar(255) DEFAULT NULL,
  `is_deleted` tinyint(1) NOT NULL,
  `payer_name` varchar(255) NOT NULL,
  `payer_phone` varchar(255) NOT NULL,
  `payment_status` enum('FAILED','PAID','PENDING') NOT NULL,
  `pg_order_id` uuid NOT NULL,
  `pg_payment_key` varchar(255) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `user_id` bigint(20) NOT NULL,
  `total_amount` decimal(16,2) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKomvh641kjpmy47d6h4sih82vh` (`pg_order_id`),
  UNIQUE KEY `UK6x935m6v9yqviepo7t13a7w3l` (`pg_payment_key`),
  KEY `FK32ql8ubntj5uh44ph9659tiih` (`user_id`),
  CONSTRAINT `FK32ql8ubntj5uh44ph9659tiih` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `product`
--

DROP TABLE IF EXISTS `product`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `product` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `alcohol` double NOT NULL,
  `description` text DEFAULT NULL,
  `discount_rate` decimal(3,1) NOT NULL,
  `final_price` decimal(8,0) NOT NULL,
  `is_deleted` tinyint(1) NOT NULL,
  `is_online_sell` tinyint(1) NOT NULL,
  `is_soldout` tinyint(1) NOT NULL,
  `name` varchar(255) NOT NULL,
  `origin_price` decimal(8,0) NOT NULL,
  `registered_at` datetime(6) NOT NULL,
  `sales_volume` int(11) NOT NULL,
  `volume` int(11) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK47nyv78b35eaufr6aa96vep6n` (`user_id`),
  CONSTRAINT `FK47nyv78b35eaufr6aa96vep6n` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=22 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `product_image`
--

DROP TABLE IF EXISTS `product_image`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `product_image` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `image_key` varchar(255) NOT NULL,
  `seq` int(11) NOT NULL,
  `volume` bigint(20) NOT NULL,
  `product_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_product_image_seq` (`product_id`,`seq`),
  UNIQUE KEY `UKjvsreh71tekpvpknc7ucq36c5` (`image_key`),
  CONSTRAINT `FK6oo0cvcdtb6qmwsga468uuukk` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=22 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `product_review`
--

DROP TABLE IF EXISTS `product_review`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `product_review` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `content` varchar(255) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `is_deleted` tinyint(1) NOT NULL,
  `star` double NOT NULL,
  `product_id` bigint(20) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_product_user` (`product_id`,`user_id`),
  KEY `FKib6mkfaqaj0beph37y4qxmu9x` (`user_id`),
  CONSTRAINT `FKib6mkfaqaj0beph37y4qxmu9x` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKkaqmhakwt05p3n0px81b9pdya` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `product_tag`
--

DROP TABLE IF EXISTS `product_tag`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `product_tag` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `product_id` bigint(20) NOT NULL,
  `tag_id` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_product_tag` (`product_id`,`tag_id`),
  KEY `FKgp9r9cogdtnjbwn6rw2dv0o00` (`tag_id`),
  CONSTRAINT `FK2rf7w3d88x20p7vuc2m9mvv91` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`),
  CONSTRAINT `FKgp9r9cogdtnjbwn6rw2dv0o00` FOREIGN KEY (`tag_id`) REFERENCES `tags` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `region_type`
--

DROP TABLE IF EXISTS `region_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `region_type` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKc7mbs7f3re5fhg7hsyb9rxfgw` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `role`
--

DROP TABLE IF EXISTS `role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `role` (
  `id` int(11) NOT NULL,
  `name` enum('ROLE_ADMIN','ROLE_BREWERY','ROLE_SELLER','ROLE_USER') NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK8sewwnpamngi6b1dwaa88askk` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `seller`
--

DROP TABLE IF EXISTS `seller`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `seller` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `business_registration_number` varchar(255) NOT NULL,
  `introduction` text DEFAULT NULL,
  `is_agreed_seller` tinyint(1) NOT NULL,
  `is_deleted` tinyint(1) NOT NULL,
  `registered_at` date NOT NULL,
  `seller_account_number` varchar(255) NOT NULL,
  `seller_address` varchar(255) NOT NULL,
  `seller_address_detail` varchar(255) NOT NULL,
  `seller_bank_name` varchar(255) NOT NULL,
  `seller_depositor` varchar(255) NOT NULL,
  `seller_name` varchar(255) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKetfpl3vymasxfqc4ri4r3euf6` (`user_id`),
  CONSTRAINT `FK614u1eblpnxmrxd25efo29qhr` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=53 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `seller_image`
--

DROP TABLE IF EXISTS `seller_image`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `seller_image` (
  `seq` int(11) NOT NULL,
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `seller_id` bigint(20) NOT NULL,
  `volume` bigint(20) NOT NULL,
  `image_key` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_seller_image_seq` (`seller_id`,`seq`),
  CONSTRAINT `FKce89y8gn2x1mat1ugne2hs68q` FOREIGN KEY (`seller_id`) REFERENCES `seller` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tag_category`
--

DROP TABLE IF EXISTS `tag_category`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `tag_category` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `is_deleted` tinyint(1) NOT NULL,
  `name` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKr6fyn23vu41qbpfimbph6lxot` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tags`
--

DROP TABLE IF EXISTS `tags`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `tags` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `is_deleted` tinyint(1) NOT NULL,
  `name` varchar(255) NOT NULL,
  `tag_category_id` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKt48xdq560gs3gap9g7jg36kgc` (`name`),
  KEY `FK4swaxkm04gqwal7p6hhhouhqh` (`tag_category_id`),
  CONSTRAINT `FK4swaxkm04gqwal7p6hhhouhqh` FOREIGN KEY (`tag_category_id`) REFERENCES `tag_category` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `address` varchar(255) NOT NULL,
  `address_detail` varchar(255) NOT NULL,
  `birth` date NOT NULL,
  `created_at` date NOT NULL,
  `email` varchar(255) NOT NULL,
  `gender` tinyint(1) NOT NULL,
  `is_agreed` tinyint(1) NOT NULL,
  `is_deleted` tinyint(1) NOT NULL,
  `name` varchar(255) NOT NULL,
  `nickname` varchar(255) NOT NULL,
  `o_auth2id` varchar(255) DEFAULT NULL,
  `password` varchar(255) NOT NULL,
  `phone` varchar(20) NOT NULL,
  `role_id` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK6dotkott2kjsp8vw4d0m25fb7` (`email`),
  UNIQUE KEY `UKdu5v5sr43g5bfnji4vb8hg5s3` (`phone`),
  UNIQUE KEY `UKl72c0piyaqajyc5rmgo2bnqnu` (`o_auth2id`),
  KEY `FK4qu1gr772nnf6ve5af002rwya` (`role_id`),
  CONSTRAINT `FK4qu1gr772nnf6ve5af002rwya` FOREIGN KEY (`role_id`) REFERENCES `role` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=116 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping routines for database 'monghyang'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-11-18  6:10:47
