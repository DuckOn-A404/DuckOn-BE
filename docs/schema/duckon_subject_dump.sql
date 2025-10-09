-- ===== Duckon seed data (MUSIC + SPORTS + CREATOR) =====
-- 재실행 안전 / FK 일시 해제
SET @OLD_FK = @@FOREIGN_KEY_CHECKS;
SET FOREIGN_KEY_CHECKS = 0;

USE `duckon`;
SET @now6 := NOW(6);

-- 1) DOMAIN -------------------------------------------------------------------
INSERT INTO `domain` (`code`,`name`)
VALUES ('MUSIC','Music')
ON DUPLICATE KEY UPDATE domain_id = LAST_INSERT_ID(domain_id);
SET @dom_music := LAST_INSERT_ID();

INSERT INTO `domain` (`code`,`name`)
VALUES ('SPORTS','Sports')
ON DUPLICATE KEY UPDATE domain_id = LAST_INSERT_ID(domain_id);
SET @dom_sports := LAST_INSERT_ID();

INSERT INTO `domain` (`code`,`name`)
VALUES ('CREATOR','Creator / Influencer')
ON DUPLICATE KEY UPDATE domain_id = LAST_INSERT_ID(domain_id);
SET @dom_creator := LAST_INSERT_ID();

-- 2) CATEGORY (MUSIC: 장르/유형/성별) -----------------------------------------
-- ROOT: GENRE
INSERT INTO `category` (`depth`,`domain_id`,`parent_id`,`code`,`name`)
VALUES (0,@dom_music,NULL,'GENRE','장르')
ON DUPLICATE KEY UPDATE category_id = LAST_INSERT_ID(category_id);
SET @c_root_genre := LAST_INSERT_ID();

INSERT INTO `category` (`depth`,`domain_id`,`parent_id`,`code`,`name`)
VALUES (1,@dom_music,@c_root_genre,'KPOP','케이팝')
ON DUPLICATE KEY UPDATE category_id = LAST_INSERT_ID(category_id);
SET @c_genre_kpop := LAST_INSERT_ID();

INSERT INTO `category` (`depth`,`domain_id`,`parent_id`,`code`,`name`)
VALUES (1,@dom_music,@c_root_genre,'JPOP','제이팝')
ON DUPLICATE KEY UPDATE category_id = LAST_INSERT_ID(category_id);
SET @c_genre_jpop := LAST_INSERT_ID();

INSERT INTO `category` (`depth`,`domain_id`,`parent_id`,`code`,`name`)
VALUES (1,@dom_music,@c_root_genre,'ROCK','록/락')
ON DUPLICATE KEY UPDATE category_id = LAST_INSERT_ID(category_id);
SET @c_genre_rock := LAST_INSERT_ID();

INSERT INTO `category` (`depth`,`domain_id`,`parent_id`,`code`,`name`)
VALUES (1,@dom_music,@c_root_genre,'INDIE','인디')
ON DUPLICATE KEY UPDATE category_id = LAST_INSERT_ID(category_id);
SET @c_genre_indie := LAST_INSERT_ID();

INSERT INTO `category` (`depth`,`domain_id`,`parent_id`,`code`,`name`)
VALUES (1,@dom_music,@c_root_genre,'POP','팝')
ON DUPLICATE KEY UPDATE category_id = LAST_INSERT_ID(category_id);
SET @c_genre_pop := LAST_INSERT_ID();

-- ROOT: ARTIST_TYPE
INSERT INTO `category` (`depth`,`domain_id`,`parent_id`,`code`,`name`)
VALUES (0,@dom_music,NULL,'ARTIST_TYPE','아티스트 유형')
ON DUPLICATE KEY UPDATE category_id = LAST_INSERT_ID(category_id);
SET @c_root_type := LAST_INSERT_ID();

INSERT INTO `category` (`depth`,`domain_id`,`parent_id`,`code`,`name`)
VALUES (1,@dom_music,@c_root_type,'GROUP','그룹')
ON DUPLICATE KEY UPDATE category_id = LAST_INSERT_ID(category_id);
SET @c_type_group := LAST_INSERT_ID();

INSERT INTO `category` (`depth`,`domain_id`,`parent_id`,`code`,`name`)
VALUES (1,@dom_music,@c_root_type,'SOLO','솔로')
ON DUPLICATE KEY UPDATE category_id = LAST_INSERT_ID(category_id);
SET @c_type_solo := LAST_INSERT_ID();

-- ROOT: GENDER
INSERT INTO `category` (`depth`,`domain_id`,`parent_id`,`code`,`name`)
VALUES (0,@dom_music,NULL,'GENDER','성별')
ON DUPLICATE KEY UPDATE category_id = LAST_INSERT_ID(category_id);
SET @c_root_gender := LAST_INSERT_ID();

INSERT INTO `category` (`depth`,`domain_id`,`parent_id`,`code`,`name`)
VALUES (1,@dom_music,@c_root_gender,'MALE','남성')
ON DUPLICATE KEY UPDATE category_id = LAST_INSERT_ID(category_id);
SET @c_gender_male := LAST_INSERT_ID();

INSERT INTO `category` (`depth`,`domain_id`,`parent_id`,`code`,`name`)
VALUES (1,@dom_music,@c_root_gender,'FEMALE','여성')
ON DUPLICATE KEY UPDATE category_id = LAST_INSERT_ID(category_id);
SET @c_gender_female := LAST_INSERT_ID();

INSERT INTO `category` (`depth`,`domain_id`,`parent_id`,`code`,`name`)
VALUES (1,@dom_music,@c_root_gender,'MIXED','혼성')
ON DUPLICATE KEY UPDATE category_id = LAST_INSERT_ID(category_id);
SET @c_gender_mixed := LAST_INSERT_ID();

-- 3) CATEGORY (SPORTS: SPORT / ROLE / LEAGUE / GENDER) -----------------------
-- ROOT: SPORT
INSERT INTO `category` (`depth`,`domain_id`,`parent_id`,`code`,`name`)
VALUES (0,@dom_sports,NULL,'SPORT','스포츠')
ON DUPLICATE KEY UPDATE category_id = LAST_INSERT_ID(category_id);
SET @c_sp_root := LAST_INSERT_ID();

INSERT INTO `category` (`depth`,`domain_id`,`parent_id`,`code`,`name`)
VALUES (1,@dom_sports,@c_sp_root,'FOOTBALL','축구')
ON DUPLICATE KEY UPDATE category_id = LAST_INSERT_ID(category_id);
SET @c_sp_football := LAST_INSERT_ID();

INSERT INTO `category` (`depth`,`domain_id`,`parent_id`,`code`,`name`)
VALUES (1,@dom_sports,@c_sp_root,'BASEBALL','야구')
ON DUPLICATE KEY UPDATE category_id = LAST_INSERT_ID(category_id);
SET @c_sp_baseball := LAST_INSERT_ID();

INSERT INTO `category` (`depth`,`domain_id`,`parent_id`,`code`,`name`)
VALUES (1,@dom_sports,@c_sp_root,'MOTORSPORT','모터스포츠')
ON DUPLICATE KEY UPDATE category_id = LAST_INSERT_ID(category_id);
SET @c_sp_motorsport := LAST_INSERT_ID();

