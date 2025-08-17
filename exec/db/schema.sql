-- MySQL Workbench Forward Engineering

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';

-- -----------------------------------------------------
-- Schema mydb
-- -----------------------------------------------------
-- -----------------------------------------------------
-- Schema duckon_prod
-- -----------------------------------------------------

-- -----------------------------------------------------
-- Schema duckon_prod
-- -----------------------------------------------------
CREATE SCHEMA IF NOT EXISTS `duckon_prod` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci ;
USE `duckon_prod` ;

-- -----------------------------------------------------
-- Table `duckon_prod`.`artist`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `duckon_prod`.`artist` (
  `artist_id` BIGINT NOT NULL AUTO_INCREMENT,
  `debut_date` DATE NOT NULL,
  `img_url` TEXT NULL DEFAULT NULL,
  `name_en` VARCHAR(100) NOT NULL,
  `name_kr` VARCHAR(100) NOT NULL,
  PRIMARY KEY (`artist_id`),
  INDEX `idx_artist_debut` (`debut_date` ASC) VISIBLE,
  INDEX `idx_artist_name_en` (`name_en` ASC) VISIBLE,
  INDEX `idx_artist_name_kr` (`name_kr` ASC) VISIBLE)
ENGINE = InnoDB
AUTO_INCREMENT = 197
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `duckon_prod`.`user`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `duckon_prod`.`user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `created_at` DATETIME(6) NULL DEFAULT NULL,
  `deleted` BIT(1) NOT NULL,
  `deleted_at` DATETIME(6) NULL DEFAULT NULL,
  `email` VARCHAR(255) NOT NULL,
  `has_local_credential` BIT(1) NULL DEFAULT NULL,
  `img_url` TEXT NULL DEFAULT NULL,
  `language` VARCHAR(2) NOT NULL,
  `nickname` VARCHAR(100) NOT NULL,
  `password` VARCHAR(255) NOT NULL,
  `provider` ENUM('GOOGLE', 'KAKAO', 'LOCAL', 'NAVER') NULL DEFAULT NULL,
  `provider_id` VARCHAR(255) NULL DEFAULT NULL,
  `role` ENUM('ADMIN', 'USER') NOT NULL,
  `user_id` VARCHAR(50) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `UKob8kqyqqgmefl0aco34akdtpe` (`email` ASC) VISIBLE,
  UNIQUE INDEX `UKa3imlf41l37utmxiquukk8ajc` (`user_id` ASC) VISIBLE,
  UNIQUE INDEX `uk_user_user_id` (`user_id` ASC) VISIBLE,
  UNIQUE INDEX `uk_user_email` (`email` ASC) VISIBLE,
  UNIQUE INDEX `uk_user_provider_pid` (`provider` ASC, `provider_id` ASC) VISIBLE)
ENGINE = InnoDB
AUTO_INCREMENT = 147
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `duckon_prod`.`artist_follow`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `duckon_prod`.`artist_follow` (
  `artist_id` BIGINT NOT NULL,
  `user_id` BIGINT NOT NULL,
  `created_at` DATETIME(6) NOT NULL,
  PRIMARY KEY (`artist_id`, `user_id`),
  INDEX `FK62mpyj1xkf107gy8wt2lu9w4l` (`user_id` ASC) VISIBLE,
  INDEX `idx_artist_follow_artist` (`artist_id` ASC) VISIBLE,
  CONSTRAINT `FK62mpyj1xkf107gy8wt2lu9w4l`
    FOREIGN KEY (`user_id`)
    REFERENCES `duckon_prod`.`user` (`id`),
  CONSTRAINT `FKnpwfyaffcsiq7rqijceh0ow7s`
    FOREIGN KEY (`artist_id`)
    REFERENCES `duckon_prod`.`artist` (`artist_id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `duckon_prod`.`follow`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `duckon_prod`.`follow` (
  `follower_id` BIGINT NOT NULL,
  `following_id` BIGINT NOT NULL,
  `created_at` DATETIME(6) NOT NULL,
  PRIMARY KEY (`follower_id`, `following_id`),
  INDEX `FKqme6uru2g9wx9iysttk542esm` (`following_id` ASC) VISIBLE,
  CONSTRAINT `FKmow2qk674plvwyb4wqln37svv`
    FOREIGN KEY (`follower_id`)
    REFERENCES `duckon_prod`.`user` (`id`),
  CONSTRAINT `FKqme6uru2g9wx9iysttk542esm`
    FOREIGN KEY (`following_id`)
    REFERENCES `duckon_prod`.`user` (`id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `duckon_prod`.`penalty`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `duckon_prod`.`penalty` (
  `penalty_id` BIGINT NOT NULL AUTO_INCREMENT,
  `end_at` DATETIME(6) NULL DEFAULT NULL,
  `penalty_type` ENUM('ACCOUNT_SUSPENSION', 'CHAT_BAN', 'ROOM_CREATION_BAN') NOT NULL,
  `reason` VARCHAR(255) NOT NULL,
  `start_at` DATETIME(6) NULL DEFAULT NULL,
  `status` ENUM('ACTIVE', 'EXPIRED', 'RELEASED') NOT NULL,
  `user_id` BIGINT NOT NULL,
  PRIMARY KEY (`penalty_id`),
  INDEX `FKnldcdm2661qwmocy5g4ejc5mo` (`user_id` ASC) VISIBLE,
  CONSTRAINT `FKnldcdm2661qwmocy5g4ejc5mo`
    FOREIGN KEY (`user_id`)
    REFERENCES `duckon_prod`.`user` (`id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `duckon_prod`.`report`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `duckon_prod`.`report` (
  `report_id` BIGINT NOT NULL AUTO_INCREMENT,
  `report_reason` VARCHAR(255) NULL DEFAULT NULL,
  `report_status` ENUM('APPROVED', 'PENDING', 'REJECTED') NOT NULL,
  `report_type` ENUM('MESSAGE', 'ROOM') NOT NULL,
  `reported_at` DATETIME(6) NOT NULL,
  `reported_content` VARCHAR(255) NULL DEFAULT NULL,
  `reported_user_id` BIGINT NOT NULL,
  `reporter_user_id` BIGINT NOT NULL,
  PRIMARY KEY (`report_id`),
  INDEX `FKgv5el6pnw9fbo9shq49ww3m4e` (`reported_user_id` ASC) VISIBLE,
  INDEX `FKn64sd5p2ql3abexm8ht1vhi80` (`reporter_user_id` ASC) VISIBLE,
  CONSTRAINT `FKgv5el6pnw9fbo9shq49ww3m4e`
    FOREIGN KEY (`reported_user_id`)
    REFERENCES `duckon_prod`.`user` (`id`),
  CONSTRAINT `FKn64sd5p2ql3abexm8ht1vhi80`
    FOREIGN KEY (`reporter_user_id`)
    REFERENCES `duckon_prod`.`user` (`id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `duckon_prod`.`room`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `duckon_prod`.`room` (
  `room_id` BIGINT NOT NULL AUTO_INCREMENT,
  `created_at` DATETIME(6) NULL DEFAULT NULL,
  `img_url` TEXT NULL DEFAULT NULL,
  `title` VARCHAR(100) NOT NULL,
  `artist_id` BIGINT NOT NULL,
  `creator_id` BIGINT NOT NULL,
  PRIMARY KEY (`room_id`),
  INDEX `FKkx8kghicx0bnso3uobylbhmqq` (`artist_id` ASC) VISIBLE,
  INDEX `FKisdkhsvbo7y96l64ehryi59ss` (`creator_id` ASC) VISIBLE,
  CONSTRAINT `FKisdkhsvbo7y96l64ehryi59ss`
    FOREIGN KEY (`creator_id`)
    REFERENCES `duckon_prod`.`user` (`id`),
  CONSTRAINT `FKkx8kghicx0bnso3uobylbhmqq`
    FOREIGN KEY (`artist_id`)
    REFERENCES `duckon_prod`.`artist` (`artist_id`))
ENGINE = InnoDB
AUTO_INCREMENT = 133
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `duckon_prod`.`user_block`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `duckon_prod`.`user_block` (
  `blocked_id` BIGINT NOT NULL,
  `blocker_id` BIGINT NOT NULL,
  `created_at` DATETIME(6) NOT NULL,
  PRIMARY KEY (`blocked_id`, `blocker_id`),
  INDEX `FKla30ofkpxixhf1cmi2a2veban` (`blocker_id` ASC) VISIBLE,
  CONSTRAINT `FKccncjsehavren2hx4gmenhwim`
    FOREIGN KEY (`blocked_id`)
    REFERENCES `duckon_prod`.`user` (`id`),
  CONSTRAINT `FKla30ofkpxixhf1cmi2a2veban`
    FOREIGN KEY (`blocker_id`)
    REFERENCES `duckon_prod`.`user` (`id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
