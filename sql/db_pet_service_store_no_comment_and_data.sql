/*
 Navicat Premium Data Transfer

 Source Server         : MySQL
 Source Server Type    : MySQL
 Source Server Version : 80026 (8.0.26)
 Source Host           : localhost:3306
 Source Schema         : db_pet_service_store

 Target Server Type    : MySQL
 Target Server Version : 80026 (8.0.26)
 File Encoding         : 65001

 Date: 25/02/2026 18:06:06
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for tb_address
-- ----------------------------
DROP TABLE IF EXISTS `tb_address`;
CREATE TABLE `tb_address`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `address_text` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `latitude` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `longitude` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `delete_flag` tinyint NULL DEFAULT 0,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 65 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '地址表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for tb_cart
-- ----------------------------
DROP TABLE IF EXISTS `tb_cart`;
CREATE TABLE `tb_cart`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `product_id` bigint NOT NULL,
  `total_count` bigint NOT NULL,
  `total_price` decimal(10, 2) NOT NULL,
  `delete_flag` tinyint NULL DEFAULT 0,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_product_id`(`product_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 16 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '购物车表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for tb_column_info
-- ----------------------------
DROP TABLE IF EXISTS `tb_column_info`;
CREATE TABLE `tb_column_info`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(25) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `delete_flag` tinyint NULL DEFAULT 0,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '栏目表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for tb_comment_media
-- ----------------------------
DROP TABLE IF EXISTS `tb_comment_media`;
CREATE TABLE `tb_comment_media`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `comment_id` bigint NOT NULL,
  `media_url` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `media_type` tinyint NOT NULL,
  `delete_flag` tinyint NULL DEFAULT 0,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_comment_id`(`comment_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 16 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '评论媒体表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for tb_comment_super
-- ----------------------------
DROP TABLE IF EXISTS `tb_comment_super`;
CREATE TABLE `tb_comment_super`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `object_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `parent_id` bigint NULL DEFAULT NULL,
  `type` tinyint NOT NULL,
  `content` varchar(2048) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `status` tinyint NULL DEFAULT 1,
  `delete_flag` tinyint NULL DEFAULT 0,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_object_id`(`object_id` ASC) USING BTREE,
  INDEX `idx_parent_id`(`parent_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 97 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '评论总表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for tb_message
-- ----------------------------
DROP TABLE IF EXISTS `tb_message`;
CREATE TABLE `tb_message`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `receive_user_id` bigint NOT NULL,
  `send_user_id` bigint NOT NULL,
  `type` tinyint NULL DEFAULT NULL,
  `message` varchar(2048) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `status` tinyint NULL DEFAULT NULL,
  `delete_flag` tinyint NULL DEFAULT 0,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_receive_user_id`(`receive_user_id` ASC) USING BTREE,
  INDEX `idx_send_user_id`(`send_user_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 92 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '消息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for tb_message_latest
-- ----------------------------
DROP TABLE IF EXISTS `tb_message_latest`;
CREATE TABLE `tb_message_latest`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `send_user_id` bigint NOT NULL,
  `receive_user_id` bigint NULL DEFAULT NULL,
  `message_id` bigint NOT NULL,
  `delete_flag` tinyint NULL DEFAULT 0,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`send_user_id` ASC) USING BTREE,
  INDEX `idx_message_id`(`message_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '最近消息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for tb_message_media
-- ----------------------------
DROP TABLE IF EXISTS `tb_message_media`;
CREATE TABLE `tb_message_media`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `message_id` bigint NOT NULL,
  `media_url` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `media_type` tinyint NOT NULL,
  `delete_flag` tinyint NULL DEFAULT 0,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '消息媒体表' ROW_FORMAT = Dynamic;


-- ----------------------------
-- Table structure for tb_notify
-- ----------------------------
DROP TABLE IF EXISTS `tb_notify`;
CREATE TABLE `tb_notify`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `receive_user_id` bigint NOT NULL,
  `title` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `content` varchar(2048) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `type` tinyint NOT NULL,
  `status` tinyint NULL DEFAULT 0,
  `delete_flag` tinyint NULL DEFAULT 0,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_receive_user_id`(`receive_user_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_delete_flag`(`delete_flag` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 39 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '通知表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for tb_order
-- ----------------------------
DROP TABLE IF EXISTS `tb_order`;
CREATE TABLE `tb_order`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `product_id` bigint NOT NULL,
  `phone` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `receipt_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `receipt_id` bigint NOT NULL,
  `total_count` bigint NOT NULL,
  `total_price` decimal(10, 2) NOT NULL,
  `status` tinyint NULL DEFAULT 1,
  `refund_flag` tinyint NULL DEFAULT 0,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_product_id`(`product_id` ASC) USING BTREE,
  INDEX `idx_receipt_id`(`receipt_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 64 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '订单表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for tb_order_logistics
-- ----------------------------
DROP TABLE IF EXISTS `tb_order_logistics`;
CREATE TABLE `tb_order_logistics`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_id` bigint NOT NULL,
  `transport_type` tinyint NOT NULL,
  `origin_lat` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `origin_lng` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `dest_lat` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `dest_lng` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `curr_lat` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `curr_lng` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `delete_flag` tinyint NOT NULL DEFAULT 0,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 16 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '订单物流表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for tb_pet_sub
-- ----------------------------
DROP TABLE IF EXISTS `tb_pet_sub`;
CREATE TABLE `tb_pet_sub`  (
  `sub_id` bigint NOT NULL AUTO_INCREMENT,
  `product_id` bigint NOT NULL,
  `health_status` tinyint NULL DEFAULT 1,
  `train_note` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `raise_note` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `vaccine_flag` tinyint NULL DEFAULT 1,
  `sub_create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `sub_update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`sub_id`) USING BTREE,
  UNIQUE INDEX `product_id`(`product_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 32 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '宠物子表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for tb_pet_supply_sub
-- ----------------------------
DROP TABLE IF EXISTS `tb_pet_supply_sub`;
CREATE TABLE `tb_pet_supply_sub`  (
  `sub_id` bigint NOT NULL AUTO_INCREMENT,
  `product_id` bigint NOT NULL,
  `brand` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `fit_age` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `fit_variety` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `manufacture_date` datetime NOT NULL,
  `guarantee_date` datetime NOT NULL,
  `company` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `sub_create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `sub_update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`sub_id`) USING BTREE,
  UNIQUE INDEX `product_id`(`product_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 23 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '宠物用品子表' ROW_FORMAT = Dynamic;


-- ----------------------------
-- Table structure for tb_post
-- ----------------------------
DROP TABLE IF EXISTS `tb_post`;
CREATE TABLE `tb_post`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `column_id` bigint NOT NULL,
  `title` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `content` varchar(2560) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `status` tinyint NULL DEFAULT 1,
  `like_count` bigint NULL DEFAULT 0,
  `reject_count` bigint NULL DEFAULT 0,
  `delete_flag` tinyint NULL DEFAULT 0,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_column_id`(`column_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 50 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '帖子表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for tb_post_dislike
-- ----------------------------
DROP TABLE IF EXISTS `tb_post_dislike`;
CREATE TABLE `tb_post_dislike`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `post_id` bigint NOT NULL,
  `cancel_flag` tinyint NOT NULL DEFAULT 0,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '帖子点踩表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for tb_post_favorite
-- ----------------------------
DROP TABLE IF EXISTS `tb_post_favorite`;
CREATE TABLE `tb_post_favorite`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `post_id` bigint NOT NULL,
  `cancel_flag` tinyint NULL DEFAULT 0,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_post_id`(`post_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '帖子收藏表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for tb_post_like
-- ----------------------------
DROP TABLE IF EXISTS `tb_post_like`;
CREATE TABLE `tb_post_like`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `post_id` bigint NOT NULL,
  `cancel_flag` tinyint NOT NULL DEFAULT 0,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 20 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '帖子点赞表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for tb_post_media
-- ----------------------------
DROP TABLE IF EXISTS `tb_post_media`;
CREATE TABLE `tb_post_media`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `post_id` bigint NOT NULL,
  `media_url` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `media_type` tinyint NOT NULL,
  `delete_flag` tinyint NULL DEFAULT 0,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_post_id`(`post_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 49 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '帖子媒体表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for tb_product_category_sub
-- ----------------------------
DROP TABLE IF EXISTS `tb_product_category_sub`;
CREATE TABLE `tb_product_category_sub`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `main_category_id` bigint NOT NULL,
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `delete_flag` tinyint NOT NULL DEFAULT 0,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 111 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '商品二级分类表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for tb_product_category_super
-- ----------------------------
DROP TABLE IF EXISTS `tb_product_category_super`;
CREATE TABLE `tb_product_category_super`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `type` tinyint NOT NULL,
  `delete_flag` tinyint NULL DEFAULT 0,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_type`(`type` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 41 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '商品分类表' ROW_FORMAT = Dynamic;


-- ----------------------------
-- Table structure for tb_product_comment_sub
-- ----------------------------
DROP TABLE IF EXISTS `tb_product_comment_sub`;
CREATE TABLE `tb_product_comment_sub`  (
  `sub_id` bigint NOT NULL AUTO_INCREMENT,
  `comment_id` bigint NOT NULL,
  `stars` bigint NULL DEFAULT NULL,
  `sub_create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `sub_update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`sub_id`) USING BTREE,
  INDEX `idx_comment_id`(`comment_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 41 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '商品评论子表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for tb_product_image
-- ----------------------------
DROP TABLE IF EXISTS `tb_product_image`;
CREATE TABLE `tb_product_image`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `product_id` bigint NOT NULL,
  `image_url` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `main_flag` tinyint NULL DEFAULT 0,
  `delete_flag` tinyint NULL DEFAULT 0,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_product_id`(`product_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 77 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '商品图片表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for tb_product_super
-- ----------------------------
DROP TABLE IF EXISTS `tb_product_super`;
CREATE TABLE `tb_product_super`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `identifier` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` varchar(2048) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `type` bigint NOT NULL,
  `ship_id` bigint NOT NULL,
  `main_category_id` bigint NOT NULL,
  `sub_category_id` bigint NOT NULL,
  `price` decimal(10, 2) NOT NULL,
  `stock` bigint NULL DEFAULT 1,
  `status` tinyint NULL DEFAULT 1,
  `delete_flag` tinyint NULL DEFAULT 0,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `identifier`(`identifier` ASC) USING BTREE,
  UNIQUE INDEX `index_identifier`(`identifier` ASC) USING BTREE,
  INDEX `idx_ship_id`(`ship_id` ASC) USING BTREE,
  INDEX `idx_category_id`(`main_category_id` ASC) USING BTREE,
  FULLTEXT INDEX `idx_product_super_name`(`name`) WITH PARSER `ngram`
) ENGINE = InnoDB AUTO_INCREMENT = 37 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '商品总表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for tb_refund_info
-- ----------------------------
DROP TABLE IF EXISTS `tb_refund_info`;
CREATE TABLE `tb_refund_info`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `message` varchar(2048) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `delete_flag` tinyint NULL DEFAULT 0,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 12 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '退款信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for tb_role
-- ----------------------------
DROP TABLE IF EXISTS `tb_role`;
CREATE TABLE `tb_role`  (
  `id` bigint NOT NULL,
  `name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE INDEX `id`(`id` ASC) USING BTREE,
  UNIQUE INDEX `name`(`name` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '角色表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for tb_sensitive_word
-- ----------------------------
DROP TABLE IF EXISTS `tb_sensitive_word`;
CREATE TABLE `tb_sensitive_word`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `word` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `category_id` bigint NOT NULL,
  `delete_flag` tinyint NULL DEFAULT 0,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE INDEX `id`(`id` ASC) USING BTREE,
  UNIQUE INDEX `word`(`word` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3362 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '敏感词表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for tb_sensitive_word_category
-- ----------------------------
DROP TABLE IF EXISTS `tb_sensitive_word_category`;
CREATE TABLE `tb_sensitive_word_category`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `delete_flag` tinyint NULL DEFAULT 0,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 13 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '敏感词分类表' ROW_FORMAT = Dynamic;


-- ----------------------------
-- Table structure for tb_topic
-- ----------------------------
DROP TABLE IF EXISTS `tb_topic`;
CREATE TABLE `tb_topic`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(25) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `delete_flag` tinyint NULL DEFAULT 0,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '话题表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for tb_topic_post
-- ----------------------------
DROP TABLE IF EXISTS `tb_topic_post`;
CREATE TABLE `tb_topic_post`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `post_id` bigint NOT NULL,
  `topic_id` bigint NOT NULL,
  `delete_flag` tinyint NULL DEFAULT 0,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_post_id`(`post_id` ASC) USING BTREE,
  INDEX `idx_topic_id`(`topic_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 49 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '话题帖子关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for tb_user
-- ----------------------------
DROP TABLE IF EXISTS `tb_user`;
CREATE TABLE `tb_user`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `email` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `password` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `gender` tinyint NULL DEFAULT 3,
  `real_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `id_card` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `phone` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `avatar_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/default_avatar.png',
  `role_id` bigint NULL DEFAULT 1,
  `receipt_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `receipt_id` bigint NULL DEFAULT NULL,
  `profile` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `status` tinyint NULL DEFAULT 0,
  `ban_flag` tinyint NULL DEFAULT 0,
  `delete_flag` tinyint NULL DEFAULT 0,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_role_id`(`role_id` ASC) USING BTREE,
  INDEX `idx_receipt_id`(`receipt_id` ASC) USING BTREE,
  INDEX `idx_username`(`username` ASC) USING BTREE,
  INDEX `idx_email`(`email` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 15 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户表' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