-- MOTORSPORT child: F1
INSERT INTO `category` (`depth`,`domain_id`,`parent_id`,`code`,`name`)
VALUES (2,@dom_sports,@c_sp_motorsport,'F1','포뮬러 1')
ON DUPLICATE KEY UPDATE category_id = LAST_INSERT_ID(category_id);
SET @c_sp_f1 := LAST_INSERT_ID();

-- ROOT: ROLE
INSERT INTO `category` (`depth`,`domain_id`,`parent_id`,`code`,`name`)
VALUES (0,@dom_sports,NULL,'ROLE','역할')
ON DUPLICATE KEY UPDATE category_id = LAST_INSERT_ID(category_id);
SET @c_role_root := LAST_INSERT_ID();

INSERT INTO `category` (`depth`,`domain_id`,`parent_id`,`code`,`name`)
VALUES (1,@dom_sports,@c_role_root,'PLAYER','선수')
ON DUPLICATE KEY UPDATE category_id = LAST_INSERT_ID(category_id);
SET @c_role_player := LAST_INSERT_ID();

INSERT INTO `category` (`depth`,`domain_id`,`parent_id`,`code`,`name`)
VALUES (1,@dom_sports,@c_role_root,'TEAM','팀')
ON DUPLICATE KEY UPDATE category_id = LAST_INSERT_ID(category_id);
SET @c_role_team := LAST_INSERT_ID();

INSERT INTO `category` (`depth`,`domain_id`,`parent_id`,`code`,`name`)
VALUES (1,@dom_sports,@c_role_root,'DRIVER','드라이버')
ON DUPLICATE KEY UPDATE category_id = LAST_INSERT_ID(category_id);
SET @c_role_driver := LAST_INSERT_ID();

INSERT INTO `category` (`depth`,`domain_id`,`parent_id`,`code`,`name`)
VALUES (1,@dom_sports,@c_role_root,'CONSTRUCTOR','컨스트럭터')
ON DUPLICATE KEY UPDATE category_id = LAST_INSERT_ID(category_id);
SET @c_role_constructor := LAST_INSERT_ID();

-- ROOT: LEAGUE
INSERT INTO `category` (`depth`,`domain_id`,`parent_id`,`code`,`name`)
VALUES (0,@dom_sports,NULL,'LEAGUE','리그/대회')
ON DUPLICATE KEY UPDATE category_id = LAST_INSERT_ID(category_id);
SET @c_lg_root := LAST_INSERT_ID();

-- FOOTBALL leagues
INSERT INTO `category` (`depth`,`domain_id`,`parent_id`,`code`,`name`)
VALUES (1,@dom_sports,@c_lg_root,'EPL','프리미어리그')
ON DUPLICATE KEY UPDATE category_id = LAST_INSERT_ID(category_id);
SET @c_lg_epl := LAST_INSERT_ID();

INSERT INTO `category` (`depth`,`domain_id`,`parent_id`,`code`,`name`)
VALUES (1,@dom_sports,@c_lg_root,'LALIGA','라리가')
ON DUPLICATE KEY UPDATE category_id = LAST_INSERT_ID(category_id);
SET @c_lg_laliga := LAST_INSERT_ID();

INSERT INTO `category` (`depth`,`domain_id`,`parent_id`,`code`,`name`)
VALUES (1,@dom_sports,@c_lg_root,'K_LEAGUE_1','K리그1')
ON DUPLICATE KEY UPDATE category_id = LAST_INSERT_ID(category_id);
SET @c_lg_k1 := LAST_INSERT_ID();

-- BASEBALL leagues
INSERT INTO `category` (`depth`,`domain_id`,`parent_id`,`code`,`name`)
VALUES (1,@dom_sports,@c_lg_root,'MLB','메이저리그')
ON DUPLICATE KEY UPDATE category_id = LAST_INSERT_ID(category_id);
SET @c_lg_mlb := LAST_INSERT_ID();

INSERT INTO `category` (`depth`,`domain_id`,`parent_id`,`code`,`name`)
VALUES (1,@dom_sports,@c_lg_root,'KBO','KBO리그')
ON DUPLICATE KEY UPDATE category_id = LAST_INSERT_ID(category_id);
SET @c_lg_kbo := LAST_INSERT_ID();

-- F1 (챔피언십을 리그로)
INSERT INTO `category` (`depth`,`domain_id`,`parent_id`,`code`,`name`)
VALUES (1,@dom_sports,@c_lg_root,'FIA_F1','FIA 포뮬러 원 월드 챔피언십')
ON DUPLICATE KEY UPDATE category_id = LAST_INSERT_ID(category_id);
SET @c_lg_f1 := LAST_INSERT_ID();

-- ROOT: GENDER
INSERT INTO `category` (`depth`,`domain_id`,`parent_id`,`code`,`name`)
VALUES (0,@dom_sports,NULL,'GENDER','성별')
ON DUPLICATE KEY UPDATE category_id = LAST_INSERT_ID(category_id);
SET @c_sp_gender_root := LAST_INSERT_ID();

INSERT INTO `category` (`depth`,`domain_id`,`parent_id`,`code`,`name`)
VALUES (1,@dom_sports,@c_sp_gender_root,'MALE','남성')
ON DUPLICATE KEY UPDATE category_id = LAST_INSERT_ID(category_id);
SET @c_sp_gender_male := LAST_INSERT_ID();

INSERT INTO `category` (`depth`,`domain_id`,`parent_id`,`code`,`name`)
VALUES (1,@dom_sports,@c_sp_gender_root,'FEMALE','여성')
ON DUPLICATE KEY UPDATE category_id = LAST_INSERT_ID(category_id);
SET @c_sp_gender_female := LAST_INSERT_ID();

INSERT INTO `category` (`depth`,`domain_id`,`parent_id`,`code`,`name`)
VALUES (1,@dom_sports,@c_sp_gender_root,'MIXED','혼성')
ON DUPLICATE KEY UPDATE category_id = LAST_INSERT_ID(category_id);
SET @c_sp_gender_mixed := LAST_INSERT_ID();

-- 4) CATEGORY (CREATOR: PLATFORM / GENRE / ENTITY_TYPE) ----------------------
-- PLATFORM
INSERT INTO `category` (`depth`,`domain_id`,`parent_id`,`code`,`name`)
VALUES (0,@dom_creator,NULL,'PLATFORM','플랫폼')
ON DUPLICATE KEY UPDATE category_id = LAST_INSERT_ID(category_id);
SET @c_cr_plat_root := LAST_INSERT_ID();

INSERT INTO `category` (`depth`,`domain_id`,`parent_id`,`code`,`name`)
VALUES (1,@dom_creator,@c_cr_plat_root,'YOUTUBE','YouTube')
ON DUPLICATE KEY UPDATE category_id = LAST_INSERT_ID(category_id);
SET @c_cr_yt := LAST_INSERT_ID();

INSERT INTO `category` (`depth`,`domain_id`,`parent_id`,`code`,`name`)
VALUES (1,@dom_creator,@c_cr_plat_root,'TWITCH','Twitch')
ON DUPLICATE KEY UPDATE category_id = LAST_INSERT_ID(category_id);
SET @c_cr_tw := LAST_INSERT_ID();

-- GENRE
INSERT INTO `category` (`depth`,`domain_id`,`parent_id`,`code`,`name`)
VALUES (0,@dom_creator,NULL,'GENRE','콘텐츠 장르')
ON DUPLICATE KEY UPDATE category_id = LAST_INSERT_ID(category_id);
SET @c_cr_genre_root := LAST_INSERT_ID();

