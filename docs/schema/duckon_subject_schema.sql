-- MySQL Workbench Forward Engineering

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';

-- -----------------------------------------------------
-- Schema mydb
-- -----------------------------------------------------
-- -----------------------------------------------------
-- Schema duckon
-- -----------------------------------------------------

-- -----------------------------------------------------
-- Schema duckon
-- -----------------------------------------------------
CREATE SCHEMA IF NOT EXISTS `duckon` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci ;
USE `duckon` ;

-- -----------------------------------------------------
-- Table `duckon`.`domain`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `duckon`.`domain` ;

CREATE TABLE IF NOT EXISTS `duckon`.`domain` (
  `domain_id` BIGINT NOT NULL AUTO_INCREMENT,
  `code` VARCHAR(50) NOT NULL,
  `name` VARCHAR(100) NOT NULL,
  PRIMARY KEY (`domain_id`),
  UNIQUE INDEX `UK88abui0ticslb6nb2mnjipfjw` (`code` ASC) VISIBLE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `duckon`.`category`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `duckon`.`category` ;

CREATE TABLE IF NOT EXISTS `duckon`.`category` (
  `depth` TINYINT NOT NULL,
  `category_id` BIGINT NOT NULL AUTO_INCREMENT,
  `domain_id` BIGINT NOT NULL,
  `parent_id` BIGINT NULL DEFAULT NULL,
  `code` VARCHAR(100) NOT NULL,
  `name` VARCHAR(100) NOT NULL,
  PRIMARY KEY (`category_id`),
  UNIQUE INDEX `uk_category_domain_code` (`domain_id` ASC, `code` ASC) VISIBLE,
  INDEX `FK2y94svpmqttx80mshyny85wqr` (`parent_id` ASC) VISIBLE,
  CONSTRAINT `FK2y94svpmqttx80mshyny85wqr`
    FOREIGN KEY (`parent_id`)
    REFERENCES `duckon`.`category` (`category_id`),
  CONSTRAINT `FKm8kqjvj9rkgqkodggplaki3n2`
    FOREIGN KEY (`domain_id`)
    REFERENCES `duckon`.`domain` (`domain_id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `duckon`.`user`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `duckon`.`user` ;

CREATE TABLE IF NOT EXISTS `duckon`.`user` (
  `deleted` BIT(1) NOT NULL,
  `has_local_credential` BIT(1) NULL DEFAULT NULL,
  `language` VARCHAR(2) NOT NULL,
  `created_at` DATETIME(6) NULL DEFAULT NULL,
  `deleted_at` DATETIME(6) NULL DEFAULT NULL,
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` VARCHAR(50) NOT NULL,
  `nickname` VARCHAR(100) NOT NULL,
  `email` VARCHAR(255) NOT NULL,
  `img_url` TEXT NULL DEFAULT NULL,
  `password` VARCHAR(255) NOT NULL,
  `provider_id` VARCHAR(255) NULL DEFAULT NULL,
  `provider` ENUM('GOOGLE', 'KAKAO', 'LOCAL', 'NAVER') NULL DEFAULT NULL,
  `role` ENUM('ADMIN', 'USER') NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `uk_user_user_id` (`user_id` ASC) VISIBLE,
  UNIQUE INDEX `uk_user_email` (`email` ASC) VISIBLE,
  UNIQUE INDEX `uk_user_provider_pid` (`provider` ASC, `provider_id` ASC) VISIBLE)
ENGINE = InnoDB
AUTO_INCREMENT = 2
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `duckon`.`follow`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `duckon`.`follow` ;

CREATE TABLE IF NOT EXISTS `duckon`.`follow` (
  `created_at` DATETIME(6) NOT NULL,
  `follower_id` BIGINT NOT NULL,
  `following_id` BIGINT NOT NULL,
  PRIMARY KEY (`follower_id`, `following_id`),
  INDEX `FKqme6uru2g9wx9iysttk542esm` (`following_id` ASC) VISIBLE,
  CONSTRAINT `FKmow2qk674plvwyb4wqln37svv`
    FOREIGN KEY (`follower_id`)
    REFERENCES `duckon`.`user` (`id`),
  CONSTRAINT `FKqme6uru2g9wx9iysttk542esm`
    FOREIGN KEY (`following_id`)
    REFERENCES `duckon`.`user` (`id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `duckon`.`penalty`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `duckon`.`penalty` ;

CREATE TABLE IF NOT EXISTS `duckon`.`penalty` (
  `end_at` DATETIME(6) NULL DEFAULT NULL,
  `penalty_id` BIGINT NOT NULL AUTO_INCREMENT,
  `start_at` DATETIME(6) NULL DEFAULT NULL,
  `user_id` BIGINT NOT NULL,
  `reason` VARCHAR(255) NOT NULL,
  `penalty_type` ENUM('ACCOUNT_SUSPENSION', 'CHAT_BAN', 'ROOM_CREATION_BAN') NOT NULL,
  `status` ENUM('ACTIVE', 'EXPIRED', 'RELEASED') NOT NULL,
  PRIMARY KEY (`penalty_id`),
  INDEX `FKnldcdm2661qwmocy5g4ejc5mo` (`user_id` ASC) VISIBLE,
  CONSTRAINT `FKnldcdm2661qwmocy5g4ejc5mo`
    FOREIGN KEY (`user_id`)
    REFERENCES `duckon`.`user` (`id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `duckon`.`report`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `duckon`.`report` ;

CREATE TABLE IF NOT EXISTS `duckon`.`report` (
  `report_id` BIGINT NOT NULL AUTO_INCREMENT,
  `reported_at` DATETIME(6) NOT NULL,
  `reported_user_id` BIGINT NOT NULL,
  `reporter_user_id` BIGINT NOT NULL,
  `report_reason` VARCHAR(255) NULL DEFAULT NULL,
  `reported_content` VARCHAR(255) NULL DEFAULT NULL,
  `report_status` ENUM('APPROVED', 'PENDING', 'REJECTED') NOT NULL,
  `report_type` ENUM('MESSAGE', 'ROOM') NOT NULL,
  PRIMARY KEY (`report_id`),
  INDEX `FKgv5el6pnw9fbo9shq49ww3m4e` (`reported_user_id` ASC) VISIBLE,
  INDEX `FKn64sd5p2ql3abexm8ht1vhi80` (`reporter_user_id` ASC) VISIBLE,
  CONSTRAINT `FKgv5el6pnw9fbo9shq49ww3m4e`
    FOREIGN KEY (`reported_user_id`)
    REFERENCES `duckon`.`user` (`id`),
  CONSTRAINT `FKn64sd5p2ql3abexm8ht1vhi80`
    FOREIGN KEY (`reporter_user_id`)
    REFERENCES `duckon`.`user` (`id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `duckon`.`subject`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `duckon`.`subject` ;

CREATE TABLE IF NOT EXISTS `duckon`.`subject` (
  `country_code` VARCHAR(2) NOT NULL,
  `debut_date` DATE NULL DEFAULT NULL,
  `created_at` DATETIME(6) NULL DEFAULT NULL,
  `domain_id` BIGINT NOT NULL,
  `primary_category_id` BIGINT NULL DEFAULT NULL,
  `subject_id` BIGINT NOT NULL AUTO_INCREMENT,
  `native_locale` VARCHAR(20) NOT NULL,
  `slug` VARCHAR(120) NOT NULL,
  `img_url` TINYTEXT NULL DEFAULT NULL,
  PRIMARY KEY (`subject_id`),
  UNIQUE INDEX `uk_subject_slug` (`slug` ASC) VISIBLE,
  INDEX `idx_subject_domain` (`domain_id` ASC) VISIBLE,
  INDEX `idx_subject_primary_cat` (`primary_category_id` ASC) VISIBLE,
  CONSTRAINT `FK7upjguyt42l48u378b88fjy0`
    FOREIGN KEY (`domain_id`)
    REFERENCES `duckon`.`domain` (`domain_id`),
  CONSTRAINT `FKgdvdmfingjie7kgv56g2vvhis`
    FOREIGN KEY (`primary_category_id`)
    REFERENCES `duckon`.`category` (`category_id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `duckon`.`room`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `duckon`.`room` ;

CREATE TABLE IF NOT EXISTS `duckon`.`room` (
  `created_at` DATETIME(6) NULL DEFAULT NULL,
  `creator_id` BIGINT NOT NULL,
  `room_id` BIGINT NOT NULL AUTO_INCREMENT,
  `subject_id` BIGINT NOT NULL,
  `title` VARCHAR(100) NOT NULL,
  `img_url` TEXT NULL DEFAULT NULL,
  PRIMARY KEY (`room_id`),
  INDEX `FKisdkhsvbo7y96l64ehryi59ss` (`creator_id` ASC) VISIBLE,
  INDEX `FK57s6s3jp9mq6qt4adofm04ckf` (`subject_id` ASC) VISIBLE,
  CONSTRAINT `FK57s6s3jp9mq6qt4adofm04ckf`
    FOREIGN KEY (`subject_id`)
    REFERENCES `duckon`.`subject` (`subject_id`),
  CONSTRAINT `FKisdkhsvbo7y96l64ehryi59ss`
    FOREIGN KEY (`creator_id`)
    REFERENCES `duckon`.`user` (`id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `duckon`.`subject_category_map`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `duckon`.`subject_category_map` ;

CREATE TABLE IF NOT EXISTS `duckon`.`subject_category_map` (
  `category_id` BIGINT NOT NULL,
  `subject_id` BIGINT NOT NULL,
  PRIMARY KEY (`category_id`, `subject_id`),
  INDEX `FK3v0ub47ln8x3f7mpj7onc3ng1` (`subject_id` ASC) VISIBLE,
  CONSTRAINT `FK3v0ub47ln8x3f7mpj7onc3ng1`
    FOREIGN KEY (`subject_id`)
    REFERENCES `duckon`.`subject` (`subject_id`),
  CONSTRAINT `FK9su0hbkb289xohvmin86ljfb9`
    FOREIGN KEY (`category_id`)
    REFERENCES `duckon`.`category` (`category_id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `duckon`.`subject_follow`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `duckon`.`subject_follow` ;

CREATE TABLE IF NOT EXISTS `duckon`.`subject_follow` (
  `created_at` DATETIME(6) NOT NULL,
  `subject_id` BIGINT NOT NULL,
  `user_id` BIGINT NOT NULL,
  PRIMARY KEY (`subject_id`, `user_id`),
  INDEX `FK1au8gkktm44jl11wsdfhq7j73` (`user_id` ASC) VISIBLE,
  CONSTRAINT `FK1au8gkktm44jl11wsdfhq7j73`
    FOREIGN KEY (`user_id`)
    REFERENCES `duckon`.`user` (`id`),
  CONSTRAINT `FKtjqjnpvjan47sd4sw8mquitpy`
    FOREIGN KEY (`subject_id`)
    REFERENCES `duckon`.`subject` (`subject_id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `duckon`.`subject_name`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `duckon`.`subject_name` ;

CREATE TABLE IF NOT EXISTS `duckon`.`subject_name` (
  `is_primary` BIT(1) NOT NULL,
  `priority` SMALLINT NOT NULL,
  `created_at` DATETIME(6) NOT NULL,
  `name_id` BIGINT NOT NULL AUTO_INCREMENT,
  `subject_id` BIGINT NOT NULL,
  `updated_at` DATETIME(6) NULL DEFAULT NULL,
  `locale_tag` VARCHAR(20) NOT NULL,
  `name` VARCHAR(200) NOT NULL,
  `name_type` ENUM('ALIAS', 'OFFICIAL', 'ROMANIZED', 'TRANSLATED') NOT NULL,
  PRIMARY KEY (`name_id`),
  UNIQUE INDEX `uk_sn_subject_locale_name` (`subject_id` ASC, `locale_tag` ASC, `name` ASC) VISIBLE,
  INDEX `idx_sn_subject` (`subject_id` ASC) VISIBLE,
  INDEX `idx_sn_locale` (`locale_tag` ASC) VISIBLE,
  INDEX `idx_sn_name` (`name` ASC) VISIBLE,
  CONSTRAINT `FKefinoj1gor6cpc4n1bfphbea6`
    FOREIGN KEY (`subject_id`)
    REFERENCES `duckon`.`subject` (`subject_id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `duckon`.`user_block`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `duckon`.`user_block` ;

CREATE TABLE IF NOT EXISTS `duckon`.`user_block` (
  `blocked_id` BIGINT NOT NULL,
  `blocker_id` BIGINT NOT NULL,
  `created_at` DATETIME(6) NOT NULL,
  PRIMARY KEY (`blocked_id`, `blocker_id`),
  INDEX `FKla30ofkpxixhf1cmi2a2veban` (`blocker_id` ASC) VISIBLE,
  CONSTRAINT `FKccncjsehavren2hx4gmenhwim`
    FOREIGN KEY (`blocked_id`)
    REFERENCES `duckon`.`user` (`id`),
  CONSTRAINT `FKla30ofkpxixhf1cmi2a2veban`
    FOREIGN KEY (`blocker_id`)
    REFERENCES `duckon`.`user` (`id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