INSERT INTO `category` (`depth`,`domain_id`,`parent_id`,`code`,`name`)
VALUES (1,@dom_creator,@c_cr_genre_root,'GAMING','게임')
ON DUPLICATE KEY UPDATE category_id = LAST_INSERT_ID(category_id);
SET @c_cr_gaming := LAST_INSERT_ID();

INSERT INTO `category` (`depth`,`domain_id`,`parent_id`,`code`,`name`)
VALUES (1,@dom_creator,@c_cr_genre_root,'TALK','토크/예능')
ON DUPLICATE KEY UPDATE category_id = LAST_INSERT_ID(category_id);
SET @c_cr_talk := LAST_INSERT_ID();

INSERT INTO `category` (`depth`,`domain_id`,`parent_id`,`code`,`name`)
VALUES (1,@dom_creator,@c_cr_genre_root,'TRAVEL','여행')
ON DUPLICATE KEY UPDATE category_id = LAST_INSERT_ID(category_id);
SET @c_cr_travel := LAST_INSERT_ID();

INSERT INTO `category` (`depth`,`domain_id`,`parent_id`,`code`,`name`)
VALUES (1,@dom_creator,@c_cr_genre_root,'FOOD','먹방/요리')
ON DUPLICATE KEY UPDATE category_id = LAST_INSERT_ID(category_id);
SET @c_cr_food := LAST_INSERT_ID();

-- ENTITY TYPE
INSERT INTO `category` (`depth`,`domain_id`,`parent_id`,`code`,`name`)
VALUES (0,@dom_creator,NULL,'ENTITY_TYPE','개체 유형')
ON DUPLICATE KEY UPDATE category_id = LAST_INSERT_ID(category_id);
SET @c_cr_et_root := LAST_INSERT_ID();

INSERT INTO `category` (`depth`,`domain_id`,`parent_id`,`code`,`name`)
VALUES (1,@dom_creator,@c_cr_et_root,'INDIVIDUAL','개인')
ON DUPLICATE KEY UPDATE category_id = LAST_INSERT_ID(category_id);
SET @c_cr_individual := LAST_INSERT_ID();

INSERT INTO `category` (`depth`,`domain_id`,`parent_id`,`code`,`name`)
VALUES (1,@dom_creator,@c_cr_et_root,'ORG','단체/기업')
ON DUPLICATE KEY UPDATE category_id = LAST_INSERT_ID(category_id);
SET @c_cr_org := LAST_INSERT_ID();

-- 5) SUBJECTS (MUSIC) ---------------------------------------------------------
-- BLACKPINK
INSERT INTO `subject`
(`country_code`,`debut_date`,`created_at`,`domain_id`,`primary_category_id`,`native_locale`,`slug`,`img_url`)
VALUES ('KR','2016-08-08',@now6,@dom_music,@c_genre_kpop,'ko-KR','blackpink',NULL)
ON DUPLICATE KEY UPDATE subject_id = LAST_INSERT_ID(subject_id);
SET @s := LAST_INSERT_ID();

INSERT IGNORE INTO `subject_name`
(`is_primary`,`priority`,`created_at`,`subject_id`,`updated_at`,`locale_tag`,`name`,`name_type`)
VALUES (b'1',1,@now6,@s,NULL,'ko-KR','블랙핑크','OFFICIAL'),
       (b'0',2,@now6,@s,NULL,'en-US','BLACKPINK','OFFICIAL');

INSERT IGNORE INTO `subject_category_map` (`category_id`,`subject_id`)
VALUES (@c_genre_kpop,@s),(@c_type_group,@s),(@c_gender_female,@s);

-- BTS
INSERT INTO `subject`
(`country_code`,`debut_date`,`created_at`,`domain_id`,`primary_category_id`,`native_locale`,`slug`,`img_url`)
VALUES ('KR','2013-06-13',@now6,@dom_music,@c_genre_kpop,'ko-KR','bts',NULL)
ON DUPLICATE KEY UPDATE subject_id = LAST_INSERT_ID(subject_id);
SET @s := LAST_INSERT_ID();

INSERT IGNORE INTO `subject_name`
(`is_primary`,`priority`,`created_at`,`subject_id`,`updated_at`,`locale_tag`,`name`,`name_type`)
VALUES (b'1',1,@now6,@s,NULL,'ko-KR','방탄소년단','OFFICIAL'),
       (b'0',2,@now6,@s,NULL,'en-US','BTS','OFFICIAL');

INSERT IGNORE INTO `subject_category_map` (`category_id`,`subject_id`)
VALUES (@c_genre_kpop,@s),(@c_type_group,@s),(@c_gender_male,@s);

-- EXO
INSERT INTO `subject`
(`country_code`,`debut_date`,`created_at`,`domain_id`,`primary_category_id`,`native_locale`,`slug`,`img_url`)
VALUES ('KR','2012-04-08',@now6,@dom_music,@c_genre_kpop,'ko-KR','exo',NULL)
ON DUPLICATE KEY UPDATE subject_id = LAST_INSERT_ID(subject_id);
SET @s := LAST_INSERT_ID();

INSERT IGNORE INTO `subject_name`
(`is_primary`,`priority`,`created_at`,`subject_id`,`updated_at`,`locale_tag`,`name`,`name_type`)
VALUES (b'1',1,@now6,@s,NULL,'ko-KR','엑소','OFFICIAL'),
       (b'0',2,@now6,@s,NULL,'en-US','EXO','OFFICIAL');

INSERT IGNORE INTO `subject_category_map` (`category_id`,`subject_id`)
VALUES (@c_genre_kpop,@s),(@c_type_group,@s),(@c_gender_male,@s);

-- TWICE
INSERT INTO `subject`
(`country_code`,`debut_date`,`created_at`,`domain_id`,`primary_category_id`,`native_locale`,`slug`,`img_url`)
VALUES ('KR','2015-10-20',@now6,@dom_music,@c_genre_kpop,'ko-KR','twice',NULL)
ON DUPLICATE KEY UPDATE subject_id = LAST_INSERT_ID(subject_id);
SET @s := LAST_INSERT_ID();

INSERT IGNORE INTO `subject_name`
(`is_primary`,`priority`,`created_at`,`subject_id`,`updated_at`,`locale_tag`,`name`,`name_type`)
VALUES (b'1',1,@now6,@s,NULL,'ko-KR','트와이스','OFFICIAL'),
       (b'0',2,@now6,@s,NULL,'en-US','TWICE','OFFICIAL');

INSERT IGNORE INTO `subject_category_map` (`category_id`,`subject_id`)
VALUES (@c_genre_kpop,@s),(@c_type_group,@s),(@c_gender_female,@s);

-- Red Velvet
INSERT INTO `subject`
(`country_code`,`debut_date`,`created_at`,`domain_id`,`primary_category_id`,`native_locale`,`slug`,`img_url`)
VALUES ('KR','2014-08-01',@now6,@dom_music,@c_genre_kpop,'ko-KR','red-velvet',NULL)
ON DUPLICATE KEY UPDATE subject_id = LAST_INSERT_ID(subject_id);
SET @s := LAST_INSERT_ID();

INSERT IGNORE INTO `subject_name`
(`is_primary`,`priority`,`created_at`,`subject_id`,`updated_at`,`locale_tag`,`name`,`name_type`)
VALUES (b'1',1,@now6,@s,NULL,'ko-KR','레드벨벳','OFFICIAL'),
       (b'0',2,@now6,@s,NULL,'en-US','Red Velvet','OFFICIAL');

INSERT IGNORE INTO `subject_category_map` (`category_id`,`subject_id`)
VALUES (@c_genre_kpop,@s),(@c_type_group,@s),(@c_gender_female,@s);

-- (G)I-DLE
INSERT INTO `subject`
(`country_code`,`debut_date`,`created_at`,`domain_id`,`primary_category_id`,`native_locale`,`slug`,`img_url`)
VALUES ('KR','2018-05-02',@now6,@dom_music,@c_genre_kpop,'ko-KR','g-idle',NULL)
ON DUPLICATE KEY UPDATE subject_id = LAST_INSERT_ID(subject_id);
SET @s := LAST_INSERT_ID();

INSERT IGNORE INTO `subject_name`
(`is_primary`,`priority`,`created_at`,`subject_id`,`updated_at`,`locale_tag`,`name`,`name_type`)
VALUES (b'1',1,@now6,@s,NULL,'ko-KR','아이들','OFFICIAL'),
       (b'0',2,@now6,@s,NULL,'en-US','(G)I-DLE','OFFICIAL');

INSERT IGNORE INTO `subject_category_map` (`category_id`,`subject_id`)
VALUES (@c_genre_kpop,@s),(@c_type_group,@s),(@c_gender_female,@s);

-- IVE
INSERT INTO `subject`
(`country_code`,`debut_date`,`created_at`,`domain_id`,`primary_category_id`,`native_locale`,`slug`,`img_url`)
VALUES ('KR','2021-12-01',@now6,@dom_music,@c_genre_kpop,'ko-KR','ive',NULL)
ON DUPLICATE KEY UPDATE subject_id = LAST_INSERT_ID(subject_id);
SET @s := LAST_INSERT_ID();

INSERT IGNORE INTO `subject_name`
(`is_primary`,`priority`,`created_at`,`subject_id`,`updated_at`,`locale_tag`,`name`,`name_type`)
VALUES (b'1',1,@now6,@s,NULL,'ko-KR','아이브','OFFICIAL'),
       (b'0',2,@now6,@s,NULL,'en-US','IVE','OFFICIAL');

INSERT IGNORE INTO `subject_category_map` (`category_id`,`subject_id`)
VALUES (@c_genre_kpop,@s),(@c_type_group,@s),(@c_gender_female,@s);

-- ITZY
INSERT INTO `subject`
(`country_code`,`debut_date`,`created_at`,`domain_id`,`primary_category_id`,`native_locale`,`slug`,`img_url`)
VALUES ('KR','2019-02-12',@now6,@dom_music,@c_genre_kpop,'ko-KR','itzy',NULL)
ON DUPLICATE KEY UPDATE subject_id = LAST_INSERT_ID(subject_id);
SET @s := LAST_INSERT_ID();

INSERT IGNORE INTO `subject_name`
(`is_primary`,`priority`,`created_at`,`subject_id`,`updated_at`,`locale_tag`,`name`,`name_type`)
VALUES (b'1',1,@now6,@s,NULL,'ko-KR','있지','OFFICIAL'),
       (b'0',2,@now6,@s,NULL,'en-US','ITZY','OFFICIAL');

INSERT IGNORE INTO `subject_category_map` (`category_id`,`subject_id`)
VALUES (@c_genre_kpop,@s),(@c_type_group,@s),(@c_gender_female,@s);

-- NewJeans
INSERT INTO `subject`
(`country_code`,`debut_date`,`created_at`,`domain_id`,`primary_category_id`,`native_locale`,`slug`,`img_url`)
VALUES ('KR','2022-07-22',@now6,@dom_music,@c_genre_kpop,'ko-KR','newjeans',NULL)
ON DUPLICATE KEY UPDATE subject_id = LAST_INSERT_ID(subject_id);
SET @s := LAST_INSERT_ID();

INSERT IGNORE INTO `subject_name`
(`is_primary`,`priority`,`created_at`,`subject_id`,`updated_at`,`locale_tag`,`name`,`name_type`)
VALUES (b'1',1,@now6,@s,NULL,'ko-KR','뉴진스','OFFICIAL'),
       (b'0',2,@now6,@s,NULL,'en-US','NewJeans','OFFICIAL');

INSERT IGNORE INTO `subject_category_map` (`category_id`,`subject_id`)
VALUES (@c_genre_kpop,@s),(@c_type_group,@s),(@c_gender_female,@s);

-- Seventeen
INSERT INTO `subject`
(`country_code`,`debut_date`,`created_at`,`domain_id`,`primary_category_id`,`native_locale`,`slug`,`img_url`)
VALUES ('KR','2015-05-26',@now6,@dom_music,@c_genre_kpop,'ko-KR','seventeen',NULL)
ON DUPLICATE KEY UPDATE subject_id = LAST_INSERT_ID(subject_id);
SET @s := LAST_INSERT_ID();

INSERT IGNORE INTO `subject_name`
(`is_primary`,`priority`,`created_at`,`subject_id`,`updated_at`,`locale_tag`,`name`,`name_type`)
VALUES (b'1',1,@now6,@s,NULL,'ko-KR','세븐틴','OFFICIAL'),
       (b'0',2,@now6,@s,NULL,'en-US','Seventeen','OFFICIAL');

INSERT IGNORE INTO `subject_category_map` (`category_id`,`subject_id`)
VALUES (@c_genre_kpop,@s),(@c_type_group,@s),(@c_gender_male,@s);

-- Stray Kids
INSERT INTO `subject`
(`country_code`,`debut_date`,`created_at`,`domain_id`,`primary_category_id`,`native_locale`,`slug`,`img_url`)
VALUES ('KR','2018-03-25',@now6,@dom_music,@c_genre_kpop,'ko-KR','stray-kids',NULL)
ON DUPLICATE KEY UPDATE subject_id = LAST_INSERT_ID(subject_id);
SET @s := LAST_INSERT_ID();

INSERT IGNORE INTO `subject_name`
(`is_primary`,`priority`,`created_at`,`subject_id`,`updated_at`,`locale_tag`,`name`,`name_type`)
VALUES (b'1',1,@now6,@s,NULL,'ko-KR','스트레이 키즈','OFFICIAL'),
       (b'0',2,@now6,@s,NULL,'en-US','Stray Kids','OFFICIAL');

INSERT IGNORE INTO `subject_category_map` (`category_id`,`subject_id`)
VALUES (@c_genre_kpop,@s),(@c_type_group,@s),(@c_gender_male,@s);

-- NCT
INSERT INTO `subject`
(`country_code`,`debut_date`,`created_at`,`domain_id`,`primary_category_id`,`native_locale`,`slug`,`img_url`)
VALUES ('KR','2016-04-09',@now6,@dom_music,@c_genre_kpop,'ko-KR','nct',NULL)
ON DUPLICATE KEY UPDATE subject_id = LAST_INSERT_ID(subject_id);
SET @s := LAST_INSERT_ID();

INSERT IGNORE INTO `subject_name`
(`is_primary`,`priority`,`created_at`,`subject_id`,`updated_at`,`locale_tag`,`name`,`name_type`)
VALUES (b'1',1,@now6,@s,NULL,'ko-KR','엔시티','OFFICIAL'),
       (b'0',2,@now6,@s,NULL,'en-US','NCT','OFFICIAL');

INSERT IGNORE INTO `subject_category_map` (`category_id`,`subject_id`)
VALUES (@c_genre_kpop,@s),(@c_type_group,@s),(@c_gender_male,@s);

-- ENHYPEN
INSERT INTO `subject`
(`country_code`,`debut_date`,`created_at`,`domain_id`,`primary_category_id`,`native_locale`,`slug`,`img_url`)
VALUES ('KR','2020-11-30',@now6,@dom_music,@c_genre_kpop,'ko-KR','enhypen',NULL)
ON DUPLICATE KEY UPDATE subject_id = LAST_INSERT_ID(subject_id);
SET @s := LAST_INSERT_ID();

INSERT IGNORE INTO `subject_name`
(`is_primary`,`priority`,`created_at`,`subject_id`,`updated_at`,`locale_tag`,`name`,`name_type`)
VALUES (b'1',1,@now6,@s,NULL,'ko-KR','엔하이픈','OFFICIAL'),
       (b'0',2,@now6,@s,NULL,'en-US','ENHYPEN','OFFICIAL');

INSERT IGNORE INTO `subject_category_map` (`category_id`,`subject_id`)
VALUES (@c_genre_kpop,@s),(@c_type_group,@s),(@c_gender_male,@s);

-- TXT
INSERT INTO `subject`
(`country_code`,`debut_date`,`created_at`,`domain_id`,`primary_category_id`,`native_locale`,`slug`,`img_url`)
VALUES ('KR','2019-03-04',@now6,@dom_music,@c_genre_kpop,'ko-KR','txt',NULL)
ON DUPLICATE KEY UPDATE subject_id = LAST_INSERT_ID(subject_id);
SET @s := LAST_INSERT_ID();

INSERT IGNORE INTO `subject_name`
(`is_primary`,`priority`,`created_at`,`subject_id`,`updated_at`,`locale_tag`,`name`,`name_type`)
VALUES (b'1',1,@now6,@s,NULL,'ko-KR','투모로우바이투게더','OFFICIAL'),
       (b'0',2,@now6,@s,NULL,'en-US','TXT','OFFICIAL');

INSERT IGNORE INTO `subject_category_map` (`category_id`,`subject_id`)
VALUES (@c_genre_kpop,@s),(@c_type_group,@s),(@c_gender_male,@s);

-- ATEEZ
INSERT INTO `subject`
(`country_code`,`debut_date`,`created_at`,`domain_id`,`primary_category_id`,`native_locale`,`slug`,`img_url`)
VALUES ('KR','2018-10-24',@now6,@dom_music,@c_genre_kpop,'ko-KR','ateez',NULL)
ON DUPLICATE KEY UPDATE subject_id = LAST_INSERT_ID(subject_id);
SET @s := LAST_INSERT_ID();

INSERT IGNORE INTO `subject_name`
(`is_primary`,`priority`,`created_at`,`subject_id`,`updated_at`,`locale_tag`,`name`,`name_type`)
VALUES (b'1',1,@now6,@s,NULL,'ko-KR','에이티즈','OFFICIAL'),
       (b'0',2,@now6,@s,NULL,'en-US','ATEEZ','OFFICIAL');

INSERT IGNORE INTO `subject_category_map` (`category_id`,`subject_id`)
VALUES (@c_genre_kpop,@s),(@c_type_group,@s),(@c_gender_male,@s);

-- IU (SOLO)
INSERT INTO `subject`
(`country_code`,`debut_date`,`created_at`,`domain_id`,`primary_category_id`,`native_locale`,`slug`,`img_url`)
VALUES ('KR','2008-09-18',@now6,@dom_music,@c_genre_kpop,'ko-KR','iu',NULL)
ON DUPLICATE KEY UPDATE subject_id = LAST_INSERT_ID(subject_id);
SET @s := LAST_INSERT_ID();

INSERT IGNORE INTO `subject_name`
(`is_primary`,`priority`,`created_at`,`subject_id`,`updated_at`,`locale_tag`,`name`,`name_type`)
VALUES (b'1',1,@now6,@s,NULL,'ko-KR','아이유','OFFICIAL'),
       (b'0',2,@now6,@s,NULL,'en-US','IU','OFFICIAL');

INSERT IGNORE INTO `subject_category_map` (`category_id`,`subject_id`)
VALUES (@c_genre_kpop,@s),(@c_type_solo,@s),(@c_gender_female,@s);

-- JEON SOMI (SOLO)
INSERT INTO `subject`
(`country_code`,`debut_date`,`created_at`,`domain_id`,`primary_category_id`,`native_locale`,`slug`,`img_url`)
VALUES ('KR','2016-05-04',@now6,@dom_music,@c_genre_kpop,'ko-KR','jeon-somi',NULL)
ON DUPLICATE KEY UPDATE subject_id = LAST_INSERT_ID(subject_id);
SET @s := LAST_INSERT_ID();

INSERT IGNORE INTO `subject_name`
(`is_primary`,`priority`,`created_at`,`subject_id`,`updated_at`,`locale_tag`,`name`,`name_type`)
VALUES (b'1',1,@now6,@s,NULL,'ko-KR','전소미','OFFICIAL'),
       (b'0',2,@now6,@s,NULL,'en-US','JEON SOMI','OFFICIAL');

INSERT IGNORE INTO `subject_category_map` (`category_id`,`subject_id`)
VALUES (@c_genre_kpop,@s),(@c_type_solo,@s),(@c_gender_female,@s);

-- YENA (SOLO)
INSERT INTO `subject`
(`country_code`,`debut_date`,`created_at`,`domain_id`,`primary_category_id`,`native_locale`,`slug`,`img_url`)
VALUES ('KR','2018-10-29',@now6,@dom_music,@c_genre_kpop,'ko-KR','yena',NULL)
ON DUPLICATE KEY UPDATE subject_id = LAST_INSERT_ID(subject_id);
SET @s := LAST_INSERT_ID();

INSERT IGNORE INTO `subject_name`
(`is_primary`,`priority`,`created_at`,`subject_id`,`updated_at`,`locale_tag`,`name`,`name_type`)
VALUES (b'1',1,@now6,@s,NULL,'ko-KR','최예나','OFFICIAL'),
       (b'0',2,@now6,@s,NULL,'en-US','YENA','OFFICIAL');

INSERT IGNORE INTO `subject_category_map` (`category_id`,`subject_id`)
VALUES (@c_genre_kpop,@s),(@c_type_solo,@s),(@c_gender_female,@s);

-- G-DRAGON (SOLO, debut NULL)
INSERT INTO `subject`
(`country_code`,`debut_date`,`created_at`,`domain_id`,`primary_category_id`,`native_locale`,`slug`,`img_url`)
VALUES ('KR',NULL,@now6,@dom_music,@c_genre_kpop,'ko-KR','g-dragon',NULL)
ON DUPLICATE KEY UPDATE subject_id = LAST_INSERT_ID(subject_id);
SET @s := LAST_INSERT_ID();

INSERT IGNORE INTO `subject_name`
(`is_primary`,`priority`,`created_at`,`subject_id`,`updated_at`,`locale_tag`,`name`,`name_type`)
VALUES (b'1',1,@now6,@s,NULL,'ko-KR','지드래곤','OFFICIAL'),
       (b'0',2,@now6,@s,NULL,'en-US','G-DRAGON','OFFICIAL');

INSERT IGNORE INTO `subject_category_map` (`category_id`,`subject_id`)
VALUES (@c_genre_kpop,@s),(@c_type_solo,@s),(@c_gender_male,@s);

-- Roy Kim (SOLO)
INSERT INTO `subject`
(`country_code`,`debut_date`,`created_at`,`domain_id`,`primary_category_id`,`native_locale`,`slug`,`img_url`)
VALUES ('KR','2013-04-22',@now6,@dom_music,@c_genre_kpop,'ko-KR','roy-kim',NULL)
ON DUPLICATE KEY UPDATE subject_id = LAST_INSERT_ID(subject_id);
SET @s := LAST_INSERT_ID();

INSERT IGNORE INTO `subject_name`
(`is_primary`,`priority`,`created_at`,`subject_id`,`updated_at`,`locale_tag`,`name`,`name_type`)
VALUES (b'1',1,@now6,@s,NULL,'ko-KR','로이킴','OFFICIAL'),
       (b'0',2,@now6,@s,NULL,'en-US','Roy Kim','OFFICIAL');

INSERT IGNORE INTO `subject_category_map` (`category_id`,`subject_id`)
VALUES (@c_genre_kpop,@s),(@c_type_solo,@s),(@c_gender_male,@s);

-- 10CM (INDIE, SOLO)
INSERT INTO `subject`
(`country_code`,`debut_date`,`created_at`,`domain_id`,`primary_category_id`,`native_locale`,`slug`,`img_url`)
VALUES ('KR','2019-08-23',@now6,@dom_music,@c_genre_indie,'ko-KR','10cm',NULL)
ON DUPLICATE KEY UPDATE subject_id = LAST_INSERT_ID(subject_id);
SET @s := LAST_INSERT_ID();

INSERT IGNORE INTO `subject_name`
(`is_primary`,`priority`,`created_at`,`subject_id`,`updated_at`,`locale_tag`,`name`,`name_type`)
VALUES (b'1',1,@now6,@s,NULL,'ko-KR','십센치','OFFICIAL'),
       (b'0',2,@now6,@s,NULL,'en-US','10CM','OFFICIAL');

INSERT IGNORE INTO `subject_category_map` (`category_id`,`subject_id`)
VALUES (@c_genre_indie,@s),(@c_type_solo,@s),(@c_gender_male,@s);

-- wave to earth (INDIE, GROUP)
INSERT INTO `subject`
(`country_code`,`debut_date`,`created_at`,`domain_id`,`primary_category_id`,`native_locale`,`slug`,`img_url`)
VALUES ('KR','2019-08-23',@now6,@dom_music,@c_genre_indie,'ko-KR','wave-to-earth',NULL)
ON DUPLICATE KEY UPDATE subject_id = LAST_INSERT_ID(subject_id);
SET @s := LAST_INSERT_ID();

INSERT IGNORE INTO `subject_name`
(`is_primary`,`priority`,`created_at`,`subject_id`,`updated_at`,`locale_tag`,`name`,`name_type`)
VALUES (b'0',2,@now6,@s,NULL,'en-US','wave to earth','OFFICIAL'),
       (b'1',1,@now6,@s,NULL,'ko-KR','웨이브 투 어스','TRANSLATED');

INSERT IGNORE INTO `subject_category_map` (`category_id`,`subject_id`)
VALUES (@c_genre_indie,@s),(@c_type_group,@s),(@c_gender_male,@s);

-- The Black Skirts (INDIE, SOLO)
INSERT INTO `subject`
(`country_code`,`debut_date`,`created_at`,`domain_id`,`primary_category_id`,`native_locale`,`slug`,`img_url`)
VALUES ('KR','2008-11-13',@now6,@dom_music,@c_genre_indie,'ko-KR','the-black-skirts',NULL)
ON DUPLICATE KEY UPDATE subject_id = LAST_INSERT_ID(subject_id);
SET @s := LAST_INSERT_ID();

INSERT IGNORE INTO `subject_name`
(`is_primary`,`priority`,`created_at`,`subject_id`,`updated_at`,`locale_tag`,`name`,`name_type`)
VALUES (b'0',2,@now6,@s,NULL,'en-US','The Black Skirts','OFFICIAL'),
       (b'1',1,@now6,@s,NULL,'ko-KR','검정치마','OFFICIAL');

INSERT IGNORE INTO `subject_category_map` (`category_id`,`subject_id`)
VALUES (@c_genre_indie,@s),(@c_type_solo,@s),(@c_gender_male,@s);

-- Metallica (ROCK, GROUP)
INSERT INTO `subject`
(`country_code`,`debut_date`,`created_at`,`domain_id`,`primary_category_id`,`native_locale`,`slug`,`img_url`)
VALUES ('US','1981-10-28',@now6,@dom_music,@c_genre_rock,'en-US','metallica',NULL)
ON DUPLICATE KEY UPDATE subject_id = LAST_INSERT_ID(subject_id);
SET @s := LAST_INSERT_ID();

INSERT IGNORE INTO `subject_name`
(`is_primary`,`priority`,`created_at`,`subject_id`,`updated_at`,`locale_tag`,`name`,`name_type`)
VALUES (b'1',1,@now6,@s,NULL,'en-US','Metallica','OFFICIAL'),
       (b'0',2,@now6,@s,NULL,'ko-KR','메탈리카','TRANSLATED');

INSERT IGNORE INTO `subject_category_map` (`category_id`,`subject_id`)
VALUES (@c_genre_rock,@s),(@c_type_group,@s),(@c_gender_male,@s);

-- The Beatles (ROCK, GROUP)
INSERT INTO `subject`
(`country_code`,`debut_date`,`created_at`,`domain_id`,`primary_category_id`,`native_locale`,`slug`,`img_url`)
VALUES ('GB','1962-10-05',@now6,@dom_music,@c_genre_rock,'en-GB','the-beatles',NULL)
ON DUPLICATE KEY UPDATE subject_id = LAST_INSERT_ID(subject_id);
SET @s := LAST_INSERT_ID();

INSERT IGNORE INTO `subject_name`
(`is_primary`,`priority`,`created_at`,`subject_id`,`updated_at`,`locale_tag`,`name`,`name_type`)
VALUES (b'1',1,@now6,@s,NULL,'en-GB','The Beatles','OFFICIAL'),
       (b'0',2,@now6,@s,NULL,'ko-KR','비틀즈','TRANSLATED');

INSERT IGNORE INTO `subject_category_map` (`category_id`,`subject_id`)
VALUES (@c_genre_rock,@s),(@c_type_group,@s),(@c_gender_male,@s);

-- Oasis (ROCK, GROUP)
INSERT INTO `subject`
(`country_code`,`debut_date`,`created_at`,`domain_id`,`primary_category_id`,`native_locale`,`slug`,`img_url`)
VALUES ('GB','1994-04-11',@now6,@dom_music,@c_genre_rock,'en-GB','oasis',NULL)
ON DUPLICATE KEY UPDATE subject_id = LAST_INSERT_ID(subject_id);
SET @s := LAST_INSERT_ID();

INSERT IGNORE INTO `subject_name`
(`is_primary`,`priority`,`created_at`,`subject_id`,`updated_at`,`locale_tag`,`name`,`name_type`)
VALUES (b'1',1,@now6,@s,NULL,'en-GB','Oasis','OFFICIAL'),
       (b'0',2,@now6,@s,NULL,'ko-KR','오아시스','TRANSLATED');

INSERT IGNORE INTO `subject_category_map` (`category_id`,`subject_id`)
VALUES (@c_genre_rock,@s),(@c_type_group,@s),(@c_gender_male,@s);

-- AC/DC (ROCK, GROUP)
INSERT INTO `subject`
(`country_code`,`debut_date`,`created_at`,`domain_id`,`primary_category_id`,`native_locale`,`slug`,`img_url`)
VALUES ('AU','1975-02-17',@now6,@dom_music,@c_genre_rock,'en-AU','ac-dc',NULL)
ON DUPLICATE KEY UPDATE subject_id = LAST_INSERT_ID(subject_id);
SET @s := LAST_INSERT_ID();

INSERT IGNORE INTO `subject_name`
(`is_primary`,`priority`,`created_at`,`subject_id`,`updated_at`,`locale_tag`,`name`,`name_type`)
VALUES (b'1',1,@now6,@s,NULL,'en-AU','AC/DC','OFFICIAL'),
       (b'0',2,@now6,@s,NULL,'ko-KR','에이시디시','TRANSLATED');

INSERT IGNORE INTO `subject_category_map` (`category_id`,`subject_id`)
VALUES (@c_genre_rock,@s),(@c_type_group,@s),(@c_gender_male,@s);

-- Charli XCX (POP, SOLO)
INSERT INTO `subject`
(`country_code`,`debut_date`,`created_at`,`domain_id`,`primary_category_id`,`native_locale`,`slug`,`img_url`)
VALUES ('GB','2008-08-18',@now6,@dom_music,@c_genre_pop,'en-GB','charli-xcx',NULL)
ON DUPLICATE KEY UPDATE subject_id = LAST_INSERT_ID(subject_id);
SET @s := LAST_INSERT_ID();

INSERT IGNORE INTO `subject_name`
(`is_primary`,`priority`,`created_at`,`subject_id`,`updated_at`,`locale_tag`,`name`,`name_type`)
VALUES (b'1',1,@now6,@s,NULL,'en-GB','Charli XCX','OFFICIAL'),
       (b'0',2,@now6,@s,NULL,'ko-KR','찰리 xcx','TRANSLATED');

INSERT IGNORE INTO `subject_category_map` (`category_id`,`subject_id`)
VALUES (@c_genre_pop,@s),(@c_type_solo,@s),(@c_gender_female,@s);

-- Aimyon (JPOP, SOLO)
INSERT INTO `subject`
(`country_code`,`debut_date`,`created_at`,`domain_id`,`primary_category_id`,`native_locale`,`slug`,`img_url`)
VALUES ('JP','2015-03-04',@now6,@dom_music,@c_genre_jpop,'ja-JP','aimyon',NULL)
ON DUPLICATE KEY UPDATE subject_id = LAST_INSERT_ID(subject_id);
SET @s := LAST_INSERT_ID();

INSERT IGNORE INTO `subject_name`
(`is_primary`,`priority`,`created_at`,`subject_id`,`updated_at`,`locale_tag`,`name`,`name_type`)
VALUES (b'1',1,@now6,@s,NULL,'ja-JP','あいみょん','OFFICIAL'),
       (b'0',2,@now6,@s,NULL,'ko-KR','아이묭','TRANSLATED'),
       (b'0',3,@now6,@s,NULL,'en-US','Aimyon','ROMANIZED');

INSERT IGNORE INTO `subject_category_map` (`category_id`,`subject_id`)
VALUES (@c_genre_jpop,@s),(@c_type_solo,@s),(@c_gender_female,@s);

-- Official HIGE DANdism (JPOP, GROUP)
INSERT INTO `subject`
(`country_code`,`debut_date`,`created_at`,`domain_id`,`primary_category_id`,`native_locale`,`slug`,`img_url`)
VALUES ('JP','2012-06-07',@now6,@dom_music,@c_genre_jpop,'ja-JP','official-hige-dandism',NULL)
ON DUPLICATE KEY UPDATE subject_id = LAST_INSERT_ID(subject_id);
SET @s := LAST_INSERT_ID();

INSERT IGNORE INTO `subject_name`
(`is_primary`,`priority`,`created_at`,`subject_id`,`updated_at`,`locale_tag`,`name`,`name_type`)
VALUES (b'1',1,@now6,@s,NULL,'ja-JP','Official髭男dism','OFFICIAL'),
       (b'0',2,@now6,@s,NULL,'ko-KR','오피셜히게단디즘','TRANSLATED'),
       (b'0',3,@now6,@s,NULL,'en-US','Official HIGE DANdism','ROMANIZED');

INSERT IGNORE INTO `subject_category_map` (`category_id`,`subject_id`)
VALUES (@c_genre_jpop,@s),(@c_type_group,@s),(@c_gender_male,@s);

-- 6) SUBJECTS (SPORTS) --------------------------------------------------------
-- 손흥민
INSERT INTO `subject`
(`country_code`,`debut_date`,`created_at`,`domain_id`,`primary_category_id`,`native_locale`,`slug`,`img_url`)
VALUES ('KR',NULL,@now6,@dom_sports,@c_sp_football,'ko-KR','son-heung-min',NULL)
ON DUPLICATE KEY UPDATE subject_id = LAST_INSERT_ID(subject_id);
SET @s := LAST_INSERT_ID();

INSERT IGNORE INTO `subject_name`
(`is_primary`,`priority`,`created_at`,`subject_id`,`updated_at`,`locale_tag`,`name`,`name_type`)
VALUES (b'1',1,@now6,@s,NULL,'ko-KR','손흥민','OFFICIAL'),
       (b'0',2,@now6,@s,NULL,'en-GB','Son Heung-min','ROMANIZED');

INSERT IGNORE INTO `subject_category_map` (`category_id`,`subject_id`)
VALUES (@c_sp_football,@s),(@c_role_player,@s),(@c_lg_epl,@s),(@c_sp_gender_male,@s);

-- 토트넘 홋스퍼 FC
INSERT INTO `subject`
(`country_code`,`debut_date`,`created_at`,`domain_id`,`primary_category_id`,`native_locale`,`slug`,`img_url`)
VALUES ('GB','1882-09-05',@now6,@dom_sports,@c_sp_football,'en-GB','tottenham-hotspur',NULL)
ON DUPLICATE KEY UPDATE subject_id = LAST_INSERT_ID(subject_id);
SET @s := LAST_INSERT_ID();

INSERT IGNORE INTO `subject_name`
(`is_primary`,`priority`,`created_at`,`subject_id`,`updated_at`,`locale_tag`,`name`,`name_type`)
VALUES (b'1',1,@now6,@s,NULL,'en-GB','Tottenham Hotspur','OFFICIAL'),
       (b'0',2,@now6,@s,NULL,'ko-KR','토트넘 홋스퍼','TRANSLATED');

INSERT IGNORE INTO `subject_category_map` (`category_id`,`subject_id`)
VALUES (@c_sp_football,@s),(@c_role_team,@s),(@c_lg_epl,@s),(@c_sp_gender_male,@s);

-- 오타니 쇼헤이
INSERT INTO `subject`
(`country_code`,`debut_date`,`created_at`,`domain_id`,`primary_category_id`,`native_locale`,`slug`,`img_url`)
VALUES ('JP',NULL,@now6,@dom_sports,@c_sp_baseball,'ja-JP','shohei-ohtani',NULL)
ON DUPLICATE KEY UPDATE subject_id = LAST_INSERT_ID(subject_id);
SET @s := LAST_INSERT_ID();

INSERT IGNORE INTO `subject_name`
(`is_primary`,`priority`,`created_at`,`subject_id`,`updated_at`,`locale_tag`,`name`,`name_type`)
VALUES (b'1',1,@now6,@s,NULL,'ja-JP','大谷翔平','OFFICIAL'),
       (b'0',2,@now6,@s,NULL,'en-US','Shohei Ohtani','ROMANIZED'),
       (b'0',3,@now6,@s,NULL,'ko-KR','오타니 쇼헤이','TRANSLATED');

INSERT IGNORE INTO `subject_category_map` (`category_id`,`subject_id`)
VALUES (@c_sp_baseball,@s),(@c_role_player,@s),(@c_lg_mlb,@s),(@c_sp_gender_male,@s);

-- LA 다저스
INSERT INTO `subject`
(`country_code`,`debut_date`,`created_at`,`domain_id`,`primary_category_id`,`native_locale`,`slug`,`img_url`)
VALUES ('US',NULL,@now6,@dom_sports,@c_sp_baseball,'en-US','los-angeles-dodgers',NULL)
ON DUPLICATE KEY UPDATE subject_id = LAST_INSERT_ID(subject_id);
SET @s := LAST_INSERT_ID();

INSERT IGNORE INTO `subject_name`
(`is_primary`,`priority`,`created_at`,`subject_id`,`updated_at`,`locale_tag`,`name`,`name_type`)
VALUES (b'1',1,@now6,@s,NULL,'en-US','Los Angeles Dodgers','OFFICIAL'),
       (b'0',2,@now6,@s,NULL,'ko-KR','로스앤젤레스 다저스','TRANSLATED');

INSERT IGNORE INTO `subject_category_map` (`category_id`,`subject_id`)
VALUES (@c_sp_baseball,@s),(@c_role_team,@s),(@c_lg_mlb,@s),(@c_sp_gender_male,@s);

-- 맥스 페르스타펜
INSERT INTO `subject`
(`country_code`,`debut_date`,`created_at`,`domain_id`,`primary_category_id`,`native_locale`,`slug`,`img_url`)
VALUES ('NL','2015-03-15',@now6,@dom_sports,@c_sp_f1,'en-NL','max-verstappen',NULL)
ON DUPLICATE KEY UPDATE subject_id = LAST_INSERT_ID(subject_id);
SET @s := LAST_INSERT_ID();

INSERT IGNORE INTO `subject_name`
(`is_primary`,`priority`,`created_at`,`subject_id`,`updated_at`,`locale_tag`,`name`,`name_type`)
VALUES (b'1',1,@now6,@s,NULL,'en-NL','Max Verstappen','OFFICIAL'),
       (b'0',2,@now6,@s,NULL,'ko-KR','맥스 페르스타펜','TRANSLATED');

INSERT IGNORE INTO `subject_category_map` (`category_id`,`subject_id`)
VALUES (@c_sp_f1,@s),(@c_role_driver,@s),(@c_lg_f1,@s),(@c_sp_gender_male,@s);

-- 레드불 레이싱 (컨스트럭터)
INSERT INTO `subject`
(`country_code`,`debut_date`,`created_at`,`domain_id`,`primary_category_id`,`native_locale`,`slug`,`img_url`)
VALUES ('AT','2005-11-15',@now6,@dom_sports,@c_sp_f1,'en-GB','red-bull-racing',NULL)
ON DUPLICATE KEY UPDATE subject_id = LAST_INSERT_ID(subject_id);
SET @s := LAST_INSERT_ID();

INSERT IGNORE INTO `subject_name`
(`is_primary`,`priority`,`created_at`,`subject_id`,`updated_at`,`locale_tag`,`name`,`name_type`)
VALUES (b'1',1,@now6,@s,NULL,'en-GB','Red Bull Racing','OFFICIAL'),
       (b'0',2,@now6,@s,NULL,'ko-KR','레드불 레이싱','TRANSLATED');

INSERT IGNORE INTO `subject_category_map` (`category_id`,`subject_id`)
VALUES (@c_sp_f1,@s),(@c_role_constructor,@s),(@c_lg_f1,@s);

-- 7) SUBJECTS (CREATOR) -------------------------------------------------------
-- 침착맨
INSERT INTO `subject`
(`country_code`,`debut_date`,`created_at`,`domain_id`,`primary_category_id`,`native_locale`,`slug`,`img_url`)
VALUES ('KR',NULL,@now6,@dom_creator,@c_cr_yt,'ko-KR','calm-down-man',NULL)
ON DUPLICATE KEY UPDATE subject_id = LAST_INSERT_ID(subject_id);
SET @s := LAST_INSERT_ID();

INSERT IGNORE INTO `subject_name`
(`is_primary`,`priority`,`created_at`,`subject_id`,`updated_at`,`locale_tag`,`name`,`name_type`)
VALUES (b'1',1,@now6,@s,NULL,'ko-KR','침착맨','OFFICIAL'),
       (b'0',2,@now6,@s,NULL,'en-US','Calm Down Man','TRANSLATED');

INSERT IGNORE INTO `subject_category_map` (`category_id`,`subject_id`)
VALUES (@c_cr_yt,@s),(@c_cr_talk,@s),(@c_cr_individual,@s);

-- 곽튜브
INSERT INTO `subject`
(`country_code`,`debut_date`,`created_at`,`domain_id`,`primary_category_id`,`native_locale`,`slug`,`img_url`)
VALUES ('KR',NULL,@now6,@dom_creator,@c_cr_yt,'ko-KR','kwaktube',NULL)
ON DUPLICATE KEY UPDATE subject_id = LAST_INSERT_ID(subject_id);
SET @s := LAST_INSERT_ID();

INSERT IGNORE INTO `subject_name`
(`is_primary`,`priority`,`created_at`,`subject_id`,`updated_at`,`locale_tag`,`name`,`name_type`)
VALUES (b'1',1,@now6,@s,NULL,'ko-KR','곽튜브','OFFICIAL'),
       (b'0',2,@now6,@s,NULL,'en-US','KwakTube','ROMANIZED');

INSERT IGNORE INTO `subject_category_map` (`category_id`,`subject_id`)
VALUES (@c_cr_yt,@s),(@c_cr_travel,@s),(@c_cr_individual,@s);

-- 쯔양
INSERT INTO `subject`
(`country_code`,`debut_date`,`created_at`,`domain_id`,`primary_category_id`,`native_locale`,`slug`,`img_url`)
VALUES ('KR',NULL,@now6,@dom_creator,@c_cr_yt,'ko-KR','tzuyang',NULL)
ON DUPLICATE KEY UPDATE subject_id = LAST_INSERT_ID(subject_id);
SET @s := LAST_INSERT_ID();

INSERT IGNORE INTO `subject_name`
(`is_primary`,`priority`,`created_at`,`subject_id`,`updated_at`,`locale_tag`,`name`,`name_type`)
VALUES (b'1',1,@now6,@s,NULL,'ko-KR','쯔양','OFFICIAL'),
       (b'0',2,@now6,@s,NULL,'en-US','Tzuyang','ROMANIZED');

INSERT IGNORE INTO `subject_category_map` (`category_id`,`subject_id`)
VALUES (@c_cr_yt,@s),(@c_cr_food,@s),(@c_cr_individual,@s);

-- 마무리 ----------------------------------------------------------------------
SET FOREIGN_KEY_CHECKS = @OLD_FK;
-- ===== END =====
