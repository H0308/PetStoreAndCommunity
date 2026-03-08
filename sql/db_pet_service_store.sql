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

 Date: 25/02/2026 18:34:22
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for tb_address
-- ----------------------------
DROP TABLE IF EXISTS `tb_address`;
CREATE TABLE `tb_address`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '地址ID，主键自增',
  `address_text` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '地址名称，不为空且唯一',
  `latitude` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '纬度值，不为空。填写时先写入纬度',
  `longitude` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '经度值，不为空。填写时后写入经度',
  `delete_flag` tinyint NULL DEFAULT 0 COMMENT '删除标记，0-未删除，1-已删除，默认为0',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间，默认为插入时时间戳',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间，默认为插入时时间戳，插入时自动更新',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 65 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '地址表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_address
-- ----------------------------
INSERT INTO `tb_address` VALUES (1, '北京市海淀区', '39.96548984110075', '116.3054340544974', 0, '2025-12-08 18:20:25', '2025-12-08 18:20:25');
INSERT INTO `tb_address` VALUES (3, '湖北省武汉市湖北工业大学', '30.485183656608755', '114.31630816883417', 0, '2025-12-15 12:56:49', '2025-12-15 12:56:48');
INSERT INTO `tb_address` VALUES (4, '湖北省武汉市', '30.598466736400988', '114.31158155473232', 0, '2025-12-15 12:58:58', '2025-12-15 12:58:57');
INSERT INTO `tb_address` VALUES (5, '上海市奉贤区齐贤小学', '30.960127594633577', '121.51684504515943', 0, '2025-12-15 13:00:12', '2025-12-15 13:00:11');
INSERT INTO `tb_address` VALUES (6, '湖北省武汉市洪山区南李路28号 湖北工业大学', '30.48645994791441', '114.31530997175149', 0, '2025-12-15 13:42:01', '2025-12-15 13:42:01');
INSERT INTO `tb_address` VALUES (7, '上海市奉贤区文明街16号', '30.959582879905157', '121.51703306050267', 0, '2025-12-15 13:48:33', '2025-12-15 13:48:33');
INSERT INTO `tb_address` VALUES (8, '湖北省武汉市洪山区南李路28号 湖北工业大学', '30.48645994791441', '114.31530997175149', 0, '2025-12-15 13:51:17', '2025-12-15 13:51:16');
INSERT INTO `tb_address` VALUES (9, '湖北省武汉市洪山区南李路28号附1号 湖北工业大学-工程技术学院', '30.484014486112416', '114.31434231705613', 0, '2025-12-15 22:24:54', '2025-12-15 22:24:54');
INSERT INTO `tb_address` VALUES (10, '湖北省武汉市洪山区南李路28号 湖北工业大学', '30.48645994791441', '114.31530997175149', 0, '2025-12-17 20:53:53', '2025-12-17 20:53:53');
INSERT INTO `tb_address` VALUES (11, '北京市海淀区颐和园路5号 北京大学', '39.99700408060357', '116.32033999714666', 0, '2025-12-17 20:54:56', '2025-12-17 20:54:56');
INSERT INTO `tb_address` VALUES (12, '海淀区 海淀区', '39.96548984110075', '116.3054340544974', 0, '2025-12-23 11:45:33', '2025-12-23 11:45:33');
INSERT INTO `tb_address` VALUES (13, '湖北省黄冈市英山县汤河美食街', '30.749763507987038', '115.6763846618081', 0, '2025-12-23 11:49:27', '2025-12-23 11:49:26');
INSERT INTO `tb_address` VALUES (14, '湖北省武汉市洪山区南李路28号 湖北工业大学', '30.48645994791441', '114.31530997175149', 0, '2025-12-23 11:49:52', '2025-12-23 11:49:51');
INSERT INTO `tb_address` VALUES (15, '湖北省武汉市洪山区南李路28号 湖北工业大学', '30.48645994791441', '114.31530997175149', 0, '2025-12-23 12:35:46', '2025-12-23 12:35:45');
INSERT INTO `tb_address` VALUES (16, '青岛市黄岛区桃林路 哈尔滨工程大学(青岛校区)', '35.789668151091088', '120.04170424009021', 1, '2025-12-23 13:18:19', '2026-01-02 20:40:55');
INSERT INTO `tb_address` VALUES (17, '温泉镇夜市一街与夜市二街交汇处的山城都市项目1号楼和2号楼 黄商生活超市(山城都市店)', '30.74982294169004', '115.67758228271402', 0, '2025-12-30 21:16:33', '2025-12-30 21:16:32');
INSERT INTO `tb_address` VALUES (19, '湖北省武汉市武昌区新河街新生路幸福里小区 武汉大型宠物基地(猫狗市场)', '30.57258426535204', '114.32253531245989', 0, '2025-12-30 21:28:25', '2025-12-30 21:28:25');
INSERT INTO `tb_address` VALUES (20, '太阳宫地区太阳宫UHN国际村4-8底商 Maruko宠物', '39.97824889829926', '116.45293249550538', 0, '2025-12-30 21:50:08', '2025-12-30 21:50:07');
INSERT INTO `tb_address` VALUES (21, '太阳宫地区太阳宫UHN国际村4-8底商 Maruko宠物', '39.97824889829926', '116.45293249550538', 0, '2025-12-30 21:52:42', '2025-12-30 21:52:41');
INSERT INTO `tb_address` VALUES (22, '太阳宫地区太阳宫UHN国际村4-8底商 Maruko宠物', '39.97824889829926', '116.45293249550538', 0, '2025-12-30 21:53:40', '2025-12-30 21:53:40');
INSERT INTO `tb_address` VALUES (23, '太阳宫地区太阳宫UHN国际村4-8底商 Maruko宠物', '39.97824889829926', '116.45293249550538', 0, '2025-12-30 21:54:20', '2025-12-30 21:54:20');
INSERT INTO `tb_address` VALUES (24, '太阳宫地区太阳宫UHN国际村4-8底商 Maruko宠物', '39.97824889829926', '116.45293249550538', 0, '2025-12-30 21:54:50', '2025-12-30 21:54:50');
INSERT INTO `tb_address` VALUES (25, '江苏省南京市江宁区招商街32号二招幢32室 南京有鱼宠物市场猫舍犬舍购宠基地', '31.95678598585978', '118.84102828697871', 0, '2025-12-30 22:03:26', '2025-12-30 22:03:26');
INSERT INTO `tb_address` VALUES (26, '北京市海淀区三嘉信苑14号楼 宠友到家宠物店', '40.13212557259428', '116.21154127615292', 0, '2025-12-30 22:21:30', '2025-12-30 22:21:30');
INSERT INTO `tb_address` VALUES (27, '青年路22号 小七宠物医院', '30.991751589253224', '103.94893454426622', 0, '2025-12-30 22:32:17', '2025-12-30 22:32:16');
INSERT INTO `tb_address` VALUES (28, '广渠路36号院5号楼首城国际B座3层359 Q萌·宠物全国连锁宠物商店(双井店)', '39.89845817862995', '116.47626400485494', 0, '2025-12-30 22:43:30', '2025-12-30 22:43:29');
INSERT INTO `tb_address` VALUES (29, '庑殿路南口33-4号 派特屋动物医院(旧宫店)', '40.25611260286408', '116.13903580454277', 0, '2025-12-30 22:45:17', '2025-12-30 22:45:17');
INSERT INTO `tb_address` VALUES (30, '庑殿路南口33-4号 派特屋动物医院(旧宫店)', '40.25611260286408', '116.13903580454277', 0, '2025-12-30 22:45:53', '2025-12-30 22:45:53');
INSERT INTO `tb_address` VALUES (31, '高碑店镇建国路华汇大厦B座宠乐汇L1-L5层 宠爱国际动物医院·疑难骨科(中心医院店)', '39.914086352523728', '116.55725469065125', 0, '2025-12-30 22:57:44', '2025-12-30 22:57:43');
INSERT INTO `tb_address` VALUES (32, '崇文门外大街40号搜秀城5层 萌宠之家(搜秀商城店)', '27.691129638002189', '106.92718838743969', 0, '2025-12-30 23:06:04', '2025-12-30 23:06:04');
INSERT INTO `tb_address` VALUES (33, '崇文门外大街40号搜秀城5层 萌宠之家(搜秀商城店)', '27.691129638002189', '106.92718838743969', 0, '2025-12-30 23:12:44', '2025-12-30 23:12:43');
INSERT INTO `tb_address` VALUES (48, '湖北省黄冈市英山县G318(毕昇大道)', '30.74963882123383', '115.6806030146524', 1, '2025-12-31 21:33:36', '2025-12-31 22:42:31');
INSERT INTO `tb_address` VALUES (51, '黄冈市英山县汤河家居街 明德幼儿园', '30.751255391351806', '115.67747924707202', 1, '2025-12-31 22:42:31', '2026-01-01 22:03:39');
INSERT INTO `tb_address` VALUES (52, '青岛市崂山区松岭路99号 青岛科技大学(崂山校区)', '36.126442007123959', '120.48729485440993', 0, '2026-01-02 20:40:56', '2026-01-02 20:40:56');
INSERT INTO `tb_address` VALUES (53, '湖北省黄冈市英山县汤河美食街', '30.749763507987038', '115.6763846618081', 1, '2026-01-03 19:30:21', '2026-01-04 16:12:21');
INSERT INTO `tb_address` VALUES (54, '湖北省黄冈市英山县', '30.74135008408465', '115.6874216026259', 0, '2026-01-04 16:47:24', '2026-01-04 16:47:24');
INSERT INTO `tb_address` VALUES (55, '上海市奉贤区文明街16号 齐贤小学', '30.960127594633577', '121.51684504515943', 0, '2026-01-05 20:36:58', '2026-01-05 20:36:57');
INSERT INTO `tb_address` VALUES (56, '上海市奉贤区文明街16号 齐贤小学', '30.960127594633577', '121.51684504515943', 0, '2026-01-05 20:42:34', '2026-01-05 20:42:34');
INSERT INTO `tb_address` VALUES (57, '上海市奉贤区文明街16号 齐贤小学', '30.960127594633577', '121.51684504515943', 0, '2026-01-05 20:44:30', '2026-01-05 20:44:29');
INSERT INTO `tb_address` VALUES (58, '新疆维吾尔自治区阿拉尔市虹桥南路705号 塔里木大学', '40.55022069705901', '81.30399515530668', 0, '2026-01-05 22:01:03', '2026-01-05 22:01:03');
INSERT INTO `tb_address` VALUES (59, '莒南县 莒南县', '35.18081163178793', '118.84154077027218', 0, '2026-01-05 23:12:46', '2026-01-05 23:12:45');
INSERT INTO `tb_address` VALUES (60, '莒南县 莒南县', '35.18081163178793', '118.84154077027218', 0, '2026-01-05 23:14:03', '2026-01-05 23:14:02');
INSERT INTO `tb_address` VALUES (61, '北京市东城区', '39.93482727239599', '116.4224009776628', 0, '2026-01-13 21:56:38', '2026-01-13 21:56:38');
INSERT INTO `tb_address` VALUES (62, '北京市东城区东长安街16号', '39.911460207094538', '116.40801632170097', 0, '2026-01-13 22:07:29', '2026-01-13 22:07:29');
INSERT INTO `tb_address` VALUES (63, '北京市东城区东长安街', '39.91422837261614', '116.4150678402932', 0, '2026-01-15 11:42:15', '2026-01-15 11:42:14');
INSERT INTO `tb_address` VALUES (64, '湖北省黄冈市英山县汤河美食街', '30.749763507987038', '115.6763846618081', 0, '2026-02-10 20:18:23', '2026-02-10 20:18:22');

-- ----------------------------
-- Table structure for tb_cart
-- ----------------------------
DROP TABLE IF EXISTS `tb_cart`;
CREATE TABLE `tb_cart`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '购物车ID，主键自增',
  `user_id` bigint NOT NULL COMMENT '用户ID，不为空',
  `product_id` bigint NOT NULL COMMENT '商品ID，不为空',
  `total_count` bigint NOT NULL COMMENT '商品数量',
  `total_price` decimal(10, 2) NOT NULL COMMENT '商品总价',
  `delete_flag` tinyint NULL DEFAULT 0 COMMENT '删除标记，0-未删除，1-已删除，默认为0',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间，默认为插入时时间戳',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间，默认为插入时时间戳，插入时自动更新',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_product_id`(`product_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 16 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '购物车表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_cart
-- ----------------------------
INSERT INTO `tb_cart` VALUES (1, 1, 3, 1, 35.00, 1, '2025-12-16 12:57:04', '2025-12-17 20:48:05');
INSERT INTO `tb_cart` VALUES (2, 1, 5, 1, 198.00, 1, '2025-12-17 15:42:30', '2025-12-17 20:48:09');
INSERT INTO `tb_cart` VALUES (5, 1, 4, 1, 320.00, 1, '2025-12-17 17:21:51', '2025-12-17 20:48:09');
INSERT INTO `tb_cart` VALUES (6, 1, 3, 1, 35.00, 1, '2025-12-17 20:49:48', '2025-12-17 20:49:52');
INSERT INTO `tb_cart` VALUES (7, 1, 3, 1, 35.00, 1, '2025-12-17 20:52:33', '2025-12-17 20:52:36');
INSERT INTO `tb_cart` VALUES (8, 1, 3, 2, 70.00, 1, '2025-12-17 20:55:25', '2025-12-17 20:55:49');
INSERT INTO `tb_cart` VALUES (9, 1, 4, 2, 640.00, 0, '2025-12-17 20:55:38', '2025-12-17 21:45:00');
INSERT INTO `tb_cart` VALUES (10, 1, 5, 1, 198.00, 1, '2025-12-17 20:55:44', '2025-12-17 20:55:49');
INSERT INTO `tb_cart` VALUES (11, 1, 3, 1, 35.00, 0, '2025-12-17 21:44:57', '2025-12-17 21:44:57');
INSERT INTO `tb_cart` VALUES (12, 1, 5, 1, 198.00, 0, '2025-12-17 21:44:59', '2025-12-17 21:44:59');
INSERT INTO `tb_cart` VALUES (15, 1, 35, 1, 1.00, 1, '2026-02-10 18:13:44', '2026-02-10 18:15:42');

-- ----------------------------
-- Table structure for tb_column_info
-- ----------------------------
DROP TABLE IF EXISTS `tb_column_info`;
CREATE TABLE `tb_column_info`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '栏目ID，主键自增',
  `name` varchar(25) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '栏目名称，不为空',
  `delete_flag` tinyint NULL DEFAULT 0 COMMENT '删除标记，0-未删除，1-已删除',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间，默认为插入时时间戳',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间，默认为插入时时间戳，插入时自动更新',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '栏目表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_column_info
-- ----------------------------
INSERT INTO `tb_column_info` VALUES (1, '宠物日常', 0, '2025-12-18 12:20:44', '2025-12-18 12:20:44');
INSERT INTO `tb_column_info` VALUES (2, '救助分享', 0, '2025-12-18 12:20:44', '2025-12-18 12:20:44');
INSERT INTO `tb_column_info` VALUES (3, '医疗科普', 0, '2025-12-18 12:20:44', '2025-12-18 12:20:44');
INSERT INTO `tb_column_info` VALUES (4, 'test', 1, '2026-01-07 18:59:01', '2026-01-07 19:02:06');
INSERT INTO `tb_column_info` VALUES (5, 'test1', 1, '2026-01-07 19:01:15', '2026-01-07 19:02:06');
INSERT INTO `tb_column_info` VALUES (6, 'test2', 1, '2026-01-07 19:01:19', '2026-01-07 19:02:02');
INSERT INTO `tb_column_info` VALUES (7, 'test1', 1, '2026-02-23 16:04:44', '2026-02-23 16:05:40');

-- ----------------------------
-- Table structure for tb_comment_media
-- ----------------------------
DROP TABLE IF EXISTS `tb_comment_media`;
CREATE TABLE `tb_comment_media`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '媒体ID，主键自增',
  `comment_id` bigint NOT NULL COMMENT '评论ID，与评论总表关联',
  `media_url` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '媒体地址',
  `media_type` tinyint NOT NULL COMMENT '媒体类型，1-图片，2-视频',
  `delete_flag` tinyint NULL DEFAULT 0 COMMENT '删除标记，0-未删除，1-已删除',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间，默认为插入时时间戳',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间，默认为插入时时间戳，插入时自动更新',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_comment_id`(`comment_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 16 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '评论媒体表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_comment_media
-- ----------------------------
INSERT INTO `tb_comment_media` VALUES (1, 1, 'https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/golden_retriever_comment_2.png', 1, 0, '2025-12-13 10:35:56', '2025-12-13 10:35:56');
INSERT INTO `tb_comment_media` VALUES (2, 1, 'https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/golden_retriever_comment_1.png', 1, 0, '2025-12-13 10:35:59', '2025-12-13 10:35:59');
INSERT INTO `tb_comment_media` VALUES (3, 12, 'https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/golden_retriever_comment_video_1.mp4', 2, 0, '2025-12-13 10:43:49', '2025-12-13 10:43:49');
INSERT INTO `tb_comment_media` VALUES (4, 17, 'https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/846d21db-38c5-4cfb-a499-0dba9560abe4_british_shorthair_comment_1.png', 1, 0, '2025-12-13 16:36:46', '2025-12-13 16:36:46');
INSERT INTO `tb_comment_media` VALUES (5, 17, 'https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/3b2621cd-eb88-491e-9f3e-d2c9382ed647_british_shorthair_comment_2.png', 1, 0, '2025-12-13 16:37:07', '2025-12-13 16:37:07');
INSERT INTO `tb_comment_media` VALUES (6, 17, 'https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/45909a88-f507-4351-96a5-d5f97b32b602_british_short_hair_comment_video.mp4', 2, 0, '2025-12-13 16:40:57', '2025-12-13 16:40:57');
INSERT INTO `tb_comment_media` VALUES (7, 20, 'https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/410338d4-378c-478f-96a5-085df5f8a2e9_avatar.jpg', 1, 0, '2025-12-13 18:33:22', '2025-12-13 18:33:21');
INSERT INTO `tb_comment_media` VALUES (8, 20, 'https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/b35eed15-f083-42bd-8cc3-0b731b5e0822_抖音20251127-058170.mp4', 2, 0, '2025-12-13 18:33:25', '2025-12-13 18:33:24');
INSERT INTO `tb_comment_media` VALUES (9, 24, 'https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/golden_retriever_post_1.png', 1, 0, '2025-12-18 22:51:54', '2025-12-18 22:51:54');
INSERT INTO `tb_comment_media` VALUES (10, 26, 'https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/british_shorthair_post1.png', 1, 0, '2025-12-18 22:51:56', '2025-12-18 22:51:56');
INSERT INTO `tb_comment_media` VALUES (11, 29, 'https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/cat_post.mp4', 2, 1, '2025-12-18 22:52:07', '2025-12-21 21:04:39');
INSERT INTO `tb_comment_media` VALUES (12, 31, 'https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/ceef34ac-68d1-4789-9545-9122bd13b7c1_british_shorthair_comment_2.png', 1, 0, '2025-12-19 11:27:47', '2025-12-19 11:27:47');
INSERT INTO `tb_comment_media` VALUES (13, 32, 'https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/73ac69a0-8411-4a26-8500-e0538be9682d_british_shorthair_comment_2.png', 1, 0, '2025-12-19 11:29:37', '2025-12-19 11:29:37');
INSERT INTO `tb_comment_media` VALUES (14, 33, 'https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/e1134a4a-f3b5-4321-8d4f-55f2a0af004f_british_short_hair_comment_video.mp4', 2, 0, '2025-12-19 11:39:16', '2025-12-19 11:39:15');
INSERT INTO `tb_comment_media` VALUES (15, 71, 'https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/c8f47124-3e06-44c0-95c4-e44409ecc6f4_default_avatar.png', 1, 1, '2026-01-15 11:46:38', '2026-01-15 11:52:36');

-- ----------------------------
-- Table structure for tb_comment_super
-- ----------------------------
DROP TABLE IF EXISTS `tb_comment_super`;
CREATE TABLE `tb_comment_super`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '评论ID，主键自增',
  `object_id` bigint NOT NULL COMMENT '评论对象ID，不为空，与商品表或帖子表关联',
  `user_id` bigint NOT NULL COMMENT '评论人ID，不为空，与用户表关联',
  `parent_id` bigint NULL DEFAULT NULL COMMENT '父级评论ID，默认为顶级评论',
  `type` tinyint NOT NULL COMMENT '评论类型，不为空，1-商品评论，2-帖子评论',
  `content` varchar(2048) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '评论内容，不为空',
  `status` tinyint NULL DEFAULT 1 COMMENT '评论状态，1--审核中，2-审核成功，3-审核失败，默认为1',
  `delete_flag` tinyint NULL DEFAULT 0 COMMENT '删除标记，0-未删除，1-已删除，默认为0',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间，默认为插入时时间戳',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间，默认为插入时时间戳，插入时自动更新',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_object_id`(`object_id` ASC) USING BTREE,
  INDEX `idx_parent_id`(`parent_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 97 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '评论总表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_comment_super
-- ----------------------------
INSERT INTO `tb_comment_super` VALUES (1, 1, 1, NULL, 1, '这个商品质量很好，非常喜欢！', 2, 0, '2025-12-12 19:54:51', '2026-01-15 11:56:40');
INSERT INTO `tb_comment_super` VALUES (2, 1, 2, NULL, 1, '物流很快，包装也很仔细，五星好评！', 2, 0, '2025-12-12 19:54:58', '2026-01-15 11:56:41');
INSERT INTO `tb_comment_super` VALUES (3, 1, 3, NULL, 1, '性价比很高，会推荐给朋友的。', 2, 0, '2025-12-12 19:55:14', '2026-01-15 11:56:42');
INSERT INTO `tb_comment_super` VALUES (4, 2, 1, NULL, 1, '还不错，符合描述。', 2, 1, '2025-12-12 19:55:18', '2026-01-15 11:56:43');
INSERT INTO `tb_comment_super` VALUES (5, 2, 2, NULL, 1, '有点小瑕疵，但总体还行。', 2, 0, '2025-12-12 19:55:32', '2026-01-15 11:56:44');
INSERT INTO `tb_comment_super` VALUES (6, 3, 1, NULL, 1, '非常满意的一次购物体验。', 2, 0, '2025-12-12 19:55:37', '2026-01-15 11:56:45');
INSERT INTO `tb_comment_super` VALUES (7, 3, 3, NULL, 1, '发货速度很快，商品也不错。', 2, 0, '2025-12-12 19:55:47', '2026-01-15 11:56:46');
INSERT INTO `tb_comment_super` VALUES (8, 4, 2, NULL, 1, '质量一般般，不过价格便宜。', 2, 0, '2025-12-12 19:55:52', '2026-01-15 11:56:47');
INSERT INTO `tb_comment_super` VALUES (9, 5, 1, NULL, 1, '超级喜欢，已经推荐给朋友们了。', 2, 0, '2025-12-12 19:55:59', '2026-01-15 11:56:48');
INSERT INTO `tb_comment_super` VALUES (10, 5, 3, NULL, 1, '很不错的选择，值得购买。', 2, 0, '2025-12-12 19:56:13', '2026-01-15 11:56:49');
INSERT INTO `tb_comment_super` VALUES (11, 1, 2, 1, 1, '感谢您的好评，我们会继续努力提供更好的商品和服务！', 2, 0, '2025-12-12 21:21:19', '2026-01-15 11:56:50');
INSERT INTO `tb_comment_super` VALUES (12, 1, 3, 1, 1, '我也觉得这个商品很不错，正在考虑要不要买一个。', 2, 0, '2025-12-12 21:28:08', '2026-01-15 11:56:51');
INSERT INTO `tb_comment_super` VALUES (13, 1, 1, 11, 1, '谢谢您的回复，期待更多优质商品！', 2, 0, '2025-12-12 21:28:21', '2026-01-15 11:56:54');
INSERT INTO `tb_comment_super` VALUES (17, 2, 3, 4, 1, '的确是很不错的商品', 2, 0, '2025-12-13 16:20:15', '2026-01-15 11:56:55');
INSERT INTO `tb_comment_super` VALUES (18, 1, 1, NULL, 1, '我是大学生，送我', 2, 1, '2025-12-13 18:11:26', '2026-01-15 11:56:56');
INSERT INTO `tb_comment_super` VALUES (19, 1, 1, NULL, 1, 'wdf', 2, 1, '2025-12-13 18:30:07', '2026-01-15 11:56:57');
INSERT INTO `tb_comment_super` VALUES (20, 1, 1, NULL, 1, '我是大学生，送我', 2, 0, '2025-12-13 18:33:21', '2026-01-15 11:56:59');
INSERT INTO `tb_comment_super` VALUES (21, 1, 2, 20, 1, '我也要', 2, 0, '2025-12-14 08:21:50', '2026-01-15 11:57:00');
INSERT INTO `tb_comment_super` VALUES (22, 2, 2, 5, 1, '不好意思，商品还是很不错的', 2, 0, '2025-12-14 08:36:10', '2026-01-15 11:57:01');
INSERT INTO `tb_comment_super` VALUES (23, 1, 2, 12, 1, '好可爱啊', 2, 0, '2025-12-14 08:49:37', '2026-01-15 11:57:02');
INSERT INTO `tb_comment_super` VALUES (24, 1, 2, NULL, 2, '这真是太棒了！金毛看起来很开心！', 2, 0, '2025-12-18 22:51:48', '2026-01-15 11:57:04');
INSERT INTO `tb_comment_super` VALUES (25, 1, 3, NULL, 2, '我家的金毛也很喜欢水，每次洗澡都特别兴奋！', 2, 0, '2025-12-18 22:51:50', '2026-01-15 11:57:05');
INSERT INTO `tb_comment_super` VALUES (26, 2, 1, NULL, 2, '救助流浪猫真的很有意义，感谢分享！', 2, 0, '2025-12-18 22:51:52', '2026-01-15 11:57:06');
INSERT INTO `tb_comment_super` VALUES (27, 1, 1, 24, 2, '是的，金毛是最友善的犬种之一！', 2, 0, '2025-12-18 22:51:59', '2026-01-15 11:57:07');
INSERT INTO `tb_comment_super` VALUES (28, 3, 2, NULL, 2, '这个驱虫方法很实用，收藏了！', 2, 0, '2025-12-18 22:52:00', '2026-01-15 11:57:09');
INSERT INTO `tb_comment_super` VALUES (29, 3, 3, NULL, 2, '我家猫咪用这个方法也很有效果！', 2, 1, '2025-12-18 22:52:02', '2026-01-15 11:57:10');
INSERT INTO `tb_comment_super` VALUES (30, 3, 3, 29, 2, '不错不错', 2, 0, '2025-12-19 11:20:30', '2026-01-15 11:57:11');
INSERT INTO `tb_comment_super` VALUES (31, 3, 3, 30, 2, '这个是我的猫', 2, 0, '2025-12-19 11:27:46', '2026-01-15 11:57:12');
INSERT INTO `tb_comment_super` VALUES (32, 3, 3, 6, 1, '我也觉得', 2, 0, '2025-12-19 11:29:37', '2026-01-15 11:57:13');
INSERT INTO `tb_comment_super` VALUES (33, 3, 3, 32, 1, '我的猫好看吗', 2, 0, '2025-12-19 11:39:15', '2026-01-15 11:57:15');
INSERT INTO `tb_comment_super` VALUES (34, 3, 3, 29, 2, '可以可以', 2, 1, '2025-12-19 16:36:49', '2026-01-15 11:57:16');
INSERT INTO `tb_comment_super` VALUES (37, 10, 3, NULL, 2, '沙发', 2, 1, '2025-12-20 16:57:42', '2026-01-15 11:57:17');
INSERT INTO `tb_comment_super` VALUES (38, 15, 1, NULL, 2, '兄弟，我也喜欢这首歌，emo神曲', 2, 0, '2025-12-23 16:54:56', '2026-01-15 12:41:53');
INSERT INTO `tb_comment_super` VALUES (39, 16, 1, NULL, 2, '所以呀，我该彻底放下我很喜欢很在意的男孩子…我不会忘记他的心跳声和眼睛…永远都不会忘记，hhhh，我会好好的生活下去（虽然我也不太信），祝他能找到更爱他的吧，希望他生活能更好', 2, 0, '2025-12-26 20:56:12', '2026-01-15 11:57:19');
INSERT INTO `tb_comment_super` VALUES (55, 15, 3, 38, 2, '赞同！', 2, 0, '2026-01-02 21:00:57', '2026-01-15 11:57:24');
INSERT INTO `tb_comment_super` VALUES (56, 33, 4, NULL, 1, '111', 2, 1, '2026-01-13 21:56:55', '2026-01-15 11:57:25');
INSERT INTO `tb_comment_super` VALUES (57, 33, 4, NULL, 1, '1111', 2, 1, '2026-01-13 21:56:57', '2026-01-15 11:57:26');
INSERT INTO `tb_comment_super` VALUES (58, 33, 4, NULL, 1, '111', 2, 1, '2026-01-13 21:57:03', '2026-01-15 11:57:27');
INSERT INTO `tb_comment_super` VALUES (59, 33, 4, NULL, 1, '111', 2, 1, '2026-01-13 22:01:39', '2026-01-15 11:57:28');
INSERT INTO `tb_comment_super` VALUES (60, 33, 4, NULL, 1, '1111', 2, 1, '2026-01-13 22:01:41', '2026-01-15 11:57:29');
INSERT INTO `tb_comment_super` VALUES (61, 33, 4, NULL, 1, '111', 2, 1, '2026-01-13 22:01:42', '2026-01-15 11:57:30');
INSERT INTO `tb_comment_super` VALUES (62, 34, 4, NULL, 1, '11', 2, 1, '2026-01-13 22:07:40', '2026-01-15 11:57:31');
INSERT INTO `tb_comment_super` VALUES (63, 34, 4, NULL, 1, '11', 2, 1, '2026-01-13 22:07:41', '2026-01-15 11:57:32');
INSERT INTO `tb_comment_super` VALUES (64, 34, 4, NULL, 1, '11', 2, 1, '2026-01-13 22:07:42', '2026-01-15 11:57:33');
INSERT INTO `tb_comment_super` VALUES (65, 35, 4, NULL, 2, '222', 2, 1, '2026-01-13 22:11:06', '2026-01-15 11:57:34');
INSERT INTO `tb_comment_super` VALUES (66, 35, 4, NULL, 1, '11', 2, 1, '2026-01-15 11:42:26', '2026-01-15 11:57:35');
INSERT INTO `tb_comment_super` VALUES (67, 35, 4, NULL, 1, '1111', 2, 1, '2026-01-15 11:42:28', '2026-01-15 11:57:37');
INSERT INTO `tb_comment_super` VALUES (68, 35, 3, NULL, 1, '222', 2, 1, '2026-01-15 11:42:51', '2026-01-15 11:57:38');
INSERT INTO `tb_comment_super` VALUES (69, 35, 3, NULL, 1, '222', 2, 1, '2026-01-15 11:45:44', '2026-01-15 11:57:39');
INSERT INTO `tb_comment_super` VALUES (70, 35, 3, NULL, 1, '222', 2, 1, '2026-01-15 11:45:47', '2026-01-15 11:57:40');
INSERT INTO `tb_comment_super` VALUES (71, 35, 4, NULL, 1, '222', 2, 1, '2026-01-15 11:46:37', '2026-01-15 11:57:41');
INSERT INTO `tb_comment_super` VALUES (72, 35, 3, NULL, 1, '3333', 2, 1, '2026-01-15 11:52:25', '2026-01-15 11:57:42');
INSERT INTO `tb_comment_super` VALUES (73, 35, 4, NULL, 1, '卧槽', 3, 1, '2026-01-15 12:15:06', '2026-01-15 12:27:24');
INSERT INTO `tb_comment_super` VALUES (74, 35, 4, NULL, 1, '测试正常通过审核的评论', 2, 1, '2026-01-15 12:24:28', '2026-01-15 12:27:24');
INSERT INTO `tb_comment_super` VALUES (75, 35, 4, NULL, 1, '卧槽你妈', 3, 1, '2026-01-15 12:25:40', '2026-01-15 12:27:24');
INSERT INTO `tb_comment_super` VALUES (76, 35, 4, NULL, 1, '卧槽，测试手动审核', 2, 1, '2026-01-15 12:27:47', '2026-01-15 12:31:01');
INSERT INTO `tb_comment_super` VALUES (77, 35, 4, NULL, 1, '卧槽', 3, 0, '2026-01-15 12:36:19', '2026-01-15 12:37:00');
INSERT INTO `tb_comment_super` VALUES (78, 15, 4, NULL, 2, '好听爱听', 2, 0, '2026-01-15 12:47:06', '2026-01-15 12:48:01');
INSERT INTO `tb_comment_super` VALUES (79, 40, 1, NULL, 2, '111', 2, 1, '2026-02-10 15:00:17', '2026-02-10 15:14:57');
INSERT INTO `tb_comment_super` VALUES (80, 41, 1, NULL, 2, '111', 2, 1, '2026-02-10 15:16:23', '2026-02-10 16:44:48');
INSERT INTO `tb_comment_super` VALUES (81, 41, 1, NULL, 2, '222', 2, 1, '2026-02-10 15:17:51', '2026-02-10 16:44:48');
INSERT INTO `tb_comment_super` VALUES (82, 47, 1, NULL, 2, '测试websocket评论审核', 2, 0, '2026-02-14 22:04:30', '2026-02-14 22:05:01');
INSERT INTO `tb_comment_super` VALUES (83, 47, 1, 82, 2, '自己给自己回复，不发通知', 2, 0, '2026-02-14 22:16:01', '2026-02-14 22:17:01');
INSERT INTO `tb_comment_super` VALUES (84, 7, 1, NULL, 1, '发一条评论测试评论通知', 2, 0, '2026-02-14 22:18:03', '2026-02-14 22:19:00');
INSERT INTO `tb_comment_super` VALUES (85, 7, 3, 84, 1, 'websocket通知测试', 2, 0, '2026-02-14 22:19:37', '2026-02-14 22:20:00');
INSERT INTO `tb_comment_super` VALUES (86, 7, 3, 84, 1, 'websocket通知测试2', 2, 0, '2026-02-14 22:23:53', '2026-02-14 22:24:01');
INSERT INTO `tb_comment_super` VALUES (87, 7, 3, 84, 1, 'websocket通知测试3', 2, 0, '2026-02-14 22:29:34', '2026-02-14 22:30:00');
INSERT INTO `tb_comment_super` VALUES (88, 7, 3, 84, 1, 'websocket通知测试4', 2, 0, '2026-02-14 22:31:49', '2026-02-14 22:32:01');
INSERT INTO `tb_comment_super` VALUES (89, 47, 1, 82, 2, '自己给自己回复测试', 2, 0, '2026-02-14 22:37:08', '2026-02-14 22:38:01');
INSERT INTO `tb_comment_super` VALUES (90, 47, 1, 82, 2, '自己给自己回复测试2', 2, 0, '2026-02-14 22:41:55', '2026-02-14 22:42:00');
INSERT INTO `tb_comment_super` VALUES (91, 47, 3, 82, 2, '其他人回复测试', 2, 0, '2026-02-14 22:42:33', '2026-02-14 22:43:00');
INSERT INTO `tb_comment_super` VALUES (92, 7, 3, 84, 1, '测试评论', 2, 0, '2026-02-14 23:05:57', '2026-02-14 23:06:01');
INSERT INTO `tb_comment_super` VALUES (93, 7, 3, 84, 1, '回复消息，测试RabbitMQ评论审核', 1, 1, '2026-02-21 17:16:51', '2026-02-21 17:18:29');
INSERT INTO `tb_comment_super` VALUES (94, 7, 3, 84, 1, '测试RabbitMQ审核评论', 2, 0, '2026-02-21 17:23:50', '2026-02-21 17:23:51');
INSERT INTO `tb_comment_super` VALUES (95, 7, 3, 84, 1, '卧槽，测试RabbitMQ审核', 3, 0, '2026-02-21 17:24:31', '2026-02-21 17:24:31');
INSERT INTO `tb_comment_super` VALUES (96, 7, 1, 84, 1, '自己给自己回复', 2, 0, '2026-02-21 17:24:48', '2026-02-21 17:24:48');

-- ----------------------------
-- Table structure for tb_message
-- ----------------------------
DROP TABLE IF EXISTS `tb_message`;
CREATE TABLE `tb_message`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '消息ID，主键自增',
  `receive_user_id` bigint NOT NULL COMMENT '接收消息的用户ID',
  `send_user_id` bigint NOT NULL COMMENT '发送消息的用户ID（当前只能管理员发送消息，后续可以借此开发好友关注发私信的功能）',
  `type` tinyint NULL DEFAULT NULL COMMENT '消息类型，1-系统消息，2-聊天消息，3-商品卡片消息，4-订单卡片消息，5-媒体消息',
  `message` varchar(2048) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '消息内容',
  `status` tinyint NULL DEFAULT NULL COMMENT '已读标记，0-未读，1-已读',
  `delete_flag` tinyint NULL DEFAULT 0 COMMENT '删除标记，0-未删除，1-已删除，默认为0',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间，默认为插入时时间戳',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间，默认为插入时时间戳，插入时自动更新',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_receive_user_id`(`receive_user_id` ASC) USING BTREE,
  INDEX `idx_send_user_id`(`send_user_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 92 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '消息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_message
-- ----------------------------
INSERT INTO `tb_message` VALUES (3, 4, 1, 5, NULL, 1, 0, '2026-02-13 23:44:16', '2026-02-14 00:25:15');
INSERT INTO `tb_message` VALUES (4, 1, 4, 2, '你好', 1, 0, '2026-02-14 00:05:17', '2026-02-14 00:32:41');
INSERT INTO `tb_message` VALUES (5, 1, 4, 2, '你好', 1, 0, '2026-02-14 00:07:46', '2026-02-14 00:32:41');
INSERT INTO `tb_message` VALUES (6, 4, 1, 5, NULL, 1, 0, '2026-02-14 00:07:54', '2026-02-14 00:27:34');
INSERT INTO `tb_message` VALUES (7, 1, 4, 2, '你好', 1, 0, '2026-02-14 00:09:16', '2026-02-14 00:32:41');
INSERT INTO `tb_message` VALUES (8, 4, 1, 5, NULL, 1, 0, '2026-02-14 00:09:22', '2026-02-14 00:27:34');
INSERT INTO `tb_message` VALUES (9, 1, 4, 2, '你好', 1, 0, '2026-02-14 00:11:15', '2026-02-14 00:32:41');
INSERT INTO `tb_message` VALUES (10, 1, 4, 2, '你好', 1, 0, '2026-02-14 00:22:02', '2026-02-14 00:32:41');
INSERT INTO `tb_message` VALUES (11, 4, 1, 2, '你好', 1, 0, '2026-02-14 00:22:06', '2026-02-14 00:25:15');
INSERT INTO `tb_message` VALUES (12, 4, 1, 5, NULL, 1, 0, '2026-02-14 00:22:13', '2026-02-14 00:25:15');
INSERT INTO `tb_message` VALUES (13, 4, 2, 2, '你好', 1, 0, '2026-02-14 00:35:47', '2026-02-14 00:35:46');
INSERT INTO `tb_message` VALUES (14, 4, 2, 2, '你好', 1, 0, '2026-02-14 00:35:56', '2026-02-14 00:35:58');
INSERT INTO `tb_message` VALUES (15, 1, 4, 2, '你好', 1, 0, '2026-02-14 00:42:41', '2026-02-14 00:43:00');
INSERT INTO `tb_message` VALUES (16, 1, 4, 2, '测试', 1, 0, '2026-02-14 00:46:35', '2026-02-14 00:47:14');
INSERT INTO `tb_message` VALUES (17, 2, 4, 2, '你好', 1, 0, '2026-02-14 01:05:26', '2026-02-14 01:05:30');
INSERT INTO `tb_message` VALUES (18, 2, 4, 5, NULL, 1, 0, '2026-02-14 01:06:15', '2026-02-14 01:06:19');
INSERT INTO `tb_message` VALUES (19, 4, 2, 2, '你好', 1, 0, '2026-02-14 10:13:12', '2026-02-14 10:13:15');
INSERT INTO `tb_message` VALUES (20, 4, 3, 2, '你好', 1, 0, '2026-02-14 10:31:16', '2026-02-14 10:31:15');
INSERT INTO `tb_message` VALUES (21, 3, 4, 2, '你好', 1, 0, '2026-02-14 10:31:23', '2026-02-14 10:33:24');
INSERT INTO `tb_message` VALUES (22, 4, 3, 2, '你好', 1, 0, '2026-02-14 10:33:12', '2026-02-14 10:33:11');
INSERT INTO `tb_message` VALUES (23, 1, 4, 2, '你好', 1, 0, '2026-02-14 11:01:31', '2026-02-14 12:16:17');
INSERT INTO `tb_message` VALUES (24, 2, 4, 2, '测试新消息', 0, 0, '2026-02-14 11:03:16', '2026-02-14 11:03:16');
INSERT INTO `tb_message` VALUES (25, 1, 4, 2, '测试新消息', 1, 0, '2026-02-14 11:03:31', '2026-02-14 12:16:17');
INSERT INTO `tb_message` VALUES (26, 3, 4, 2, '你好', 1, 0, '2026-02-14 11:10:02', '2026-02-14 11:10:03');
INSERT INTO `tb_message` VALUES (27, 3, 4, 2, '测试', 1, 0, '2026-02-14 11:10:31', '2026-02-14 11:10:33');
INSERT INTO `tb_message` VALUES (28, 4, 3, 2, '测试商品卡片', 1, 0, '2026-02-14 11:52:44', '2026-02-14 11:52:50');
INSERT INTO `tb_message` VALUES (29, 4, 3, 3, '{\"productId\":36,\"name\":\"es test1\",\"price\":1,\"image\":\"https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/b3842cb3-3c28-4017-b38b-508ce1895712_unnamed.png\",\"status\":1}', 1, 0, '2026-02-14 11:52:45', '2026-02-14 11:52:50');
INSERT INTO `tb_message` VALUES (30, 4, 3, 3, '{\"productId\":36,\"name\":\"es test1\",\"price\":1,\"image\":\"https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/b3842cb3-3c28-4017-b38b-508ce1895712_unnamed.png\",\"status\":1}', 1, 0, '2026-02-14 11:55:58', '2026-02-14 11:55:57');
INSERT INTO `tb_message` VALUES (31, 4, 3, 2, '测试商品卡片2', 1, 0, '2026-02-14 11:55:58', '2026-02-14 11:55:57');
INSERT INTO `tb_message` VALUES (32, 4, 1, 4, '{\"orderId\":9,\"productName\":\"金毛犬\",\"totalAmount\":2500,\"status\":1,\"createTime\":\"2025-12-14 23:27:42\",\"imgUrl\":\"https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/golden_retriever_1.png\",\"totalCount\":1}', 1, 0, '2026-02-14 12:21:43', '2026-02-14 12:21:46');
INSERT INTO `tb_message` VALUES (33, 4, 1, 2, '测试订单卡片', 1, 0, '2026-02-14 12:21:43', '2026-02-14 12:21:46');
INSERT INTO `tb_message` VALUES (34, 1, 4, 2, '测试卡片功能成功', 1, 0, '2026-02-14 12:27:23', '2026-02-14 12:27:34');
INSERT INTO `tb_message` VALUES (35, 4, 1, 2, '在吗', 1, 0, '2026-02-14 12:34:10', '2026-02-14 12:34:30');
INSERT INTO `tb_message` VALUES (36, 4, 1, 2, '有问题想咨询', 1, 0, '2026-02-14 12:36:07', '2026-02-14 12:42:48');
INSERT INTO `tb_message` VALUES (37, 4, 1, 2, '你好', 1, 0, '2026-02-14 12:42:38', '2026-02-14 12:42:48');
INSERT INTO `tb_message` VALUES (38, 1, 4, 2, '在的亲亲', 1, 0, '2026-02-14 12:43:01', '2026-02-14 12:43:11');
INSERT INTO `tb_message` VALUES (39, 1, 4, 2, '亲亲有什么问题吗', 1, 0, '2026-02-14 12:58:12', '2026-02-14 12:58:19');
INSERT INTO `tb_message` VALUES (40, 4, 1, 2, '没有了', 1, 0, '2026-02-14 12:58:26', '2026-02-14 12:58:33');
INSERT INTO `tb_message` VALUES (41, 1, 4, 2, '好的亲亲', 1, 0, '2026-02-14 13:03:30', '2026-02-14 13:09:02');
INSERT INTO `tb_message` VALUES (42, 1, 4, 2, '测试最新消息', 1, 0, '2026-02-14 13:05:27', '2026-02-14 13:09:02');
INSERT INTO `tb_message` VALUES (43, 1, 4, 2, '测试', 1, 0, '2026-02-14 13:07:06', '2026-02-14 13:09:02');
INSERT INTO `tb_message` VALUES (44, 1, 4, 2, '测试', 1, 0, '2026-02-14 13:08:32', '2026-02-14 13:09:02');
INSERT INTO `tb_message` VALUES (45, 4, 1, 2, '测试新消息', 1, 0, '2026-02-14 13:09:08', '2026-02-14 13:09:33');
INSERT INTO `tb_message` VALUES (46, 1, 4, 2, '管理员发消息', 1, 0, '2026-02-14 13:19:37', '2026-02-14 13:20:16');
INSERT INTO `tb_message` VALUES (47, 4, 1, 2, '普通用户发消息', 1, 0, '2026-02-14 13:20:22', '2026-02-14 13:20:49');
INSERT INTO `tb_message` VALUES (48, 4, 1, 2, '测试新消息', 1, 0, '2026-02-14 13:27:47', '2026-02-14 13:28:06');
INSERT INTO `tb_message` VALUES (49, 4, 1, 2, '再次测试新消息', 1, 0, '2026-02-14 13:28:53', '2026-02-14 13:29:01');
INSERT INTO `tb_message` VALUES (50, 1, 4, 2, '管理员回信消息', 1, 0, '2026-02-14 13:29:25', '2026-02-14 13:29:35');
INSERT INTO `tb_message` VALUES (51, 1, 4, 2, '再次回信', 1, 0, '2026-02-14 13:29:47', '2026-02-14 13:29:49');
INSERT INTO `tb_message` VALUES (52, 1, 4, 2, '测试用户新消息标记', 1, 0, '2026-02-14 13:36:41', '2026-02-14 13:36:49');
INSERT INTO `tb_message` VALUES (53, 1, 4, 2, '测试1', 1, 0, '2026-02-14 13:36:47', '2026-02-14 13:36:49');
INSERT INTO `tb_message` VALUES (54, 1, 4, 2, '测试不在线时有新消息', 1, 0, '2026-02-14 13:37:01', '2026-02-14 13:37:16');
INSERT INTO `tb_message` VALUES (55, 1, 4, 2, '1', 1, 0, '2026-02-14 13:37:01', '2026-02-14 13:37:16');
INSERT INTO `tb_message` VALUES (56, 1, 4, 2, '2', 1, 0, '2026-02-14 13:37:02', '2026-02-14 13:37:16');
INSERT INTO `tb_message` VALUES (57, 1, 4, 2, '3', 1, 0, '2026-02-14 13:37:03', '2026-02-14 13:37:16');
INSERT INTO `tb_message` VALUES (58, 1, 4, 2, '4', 1, 0, '2026-02-14 13:37:03', '2026-02-14 13:37:16');
INSERT INTO `tb_message` VALUES (59, 1, 4, 2, '5', 1, 0, '2026-02-14 13:37:04', '2026-02-14 13:37:16');
INSERT INTO `tb_message` VALUES (60, 1, 4, 2, '6', 1, 0, '2026-02-14 13:37:04', '2026-02-14 13:37:16');
INSERT INTO `tb_message` VALUES (61, 1, 4, 2, '7', 1, 0, '2026-02-14 13:37:05', '2026-02-14 13:37:16');
INSERT INTO `tb_message` VALUES (62, 1, 4, 2, '8', 1, 0, '2026-02-14 13:37:05', '2026-02-14 13:37:16');
INSERT INTO `tb_message` VALUES (63, 1, 4, 2, '9', 1, 0, '2026-02-14 13:37:07', '2026-02-14 13:37:16');
INSERT INTO `tb_message` VALUES (64, 1, 4, 2, '10', 1, 0, '2026-02-14 13:37:08', '2026-02-14 13:37:16');
INSERT INTO `tb_message` VALUES (65, 4, 1, 2, '用户端正常，回复', 1, 0, '2026-02-14 13:37:39', '2026-02-14 13:37:38');
INSERT INTO `tb_message` VALUES (66, 4, 1, 2, '这是user1发送的消息', 1, 0, '2026-02-14 16:27:33', '2026-02-14 16:27:46');
INSERT INTO `tb_message` VALUES (67, 4, 1, 2, '关闭对话框', 1, 0, '2026-02-14 16:27:42', '2026-02-14 16:27:46');
INSERT INTO `tb_message` VALUES (68, 1, 4, 2, '管理员回复消息', 1, 0, '2026-02-14 16:27:52', '2026-02-14 16:28:06');
INSERT INTO `tb_message` VALUES (69, 1, 4, 2, '用户端会有消息出现提示', 1, 0, '2026-02-14 16:28:01', '2026-02-14 16:28:06');
INSERT INTO `tb_message` VALUES (70, 4, 1, 2, '可以发送图片和视频', 1, 0, '2026-02-14 16:28:22', '2026-02-14 16:28:21');
INSERT INTO `tb_message` VALUES (71, 4, 1, 5, NULL, 1, 0, '2026-02-14 16:28:28', '2026-02-14 16:28:27');
INSERT INTO `tb_message` VALUES (72, 4, 1, 2, '在商品和订单页面可以发送商品和订单卡片', 1, 0, '2026-02-14 16:28:57', '2026-02-14 16:28:56');
INSERT INTO `tb_message` VALUES (73, 4, 1, 3, '{\"productId\":35,\"name\":\"test\",\"price\":1,\"image\":\"https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/a1fb8b4a-a87d-43de-adce-3bdb282de261_default_avatar.png\",\"status\":1}', 1, 0, '2026-02-14 16:29:13', '2026-02-14 16:29:13');
INSERT INTO `tb_message` VALUES (74, 4, 1, 2, '发送商品卡片', 1, 0, '2026-02-14 16:29:13', '2026-02-14 16:29:13');
INSERT INTO `tb_message` VALUES (75, 1, 4, 2, '管理员点击卡片可以看到具体的商品详情', 1, 0, '2026-02-14 16:29:38', '2026-02-14 16:29:47');
INSERT INTO `tb_message` VALUES (76, 4, 1, 2, '用户点击咨询订单可以将自己的订单发给管理员', 1, 0, '2026-02-14 16:30:00', '2026-02-14 16:29:59');
INSERT INTO `tb_message` VALUES (77, 4, 1, 4, '{\"orderId\":9,\"productName\":\"金毛犬\",\"totalAmount\":2500,\"status\":1,\"createTime\":\"2025-12-14 23:27:42\",\"imgUrl\":\"https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/golden_retriever_1.png\",\"totalCount\":1}', 1, 0, '2026-02-14 16:30:07', '2026-02-14 16:30:07');
INSERT INTO `tb_message` VALUES (78, 4, 1, 2, '用户咨询订单', 1, 0, '2026-02-14 16:30:07', '2026-02-14 16:30:07');
INSERT INTO `tb_message` VALUES (79, 1, 4, 2, '管理员点击查看详情可以跳转到订单管理页面', 1, 0, '2026-02-14 16:30:26', '2026-02-14 16:30:38');
INSERT INTO `tb_message` VALUES (80, 4, 1, 2, '用户也可以选择不发送订单', 1, 0, '2026-02-14 16:30:50', '2026-02-14 16:30:50');
INSERT INTO `tb_message` VALUES (81, 4, 1, 2, '管理员可以退出，用户依旧可以发消息', 1, 0, '2026-02-14 16:31:19', '2026-02-14 16:31:28');
INSERT INTO `tb_message` VALUES (82, 1, 4, 2, '用户也可以退出，管理员也可以发消息，当前用户在线，现在使其下线', 1, 0, '2026-02-14 16:31:59', '2026-02-14 16:32:10');
INSERT INTO `tb_message` VALUES (83, 4, 1, 2, '用户看到了历史消息', 1, 0, '2026-02-14 16:32:16', '2026-02-14 16:32:16');
INSERT INTO `tb_message` VALUES (84, 4, 1, 5, NULL, 1, 0, '2026-02-14 16:36:49', '2026-02-14 16:36:48');
INSERT INTO `tb_message` VALUES (85, 4, 1, 2, '可以发送图片和视频', 1, 0, '2026-02-14 16:36:49', '2026-02-14 16:36:48');
INSERT INTO `tb_message` VALUES (86, 4, 7, 2, '在吗', 1, 0, '2026-02-14 16:48:09', '2026-02-14 16:48:09');
INSERT INTO `tb_message` VALUES (87, 7, 4, 2, '在的亲亲', 0, 0, '2026-02-14 16:48:42', '2026-02-14 16:48:42');
INSERT INTO `tb_message` VALUES (88, 1, 4, 2, '在吗老弟', 1, 0, '2026-02-14 20:16:35', '2026-02-14 20:16:37');
INSERT INTO `tb_message` VALUES (89, 4, 1, 2, 'nnd', 1, 0, '2026-02-14 20:16:59', '2026-02-14 20:16:58');
INSERT INTO `tb_message` VALUES (90, 4, 1, 2, '在吗', 1, 0, '2026-02-20 23:50:00', '2026-02-20 23:50:03');
INSERT INTO `tb_message` VALUES (91, 1, 4, 2, '在的亲亲', 1, 0, '2026-02-20 23:50:09', '2026-02-20 23:50:11');

-- ----------------------------
-- Table structure for tb_message_latest
-- ----------------------------
DROP TABLE IF EXISTS `tb_message_latest`;
CREATE TABLE `tb_message_latest`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '最近消息ID，主键自增',
  `send_user_id` bigint NOT NULL COMMENT '用户ID，与用户表关联',
  `receive_user_id` bigint NULL DEFAULT NULL COMMENT '用户ID，与用户表关联',
  `message_id` bigint NOT NULL COMMENT '消息ID，与消息表关联',
  `delete_flag` tinyint NULL DEFAULT 0 COMMENT '删除标记，0-未删除，1-已删除',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间，默认为插入时时间戳',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间，默认为插入时时间戳，插入时自动更新',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`send_user_id` ASC) USING BTREE,
  INDEX `idx_message_id`(`message_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '最近消息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_message_latest
-- ----------------------------
INSERT INTO `tb_message_latest` VALUES (1, 1, 4, 91, 0, '2026-02-13 23:44:16', '2026-02-20 23:50:09');
INSERT INTO `tb_message_latest` VALUES (3, 2, 4, 24, 0, '2026-02-14 00:35:47', '2026-02-14 13:15:06');
INSERT INTO `tb_message_latest` VALUES (4, 3, 4, 31, 0, '2026-02-14 10:31:16', '2026-02-14 13:15:06');
INSERT INTO `tb_message_latest` VALUES (9, 7, 4, 87, 0, '2026-02-14 16:48:09', '2026-02-14 16:48:42');

-- ----------------------------
-- Table structure for tb_message_media
-- ----------------------------
DROP TABLE IF EXISTS `tb_message_media`;
CREATE TABLE `tb_message_media`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '媒体ID，主键自增',
  `message_id` bigint NOT NULL COMMENT '消息ID，与消息表关联',
  `media_url` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '媒体地址',
  `media_type` tinyint NOT NULL COMMENT '媒体类型，1-图片，2-视频',
  `delete_flag` tinyint NULL DEFAULT 0 COMMENT '删除标记，0-未删除，1-已删除',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间，默认为插入时时间戳',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间，默认为插入时时间戳，插入时自动更新',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '消息媒体表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_message_media
-- ----------------------------
INSERT INTO `tb_message_media` VALUES (1, 3, 'https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/5465b468-3d48-469f-ba9a-67cc485d7471_宠物.png', 1, 0, '2026-02-13 23:44:16', '2026-02-13 23:44:16');
INSERT INTO `tb_message_media` VALUES (2, 6, 'https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/83afd5d4-f3ea-425c-85d6-ae4bc95b117b_default_avatar.png', 1, 0, '2026-02-14 00:07:54', '2026-02-14 00:07:53');
INSERT INTO `tb_message_media` VALUES (3, 8, 'https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/bbbe50fc-b948-4148-9dc8-ff25da2fcbce_default_avatar.png', 1, 0, '2026-02-14 00:09:22', '2026-02-14 00:09:22');
INSERT INTO `tb_message_media` VALUES (4, 12, 'https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/a1ce85e7-883d-46b2-bf87-645a7285ef7d_default_avatar.png', 1, 0, '2026-02-14 00:22:13', '2026-02-14 00:22:12');
INSERT INTO `tb_message_media` VALUES (5, 18, 'https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/7da65c85-3a4a-4084-8625-43b20b7576f9_xw_20251227120318.mp4', 2, 0, '2026-02-14 01:06:15', '2026-02-14 01:06:15');
INSERT INTO `tb_message_media` VALUES (6, 71, 'https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/dbf1d5ba-6f88-4ebc-a820-8820a8517102_xw_20251221204956.mp4', 2, 0, '2026-02-14 16:28:28', '2026-02-14 16:28:27');
INSERT INTO `tb_message_media` VALUES (7, 84, 'https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/2151523c-d52c-48ab-a28f-3cdc3ca0ac23_宠物.png', 1, 0, '2026-02-14 16:36:49', '2026-02-14 16:36:48');

-- ----------------------------
-- Table structure for tb_notify
-- ----------------------------
DROP TABLE IF EXISTS `tb_notify`;
CREATE TABLE `tb_notify`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '通知ID，主键自增',
  `receive_user_id` bigint NOT NULL COMMENT '接收消息的用户ID',
  `title` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '通知标题',
  `content` varchar(2048) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '通知内容',
  `type` tinyint NOT NULL COMMENT '通知类型，1-订单通知，2-审核通知，3-赞评通知，4-禁言与解禁通知',
  `status` tinyint NULL DEFAULT 0 COMMENT '是否已读标记，0-未读，1-已读，默认为0',
  `delete_flag` tinyint NULL DEFAULT 0 COMMENT '删除标记，0-未删除，1-已删除，默认为0',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间，默认为插入时时间戳',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间，默认为插入时时间戳，插入时自动更新',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_receive_user_id`(`receive_user_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_delete_flag`(`delete_flag` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 39 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '通知表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_notify
-- ----------------------------
INSERT INTO `tb_notify` VALUES (1, 1, '订单通知', '您的订单 9 已发货/已成功退款/退款失败，您可以点击具体的订单查看物流状态。', 1, 1, 1, '2026-02-14 21:03:39', '2026-02-14 21:29:37');
INSERT INTO `tb_notify` VALUES (2, 1, '订单通知', '您的订单 50 退款失败，具体原因可以询问客服。', 1, 1, 1, '2026-02-14 21:12:05', '2026-02-14 21:29:26');
INSERT INTO `tb_notify` VALUES (3, 1, '订单通知', '您的订单 50 已成功退款。', 1, 1, 1, '2026-02-14 21:31:35', '2026-02-14 21:31:54');
INSERT INTO `tb_notify` VALUES (4, 1, '订单通知', '您的订单（编号为：49）退款失败，具体原因可以询问客服。', 1, 1, 0, '2026-02-14 21:32:51', '2026-02-14 21:37:16');
INSERT INTO `tb_notify` VALUES (5, 1, '审核通知', '您的帖子《null》通过审核，已对外可见。', 2, 1, 1, '2026-02-14 21:58:44', '2026-02-14 22:00:01');
INSERT INTO `tb_notify` VALUES (6, 1, '审核通知', '您的帖子《测试websocket111》通过审核，已对外可见。', 2, 1, 0, '2026-02-14 22:00:32', '2026-02-14 22:00:53');
INSERT INTO `tb_notify` VALUES (7, 1, '审核通知', '您的评论通过审核，已对外可见。', 2, 1, 0, '2026-02-14 22:05:01', '2026-02-14 22:05:55');
INSERT INTO `tb_notify` VALUES (8, 1, '审核通知', '您的评论通过审核，已对外可见。', 2, 1, 0, '2026-02-14 22:17:01', '2026-02-14 22:17:20');
INSERT INTO `tb_notify` VALUES (9, 1, '审核通知', '您的评论通过审核，已对外可见。', 2, 1, 0, '2026-02-14 22:19:00', '2026-02-14 22:19:20');
INSERT INTO `tb_notify` VALUES (10, 3, '帖子回复通知', '有人回复了你的商品评论：websocket通知测试', 3, 1, 0, '2026-02-14 22:19:37', '2026-02-14 22:21:08');
INSERT INTO `tb_notify` VALUES (11, 3, '审核通知', '您的评论通过审核，已对外可见。', 2, 1, 0, '2026-02-14 22:20:00', '2026-02-14 22:21:08');
INSERT INTO `tb_notify` VALUES (12, 1, '帖子回复通知', '有人回复了你的商品评论：websocket通知测试2', 3, 1, 0, '2026-02-14 22:23:53', '2026-02-14 22:24:02');
INSERT INTO `tb_notify` VALUES (13, 3, '审核通知', '您的评论通过审核，已对外可见。', 2, 1, 0, '2026-02-14 22:24:01', '2026-02-14 22:29:39');
INSERT INTO `tb_notify` VALUES (14, 3, '审核通知', '您的评论通过审核，已对外可见。', 2, 1, 0, '2026-02-14 22:30:01', '2026-02-14 22:30:14');
INSERT INTO `tb_notify` VALUES (15, 3, '审核通知', '您的评论通过审核，已对外可见。', 3, 1, 0, '2026-02-14 22:30:01', '2026-02-14 22:30:12');
INSERT INTO `tb_notify` VALUES (16, 3, '审核通知', '您的评论通过审核，已对外可见。', 2, 1, 0, '2026-02-14 22:32:01', '2026-02-14 22:33:24');
INSERT INTO `tb_notify` VALUES (17, 1, '帖子回复通知', '有人回复了你的商品评论：发一条评论测试评论通知', 3, 1, 0, '2026-02-14 22:32:01', '2026-02-14 22:33:20');
INSERT INTO `tb_notify` VALUES (18, 1, '审核通知', '您的评论通过审核，已对外可见。', 2, 1, 0, '2026-02-14 22:38:01', '2026-02-14 22:39:11');
INSERT INTO `tb_notify` VALUES (19, 1, '帖子回复通知', '有人回复了你的帖子评论：测试websocket评论审核', 3, 1, 0, '2026-02-14 22:38:01', '2026-02-14 22:39:10');
INSERT INTO `tb_notify` VALUES (20, 1, '审核通知', '您的评论通过审核，已对外可见。', 2, 1, 0, '2026-02-14 22:42:01', '2026-02-14 22:42:08');
INSERT INTO `tb_notify` VALUES (21, 3, '审核通知', '您的评论通过审核，已对外可见。', 2, 1, 0, '2026-02-14 22:43:00', '2026-02-14 22:43:06');
INSERT INTO `tb_notify` VALUES (22, 1, '帖子回复通知', '有人回复了你的帖子评论：测试websocket评论审核', 3, 1, 0, '2026-02-14 22:43:00', '2026-02-14 22:43:13');
INSERT INTO `tb_notify` VALUES (23, 1, '帖子点赞通知', 'user3点赞了你的帖子《测试websocket111》', 3, 1, 0, '2026-02-14 23:00:59', '2026-02-14 23:01:07');
INSERT INTO `tb_notify` VALUES (24, 1, '帖子点赞通知', 'user3点赞了你的帖子《测试websocket111》', 3, 1, 0, '2026-02-14 23:05:19', '2026-02-14 23:05:22');
INSERT INTO `tb_notify` VALUES (25, 3, '审核通知', '您的评论通过审核，已对外可见。', 2, 1, 0, '2026-02-14 23:06:01', '2026-02-14 23:06:19');
INSERT INTO `tb_notify` VALUES (26, 1, '帖子回复通知', 'user3回复了你的商品评论：发一条评论测试评论通知', 3, 1, 0, '2026-02-14 23:06:01', '2026-02-14 23:06:16');
INSERT INTO `tb_notify` VALUES (27, 1, '帖子点赞通知', 'user3点赞了你的帖子《测试websocket111》', 3, 1, 0, '2026-02-14 23:30:37', '2026-02-14 23:30:42');
INSERT INTO `tb_notify` VALUES (28, 3, '帖子点赞通知', 'user2点赞了你的帖子《你，好不好？》', 3, 1, 0, '2026-02-19 20:51:21', '2026-02-21 17:16:09');
INSERT INTO `tb_notify` VALUES (29, 2, '帖子点赞通知', 'user1点赞了你的帖子《流浪猫救助记录》', 3, 0, 0, '2026-02-21 00:01:14', '2026-02-21 00:01:13');
INSERT INTO `tb_notify` VALUES (30, 1, '禁言通知', '您的账号因违反规则，已被禁言。如有疑问，请咨询客服', 4, 1, 0, '2026-02-21 15:50:46', '2026-02-21 15:52:26');
INSERT INTO `tb_notify` VALUES (31, 1, '禁言通知', '您的账号已解除禁言，请继续遵守社区规则。如有疑问，请咨询客服', 4, 1, 0, '2026-02-21 15:54:16', '2026-02-21 15:54:19');
INSERT INTO `tb_notify` VALUES (32, 1, '审核通知', '您的帖子《测试RabbitMQ审核》通过审核，已对外可见。', 2, 1, 0, '2026-02-21 16:50:31', '2026-02-21 16:50:55');
INSERT INTO `tb_notify` VALUES (33, 1, '审核通知', '您的帖子《测试RabbitMQ失败审核》未通过审核，建议修改后重新提交。', 2, 1, 0, '2026-02-21 16:51:36', '2026-02-21 16:51:50');
INSERT INTO `tb_notify` VALUES (34, 1, '帖子回复通知', 'user3回复了你的商品评论：发一条评论测试评论通知', 3, 1, 0, '2026-02-21 17:23:51', '2026-02-21 17:24:01');
INSERT INTO `tb_notify` VALUES (35, 3, '审核通知', '您的评论通过审核，已对外可见。', 2, 1, 0, '2026-02-21 17:23:51', '2026-02-21 17:24:02');
INSERT INTO `tb_notify` VALUES (36, 3, '审核通知', '您的评论未通过审核，建议修改后重新提交。', 2, 1, 0, '2026-02-21 17:24:31', '2026-02-21 17:24:34');
INSERT INTO `tb_notify` VALUES (37, 1, '审核通知', '您的评论通过审核，已对外可见。', 2, 1, 0, '2026-02-21 17:24:48', '2026-02-21 17:24:53');
INSERT INTO `tb_notify` VALUES (38, 1, '审核通知', '您的帖子《测试RabbitMQ审核22》通过审核，已对外可见。', 2, 1, 0, '2026-02-24 11:20:15', '2026-02-24 11:20:20');

-- ----------------------------
-- Table structure for tb_order
-- ----------------------------
DROP TABLE IF EXISTS `tb_order`;
CREATE TABLE `tb_order`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '订单ID，主键自增',
  `user_id` bigint NOT NULL COMMENT '用户ID，不为空',
  `product_id` bigint NOT NULL COMMENT '商品ID，不为空',
  `phone` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '额外存储用户手机号（默认从用户表拿到）',
  `receipt_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '收货人名称',
  `receipt_id` bigint NOT NULL COMMENT '收货地址ID，与地址表关联（默认从用户表拿到）',
  `total_count` bigint NOT NULL COMMENT '商品数量',
  `total_price` decimal(10, 2) NOT NULL COMMENT '商品总价',
  `status` tinyint NULL DEFAULT 1 COMMENT '订单状态，1-待支付，2-待发货，3-已发货，4-待签收，5-已收货，6-订单取消',
  `refund_flag` tinyint NULL DEFAULT 0 COMMENT '退款标记，默认为0，0-未退款，1-已退款',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间，默认为插入时时间戳',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间，默认为插入时时间戳，插入时自动更新',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_product_id`(`product_id` ASC) USING BTREE,
  INDEX `idx_receipt_id`(`receipt_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 64 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '订单表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_order
-- ----------------------------
INSERT INTO `tb_order` VALUES (9, 1, 1, '15942657845', 'user1', 54, 1, 2500.00, 4, 0, '2025-12-14 23:27:42', '2026-02-14 21:06:38');
INSERT INTO `tb_order` VALUES (10, 1, 1, '12345678901', 'user1', 1, 1, 2500.00, 1, 0, '2025-12-14 23:33:40', '2026-01-04 19:17:17');
INSERT INTO `tb_order` VALUES (11, 1, 1, '12345678901', 'user1', 1, 1, 2500.00, 1, 0, '2025-12-14 23:39:51', '2026-01-04 19:17:17');
INSERT INTO `tb_order` VALUES (12, 1, 2, '12345678901', 'user1', 1, 1, 1800.00, 1, 0, '2025-12-14 23:54:12', '2026-01-04 19:17:18');
INSERT INTO `tb_order` VALUES (13, 1, 2, '15924562301', 'user1', 1, 2, 3600.00, 1, 0, '2025-12-15 12:53:21', '2026-01-04 19:17:18');
INSERT INTO `tb_order` VALUES (14, 1, 3, '18152457569', 'user1', 3, 1, 35.00, 1, 0, '2025-12-15 12:57:46', '2026-01-04 19:17:19');
INSERT INTO `tb_order` VALUES (15, 1, 3, '12345678901', 'user1', 4, 1, 35.00, 1, 0, '2025-12-15 12:59:08', '2026-01-04 19:17:20');
INSERT INTO `tb_order` VALUES (16, 1, 3, '12345678901', 'user1', 1, 3, 105.00, 6, 0, '2025-12-15 13:04:47', '2026-01-04 20:06:09');
INSERT INTO `tb_order` VALUES (17, 1, 3, '15926451234', 'user1', 6, 2, 70.00, 6, 0, '2025-12-15 17:50:54', '2026-01-04 19:17:51');
INSERT INTO `tb_order` VALUES (18, 1, 3, '15926451234', 'user1', 6, 2, 70.00, 6, 0, '2025-12-15 17:54:20', '2026-01-04 19:17:54');
INSERT INTO `tb_order` VALUES (19, 1, 3, '15926451234', 'user1', 6, 3, 105.00, 6, 0, '2025-12-15 17:57:24', '2026-01-04 19:17:57');
INSERT INTO `tb_order` VALUES (20, 1, 3, '15926451234', 'user1', 6, 1, 35.00, 6, 0, '2025-12-15 18:16:15', '2026-01-04 19:17:58');
INSERT INTO `tb_order` VALUES (21, 1, 3, '15926451234', 'user1', 6, 1, 35.00, 6, 0, '2025-12-15 18:17:29', '2026-01-04 19:18:01');
INSERT INTO `tb_order` VALUES (22, 1, 3, '15926451234', 'user1', 6, 1, 35.00, 5, 0, '2025-12-15 18:17:43', '2026-01-05 21:55:34');
INSERT INTO `tb_order` VALUES (23, 1, 3, '15926451234', 'user1', 9, 1, 35.00, 6, 0, '2025-12-17 15:43:08', '2026-01-04 19:18:02');
INSERT INTO `tb_order` VALUES (24, 1, 5, '15926451234', 'user1', 9, 1, 198.00, 6, 0, '2025-12-17 15:43:08', '2026-01-04 19:18:03');
INSERT INTO `tb_order` VALUES (25, 1, 3, '15926451234', 'user1', 9, 1, 35.00, 6, 0, '2025-12-17 16:35:07', '2026-01-04 19:18:04');
INSERT INTO `tb_order` VALUES (26, 1, 5, '15926451234', 'user1', 9, 1, 198.00, 6, 0, '2025-12-17 16:35:07', '2026-01-04 19:18:06');
INSERT INTO `tb_order` VALUES (27, 1, 3, '15926451234', 'user1', 9, 1, 35.00, 6, 0, '2025-12-17 16:44:49', '2026-01-04 19:18:07');
INSERT INTO `tb_order` VALUES (28, 1, 3, '15926451234', 'user1', 9, 1, 35.00, 6, 0, '2025-12-17 16:49:25', '2026-01-04 19:18:08');
INSERT INTO `tb_order` VALUES (29, 1, 3, '15926451234', 'user1', 9, 1, 35.00, 6, 0, '2025-12-17 16:49:54', '2026-01-04 19:18:10');
INSERT INTO `tb_order` VALUES (30, 1, 3, '15926451234', 'user1', 9, 1, 35.00, 6, 0, '2025-12-17 16:52:28', '2026-01-04 19:18:11');
INSERT INTO `tb_order` VALUES (31, 1, 3, '15926451234', 'user1', 9, 1, 35.00, 6, 0, '2025-12-17 16:54:31', '2026-01-04 19:18:12');
INSERT INTO `tb_order` VALUES (32, 1, 3, '15926451234', 'user1', 9, 1, 35.00, 6, 0, '2025-12-17 16:57:45', '2026-01-04 19:18:14');
INSERT INTO `tb_order` VALUES (33, 1, 3, '15926451234', 'user1', 9, 1, 35.00, 6, 0, '2025-12-17 17:03:52', '2026-01-04 19:18:15');
INSERT INTO `tb_order` VALUES (34, 1, 3, '15926451234', 'user1', 9, 1, 35.00, 6, 0, '2025-12-17 17:05:46', '2026-01-04 19:18:16');
INSERT INTO `tb_order` VALUES (35, 1, 3, '15926451234', 'user1', 9, 1, 35.00, 6, 0, '2025-12-17 17:07:10', '2026-01-04 19:18:17');
INSERT INTO `tb_order` VALUES (36, 1, 3, '15926451234', 'user1', 9, 1, 35.00, 6, 0, '2025-12-17 17:07:24', '2026-01-04 19:18:19');
INSERT INTO `tb_order` VALUES (37, 1, 3, '15926451234', 'user1', 9, 1, 35.00, 6, 0, '2025-12-17 17:09:35', '2026-01-04 19:18:20');
INSERT INTO `tb_order` VALUES (38, 1, 3, '15926451234', 'user1', 9, 1, 35.00, 6, 0, '2025-12-17 17:10:52', '2026-01-04 19:18:22');
INSERT INTO `tb_order` VALUES (39, 1, 3, '15926451234', 'user1', 9, 1, 35.00, 6, 0, '2025-12-17 17:11:45', '2026-01-04 19:18:23');
INSERT INTO `tb_order` VALUES (40, 1, 3, '15926451234', 'user1', 9, 1, 35.00, 6, 0, '2025-12-17 20:41:24', '2026-01-04 19:18:23');
INSERT INTO `tb_order` VALUES (41, 1, 5, '15926451234', 'user1', 9, 1, 198.00, 6, 0, '2025-12-17 20:41:24', '2026-01-04 19:18:24');
INSERT INTO `tb_order` VALUES (42, 1, 3, '15926451234', 'user1', 11, 2, 70.00, 6, 0, '2025-12-17 20:56:03', '2026-01-04 19:18:26');
INSERT INTO `tb_order` VALUES (43, 1, 5, '15926451234', 'user1', 11, 1, 198.00, 6, 0, '2025-12-17 20:56:03', '2026-01-04 19:18:27');
INSERT INTO `tb_order` VALUES (45, 1, 7, '15924567856', 'user1', 11, 1, 3500.00, 6, 0, '2026-01-05 20:39:41', '2026-01-05 21:58:16');
INSERT INTO `tb_order` VALUES (46, 1, 7, '15924567856', 'user1', 56, 1, 3500.00, 4, 0, '2026-01-05 20:42:37', '2026-01-05 20:43:35');
INSERT INTO `tb_order` VALUES (47, 1, 14, '15924567856', 'user1', 57, 1, 211.14, 4, 0, '2026-01-05 20:44:32', '2026-01-05 21:50:36');
INSERT INTO `tb_order` VALUES (48, 1, 14, '15924567856', 'user1', 58, 1, 211.14, 6, 0, '2026-01-05 22:01:06', '2026-01-05 22:04:25');
INSERT INTO `tb_order` VALUES (49, 1, 9, '15924567856', 'user1', 59, 1, 3000.00, 4, 0, '2026-01-05 23:12:48', '2026-02-14 21:32:51');
INSERT INTO `tb_order` VALUES (50, 1, 14, '15924567856', 'user1', 60, 1, 211.14, 6, 0, '2026-01-05 23:14:05', '2026-02-14 21:31:35');
INSERT INTO `tb_order` VALUES (51, 2, 1, '13800000002', 'user2', 1, 1, 500.00, 5, 0, '2026-02-19 19:54:43', '2026-02-19 19:54:43');
INSERT INTO `tb_order` VALUES (52, 2, 2, '13800000002', 'user2', 1, 2, 300.00, 5, 0, '2026-02-19 19:54:43', '2026-02-19 19:54:43');
INSERT INTO `tb_order` VALUES (53, 2, 4, '13800000002', 'user2', 1, 3, 960.00, 5, 0, '2026-02-19 19:54:43', '2026-02-19 19:54:43');
INSERT INTO `tb_order` VALUES (54, 2, 14, '13800000002', 'user2', 1, 1, 200.00, 5, 0, '2026-02-19 19:54:43', '2026-02-19 19:54:43');
INSERT INTO `tb_order` VALUES (55, 3, 1, '13800000003', 'user3', 1, 1, 500.00, 5, 0, '2026-02-19 19:54:43', '2026-02-19 19:54:43');
INSERT INTO `tb_order` VALUES (56, 3, 3, '13800000003', 'user3', 1, 2, 70.00, 5, 0, '2026-02-19 19:54:43', '2026-02-19 19:54:43');
INSERT INTO `tb_order` VALUES (57, 3, 5, '13800000003', 'user3', 1, 1, 198.00, 5, 0, '2026-02-19 19:54:43', '2026-02-19 19:54:43');
INSERT INTO `tb_order` VALUES (58, 3, 8, '13800000003', 'user3', 1, 1, 800.00, 5, 0, '2026-02-19 19:54:43', '2026-02-19 19:54:43');
INSERT INTO `tb_order` VALUES (59, 3, 15, '13800000003', 'user3', 1, 1, 150.00, 5, 0, '2026-02-19 19:54:43', '2026-02-19 19:54:43');
INSERT INTO `tb_order` VALUES (60, 7, 2, '13800000007', 'epsda', 1, 1, 300.00, 5, 0, '2026-02-19 19:54:43', '2026-02-19 19:54:43');
INSERT INTO `tb_order` VALUES (61, 7, 4, '13800000007', 'epsda', 1, 2, 640.00, 5, 0, '2026-02-19 19:54:43', '2026-02-19 19:54:43');
INSERT INTO `tb_order` VALUES (62, 7, 7, '13800000007', 'epsda', 1, 1, 1200.00, 5, 0, '2026-02-19 19:54:43', '2026-02-19 19:54:43');
INSERT INTO `tb_order` VALUES (63, 7, 9, '13800000007', 'epsda', 1, 1, 900.00, 5, 0, '2026-02-19 19:54:43', '2026-02-19 19:54:43');

-- ----------------------------
-- Table structure for tb_order_logistics
-- ----------------------------
DROP TABLE IF EXISTS `tb_order_logistics`;
CREATE TABLE `tb_order_logistics`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '订单物流主键自增ID',
  `order_id` bigint NOT NULL COMMENT '订单ID',
  `transport_type` tinyint NOT NULL COMMENT '物流类型，1-空运，2-陆运',
  `origin_lat` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '起点纬度值',
  `origin_lng` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '起点经度值',
  `dest_lat` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '终点纬度值',
  `dest_lng` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '终点经度值',
  `curr_lat` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '当前位置纬度值',
  `curr_lng` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '当前位置经度值',
  `delete_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标记，0未删除、1已删除',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 16 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '订单物流表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_order_logistics
-- ----------------------------
INSERT INTO `tb_order_logistics` VALUES (6, 22, 1, '39.96548984110075', '116.3054340544974', '30.48645994791441', '114.31530997175149', '30.48645994791441', '114.31530997175149', 1, '2026-01-05 20:34:42', '2026-01-05 21:55:33');
INSERT INTO `tb_order_logistics` VALUES (7, 45, 1, '30.57258426535204', '114.32253531245989', '39.99700408060357', '116.32033999714666', '39.99700408060357', '116.32033999714666', 1, '2026-01-05 20:40:06', '2026-01-05 21:58:15');
INSERT INTO `tb_order_logistics` VALUES (8, 46, 1, '30.57258426535204', '114.32253531245989', '30.960127594633577', '121.51684504515943', '30.960127594633576', '121.51684504515943', 0, '2026-01-05 20:43:02', '2026-01-05 20:43:35');
INSERT INTO `tb_order_logistics` VALUES (11, 47, 2, '40.25611260286408', '116.13903580454277', '30.960127594633577', '121.51684504515943', '30.95629641652758', '121.51421249477832', 0, '2026-01-05 21:44:30', '2026-01-05 21:50:36');
INSERT INTO `tb_order_logistics` VALUES (12, 48, 2, '40.25611260286408', '116.13903580454277', '40.55022069705901', '81.30399515530668', '41.470758636945185', '103.53192497810296', 1, '2026-01-05 22:01:43', '2026-01-05 22:04:25');
INSERT INTO `tb_order_logistics` VALUES (13, 49, 1, '31.95678598585978', '118.84102828697871', '35.18081163178793', '118.84154077027218', '35.18081163178793', '118.84154077027218', 0, '2026-01-05 23:13:00', '2026-01-05 23:13:34');
INSERT INTO `tb_order_logistics` VALUES (14, 50, 2, '40.25611260286408', '116.13903580454277', '35.18081163178793', '118.84154077027218', '35.180552', '118.841541', 1, '2026-01-05 23:14:19', '2026-02-14 21:31:34');
INSERT INTO `tb_order_logistics` VALUES (15, 9, 1, '39.96548984110075', '116.3054340544974', '30.74135008408465', '115.6874216026259', '30.74135008408465', '115.6874216026259', 0, '2026-02-14 21:03:38', '2026-02-14 21:06:38');

-- ----------------------------
-- Table structure for tb_pet_sub
-- ----------------------------
DROP TABLE IF EXISTS `tb_pet_sub`;
CREATE TABLE `tb_pet_sub`  (
  `sub_id` bigint NOT NULL AUTO_INCREMENT COMMENT '宠物ID，主键自增',
  `product_id` bigint NOT NULL COMMENT '商品ID，不为空且唯一，与商品总表关联',
  `health_status` tinyint NULL DEFAULT 1 COMMENT '宠物健康状态，1-健康，2-良好，3-治疗中',
  `train_note` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '驯养须知',
  `raise_note` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '领养须知',
  `vaccine_flag` tinyint NULL DEFAULT 1 COMMENT '是否接种疫苗，0-未接种，1-已接种',
  `sub_create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间，默认为插入时时间戳',
  `sub_update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间，默认为插入时时间戳，插入时自动更新',
  PRIMARY KEY (`sub_id`) USING BTREE,
  UNIQUE INDEX `product_id`(`product_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 32 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '宠物子表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_pet_sub
-- ----------------------------
INSERT INTO `tb_pet_sub` VALUES (1, 1, 1, '易于训练，适合家庭饲养', '每日需遛弯2次，注意关节保护', 1, '2025-12-08 18:15:11', '2025-12-08 18:15:11');
INSERT INTO `tb_pet_sub` VALUES (2, 2, 2, '性格温顺，易相处', '定期梳毛，控制饮食防止肥胖', 1, '2025-12-08 18:15:11', '2025-12-14 08:59:32');
INSERT INTO `tb_pet_sub` VALUES (3, 3, 3, '夜间活动，易受惊', '提供充足木屑，定期清理笼子', 1, '2025-12-08 18:15:11', '2025-12-14 08:57:53');
INSERT INTO `tb_pet_sub` VALUES (5, 7, 1, '边牧需要充足的运动与心理刺激，采用积极强化（奖励＋正面引导）训练基础服从和社交技能，并保持一致、耐心的日常规则与例行活动，让它在体力与智力需求都得到满足，否则会因精力过剩而出现破坏性行为。', '领养边牧前应做好充分准备，包括了解该犬需大量运动、稳定的家庭环境和耐心训练，同时在带回新家初期设定清晰生活结构与安全空间，帮助它顺利适应新环境并减少应激行为。', 1, '2025-12-30 21:28:25', '2025-12-30 21:28:25');
INSERT INTO `tb_pet_sub` VALUES (6, 8, 1, '柯基需要每日适量运动与一致性训练来消耗能量、维持健康体重，同时定期梳理皮毛、注意背部与关节保护，否则可能因精力过剩和体重增加而出现行为或健康问题。', '领养柯基前应准备好稳定的家庭环境、合适的运动与护理计划，并优先通过负责任的救助机构或良好评价的繁育者获取犬只，以保证健康与性格适合家庭生活。', 1, '2025-12-30 21:54:50', '2025-12-30 21:54:50');
INSERT INTO `tb_pet_sub` VALUES (7, 9, 1, '波斯猫拥有长密毛和扁平脸结构，需要每天定期梳理防打结、清洁眼部和保持耳朵健康，并做好日常护理（如刷牙、修剪指甲等）以预防皮毛、眼泪和健康问题。', '在领养波斯猫前应准备好足够的时间、耐心和专用护理工具，了解其对日常梳毛、眼部清洁和室内环境的高要求，并优先通过信誉良好的救助机构或繁育者获取，以保证健康与适应性。', 1, '2025-12-30 22:03:26', '2025-12-30 22:03:26');
INSERT INTO `tb_pet_sub` VALUES (8, 10, 1, '俄罗斯蓝猫需要每周定期梳理短而密的毛发、控制饮食防止肥胖，并提供安静、丰富的互动和玩具刺激其智力与运动需求，保持健康与愉悦。', '领养俄罗斯蓝猫前应准备好稳定安静的生活环境、适当的猫砂、玩具、定量饮食计划和定期健康检查安排，以帮助它顺利适应家庭并维持长期健康。', 1, '2025-12-30 22:21:30', '2025-12-30 22:21:30');
INSERT INTO `tb_pet_sub` VALUES (9, 11, 1, '迷你猪虽然可爱，但需要足够空间、定期运动、环境丰富和适当训练来满足其好奇心与智能，否则可能因无聊而破坏家具或过度饮食导致肥胖等健康问题。', '在领养迷你猪前应确认有合法饲养条件和足够时间照料，并从可信的救助机构或负责任的来源获取，同时了解它们可能长大且需要长期陪伴与护理的实际需求。', 1, '2025-12-30 22:32:17', '2025-12-30 22:32:16');
INSERT INTO `tb_pet_sub` VALUES (31, 35, 1, 'test', 'test', 1, '2026-01-15 11:42:15', '2026-01-15 11:42:14');

-- ----------------------------
-- Table structure for tb_pet_supply_sub
-- ----------------------------
DROP TABLE IF EXISTS `tb_pet_supply_sub`;
CREATE TABLE `tb_pet_supply_sub`  (
  `sub_id` bigint NOT NULL AUTO_INCREMENT COMMENT '宠物用品ID，主键自增',
  `product_id` bigint NOT NULL COMMENT '商品ID，不为空且唯一，与商品总表关联',
  `brand` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '宠物用品品牌',
  `fit_age` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '适用年龄段',
  `fit_variety` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '适用品种',
  `manufacture_date` datetime NOT NULL COMMENT '生产日期',
  `guarantee_date` datetime NOT NULL COMMENT '保质期',
  `company` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '生产公司',
  `sub_create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间，默认为插入时时间戳',
  `sub_update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间，默认为插入时时间戳，插入时自动更新',
  PRIMARY KEY (`sub_id`) USING BTREE,
  UNIQUE INDEX `product_id`(`product_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 23 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '宠物用品子表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_pet_supply_sub
-- ----------------------------
INSERT INTO `tb_pet_supply_sub` VALUES (1, 4, '皇家', '12个月以上', '大型犬', '2025-01-01 00:00:00', '2027-01-01 00:00:00', '玛氏公司', '2025-12-08 18:15:16', '2025-12-08 18:15:16');
INSERT INTO `tb_pet_supply_sub` VALUES (2, 5, '渴望', '全龄', '所有猫咪', '2025-03-01 00:00:00', '2027-03-01 00:00:00', '加拿大冠军宠物食品', '2025-12-08 18:15:16', '2025-12-08 18:15:16');
INSERT INTO `tb_pet_supply_sub` VALUES (4, 14, 'Rabbitgoo Calming', '全年龄', '全品种', '2025-12-30 00:00:00', '2028-12-30 00:00:00', 'Rabbitgoo Calming', '2025-12-30 22:45:53', '2025-12-30 22:45:53');
INSERT INTO `tb_pet_supply_sub` VALUES (5, 15, 'Scruffs Chester', '全年龄', '中小型宠物', '2025-12-30 00:00:00', '2028-12-31 00:00:00', 'Scruffs Chester', '2025-12-30 22:57:44', '2025-12-30 22:57:43');
INSERT INTO `tb_pet_supply_sub` VALUES (7, 17, 'The KONG', '全年龄', '中大型犬', '2025-12-30 00:00:00', '2034-12-31 00:00:00', 'The KONG Company', '2025-12-30 23:12:44', '2025-12-30 23:12:43');
INSERT INTO `tb_pet_supply_sub` VALUES (22, 36, '1', '1', '1', '2026-02-10 00:00:00', '2026-02-11 00:00:00', '1', '2026-02-10 20:18:23', '2026-02-10 20:18:22');

-- ----------------------------
-- Table structure for tb_post
-- ----------------------------
DROP TABLE IF EXISTS `tb_post`;
CREATE TABLE `tb_post`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '帖子ID，主键自增',
  `user_id` bigint NOT NULL COMMENT '发帖人ID，不为空',
  `column_id` bigint NOT NULL COMMENT '栏目ID，不为空',
  `title` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '帖子标题（目前不做重复校验）',
  `content` varchar(2560) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '帖子内容（当是视频贴时可以为空）',
  `status` tinyint NULL DEFAULT 1 COMMENT '帖子状态，1-草稿，2-审核中，3-审核成功，4-审核失败，默认为1',
  `like_count` bigint NULL DEFAULT 0 COMMENT '点赞数量，默认为0',
  `reject_count` bigint NULL DEFAULT 0 COMMENT '反对数量，默认为0',
  `delete_flag` tinyint NULL DEFAULT 0 COMMENT '删除标记，0-未删除，1-已删除',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间，默认为插入时时间戳',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间，默认为插入时时间戳，插入时自动更新',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_column_id`(`column_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 50 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '帖子表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_post
-- ----------------------------
INSERT INTO `tb_post` VALUES (1, 1, 1, '带狗狗去海边', '分享一次和柯基去海边的日常，附上行前准备清单和注意事项。', 3, 1, 0, 0, '2025-12-18 12:21:06', '2026-02-19 20:51:34');
INSERT INTO `tb_post` VALUES (2, 2, 2, '流浪猫救助记录', '记录一只受伤橘猫的救助过程，包括消毒、喂药和寻找领养。', 3, 0, 0, 0, '2025-12-18 12:21:06', '2026-02-21 00:13:00');
INSERT INTO `tb_post` VALUES (3, 3, 3, '猫咪驱虫问答', '整理最常见的内外驱虫问题，附上兽医朋友的专业解释。', 3, 1, 0, 0, '2025-12-18 12:21:06', '2026-02-19 20:39:47');
INSERT INTO `tb_post` VALUES (7, 3, 3, '111', '<p>111</p><blockquote><p>测试</p></blockquote><p></p>', 4, 0, 0, 1, '2025-12-20 16:31:42', '2026-01-10 20:21:05');
INSERT INTO `tb_post` VALUES (8, 3, 3, '测试是否正确插入帖子数据', '<p>测试</p>', 3, 0, 0, 1, '2025-12-20 16:33:51', '2026-01-10 20:21:28');
INSERT INTO `tb_post` VALUES (9, 3, 3, '测试图文', '<h2>二级标题</h2><h3>三级标题</h3><p><s>内容</s></p><ul><li><p>1</p></li><li><p>2</p></li><li><p>3</p></li></ul><ol><li><p>序号1</p></li><li><p>序号2</p></li></ol><p></p>', 4, 0, 0, 1, '2025-12-20 16:45:40', '2026-01-10 20:21:10');
INSERT INTO `tb_post` VALUES (10, 3, 3, '测试纯文字生成图片', '<p>内容</p>', 3, 0, 0, 1, '2025-12-20 16:46:24', '2026-01-10 20:29:20');
INSERT INTO `tb_post` VALUES (12, 3, 3, '第六版：清除帖子内容，修改板式', '<p>清除帖子内容，修改板式</p>', 1, 0, 0, 1, '2025-12-20 16:52:04', '2026-01-10 20:29:20');
INSERT INTO `tb_post` VALUES (13, 3, 3, '第七版：修改板式（->纯文字）', '<p>第六版修改失败</p>', 3, 0, 0, 1, '2025-12-20 23:35:39', '2026-01-10 20:29:20');
INSERT INTO `tb_post` VALUES (14, 3, 3, '新增帖子，测试删除', '<p>新增帖子，测试删除</p>', 2, 0, 0, 1, '2025-12-21 21:18:38', '2025-12-21 21:18:50');
INSERT INTO `tb_post` VALUES (15, 3, 1, '你，好不好？', '<p>是不是还那么爱迟到<br>熬夜工作又睡不好<br>等你完成你的目标<br>要戒掉逞强的嗜好<br>都怪我<br>把自尊放太高没有把你照顾好<br>骄傲是脆弱的外表<br>最怕我的心你不要<br><br>能不能继续对我哭对我笑对我好<br>继续让我为你想为你疯陪你老<br>你好不好好想知道<br>别急着把回忆都丢掉<br><br>我只需要你在身边陪我吵陪我闹<br>用好的我把过去坏的我都换掉<br>好想听到你坚决说爱我<br>可惜回不去那一秒<br>你，好不好？<br><br>天知道我快要受不了<br>后悔钻进心里烧<br>拥抱再多一次就好<br>你要的我都做得到<br><br>能不能继续对我哭对我笑对我好<br>继续让我为你想为你疯陪你老<br>给你的好还要不要<br>答案我却不敢揭晓<br><br>我只需要你在身边陪我吵陪我闹<br>别用离开教我失去的人最重要<br>别说你曾经爱过我<br>让我们回到那一秒<br>你，好不好？<br><br>能不能继续对我哭对我笑对我好<br>继续让我为你想为你疯陪你老<br>你好不好我好想知道<br>别急着把我的爱丢掉<br><br>我只需要你在身边陪我吵陪我闹<br>别用离开教我失去的人最重要<br>别说你曾经爱过我<br>让我们回到那一秒<br>你，好不好？</p>', 3, 3, 0, 0, '2025-12-23 10:10:46', '2026-02-19 20:51:21');
INSERT INTO `tb_post` VALUES (16, 3, 1, '颜人中《我只能离开》：忘不掉的，是先离开的', '<p>BGM：我只能离开<br>歌手：颜人中<br>词曲：余竑龙</p>', 3, 2, 0, 0, '2025-12-26 20:54:15', '2026-02-19 20:52:23');
INSERT INTO `tb_post` VALUES (17, 1, 3, '11', '<p>11</p>', 2, 0, 0, 1, '2026-01-07 17:24:26', '2026-01-07 17:25:15');
INSERT INTO `tb_post` VALUES (18, 1, 4, '11', '<p>11</p>', 2, 0, 0, 1, '2026-01-07 19:01:44', '2026-01-07 19:02:00');
INSERT INTO `tb_post` VALUES (19, 4, 1, '管理员测试帖子', '<p>管理员测试帖子</p>', 2, 0, 0, 1, '2026-01-10 10:28:16', '2026-01-10 10:30:10');
INSERT INTO `tb_post` VALUES (20, 4, 2, '管理员测试帖子2', '<p>管理员测试帖子2</p>', 2, 0, 0, 1, '2026-01-10 10:28:50', '2026-01-10 10:33:05');
INSERT INTO `tb_post` VALUES (21, 4, 2, '管理员测试帖子3', '<p>管理员测试帖子3</p>', 2, 0, 0, 1, '2026-01-10 10:29:39', '2026-01-10 10:32:04');
INSERT INTO `tb_post` VALUES (22, 4, 2, '管理员测试帖子4', '<p>管理员测试帖子4</p>', 2, 0, 0, 1, '2026-01-10 10:33:30', '2026-01-10 10:37:59');
INSERT INTO `tb_post` VALUES (23, 4, 2, '管理员测试帖子5', '<p>管理员测试帖子5</p>', 2, 0, 0, 1, '2026-01-10 10:33:42', '2026-01-10 10:34:11');
INSERT INTO `tb_post` VALUES (24, 4, 3, '管理员测试帖子6', '<p>管理员测试帖子6</p>', 2, 0, 0, 1, '2026-01-10 10:36:30', '2026-01-10 10:37:59');
INSERT INTO `tb_post` VALUES (25, 4, 2, '管理员测试帖子2', '<p>管理员测试帖子2（重复标题测试）</p>', 2, 0, 0, 1, '2026-01-10 10:40:34', '2026-01-10 10:40:48');
INSERT INTO `tb_post` VALUES (26, 4, 1, '测试帖子审核', '<p>卧槽尼玛</p>', 3, 0, 0, 1, '2026-01-10 19:48:00', '2026-01-10 19:59:29');
INSERT INTO `tb_post` VALUES (27, 4, 2, '测试未分词审核', '<p>卧槽</p>', 3, 0, 0, 1, '2026-01-10 19:49:18', '2026-01-10 19:51:11');
INSERT INTO `tb_post` VALUES (28, 4, 2, '测试未分词的帖子', '<p>卧槽</p>', 4, 0, 0, 1, '2026-01-10 19:51:25', '2026-01-10 20:00:50');
INSERT INTO `tb_post` VALUES (29, 4, 2, '测试已经分词的帖子', '<p>卧槽尼玛</p>', 4, 0, 0, 1, '2026-01-10 19:59:45', '2026-01-10 20:00:52');
INSERT INTO `tb_post` VALUES (30, 4, 1, '卧槽', '<p>卧槽</p>', 4, 0, 0, 1, '2026-01-10 21:36:15', '2026-01-10 21:37:35');
INSERT INTO `tb_post` VALUES (31, 4, 1, '测试', '<p>卧槽</p>', 4, 0, 0, 1, '2026-01-10 21:37:17', '2026-01-10 21:38:07');
INSERT INTO `tb_post` VALUES (32, 4, 2, '卧槽', '<p>测试</p>', 4, 0, 0, 1, '2026-01-10 21:38:27', '2026-01-10 21:39:05');
INSERT INTO `tb_post` VALUES (33, 4, 2, '测试删除敏感词', '<p>盘古开天</p>', 3, 0, 0, 1, '2026-01-11 18:36:34', '2026-01-11 20:05:26');
INSERT INTO `tb_post` VALUES (34, 4, 2, '测试', '<p>mdmlgb</p>', 3, 0, 0, 1, '2026-01-11 20:06:07', '2026-01-11 20:07:44');
INSERT INTO `tb_post` VALUES (35, 4, 2, '11', '<p>11</p>', 3, 0, 0, 1, '2026-01-13 22:10:47', '2026-01-13 22:11:12');
INSERT INTO `tb_post` VALUES (36, 1, 2, '111', '<p>111</p>', 3, 0, 0, 1, '2026-02-10 14:36:23', '2026-02-10 14:51:18');
INSERT INTO `tb_post` VALUES (37, 1, 1, '111', '<p>111</p>', 3, 0, 0, 1, '2026-02-10 14:52:04', '2026-02-10 14:54:35');
INSERT INTO `tb_post` VALUES (38, 1, 3, '111', '<p>111</p>', 3, 0, 0, 1, '2026-02-10 14:54:47', '2026-02-10 14:56:16');
INSERT INTO `tb_post` VALUES (39, 1, 2, '111', '<p>111</p>', 3, 1, 0, 1, '2026-02-10 14:56:25', '2026-02-10 14:58:57');
INSERT INTO `tb_post` VALUES (40, 1, 3, '111', '<p>111</p>', 3, 1, 0, 1, '2026-02-10 14:59:07', '2026-02-10 15:14:58');
INSERT INTO `tb_post` VALUES (41, 1, 1, '222', '<p>111</p>', 3, 0, 0, 1, '2026-02-10 15:15:09', '2026-02-10 16:44:48');
INSERT INTO `tb_post` VALUES (42, 1, 3, '111', '<p>111</p>', 3, 0, 0, 1, '2026-02-10 17:23:52', '2026-02-10 17:31:17');
INSERT INTO `tb_post` VALUES (43, 1, 3, '222', '<p>222</p>', 3, 0, 0, 1, '2026-02-10 17:24:03', '2026-02-10 17:31:17');
INSERT INTO `tb_post` VALUES (44, 1, 3, '333', '<p>333</p>', 3, 0, 0, 1, '2026-02-10 17:30:02', '2026-02-10 17:31:17');
INSERT INTO `tb_post` VALUES (45, 1, 2, '测试websocket1', '<p>测试websocket1</p>', 3, 0, 0, 1, '2026-02-14 21:52:14', '2026-02-14 21:57:44');
INSERT INTO `tb_post` VALUES (46, 1, 2, '测试websocket11', '<p>测试websocket11</p>', 3, 0, 0, 1, '2026-02-14 21:58:34', '2026-02-14 22:00:06');
INSERT INTO `tb_post` VALUES (47, 1, 3, '测试websocket111', '<p>测试websocket111</p>', 3, 1, 0, 0, '2026-02-14 22:00:24', '2026-02-14 23:30:37');
INSERT INTO `tb_post` VALUES (48, 1, 3, '测试RabbitMQ审核22', '<p>测试RabbitMQ审核</p>', 3, 0, 0, 0, '2026-02-21 16:50:29', '2026-02-24 11:20:15');
INSERT INTO `tb_post` VALUES (49, 1, 2, '测试RabbitMQ失败审核', '<p>测试RabbitMQ失败审核卧槽</p>', 4, 0, 0, 1, '2026-02-21 16:51:35', '2026-02-21 16:51:54');

-- ----------------------------
-- Table structure for tb_post_dislike
-- ----------------------------
DROP TABLE IF EXISTS `tb_post_dislike`;
CREATE TABLE `tb_post_dislike`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '收藏ID，主键自增',
  `user_id` bigint NOT NULL COMMENT '用户ID，不可为空，与用户表关联',
  `post_id` bigint NOT NULL COMMENT '帖子ID，不可为空，与帖子表关联',
  `cancel_flag` tinyint NOT NULL DEFAULT 0 COMMENT '取消点踩标记，0-未取消，1-已取消',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间，默认为插入时时间戳',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间，默认为插入或更新时当前时间戳',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '帖子点踩表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_post_dislike
-- ----------------------------
INSERT INTO `tb_post_dislike` VALUES (1, 3, 1, 1, '2025-12-21 14:05:35', '2025-12-21 20:49:49');
INSERT INTO `tb_post_dislike` VALUES (2, 1, 15, 1, '2026-01-07 09:39:22', '2026-01-07 09:39:23');

-- ----------------------------
-- Table structure for tb_post_favorite
-- ----------------------------
DROP TABLE IF EXISTS `tb_post_favorite`;
CREATE TABLE `tb_post_favorite`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '收藏ID，主键自增',
  `user_id` bigint NOT NULL COMMENT '用户ID，不可为空，与用户表关联',
  `post_id` bigint NOT NULL COMMENT '帖子ID，不可为空，与帖子表关联',
  `cancel_flag` tinyint NULL DEFAULT 0 COMMENT '取消收藏标记，0-未取消，1-已取消\r\n',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间，默认为插入时时间戳',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间，默认为插入时时间戳，插入时自动更新',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_post_id`(`post_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '帖子收藏表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_post_favorite
-- ----------------------------
INSERT INTO `tb_post_favorite` VALUES (1, 3, 1, 1, '2025-12-21 20:31:08', '2025-12-21 20:50:43');
INSERT INTO `tb_post_favorite` VALUES (2, 3, 2, 0, '2025-12-21 20:31:40', '2025-12-21 20:51:04');
INSERT INTO `tb_post_favorite` VALUES (3, 1, 1, 0, '2025-12-21 20:35:27', '2026-02-19 20:54:45');
INSERT INTO `tb_post_favorite` VALUES (4, 1, 16, 0, '2025-12-26 20:59:16', '2025-12-26 20:59:15');
INSERT INTO `tb_post_favorite` VALUES (5, 1, 15, 0, '2026-01-07 09:39:29', '2026-01-07 09:39:28');
INSERT INTO `tb_post_favorite` VALUES (6, 1, 41, 1, '2026-02-10 16:12:58', '2026-02-10 17:09:36');
INSERT INTO `tb_post_favorite` VALUES (7, 2, 1, 1, '2026-02-19 20:52:08', '2026-02-19 20:54:34');
INSERT INTO `tb_post_favorite` VALUES (8, 1, 2, 1, '2026-02-21 00:01:24', '2026-02-21 00:10:34');

-- ----------------------------
-- Table structure for tb_post_like
-- ----------------------------
DROP TABLE IF EXISTS `tb_post_like`;
CREATE TABLE `tb_post_like`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '收藏ID，主键自增',
  `user_id` bigint NOT NULL COMMENT '用户ID，不可为空，与用户表关联',
  `post_id` bigint NOT NULL COMMENT '帖子ID，不可为空，与帖子表关联',
  `cancel_flag` tinyint NOT NULL DEFAULT 0 COMMENT '取消点赞标记，0-未取消，1-已取消',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间，默认为插入时时间戳',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间，默认为插入或更新时当前时间戳',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 20 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '帖子点赞表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_post_like
-- ----------------------------
INSERT INTO `tb_post_like` VALUES (5, 3, 1, 1, '2025-12-21 14:05:10', '2025-12-21 20:49:46');
INSERT INTO `tb_post_like` VALUES (6, 1, 15, 0, '2025-12-23 17:01:09', '2026-01-07 09:39:23');
INSERT INTO `tb_post_like` VALUES (7, 3, 15, 0, '2025-12-26 20:46:49', '2026-02-19 20:34:33');
INSERT INTO `tb_post_like` VALUES (8, 1, 16, 0, '2025-12-26 20:59:14', '2025-12-26 20:59:18');
INSERT INTO `tb_post_like` VALUES (9, 1, 1, 1, '2026-02-09 22:32:23', '2026-02-09 22:32:24');
INSERT INTO `tb_post_like` VALUES (10, 1, 39, 0, '2026-02-10 14:57:13', '2026-02-10 14:57:13');
INSERT INTO `tb_post_like` VALUES (11, 1, 40, 0, '2026-02-10 15:00:10', '2026-02-10 15:00:10');
INSERT INTO `tb_post_like` VALUES (12, 1, 41, 1, '2026-02-10 15:16:17', '2026-02-10 15:19:31');
INSERT INTO `tb_post_like` VALUES (13, 1, 2, 1, '2026-02-10 15:19:43', '2026-02-21 00:13:00');
INSERT INTO `tb_post_like` VALUES (14, 3, 47, 0, '2026-02-14 23:00:58', '2026-02-14 23:30:37');
INSERT INTO `tb_post_like` VALUES (15, 1, 47, 1, '2026-02-14 23:01:16', '2026-02-14 23:01:19');
INSERT INTO `tb_post_like` VALUES (16, 3, 3, 0, '2026-02-19 20:39:47', '2026-02-19 20:39:47');
INSERT INTO `tb_post_like` VALUES (17, 2, 15, 0, '2026-02-19 20:51:21', '2026-02-19 20:51:20');
INSERT INTO `tb_post_like` VALUES (18, 2, 1, 0, '2026-02-19 20:51:34', '2026-02-19 20:51:33');
INSERT INTO `tb_post_like` VALUES (19, 2, 16, 0, '2026-02-19 20:52:23', '2026-02-19 20:52:22');

-- ----------------------------
-- Table structure for tb_post_media
-- ----------------------------
DROP TABLE IF EXISTS `tb_post_media`;
CREATE TABLE `tb_post_media`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '媒体ID，主键自增',
  `post_id` bigint NOT NULL COMMENT '帖子ID，与帖子表关联',
  `media_url` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '媒体地址',
  `media_type` tinyint NOT NULL COMMENT '媒体类型，1-图片，2-视频',
  `delete_flag` tinyint NULL DEFAULT 0 COMMENT '删除标记，0-未删除，1-已删除',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间，默认为插入时时间戳',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间，默认为插入时时间戳，插入时自动更新',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_post_id`(`post_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 49 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '帖子媒体表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_post_media
-- ----------------------------
INSERT INTO `tb_post_media` VALUES (1, 1, 'https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/golden_retriever_post_1.png', 1, 0, '2025-12-18 12:21:15', '2025-12-18 14:10:21');
INSERT INTO `tb_post_media` VALUES (2, 1, 'https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/golden_retriever_post_2.png', 1, 0, '2025-12-18 12:21:15', '2026-02-09 23:57:17');
INSERT INTO `tb_post_media` VALUES (3, 2, 'https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/british_shorthair_post1.png', 1, 0, '2025-12-18 12:21:15', '2025-12-18 14:10:31');
INSERT INTO `tb_post_media` VALUES (4, 2, 'https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/british_shorthair_post_2.png', 1, 0, '2025-12-18 12:21:15', '2025-12-18 14:10:32');
INSERT INTO `tb_post_media` VALUES (5, 3, 'https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/cat_post.mp4', 2, 0, '2025-12-18 12:21:15', '2026-02-09 23:56:12');
INSERT INTO `tb_post_media` VALUES (6, 7, 'https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/5ff980bb-1f66-4935-92c8-414a3127ec24_抖音20251127-398602.mp4', 2, 1, '2025-12-20 16:31:43', '2026-01-10 20:21:05');
INSERT INTO `tb_post_media` VALUES (7, 8, 'https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/08b88d24-9f1a-4091-87cf-0961ee407e4a_抖音20251127-398602.mp4', 2, 1, '2025-12-20 16:33:52', '2026-01-10 20:21:28');
INSERT INTO `tb_post_media` VALUES (8, 9, 'https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/b2b93fc3-43cb-4a4f-b2d2-383efb831827_生成特定 logo.png', 1, 1, '2025-12-20 16:45:41', '2026-01-10 20:21:10');
INSERT INTO `tb_post_media` VALUES (9, 10, 'https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/74c3b15e-b7be-458d-897f-5bb30777dcd3_cover.png', 1, 0, '2025-12-20 16:46:25', '2025-12-20 16:46:24');
INSERT INTO `tb_post_media` VALUES (10, 12, 'https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/7cef2ac5-7dc2-4ace-a124-aae7a2a53ff6_cover.png', 1, 1, '2025-12-20 16:52:05', '2025-12-20 23:26:08');
INSERT INTO `tb_post_media` VALUES (11, 12, 'https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/dcddd19e-069a-4098-92b9-4db1968ae8b3_british_shorthair_comment_2.png', 1, 0, '2025-12-20 23:26:08', '2025-12-20 23:34:26');
INSERT INTO `tb_post_media` VALUES (12, 13, 'https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/6e194189-43b0-40f4-acc5-50d9221cad1d_cover.png', 1, 0, '2025-12-20 23:35:39', '2025-12-20 23:35:39');
INSERT INTO `tb_post_media` VALUES (13, 14, 'https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/b41397e3-6ae5-4f4a-8f6b-59005dbf6fef_cover.png', 1, 1, '2025-12-21 21:18:39', '2025-12-21 21:18:50');
INSERT INTO `tb_post_media` VALUES (14, 15, 'https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/cdd5342e-f354-4c54-bd24-22574c6cba83_001-《你，好不好？》周兴哲～能不能继续对我哭.mp4', 2, 0, '2025-12-23 10:10:49', '2025-12-23 10:10:49');
INSERT INTO `tb_post_media` VALUES (15, 16, 'https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/9ecec508-f072-4a6b-aef6-a83f36d07c84_我只能离开.mp4', 2, 0, '2025-12-26 20:54:17', '2025-12-26 20:54:17');
INSERT INTO `tb_post_media` VALUES (16, 17, 'https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/d96e408c-45ea-4974-9582-ac9f982016f9_cover.png', 1, 1, '2026-01-07 17:24:28', '2026-01-07 17:25:15');
INSERT INTO `tb_post_media` VALUES (17, 18, 'https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/cec89716-fa01-4da0-a05e-185a65a21759_cover.png', 1, 1, '2026-01-07 19:01:45', '2026-01-07 19:02:00');
INSERT INTO `tb_post_media` VALUES (18, 19, 'https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/14c9fcdb-a1e8-46f1-b99a-6cbc479876bd_cover.png', 1, 1, '2026-01-10 10:28:17', '2026-01-10 10:30:10');
INSERT INTO `tb_post_media` VALUES (19, 20, 'https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/ac6e1e0b-3003-4f34-b6b5-7f053c639cd1_cover.png', 1, 1, '2026-01-10 10:28:51', '2026-01-10 10:33:05');
INSERT INTO `tb_post_media` VALUES (20, 21, 'https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/02d623d3-40be-409c-9d79-814f67948b9c_cover.png', 1, 1, '2026-01-10 10:29:40', '2026-01-10 10:32:04');
INSERT INTO `tb_post_media` VALUES (21, 22, 'https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/e219bb89-6046-49f5-b1f9-bda45598507f_cover.png', 1, 1, '2026-01-10 10:33:31', '2026-01-10 10:37:59');
INSERT INTO `tb_post_media` VALUES (22, 23, 'https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/4d29c365-36b1-4e0d-a5b1-8feda09387ee_cover.png', 1, 1, '2026-01-10 10:33:43', '2026-01-10 10:34:11');
INSERT INTO `tb_post_media` VALUES (23, 24, 'https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/842611b6-c488-4ea2-b6f4-06cb7ee3843a_cover.png', 1, 1, '2026-01-10 10:36:31', '2026-01-10 10:37:59');
INSERT INTO `tb_post_media` VALUES (24, 25, 'https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/32fc7610-7f28-4c92-8039-35105fad548e_cover.png', 1, 1, '2026-01-10 10:40:35', '2026-01-10 10:40:48');
INSERT INTO `tb_post_media` VALUES (25, 26, 'https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/bfc2bfdb-95be-4936-9b69-1fc525f90265_cover.png', 1, 1, '2026-01-10 19:48:01', '2026-01-10 19:59:29');
INSERT INTO `tb_post_media` VALUES (26, 27, 'https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/bae0ad2b-5e43-4c0c-9736-061126b219a7_cover.png', 1, 1, '2026-01-10 19:49:19', '2026-01-10 19:51:11');
INSERT INTO `tb_post_media` VALUES (27, 28, 'https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/a85b6513-6a38-4ad2-8c66-e7a345358e0e_cover.png', 1, 1, '2026-01-10 19:51:25', '2026-01-10 20:00:50');
INSERT INTO `tb_post_media` VALUES (28, 29, 'https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/4239ac31-8cb8-4394-a1cd-52b95b883d5f_cover.png', 1, 1, '2026-01-10 19:59:46', '2026-01-10 20:00:52');
INSERT INTO `tb_post_media` VALUES (29, 30, 'https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/8f6dcfc9-21d1-4792-aace-b5c516ce1c31_cover.png', 1, 1, '2026-01-10 21:36:16', '2026-01-10 21:37:35');
INSERT INTO `tb_post_media` VALUES (30, 31, 'https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/de55e791-3857-4b00-b60f-742f855cc229_cover.png', 1, 1, '2026-01-10 21:37:18', '2026-01-10 21:38:07');
INSERT INTO `tb_post_media` VALUES (31, 32, 'https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/104535fd-22a4-4baf-87d5-ac39c2f62d50_cover.png', 1, 1, '2026-01-10 21:38:28', '2026-01-10 21:39:05');
INSERT INTO `tb_post_media` VALUES (32, 33, 'https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/862a7288-b3e7-47e0-98a1-70e1ac611484_cover.png', 1, 1, '2026-01-11 18:36:35', '2026-01-11 20:05:26');
INSERT INTO `tb_post_media` VALUES (33, 34, 'https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/368e909f-9680-4122-8f45-4e5cd29478aa_cover.png', 1, 1, '2026-01-11 20:06:08', '2026-01-11 20:07:44');
INSERT INTO `tb_post_media` VALUES (34, 35, 'https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/d1ed0e85-f810-46b7-bd8e-73722a2f2bb4_cover.png', 1, 1, '2026-01-13 22:10:48', '2026-01-13 22:11:12');
INSERT INTO `tb_post_media` VALUES (35, 36, 'https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/324cd022-0fc6-4db1-ba7c-60bd42bf7e77_cover.png', 1, 1, '2026-02-10 14:36:23', '2026-02-10 14:46:48');
INSERT INTO `tb_post_media` VALUES (36, 37, 'https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/2f335eb3-d352-4051-93d5-2b9d545b8e93_cover.png', 1, 1, '2026-02-10 14:52:05', '2026-02-10 14:54:35');
INSERT INTO `tb_post_media` VALUES (37, 38, 'https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/20805ee4-3c50-41c1-9441-28c4592b286d_cover.png', 1, 1, '2026-02-10 14:54:48', '2026-02-10 14:56:16');
INSERT INTO `tb_post_media` VALUES (38, 39, 'https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/528ebfec-dadb-44c9-9d1e-ec63e5a1e6d0_cover.png', 1, 1, '2026-02-10 14:56:26', '2026-02-10 14:58:57');
INSERT INTO `tb_post_media` VALUES (39, 40, 'https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/15f570bf-0658-4ecb-be37-b5f61fc3ecc8_cover.png', 1, 1, '2026-02-10 14:59:08', '2026-02-10 15:14:58');
INSERT INTO `tb_post_media` VALUES (40, 41, 'https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/5cf22a34-77b8-48bc-9bf6-9ab549d2fd54_cover.png', 1, 1, '2026-02-10 15:15:10', '2026-02-10 16:44:48');
INSERT INTO `tb_post_media` VALUES (41, 42, 'https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/91d4328f-49d3-4775-ba89-77daad56e2b4_cover.png', 1, 0, '2026-02-10 17:23:53', '2026-02-10 17:23:52');
INSERT INTO `tb_post_media` VALUES (42, 43, 'https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/8ffa8e92-fc00-41ff-b0cd-e45c88f1a2e1_cover.png', 1, 0, '2026-02-10 17:24:03', '2026-02-10 17:24:03');
INSERT INTO `tb_post_media` VALUES (43, 44, 'https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/361c522f-5dd9-44b6-a3e6-25573c9c6427_cover.png', 1, 0, '2026-02-10 17:30:03', '2026-02-10 17:30:02');
INSERT INTO `tb_post_media` VALUES (44, 45, 'https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/d4abb8c8-6dfe-4163-9227-f6236f035c3a_cover.png', 1, 1, '2026-02-14 21:52:15', '2026-02-14 21:57:44');
INSERT INTO `tb_post_media` VALUES (45, 46, 'https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/cef131c4-3941-45ec-8f6f-07eff08d5736_cover.png', 1, 1, '2026-02-14 21:58:35', '2026-02-14 22:00:06');
INSERT INTO `tb_post_media` VALUES (46, 47, 'https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/74ecfc1a-beb3-4d79-844c-8566cef2e251_cover.png', 1, 0, '2026-02-14 22:00:25', '2026-02-14 22:00:24');
INSERT INTO `tb_post_media` VALUES (47, 48, 'https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/98d58b19-a721-4564-90db-89bad1ca7019_cover.png', 1, 0, '2026-02-21 16:50:30', '2026-02-21 16:50:30');
INSERT INTO `tb_post_media` VALUES (48, 49, 'https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/f0c29401-5baf-41a2-afa0-edf5f21ba62e_cover.png', 1, 1, '2026-02-21 16:51:36', '2026-02-21 16:51:54');

-- ----------------------------
-- Table structure for tb_product_category_sub
-- ----------------------------
DROP TABLE IF EXISTS `tb_product_category_sub`;
CREATE TABLE `tb_product_category_sub`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键自增 ID',
  `main_category_id` bigint NOT NULL COMMENT '商品一级分类 ID，与 tb_category_super.id 关联',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '分类名称',
  `delete_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标记，0-未删除，1-已删除',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间，默认为插入时当前时间戳',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间，插入或更新时自动设为当前时间戳',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 111 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '商品二级分类表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_product_category_sub
-- ----------------------------
INSERT INTO `tb_product_category_sub` VALUES (1, 1, '英国短毛猫', 0, '2025-12-09 16:14:38', '2025-12-09 16:14:38');
INSERT INTO `tb_product_category_sub` VALUES (2, 1, '美国短毛猫', 0, '2025-12-09 16:14:51', '2025-12-09 16:14:51');
INSERT INTO `tb_product_category_sub` VALUES (3, 1, '波斯猫', 0, '2025-12-09 16:14:54', '2025-12-09 16:14:54');
INSERT INTO `tb_product_category_sub` VALUES (4, 1, '暹罗猫', 0, '2025-12-09 16:14:57', '2025-12-09 16:14:57');
INSERT INTO `tb_product_category_sub` VALUES (5, 1, '布偶猫', 0, '2025-12-09 16:15:00', '2025-12-09 16:15:00');
INSERT INTO `tb_product_category_sub` VALUES (6, 1, '缅因猫', 0, '2025-12-09 16:15:04', '2025-12-09 16:15:04');
INSERT INTO `tb_product_category_sub` VALUES (7, 1, '苏格兰折耳猫', 0, '2025-12-09 16:15:07', '2025-12-09 16:15:07');
INSERT INTO `tb_product_category_sub` VALUES (8, 1, ' Norwegian Forest Cat', 0, '2025-12-09 16:15:10', '2025-12-09 16:15:10');
INSERT INTO `tb_product_category_sub` VALUES (9, 1, '俄罗斯蓝猫', 0, '2025-12-09 16:15:19', '2025-12-09 16:15:19');
INSERT INTO `tb_product_category_sub` VALUES (10, 1, '孟加拉猫', 0, '2025-12-09 16:15:29', '2025-12-09 16:15:29');
INSERT INTO `tb_product_category_sub` VALUES (11, 2, '哈士奇', 0, '2025-12-09 16:15:33', '2025-12-09 16:15:33');
INSERT INTO `tb_product_category_sub` VALUES (12, 2, '拉布拉多', 0, '2025-12-09 16:15:36', '2025-12-09 16:15:36');
INSERT INTO `tb_product_category_sub` VALUES (13, 2, '金毛寻回犬', 0, '2025-12-09 16:15:39', '2025-12-09 16:15:39');
INSERT INTO `tb_product_category_sub` VALUES (14, 2, '柴犬', 0, '2025-12-09 16:15:44', '2025-12-09 16:15:44');
INSERT INTO `tb_product_category_sub` VALUES (15, 2, '柯基', 0, '2025-12-09 16:15:47', '2025-12-09 16:15:47');
INSERT INTO `tb_product_category_sub` VALUES (16, 2, '德牧', 0, '2025-12-09 16:16:00', '2025-12-09 16:16:00');
INSERT INTO `tb_product_category_sub` VALUES (17, 2, '边牧', 0, '2025-12-09 16:16:04', '2025-12-09 16:16:04');
INSERT INTO `tb_product_category_sub` VALUES (18, 2, '泰迪', 0, '2025-12-09 16:16:07', '2025-12-09 16:16:07');
INSERT INTO `tb_product_category_sub` VALUES (19, 2, '比格犬', 0, '2025-12-09 16:16:09', '2025-12-09 16:16:09');
INSERT INTO `tb_product_category_sub` VALUES (20, 2, '萨摩耶', 0, '2025-12-09 16:16:12', '2025-12-09 16:16:12');
INSERT INTO `tb_product_category_sub` VALUES (21, 3, '迷你猪', 0, '2025-12-09 16:16:15', '2025-12-09 16:16:15');
INSERT INTO `tb_product_category_sub` VALUES (22, 3, '越南大肚猪', 0, '2025-12-09 16:16:30', '2025-12-09 16:16:30');
INSERT INTO `tb_product_category_sub` VALUES (23, 3, ' pot-bellied pig', 0, '2025-12-09 16:16:33', '2025-12-09 16:16:33');
INSERT INTO `tb_product_category_sub` VALUES (41, 7, '金丝熊仓鼠', 0, '2025-12-09 16:22:24', '2026-01-01 21:41:35');
INSERT INTO `tb_product_category_sub` VALUES (42, 7, '银狐仓鼠', 0, '2025-12-09 16:22:28', '2025-12-09 16:24:15');
INSERT INTO `tb_product_category_sub` VALUES (43, 7, '布丁仓鼠', 0, '2025-12-09 16:22:31', '2025-12-09 16:24:17');
INSERT INTO `tb_product_category_sub` VALUES (44, 7, '奶茶仓鼠', 0, '2025-12-09 16:22:49', '2025-12-09 16:24:04');
INSERT INTO `tb_product_category_sub` VALUES (45, 7, '紫仓', 0, '2025-12-09 16:22:52', '2025-12-09 16:22:52');
INSERT INTO `tb_product_category_sub` VALUES (52, 9, '主粮', 0, '2025-12-09 16:37:09', '2025-12-09 16:37:09');
INSERT INTO `tb_product_category_sub` VALUES (53, 9, '零食、奖励食品', 0, '2025-12-09 16:37:24', '2025-12-09 16:37:24');
INSERT INTO `tb_product_category_sub` VALUES (54, 9, '营养补充、保健食品', 0, '2025-12-09 16:37:27', '2025-12-09 16:37:27');
INSERT INTO `tb_product_category_sub` VALUES (55, 10, '食碗', 0, '2025-12-09 16:37:31', '2025-12-09 16:37:31');
INSERT INTO `tb_product_category_sub` VALUES (56, 10, '水碗', 0, '2025-12-09 16:37:35', '2025-12-09 16:37:35');
INSERT INTO `tb_product_category_sub` VALUES (57, 10, '自动喂食器', 0, '2025-12-09 16:37:38', '2025-12-09 16:37:38');
INSERT INTO `tb_product_category_sub` VALUES (58, 10, '便携食水用品', 0, '2025-12-09 16:37:42', '2025-12-09 16:37:42');
INSERT INTO `tb_product_category_sub` VALUES (59, 11, '窝', 0, '2025-12-09 16:37:46', '2025-12-09 16:37:46');
INSERT INTO `tb_product_category_sub` VALUES (60, 11, '垫子', 0, '2025-12-09 16:37:49', '2025-12-09 16:37:49');
INSERT INTO `tb_product_category_sub` VALUES (61, 11, '毯子', 0, '2025-12-09 16:38:05', '2025-12-09 16:38:05');
INSERT INTO `tb_product_category_sub` VALUES (62, 11, '宠物床', 0, '2025-12-09 16:38:08', '2025-12-09 16:38:08');
INSERT INTO `tb_product_category_sub` VALUES (63, 11, '窝箱', 0, '2025-12-09 16:38:11', '2025-12-09 16:38:11');
INSERT INTO `tb_product_category_sub` VALUES (64, 12, '砂盆', 0, '2025-12-09 16:38:14', '2025-12-09 16:38:14');
INSERT INTO `tb_product_category_sub` VALUES (65, 12, '砂铲', 0, '2025-12-09 16:38:18', '2025-12-09 16:38:18');
INSERT INTO `tb_product_category_sub` VALUES (66, 12, '砂垫', 0, '2025-12-09 16:38:38', '2025-12-09 16:38:38');
INSERT INTO `tb_product_category_sub` VALUES (67, 12, '防漏垫', 0, '2025-12-09 16:38:41', '2025-12-09 16:38:41');
INSERT INTO `tb_product_category_sub` VALUES (68, 12, '防溅垫', 0, '2025-12-09 16:38:45', '2025-12-09 16:38:45');
INSERT INTO `tb_product_category_sub` VALUES (69, 12, '宠物厕所', 0, '2025-12-09 16:38:48', '2025-12-09 16:38:48');
INSERT INTO `tb_product_category_sub` VALUES (70, 12, '清洁用品', 0, '2025-12-09 16:38:53', '2025-12-09 16:38:53');
INSERT INTO `tb_product_category_sub` VALUES (71, 13, '沐浴用品', 0, '2025-12-09 16:38:56', '2025-12-09 16:38:56');
INSERT INTO `tb_product_category_sub` VALUES (72, 13, '护毛素', 0, '2025-12-09 16:39:00', '2025-12-09 16:39:00');
INSERT INTO `tb_product_category_sub` VALUES (74, 13, '护理清洁用品', 0, '2025-12-09 16:39:09', '2025-12-09 16:39:09');
INSERT INTO `tb_product_category_sub` VALUES (75, 13, '刷子 / 梳子 /除毛工具', 0, '2025-12-09 16:39:25', '2025-12-09 16:39:25');
INSERT INTO `tb_product_category_sub` VALUES (76, 13, '指甲剪／磨爪工具', 0, '2025-12-09 16:39:31', '2025-12-09 16:39:31');
INSERT INTO `tb_product_category_sub` VALUES (77, 13, '日常护理用品', 0, '2025-12-09 16:39:34', '2025-12-09 16:39:34');
INSERT INTO `tb_product_category_sub` VALUES (78, 13, '口腔清洁', 0, '2025-12-09 16:39:39', '2025-12-09 16:39:39');
INSERT INTO `tb_product_category_sub` VALUES (79, 14, '逗猫棒', 0, '2025-12-09 16:39:43', '2025-12-09 16:39:43');
INSERT INTO `tb_product_category_sub` VALUES (80, 14, '互动玩具', 0, '2025-12-09 16:39:52', '2025-12-09 16:39:52');
INSERT INTO `tb_product_category_sub` VALUES (81, 14, '绳结', 0, '2025-12-09 16:39:55', '2025-12-09 16:39:55');
INSERT INTO `tb_product_category_sub` VALUES (82, 14, '耐咬玩具', 0, '2025-12-09 16:39:58', '2025-12-09 16:39:58');
INSERT INTO `tb_product_category_sub` VALUES (83, 14, '智力玩具', 0, '2025-12-09 16:40:02', '2025-12-09 16:40:02');
INSERT INTO `tb_product_category_sub` VALUES (84, 15, '便携箱/宠物运输箱', 0, '2025-12-09 16:40:05', '2025-12-09 16:40:05');
INSERT INTO `tb_product_category_sub` VALUES (85, 15, '宠物背包', 0, '2025-12-09 16:40:16', '2025-12-09 16:40:16');
INSERT INTO `tb_product_category_sub` VALUES (86, 15, '牵引绳', 0, '2025-12-09 16:40:19', '2025-12-09 16:40:19');
INSERT INTO `tb_product_category_sub` VALUES (87, 15, '项圈', 0, '2025-12-09 16:40:28', '2025-12-09 16:40:28');
INSERT INTO `tb_product_category_sub` VALUES (88, 15, '识别牌', 0, '2025-12-09 16:40:44', '2025-12-09 16:40:44');
INSERT INTO `tb_product_category_sub` VALUES (89, 15, '安全绳', 0, '2025-12-09 16:42:48', '2025-12-09 16:42:48');
INSERT INTO `tb_product_category_sub` VALUES (90, 15, '防走失用品', 0, '2025-12-09 16:42:53', '2025-12-09 16:42:53');
INSERT INTO `tb_product_category_sub` VALUES (91, 15, '宠物门', 0, '2025-12-09 16:42:56', '2025-12-09 16:42:56');
INSERT INTO `tb_product_category_sub` VALUES (92, 16, '宠物衣服', 0, '2025-12-09 16:43:00', '2025-12-09 16:43:00');
INSERT INTO `tb_product_category_sub` VALUES (93, 16, '佩戴配件', 0, '2025-12-09 16:43:03', '2025-12-09 16:43:03');
INSERT INTO `tb_product_category_sub` VALUES (94, 16, '防护服', 0, '2025-12-09 16:43:07', '2025-12-09 16:43:07');
INSERT INTO `tb_product_category_sub` VALUES (95, 17, '除虫用品', 0, '2025-12-09 16:43:10', '2025-12-09 16:43:10');
INSERT INTO `tb_product_category_sub` VALUES (96, 17, '基础医疗护理用品', 0, '2025-12-09 16:43:13', '2025-12-09 16:43:13');
INSERT INTO `tb_product_category_sub` VALUES (97, 17, '关节保健', 0, '2025-12-09 16:43:17', '2025-12-09 16:43:17');
INSERT INTO `tb_product_category_sub` VALUES (98, 17, '毛发营养', 0, '2025-12-09 16:43:21', '2025-12-09 16:43:21');
INSERT INTO `tb_product_category_sub` VALUES (99, 17, '整体健康补剂', 0, '2025-12-09 16:43:24', '2025-12-09 16:43:24');
INSERT INTO `tb_product_category_sub` VALUES (100, 18, '空气除味用品', 0, '2025-12-09 16:43:35', '2025-12-09 16:43:35');
INSERT INTO `tb_product_category_sub` VALUES (101, 18, '宠物安全设施', 0, '2025-12-09 16:43:51', '2025-12-09 16:43:51');
INSERT INTO `tb_product_category_sub` VALUES (102, 18, '环境用品', 0, '2025-12-09 16:43:56', '2025-12-09 16:43:56');
INSERT INTO `tb_product_category_sub` VALUES (103, 18, '收纳用品', 0, '2025-12-09 16:44:12', '2025-12-09 16:44:12');
INSERT INTO `tb_product_category_sub` VALUES (104, 18, '废物处理用品', 0, '2025-12-09 16:44:25', '2025-12-09 16:44:25');
INSERT INTO `tb_product_category_sub` VALUES (105, 19, '金丝熊仓鼠', 1, '2026-01-01 18:00:48', '2026-01-01 22:10:22');
INSERT INTO `tb_product_category_sub` VALUES (106, 23, '11', 1, '2026-01-01 22:11:33', '2026-01-01 22:11:52');
INSERT INTO `tb_product_category_sub` VALUES (107, 38, 'testss', 1, '2026-02-23 15:34:48', '2026-02-23 15:41:58');
INSERT INTO `tb_product_category_sub` VALUES (108, 39, 'test1', 1, '2026-02-23 15:48:13', '2026-02-23 15:49:25');
INSERT INTO `tb_product_category_sub` VALUES (109, 39, 'test2', 1, '2026-02-23 15:48:22', '2026-02-23 15:48:30');
INSERT INTO `tb_product_category_sub` VALUES (110, 40, 'test1', 1, '2026-02-23 15:54:15', '2026-02-23 15:54:36');

-- ----------------------------
-- Table structure for tb_product_category_super
-- ----------------------------
DROP TABLE IF EXISTS `tb_product_category_super`;
CREATE TABLE `tb_product_category_super`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '分类ID，主键自增',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '分类名称',
  `type` tinyint NOT NULL COMMENT '商品分类类型，1-宠物分类，2-宠物用品分类',
  `delete_flag` tinyint NULL DEFAULT 0 COMMENT '删除标记，0-未删除，1-已删除',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间，默认为插入时时间戳',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间，默认为插入时时间戳，插入时自动更新',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_type`(`type` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 41 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '商品分类表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_product_category_super
-- ----------------------------
INSERT INTO `tb_product_category_super` VALUES (1, '猫', 1, 0, '2025-12-09 16:13:56', '2025-12-09 16:13:56');
INSERT INTO `tb_product_category_super` VALUES (2, '狗', 1, 0, '2025-12-09 16:13:59', '2025-12-09 16:13:59');
INSERT INTO `tb_product_category_super` VALUES (3, '猪', 1, 0, '2025-12-09 16:14:02', '2025-12-09 16:14:02');
INSERT INTO `tb_product_category_super` VALUES (7, '仓鼠', 1, 0, '2025-12-09 16:22:12', '2025-12-09 16:22:12');
INSERT INTO `tb_product_category_super` VALUES (9, '食品与零食', 2, 0, '2025-12-09 16:35:57', '2025-12-09 16:35:57');
INSERT INTO `tb_product_category_super` VALUES (10, '饮食器具', 2, 0, '2025-12-09 16:36:01', '2025-12-09 16:36:01');
INSERT INTO `tb_product_category_super` VALUES (11, '舒适用品', 2, 0, '2025-12-09 16:36:04', '2025-12-09 16:36:04');
INSERT INTO `tb_product_category_super` VALUES (12, '清洁用品', 2, 0, '2025-12-09 16:36:10', '2025-12-09 16:36:10');
INSERT INTO `tb_product_category_super` VALUES (13, '护理用品', 2, 0, '2025-12-09 16:36:13', '2025-12-09 16:36:13');
INSERT INTO `tb_product_category_super` VALUES (14, '玩具/娱乐用品', 2, 0, '2025-12-09 16:36:16', '2025-12-09 16:36:16');
INSERT INTO `tb_product_category_super` VALUES (15, '出行用品', 2, 0, '2025-12-09 16:36:44', '2025-12-09 16:36:44');
INSERT INTO `tb_product_category_super` VALUES (16, '装饰用品', 2, 0, '2025-12-09 16:36:48', '2025-12-09 16:36:48');
INSERT INTO `tb_product_category_super` VALUES (17, '健康用品', 2, 0, '2025-12-09 16:36:52', '2025-12-09 16:36:52');
INSERT INTO `tb_product_category_super` VALUES (18, '增值用品', 2, 0, '2025-12-09 16:37:05', '2025-12-09 16:37:05');
INSERT INTO `tb_product_category_super` VALUES (19, 'test11', 1, 1, '2026-01-01 17:59:13', '2026-01-01 22:10:38');
INSERT INTO `tb_product_category_super` VALUES (20, 'test1', 2, 1, '2026-01-01 20:26:11', '2026-01-01 22:12:29');
INSERT INTO `tb_product_category_super` VALUES (21, 'test2', 1, 1, '2026-01-01 20:26:22', '2026-01-01 22:08:59');
INSERT INTO `tb_product_category_super` VALUES (22, 'test3', 1, 1, '2026-01-01 20:26:25', '2026-01-01 22:09:35');
INSERT INTO `tb_product_category_super` VALUES (23, 'test4', 1, 1, '2026-01-01 20:26:34', '2026-01-01 22:12:04');
INSERT INTO `tb_product_category_super` VALUES (24, 'test5', 1, 1, '2026-01-01 20:26:43', '2026-01-01 22:12:06');
INSERT INTO `tb_product_category_super` VALUES (25, 'test6', 1, 1, '2026-01-01 20:26:47', '2026-01-01 22:12:08');
INSERT INTO `tb_product_category_super` VALUES (26, 'test7', 1, 1, '2026-01-01 20:26:57', '2026-01-01 22:12:09');
INSERT INTO `tb_product_category_super` VALUES (27, 'test8', 1, 1, '2026-01-01 20:27:01', '2026-01-01 22:12:12');
INSERT INTO `tb_product_category_super` VALUES (28, 'test9', 1, 1, '2026-01-01 20:27:05', '2026-01-01 22:12:24');
INSERT INTO `tb_product_category_super` VALUES (29, 'test10', 1, 1, '2026-01-01 20:27:09', '2026-01-01 22:12:25');
INSERT INTO `tb_product_category_super` VALUES (30, 'test', 1, 1, '2026-01-01 22:18:34', '2026-01-01 22:26:58');
INSERT INTO `tb_product_category_super` VALUES (32, 'test1', 1, 1, '2026-01-01 22:20:30', '2026-01-01 22:26:59');
INSERT INTO `tb_product_category_super` VALUES (33, 'test2', 1, 1, '2026-01-01 22:21:04', '2026-01-01 22:27:07');
INSERT INTO `tb_product_category_super` VALUES (34, 'test3', 1, 1, '2026-01-01 22:21:08', '2026-01-01 22:28:24');
INSERT INTO `tb_product_category_super` VALUES (35, 'test4', 1, 1, '2026-01-01 22:21:12', '2026-01-01 22:28:23');
INSERT INTO `tb_product_category_super` VALUES (36, 'test5', 1, 1, '2026-01-01 22:21:35', '2026-01-01 22:28:21');
INSERT INTO `tb_product_category_super` VALUES (37, 'test6', 1, 1, '2026-01-01 22:27:49', '2026-01-01 22:28:20');
INSERT INTO `tb_product_category_super` VALUES (38, 'test', 1, 1, '2026-02-23 15:33:17', '2026-02-23 15:42:06');
INSERT INTO `tb_product_category_super` VALUES (39, 'test', 1, 1, '2026-02-23 15:48:07', '2026-02-23 15:51:02');
INSERT INTO `tb_product_category_super` VALUES (40, 'test', 1, 1, '2026-02-23 15:54:08', '2026-02-23 15:54:36');

-- ----------------------------
-- Table structure for tb_product_comment_sub
-- ----------------------------
DROP TABLE IF EXISTS `tb_product_comment_sub`;
CREATE TABLE `tb_product_comment_sub`  (
  `sub_id` bigint NOT NULL AUTO_INCREMENT COMMENT '商品评论ID，主键自增',
  `comment_id` bigint NOT NULL COMMENT '评论总表ID，与评论总表关联',
  `stars` bigint NULL DEFAULT NULL COMMENT '商品评价等级，默认为5颗星',
  `sub_create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间，默认为插入时时间戳',
  `sub_update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间，默认为插入时时间戳，插入时自动更新',
  PRIMARY KEY (`sub_id`) USING BTREE,
  INDEX `idx_comment_id`(`comment_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 41 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '商品评论子表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_product_comment_sub
-- ----------------------------
INSERT INTO `tb_product_comment_sub` VALUES (1, 1, 5, '2025-12-12 19:54:53', '2025-12-12 19:54:53');
INSERT INTO `tb_product_comment_sub` VALUES (2, 2, 4, '2025-12-12 19:55:00', '2025-12-12 19:55:00');
INSERT INTO `tb_product_comment_sub` VALUES (3, 3, 5, '2025-12-12 19:55:16', '2025-12-12 19:55:16');
INSERT INTO `tb_product_comment_sub` VALUES (5, 5, 3, '2025-12-12 19:55:34', '2025-12-12 19:55:34');
INSERT INTO `tb_product_comment_sub` VALUES (6, 6, 5, '2025-12-12 19:55:44', '2025-12-12 19:55:44');
INSERT INTO `tb_product_comment_sub` VALUES (7, 7, 4, '2025-12-12 19:55:49', '2025-12-12 19:55:49');
INSERT INTO `tb_product_comment_sub` VALUES (8, 8, 3, '2025-12-12 19:55:55', '2025-12-12 19:55:55');
INSERT INTO `tb_product_comment_sub` VALUES (9, 9, 5, '2025-12-12 19:56:10', '2025-12-12 19:56:10');
INSERT INTO `tb_product_comment_sub` VALUES (10, 10, 4, '2025-12-12 19:56:15', '2025-12-12 19:56:15');
INSERT INTO `tb_product_comment_sub` VALUES (20, 20, 5, '2025-12-13 18:33:21', '2025-12-13 18:33:20');
INSERT INTO `tb_product_comment_sub` VALUES (34, 52, 5, '2025-12-31 17:33:45', '2025-12-31 17:33:45');
INSERT INTO `tb_product_comment_sub` VALUES (37, 69, 5, '2026-01-15 11:45:44', '2026-01-15 11:45:44');
INSERT INTO `tb_product_comment_sub` VALUES (40, 84, 5, '2026-02-14 22:18:03', '2026-02-14 22:18:02');

-- ----------------------------
-- Table structure for tb_product_image
-- ----------------------------
DROP TABLE IF EXISTS `tb_product_image`;
CREATE TABLE `tb_product_image`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '图片ID，主键自增',
  `product_id` bigint NOT NULL COMMENT '商品ID，关联商品总表',
  `image_url` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '图片地址（先用本地的地址，再考虑转为阿里云OSS）',
  `main_flag` tinyint NULL DEFAULT 0 COMMENT '是否为主图，0-否，1-是，默认为0',
  `delete_flag` tinyint NULL DEFAULT 0 COMMENT '删除标记，0-未删除，1-已删除，默认为0',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间，默认为插入时时间戳',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间，默认为插入时时间戳，插入时自动更新',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_product_id`(`product_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 77 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '商品图片表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_product_image
-- ----------------------------
INSERT INTO `tb_product_image` VALUES (1, 1, 'https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/golden_retriever_main.png', 1, 0, '2025-12-08 18:15:22', '2025-12-09 11:54:21');
INSERT INTO `tb_product_image` VALUES (2, 1, 'https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/golden_retriever_1.png', 0, 0, '2025-12-08 18:15:22', '2025-12-09 12:00:35');
INSERT INTO `tb_product_image` VALUES (3, 2, 'https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/british_shorthair_main.png', 1, 0, '2025-12-08 18:15:22', '2025-12-09 14:47:07');
INSERT INTO `tb_product_image` VALUES (4, 3, 'https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/hamster_main.png', 1, 0, '2025-12-08 18:15:22', '2025-12-09 14:52:35');
INSERT INTO `tb_product_image` VALUES (5, 4, 'https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/royal_dog_food_1.png', 0, 0, '2025-12-08 18:15:22', '2025-12-09 17:55:21');
INSERT INTO `tb_product_image` VALUES (6, 5, 'https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/orijen_cat_food_1.png', 0, 0, '2025-12-08 18:15:22', '2025-12-09 17:55:20');
INSERT INTO `tb_product_image` VALUES (7, 1, 'https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/golden_retriever_3.png', 0, 0, '2025-12-09 11:59:59', '2025-12-09 11:59:59');
INSERT INTO `tb_product_image` VALUES (8, 1, 'https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/golden_retriever_4.png', 0, 0, '2025-12-09 12:00:02', '2025-12-09 12:00:02');
INSERT INTO `tb_product_image` VALUES (9, 1, 'https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/golden_retriever_2.png', 0, 0, '2025-12-09 12:00:04', '2025-12-09 12:00:04');
INSERT INTO `tb_product_image` VALUES (10, 2, 'https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/british_shorthair_3.png', 0, 0, '2025-12-09 14:48:55', '2025-12-09 14:48:55');
INSERT INTO `tb_product_image` VALUES (11, 2, 'https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/british_shorthair_4.png', 0, 0, '2025-12-09 14:48:58', '2025-12-09 14:48:58');
INSERT INTO `tb_product_image` VALUES (12, 2, 'https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/british_shorthair_1.png', 0, 0, '2025-12-09 14:49:01', '2025-12-09 14:49:01');
INSERT INTO `tb_product_image` VALUES (13, 2, 'https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/british_shorthair_2.png', 0, 0, '2025-12-09 14:49:04', '2025-12-09 14:49:04');
INSERT INTO `tb_product_image` VALUES (14, 3, 'https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/hamster_2.png', 0, 0, '2025-12-09 14:53:32', '2025-12-09 14:53:32');
INSERT INTO `tb_product_image` VALUES (15, 3, 'https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/hamster_1.png', 0, 0, '2025-12-09 14:53:35', '2025-12-09 14:53:35');
INSERT INTO `tb_product_image` VALUES (16, 4, 'https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/royal_dog_food_2.png', 0, 0, '2025-12-09 14:57:05', '2025-12-09 14:57:05');
INSERT INTO `tb_product_image` VALUES (17, 4, 'https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/royal_dog_food_3.png', 0, 0, '2025-12-09 14:57:14', '2025-12-09 14:57:14');
INSERT INTO `tb_product_image` VALUES (18, 5, 'https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/orijen_cat_food_3.png', 0, 0, '2025-12-09 14:59:58', '2025-12-09 14:59:58');
INSERT INTO `tb_product_image` VALUES (19, 5, 'https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/orijen_cat_food_2.png', 0, 0, '2025-12-09 15:00:00', '2025-12-09 15:00:00');
INSERT INTO `tb_product_image` VALUES (20, 7, 'https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/70a2a219-1138-4507-8e0d-b8a00ea9cdae_Border_Collie_main.png', 1, 0, '2025-12-30 21:28:41', '2025-12-30 21:28:41');
INSERT INTO `tb_product_image` VALUES (21, 7, 'https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/3e0605fd-b0df-4251-accd-2906933808ec_Border_Collie_1.png', 0, 0, '2025-12-30 21:28:49', '2025-12-30 21:28:48');
INSERT INTO `tb_product_image` VALUES (22, 7, 'https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/71f79002-c2fa-4818-9367-ecc49ac071fe_Border_Collie_2.png', 0, 0, '2025-12-30 21:28:51', '2025-12-30 21:28:51');
INSERT INTO `tb_product_image` VALUES (23, 7, 'https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/badab096-4f61-42d5-b566-0e371146b95f_Border_Collie_3.png', 0, 0, '2025-12-30 21:28:54', '2025-12-30 21:28:53');
INSERT INTO `tb_product_image` VALUES (24, 7, 'https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/49dee2d0-87c5-4fd4-b365-3ac2ed79e677_Border_Collie_3.png', 0, 0, '2025-12-30 21:29:11', '2025-12-30 21:29:11');
INSERT INTO `tb_product_image` VALUES (25, 8, 'https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/4f77f25c-8b5a-4e28-b464-b317577c6174_生成边牧图片 (4).png', 1, 0, '2025-12-30 21:55:27', '2025-12-30 21:55:27');
INSERT INTO `tb_product_image` VALUES (26, 8, 'https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/73892db5-e108-40dd-8bde-1e66144db79b_生成边牧图片 (2).png', 0, 0, '2025-12-30 21:55:38', '2025-12-30 21:55:38');
INSERT INTO `tb_product_image` VALUES (27, 8, 'https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/5a4e1043-9b41-4432-995c-03d956ffbb64_生成边牧图片 (1).png', 0, 0, '2025-12-30 21:55:41', '2025-12-30 21:55:41');
INSERT INTO `tb_product_image` VALUES (28, 8, 'https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/db8df870-74bb-4ecb-a659-4f75bf8d4daf_生成边牧图片 (3).png', 0, 0, '2025-12-30 21:55:44', '2025-12-30 21:55:43');
INSERT INTO `tb_product_image` VALUES (29, 8, 'https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/ffc58913-abfc-47cc-94bc-f43301f6fa89_生成边牧图片 (4).png', 0, 0, '2025-12-30 21:55:46', '2025-12-30 21:55:45');
INSERT INTO `tb_product_image` VALUES (30, 8, 'https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/828e22c2-5498-4f62-98c5-cc092a7a2232_生成边牧图片.png', 0, 0, '2025-12-30 21:55:51', '2025-12-30 21:55:50');
INSERT INTO `tb_product_image` VALUES (31, 9, 'https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/a18976d3-db4d-484a-8fdc-94211632a557_Persian_cat_main.png', 1, 0, '2025-12-30 22:03:32', '2025-12-30 22:03:32');
INSERT INTO `tb_product_image` VALUES (32, 9, 'https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/9cbe5376-a7f2-4dfa-8daa-4bfff72ad011_Persian_cat_1.png', 0, 0, '2025-12-30 22:03:43', '2025-12-30 22:03:42');
INSERT INTO `tb_product_image` VALUES (33, 9, 'https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/8a71196d-d236-4af0-bd43-c6d0df44148a_Persian_cat_2.png', 0, 0, '2025-12-30 22:03:45', '2025-12-30 22:03:45');
INSERT INTO `tb_product_image` VALUES (36, 9, 'https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/6a2bf11d-dac8-4285-af81-a709c272df66_Persian_cat_3.png', 0, 0, '2025-12-30 22:04:31', '2025-12-30 22:04:31');
INSERT INTO `tb_product_image` VALUES (37, 10, 'https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/05b7e5b2-29d9-42b3-8004-0d00d5475a02_生成俄罗斯蓝猫图片 (4).png', 1, 0, '2025-12-30 22:22:56', '2025-12-30 22:22:56');
INSERT INTO `tb_product_image` VALUES (38, 10, 'https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/b84ed251-432d-4d9d-aa1f-87e40c92c960_生成俄罗斯蓝猫图片.png', 0, 0, '2025-12-30 22:22:57', '2025-12-30 22:22:56');
INSERT INTO `tb_product_image` VALUES (39, 10, 'https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/c63d5c70-0844-4578-b635-ef9906d45405_生成俄罗斯蓝猫图片 (1).png', 0, 0, '2025-12-30 22:22:57', '2025-12-30 22:22:56');
INSERT INTO `tb_product_image` VALUES (40, 10, 'https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/b25fdc43-72c0-4b7b-a89b-0d8be706e4c3_生成俄罗斯蓝猫图片 (2).png', 0, 0, '2025-12-30 22:22:57', '2025-12-30 22:22:57');
INSERT INTO `tb_product_image` VALUES (41, 10, 'https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/03b2bff6-ee89-4758-a18a-72f59714c2d8_生成俄罗斯蓝猫图片 (3).png', 0, 0, '2025-12-30 22:22:57', '2025-12-30 22:22:57');
INSERT INTO `tb_product_image` VALUES (42, 10, 'https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/f9982097-ab90-4ae1-a49c-4dad7aee82e5_生成俄罗斯蓝猫图片 (5).png', 0, 0, '2025-12-30 22:22:58', '2025-12-30 22:22:57');
INSERT INTO `tb_product_image` VALUES (43, 11, 'https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/a9545f8a-5ca4-45b5-80e4-b778ccf7f629_生成迷你猪图片 (1).png', 0, 0, '2025-12-30 22:32:48', '2025-12-30 22:32:48');
INSERT INTO `tb_product_image` VALUES (44, 11, 'https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/61192eff-4649-439b-8517-f339975b0564_生成迷你猪图片.png', 0, 0, '2025-12-30 22:32:49', '2025-12-30 22:32:48');
INSERT INTO `tb_product_image` VALUES (45, 11, 'https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/d161eaff-23fa-4df3-94cd-b71dcb7a85e6_生成迷你猪图片 (2).png', 0, 0, '2025-12-30 22:32:49', '2025-12-30 22:32:48');
INSERT INTO `tb_product_image` VALUES (46, 14, 'https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/b1a2757f-cae4-4857-a709-a701558ade60_生成猫狗安抚床图片 (3).png', 1, 0, '2025-12-30 22:49:21', '2025-12-30 22:49:21');
INSERT INTO `tb_product_image` VALUES (47, 14, 'https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/45417f11-77fb-47de-ad45-c25c9f695e83_生成猫狗安抚床图片 (1).png', 0, 0, '2025-12-30 22:49:21', '2025-12-30 22:49:21');
INSERT INTO `tb_product_image` VALUES (48, 14, 'https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/33e6edc8-0fe6-4b7d-9dd3-3ad932c61cd2_生成猫狗安抚床图片 (2).png', 0, 0, '2025-12-30 22:49:22', '2025-12-30 22:49:21');
INSERT INTO `tb_product_image` VALUES (50, 14, 'https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/c9d7d411-6372-4bf2-8e0e-83e58e3687fc_生成猫狗安抚床图片.png', 0, 0, '2025-12-30 22:49:22', '2025-12-30 22:49:22');
INSERT INTO `tb_product_image` VALUES (52, 15, 'https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/15d9f8c1-3422-474b-b34b-46fc58409010_生成宠物床图片.png', 0, 0, '2025-12-30 22:59:18', '2025-12-30 22:59:18');
INSERT INTO `tb_product_image` VALUES (53, 15, 'https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/38da6420-a555-4188-9000-5727d1621042_生成宠物床图片 (1).png', 0, 0, '2025-12-30 22:59:18', '2025-12-30 22:59:18');
INSERT INTO `tb_product_image` VALUES (54, 17, 'https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/51a7063b-bd41-4cb9-b134-f31527bf7c9e_生成 Kong Wobbler 图片.png', 0, 0, '2025-12-30 23:13:14', '2025-12-30 23:13:14');
INSERT INTO `tb_product_image` VALUES (55, 17, 'https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/287c34bf-3077-4953-8be4-b15255017ac7_生成 Kong Wobbler 图片 (1).png', 0, 0, '2025-12-30 23:13:15', '2025-12-30 23:13:14');
INSERT INTO `tb_product_image` VALUES (56, 17, 'https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/8441dd90-d0c7-4347-966f-786dc9407103_生成 Kong Wobbler 图片 (3).png', 0, 0, '2025-12-30 23:13:15', '2025-12-30 23:13:14');
INSERT INTO `tb_product_image` VALUES (57, 17, 'https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/5e210f9a-5a5a-4cde-9832-136aec4c1457_生成 Kong Wobbler 图片 (2).png', 0, 0, '2025-12-30 23:13:15', '2025-12-30 23:13:15');
INSERT INTO `tb_product_image` VALUES (70, 32, 'https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/bb64247d-c977-4792-9649-f60c27825cae_default_avatar.png', 0, 0, '2025-12-31 21:33:41', '2025-12-31 22:44:22');
INSERT INTO `tb_product_image` VALUES (71, 32, 'https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/66b34293-de28-4924-a58c-558736739d1d_International-ID-Mark Watney-Back.png', 1, 1, '2025-12-31 22:52:12', '2025-12-31 22:56:18');
INSERT INTO `tb_product_image` VALUES (72, 32, 'https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/6950cf8f-3cb8-4dbc-954e-b8433caa76f2_微信图片_20251222125754_30_54.png', 0, 0, '2025-12-31 22:52:12', '2025-12-31 22:52:11');
INSERT INTO `tb_product_image` VALUES (73, 33, 'https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/cabfa05c-035e-4842-b1d2-3cfda5ec89dc_default_avatar.png', 0, 0, '2026-01-13 21:56:45', '2026-01-13 21:56:45');
INSERT INTO `tb_product_image` VALUES (74, 34, 'https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/52225d28-3d7a-436e-ac05-4726732eead8_default_avatar.png', 0, 0, '2026-01-13 22:07:33', '2026-01-13 22:07:33');
INSERT INTO `tb_product_image` VALUES (75, 35, 'https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/a1fb8b4a-a87d-43de-adce-3bdb282de261_default_avatar.png', 0, 0, '2026-01-15 11:42:19', '2026-01-15 11:42:19');
INSERT INTO `tb_product_image` VALUES (76, 36, 'https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/b3842cb3-3c28-4017-b38b-508ce1895712_unnamed.png', 0, 0, '2026-02-10 20:18:46', '2026-02-10 20:18:46');

-- ----------------------------
-- Table structure for tb_product_super
-- ----------------------------
DROP TABLE IF EXISTS `tb_product_super`;
CREATE TABLE `tb_product_super`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '商品ID，主键自增',
  `identifier` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '商品编号，唯一',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '商品名称，不为空',
  `description` varchar(2048) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '商品描述，不为空',
  `type` bigint NOT NULL COMMENT '商品类型，1-宠物，2-宠物用品',
  `ship_id` bigint NOT NULL COMMENT '发货地址ID，不可为空，与地址表关联',
  `main_category_id` bigint NOT NULL COMMENT '商品分类ID，不可为空，与商品一级分类表关联',
  `sub_category_id` bigint NOT NULL COMMENT '子商品分类ID，不可为空，与商品二级分类表关联',
  `price` decimal(10, 2) NOT NULL COMMENT '商品价格',
  `stock` bigint NULL DEFAULT 1 COMMENT '库存数量，默认为1',
  `status` tinyint NULL DEFAULT 1 COMMENT '商品状态，1-出售中，2-售罄，3-已下架',
  `delete_flag` tinyint NULL DEFAULT 0 COMMENT '删除标记，0-未删除，1-已删除，默认为0',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间，默认为插入时时间戳',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间，默认为插入时时间戳，插入时自动更新',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `identifier`(`identifier` ASC) USING BTREE,
  UNIQUE INDEX `index_identifier`(`identifier` ASC) USING BTREE,
  INDEX `idx_ship_id`(`ship_id` ASC) USING BTREE,
  INDEX `idx_category_id`(`main_category_id` ASC) USING BTREE,
  FULLTEXT INDEX `idx_product_super_name`(`name`) WITH PARSER `ngram`
) ENGINE = InnoDB AUTO_INCREMENT = 37 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '商品总表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_product_super
-- ----------------------------
INSERT INTO `tb_product_super` VALUES (1, '1167114920916020101001', '金毛犬', '活泼友善的金毛犬幼犬', 1, 1, 2, 13, 2500.00, 0, 2, 0, '2025-12-08 18:15:05', '2025-12-17 21:44:52');
INSERT INTO `tb_product_super` VALUES (2, '2569562858668288879710', '英短蓝猫', '温顺可爱的英国短毛猫', 1, 1, 1, 1, 1800.00, 0, 2, 0, '2025-12-08 18:15:05', '2025-12-31 11:44:27');
INSERT INTO `tb_product_super` VALUES (3, '3564095474523243615405', '仓鼠', '可爱的小仓鼠', 1, 1, 7, 41, 35.00, 14, 1, 0, '2025-12-08 18:15:05', '2025-12-31 11:43:53');
INSERT INTO `tb_product_super` VALUES (4, '4875292760308163413660', '皇家狗粮', '适合成年金毛犬的专用狗粮', 2, 1, 9, 54, 320.00, 50, 1, 0, '2025-12-08 18:15:03', '2025-12-14 12:21:50');
INSERT INTO `tb_product_super` VALUES (5, '5978823274740838741714', '渴望猫粮', '高品质全龄猫粮', 2, 1, 9, 54, 198.00, 100, 1, 0, '2025-12-08 18:15:05', '2025-12-17 20:56:05');
INSERT INTO `tb_product_super` VALUES (7, '7665753415520498295206', '边牧犬', '边牧犬是一种原产于英格兰-苏格兰边境的中等体型牧羊犬，以极高的智商和工作能力著称，既能出色放牧又充满活力与敏捷性。', 1, 19, 2, 17, 3500.00, 4, 1, 0, '2025-12-30 21:28:25', '2026-01-05 20:42:37');
INSERT INTO `tb_product_super` VALUES (8, '8532955057470544904336', '柯基', '柯基是一种原产于威尔士的小型牧牛犬，以其短腿长身、聪明活泼、忠诚友好且适合作为伴侣犬而深受喜爱。', 1, 24, 2, 15, 2000.00, 10, 1, 0, '2025-12-30 21:54:50', '2025-12-30 21:54:50');
INSERT INTO `tb_product_super` VALUES (9, '9436552968718363063691', '波斯猫', '波斯猫是一种以豪华的长毛、扁平圆脸和温柔、安静、亲人性格著称的中大型家猫，深受爱猫人士喜爱。', 1, 25, 1, 3, 3000.00, 3, 1, 0, '2025-12-30 22:03:26', '2026-01-05 23:12:48');
INSERT INTO `tb_product_super` VALUES (10, '10284542232140828405862', '俄罗斯蓝猫', '俄罗斯蓝猫是一种拥有密集丝滑的银蓝色短毛、鲜明绿眼睛和优雅体态的中等体型家猫，性格温柔安静、聪明且对家人深情忠诚。', 1, 26, 1, 9, 1200.00, 10, 1, 0, '2025-12-30 22:21:30', '2025-12-30 22:21:30');
INSERT INTO `tb_product_super` VALUES (11, '11147146413759612950499', '迷你猪', '迷你猪是一种因体型明显小于普通家猪（成年体重大多远低于商业猪）而被人们作为宠物或伴侣动物饲养的猪种/分类，通常体形紧凑、外观可爱且性格温和聪明。', 1, 27, 3, 21, 900.00, 5, 1, 0, '2025-12-30 22:32:17', '2025-12-30 22:32:17');
INSERT INTO `tb_product_super` VALUES (14, '14445724584435481211474', 'Rabbitgoo Calming Pet Bed', '高评分舒适款，适合猫狗通用的安抚床，柔软支撑让宠物放松休息', 2, 30, 11, 62, 211.14, 7, 1, 0, '2025-12-30 22:45:53', '2026-01-05 23:14:05');
INSERT INTO `tb_product_super` VALUES (15, '15874463522335127893568', 'Scruffs Chester Box Bed', '经典布艺方形床，支撑舒适、评价好，适合多数中小型宠物', 2, 31, 11, 62, 502.00, 4, 1, 0, '2025-12-30 22:57:44', '2025-12-30 22:57:44');
INSERT INTO `tb_product_super` VALUES (32, '32508767049727928109832', 'test', 'test', 1, 51, 2, 12, 10.00, 1, 3, 1, '2025-12-31 21:33:36', '2026-01-01 22:03:39');
INSERT INTO `tb_product_super` VALUES (33, '33058695087829138846371', 'test', 'test', 1, 61, 3, 21, 1.00, 1, 3, 1, '2026-01-13 21:56:38', '2026-01-13 22:01:55');
INSERT INTO `tb_product_super` VALUES (34, '34828048091903169561385', 'test', 'test', 1, 62, 2, 11, 1.00, 1, 3, 1, '2026-01-13 22:07:29', '2026-01-13 22:08:54');
INSERT INTO `tb_product_super` VALUES (35, '35768187044111968219814', 'test', 'test', 1, 63, 2, 11, 1.00, 1, 1, 0, '2026-01-15 11:42:15', '2026-01-15 11:42:15');
INSERT INTO `tb_product_super` VALUES (36, '36511291997691913197318', 'es test1', 'es test', 2, 64, 9, 53, 1.00, 1, 1, 0, '2026-02-10 20:18:23', '2026-02-10 20:36:06');

-- ----------------------------
-- Table structure for tb_refund_info
-- ----------------------------
DROP TABLE IF EXISTS `tb_refund_info`;
CREATE TABLE `tb_refund_info`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_id` bigint NOT NULL COMMENT '订单ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `message` varchar(2048) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '退款原因',
  `delete_flag` tinyint NULL DEFAULT 0 COMMENT '删除标记：0-未删除，1-已删除',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 12 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '退款信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_refund_info
-- ----------------------------
INSERT INTO `tb_refund_info` VALUES (3, 16, 1, '不想要了', 1, '2026-01-04 19:42:26', '2026-01-04 20:06:20');
INSERT INTO `tb_refund_info` VALUES (4, 22, 1, '不好看', 1, '2026-01-04 19:42:31', '2026-01-04 20:07:16');
INSERT INTO `tb_refund_info` VALUES (5, 22, 1, '不想要了', 1, '2026-01-04 20:07:37', '2026-01-04 20:07:47');
INSERT INTO `tb_refund_info` VALUES (6, 22, 1, '不想要了', 1, '2026-01-05 16:42:42', '2026-01-05 16:42:55');
INSERT INTO `tb_refund_info` VALUES (7, 45, 1, '不想要了', 1, '2026-01-05 21:57:59', '2026-01-05 21:58:15');
INSERT INTO `tb_refund_info` VALUES (8, 48, 1, '不想要了', 1, '2026-01-05 22:03:13', '2026-01-05 22:04:25');
INSERT INTO `tb_refund_info` VALUES (9, 50, 1, '不想要了', 1, '2026-02-14 21:11:23', '2026-02-14 21:12:04');
INSERT INTO `tb_refund_info` VALUES (10, 50, 1, '不想要了', 1, '2026-02-14 21:31:25', '2026-02-14 21:31:34');
INSERT INTO `tb_refund_info` VALUES (11, 49, 1, '商品与描述不符', 1, '2026-02-14 21:32:44', '2026-02-14 21:32:50');

-- ----------------------------
-- Table structure for tb_role
-- ----------------------------
DROP TABLE IF EXISTS `tb_role`;
CREATE TABLE `tb_role`  (
  `id` bigint NOT NULL COMMENT '角色ID，唯一且不为空',
  `name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '角色名称，不能为空',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间，默认为插入时时间戳',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间，默认为插入时时间戳，插入时自动更新',
  UNIQUE INDEX `id`(`id` ASC) USING BTREE,
  UNIQUE INDEX `name`(`name` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '角色表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_role
-- ----------------------------
INSERT INTO `tb_role` VALUES (0, 'ADMIN', '2025-12-08 09:53:26', '2025-12-08 11:34:35');
INSERT INTO `tb_role` VALUES (1, 'NORMAL', '2025-12-08 09:53:26', '2025-12-08 11:34:39');

-- ----------------------------
-- Table structure for tb_sensitive_word
-- ----------------------------
DROP TABLE IF EXISTS `tb_sensitive_word`;
CREATE TABLE `tb_sensitive_word`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '敏感词ID，唯一且不为空',
  `word` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '敏感词名称，不能为空',
  `category_id` bigint NOT NULL COMMENT '敏感词分类ID',
  `delete_flag` tinyint NULL DEFAULT 0 COMMENT '删除标记，0-未删除，1-已删除',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间，默认为插入时时间戳',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间，默认为插入时时间戳，插入时自动更新',
  UNIQUE INDEX `id`(`id` ASC) USING BTREE,
  UNIQUE INDEX `word`(`word` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3362 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '敏感词表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_sensitive_word
-- ----------------------------
INSERT INTO `tb_sensitive_word` VALUES (1, '福音会', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (2, '中国教徒', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (3, '统一教', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (4, '观音法门', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (5, '清海无上师', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (6, '盘古', 2, 1, '2026-01-10 17:23:55', '2026-01-11 20:02:19');
INSERT INTO `tb_sensitive_word` VALUES (7, '李洪志', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (8, '志洪李', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (9, '李宏志', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (10, '轮功', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (11, '法轮', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (12, '轮法功', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (13, '三去车仑', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (14, '氵去车仑', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (15, '发论工', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (16, '法x功', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (17, '法o功', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (18, '法0功', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (19, '法一轮一功', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (20, '轮子功', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (21, '车仑工力', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (22, '法lun', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (23, 'fa轮', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (24, '法lg', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (25, 'flg', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (26, 'fl功', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (27, 'falungong', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (28, '大法弟子', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (29, '大纪元', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (30, 'dajiyuan', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (31, '明慧网', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (32, '明慧周报', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (33, '正见网', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (34, '新唐人', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (35, '伪火', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (36, '退党', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (37, 'tuidang', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (38, '退dang', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (39, '超越红墙', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (40, '自fen', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (41, '真善忍', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (42, '九评', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (43, '9评', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (44, '9ping', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (45, '九ping', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (46, 'jiuping', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (47, '藏字石', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (48, '集体自杀', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (49, '自sha', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (50, 'zi杀', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (51, 'suicide', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (52, 'titor', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (53, '逢8必灾', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (54, '逢八必灾', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (55, '逢9必乱', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (56, '逢九必乱', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (57, '朱瑟里诺', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (58, '根达亚文明', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (59, '诺查丹玛斯', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (60, '人类灭亡进程表', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (61, '按照马雅历法', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (62, '推背图', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (63, '推bei图', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (64, '济世灵文', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (65, '诸世纪', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (66, '电狗', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (67, '电话定位器', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (68, '电话拦截器', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (69, '电话窃听', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (70, '电话监', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (71, '电话交友', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (72, '电话追杀系统', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (73, '电击枪', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (74, '电鸡', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (75, '电警棒', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (76, '枪出售', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (77, '枪的制', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (78, '枪货到', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (79, '枪决女犯', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (80, '枪模', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (81, '枪手', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (82, '枪销售', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (83, '枪械制', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (84, '枪子弹', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (85, '售步枪', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (86, '售纯度', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (87, '售单管', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (88, '售弹簧刀', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (89, '售防身', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (90, '售狗子', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (91, '售虎头', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (92, '售火药', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (93, '售假币', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (94, '售健卫', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (95, '售军用', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (96, '售猎枪', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (97, '售氯胺', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (98, '售麻醉', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (99, '售枪支', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (100, '售热武', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (101, '售三棱', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (102, '售手枪', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (103, '售五四', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (104, '售一元硬', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (105, '售子弹', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (106, '售左轮', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (107, '亚砷（酸）酐', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (108, '亚砷酸钾', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (109, '亚砷酸钠', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (110, '亚硒酸', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (111, '亚硒酸二钠', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (112, '亚硒酸镁', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (113, '亚硒酸钠', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (114, '亚硒酸氢钠', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (115, '亚硝酸乙酯', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (116, '亚硝酰乙氧', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (117, '氧化二丁基锡', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (118, '氧化汞', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (119, '氧化铊', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (120, '氧化亚铊', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (121, '氧氯化磷', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (122, '原装弹', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (123, '原子弹方法', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (124, '原子弹清单', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (125, '安眠酮', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (126, '代血浆', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (127, '普萘洛尔', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (128, '呋塞米', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (129, '西布曲明', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (130, 'testosterone', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (131, '胰岛素样生长因子', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (132, '促红细胞生成素', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (133, '地西泮', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (134, '尼可刹米', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (135, '甲睾酮', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (136, 'adrenaline', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (137, 'erythropoietin', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (138, '地奈德', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (139, '莫达非尼', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (140, '氯噻嗪', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (141, '苯巴比妥', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (142, '促性腺激素', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (143, '泼尼松', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (144, '麻黄草', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (145, '雄烯二醇', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (146, '地塞米松', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (147, 'tamoxifen', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (148, 'strychnine', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (149, 'androst', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (150, '新型毒品', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (151, '杜冷丁', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (152, '兴奋剂', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (153, 'mdma', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (154, '海洛因', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (155, '海luo因', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (156, 'heroin', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (157, 'diamorphine', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (158, 'diacetylmorphine', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (159, '鸦片', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (160, '阿芙蓉', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (161, '咖啡因', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (162, 'cocain', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (163, '三唑仑', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (164, '美沙酮', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (165, '麻古', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (166, 'k粉', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (167, '凯他敏', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (168, 'ketamine', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (169, '冰毒', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (170, '苯丙胺', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (171, 'cannabis', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (172, '大麻', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (173, '爱他死', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (174, '氯胺酮', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (175, 'benzodiazepines', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (176, '甲基安非他明', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (177, '吗啡', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (178, '安非他命', 2, 0, '2026-01-10 17:23:55', '2026-01-10 17:23:55');
INSERT INTO `tb_sensitive_word` VALUES (179, '腐败中国', 1, 0, '2026-01-10 17:25:01', '2026-01-10 17:25:01');
INSERT INTO `tb_sensitive_word` VALUES (180, '三个呆婊', 1, 0, '2026-01-10 17:25:01', '2026-01-10 17:25:01');
INSERT INTO `tb_sensitive_word` VALUES (181, '你办事我放心', 1, 0, '2026-01-10 17:25:01', '2026-01-10 17:25:01');
INSERT INTO `tb_sensitive_word` VALUES (182, '社会主义灭亡', 1, 0, '2026-01-10 17:25:01', '2026-01-10 17:25:01');
INSERT INTO `tb_sensitive_word` VALUES (183, '打倒中国', 1, 0, '2026-01-10 17:25:01', '2026-01-10 17:25:01');
INSERT INTO `tb_sensitive_word` VALUES (184, '打倒共产党', 1, 0, '2026-01-10 17:25:01', '2026-01-10 17:25:01');
INSERT INTO `tb_sensitive_word` VALUES (185, '打倒共产主义', 1, 0, '2026-01-10 17:25:01', '2026-01-10 17:25:01');
INSERT INTO `tb_sensitive_word` VALUES (186, '打倒胡锦涛', 1, 0, '2026-01-10 17:25:01', '2026-01-10 17:25:01');
INSERT INTO `tb_sensitive_word` VALUES (187, '打倒江泽民', 1, 0, '2026-01-10 17:25:01', '2026-01-10 17:25:01');
INSERT INTO `tb_sensitive_word` VALUES (188, '打倒江主席', 1, 0, '2026-01-10 17:25:01', '2026-01-10 17:25:01');
INSERT INTO `tb_sensitive_word` VALUES (189, '打倒李鹏', 1, 0, '2026-01-10 17:25:01', '2026-01-10 17:25:01');
INSERT INTO `tb_sensitive_word` VALUES (190, '打倒罗干', 1, 0, '2026-01-10 17:25:01', '2026-01-10 17:25:01');
INSERT INTO `tb_sensitive_word` VALUES (191, '打倒温家宝', 1, 0, '2026-01-10 17:25:01', '2026-01-10 17:25:01');
INSERT INTO `tb_sensitive_word` VALUES (192, '打倒中共', 1, 0, '2026-01-10 17:25:01', '2026-01-10 17:25:01');
INSERT INTO `tb_sensitive_word` VALUES (193, '打倒朱镕', 1, 0, '2026-01-10 17:25:01', '2026-01-10 17:25:01');
INSERT INTO `tb_sensitive_word` VALUES (194, '抵制共产党', 1, 0, '2026-01-10 17:25:01', '2026-01-10 17:25:01');
INSERT INTO `tb_sensitive_word` VALUES (195, '抵制共产主义', 1, 0, '2026-01-10 17:25:01', '2026-01-10 17:25:01');
INSERT INTO `tb_sensitive_word` VALUES (196, '抵制胡锦涛', 1, 0, '2026-01-10 17:25:01', '2026-01-10 17:25:01');
INSERT INTO `tb_sensitive_word` VALUES (197, '抵制江泽民', 1, 0, '2026-01-10 17:25:01', '2026-01-10 17:25:01');
INSERT INTO `tb_sensitive_word` VALUES (198, '抵制江主席', 1, 0, '2026-01-10 17:25:01', '2026-01-10 17:25:01');
INSERT INTO `tb_sensitive_word` VALUES (199, '抵制李鹏', 1, 0, '2026-01-10 17:25:01', '2026-01-10 17:25:01');
INSERT INTO `tb_sensitive_word` VALUES (200, '抵制罗干', 1, 0, '2026-01-10 17:25:01', '2026-01-10 17:25:01');
INSERT INTO `tb_sensitive_word` VALUES (201, '抵制温家宝', 1, 0, '2026-01-10 17:25:01', '2026-01-10 17:25:01');
INSERT INTO `tb_sensitive_word` VALUES (202, '抵制中共', 1, 0, '2026-01-10 17:25:01', '2026-01-10 17:25:01');
INSERT INTO `tb_sensitive_word` VALUES (203, '抵制朱镕基', 1, 0, '2026-01-10 17:25:01', '2026-01-10 17:25:01');
INSERT INTO `tb_sensitive_word` VALUES (204, '灭亡中国', 1, 0, '2026-01-10 17:25:01', '2026-01-10 17:25:01');
INSERT INTO `tb_sensitive_word` VALUES (205, '亡党亡国', 1, 0, '2026-01-10 17:25:01', '2026-01-10 17:25:01');
INSERT INTO `tb_sensitive_word` VALUES (206, '粉碎四人帮', 1, 0, '2026-01-10 17:25:01', '2026-01-10 17:25:01');
INSERT INTO `tb_sensitive_word` VALUES (207, '激流中国', 1, 0, '2026-01-10 17:25:01', '2026-01-10 17:25:01');
INSERT INTO `tb_sensitive_word` VALUES (208, '特供', 1, 0, '2026-01-10 17:25:01', '2026-01-10 17:25:01');
INSERT INTO `tb_sensitive_word` VALUES (209, '特贡', 1, 0, '2026-01-10 17:25:01', '2026-01-10 17:25:01');
INSERT INTO `tb_sensitive_word` VALUES (210, '特共', 1, 0, '2026-01-10 17:25:01', '2026-01-10 17:25:01');
INSERT INTO `tb_sensitive_word` VALUES (211, 'zf大楼', 1, 0, '2026-01-10 17:25:01', '2026-01-10 17:25:01');
INSERT INTO `tb_sensitive_word` VALUES (212, '殃视', 1, 0, '2026-01-10 17:25:01', '2026-01-10 17:25:01');
INSERT INTO `tb_sensitive_word` VALUES (213, '贪污腐败', 1, 0, '2026-01-10 17:25:01', '2026-01-10 17:25:01');
INSERT INTO `tb_sensitive_word` VALUES (214, '强制拆除', 1, 0, '2026-01-10 17:25:01', '2026-01-10 17:25:01');
INSERT INTO `tb_sensitive_word` VALUES (215, '形式主义', 1, 0, '2026-01-10 17:25:01', '2026-01-10 17:25:01');
INSERT INTO `tb_sensitive_word` VALUES (216, '政治风波', 1, 0, '2026-01-10 17:25:01', '2026-01-10 17:25:01');
INSERT INTO `tb_sensitive_word` VALUES (217, '太子党', 1, 0, '2026-01-10 17:25:01', '2026-01-10 17:25:01');
INSERT INTO `tb_sensitive_word` VALUES (218, '上海帮', 1, 0, '2026-01-10 17:25:01', '2026-01-10 17:25:01');
INSERT INTO `tb_sensitive_word` VALUES (219, '北京帮', 1, 0, '2026-01-10 17:25:01', '2026-01-10 17:25:01');
INSERT INTO `tb_sensitive_word` VALUES (220, '清华帮', 1, 0, '2026-01-10 17:25:01', '2026-01-10 17:25:01');
INSERT INTO `tb_sensitive_word` VALUES (221, '红色贵族', 1, 0, '2026-01-10 17:25:01', '2026-01-10 17:25:01');
INSERT INTO `tb_sensitive_word` VALUES (222, '权贵集团', 1, 0, '2026-01-10 17:25:01', '2026-01-10 17:25:01');
INSERT INTO `tb_sensitive_word` VALUES (223, '河蟹社会', 1, 0, '2026-01-10 17:25:01', '2026-01-10 17:25:01');
INSERT INTO `tb_sensitive_word` VALUES (224, '喝血社会', 1, 0, '2026-01-10 17:25:01', '2026-01-10 17:25:01');
INSERT INTO `tb_sensitive_word` VALUES (225, '九风', 1, 0, '2026-01-10 17:25:01', '2026-01-10 17:25:01');
INSERT INTO `tb_sensitive_word` VALUES (226, '9风', 1, 0, '2026-01-10 17:25:01', '2026-01-10 17:25:01');
INSERT INTO `tb_sensitive_word` VALUES (227, '十七大', 1, 0, '2026-01-10 17:25:01', '2026-01-10 17:25:01');
INSERT INTO `tb_sensitive_word` VALUES (228, '十7大', 1, 0, '2026-01-10 17:25:01', '2026-01-10 17:25:01');
INSERT INTO `tb_sensitive_word` VALUES (229, '17da', 1, 0, '2026-01-10 17:25:01', '2026-01-10 17:25:01');
INSERT INTO `tb_sensitive_word` VALUES (230, '九学', 1, 0, '2026-01-10 17:25:01', '2026-01-10 17:25:01');
INSERT INTO `tb_sensitive_word` VALUES (231, '9学', 1, 0, '2026-01-10 17:25:01', '2026-01-10 17:25:01');
INSERT INTO `tb_sensitive_word` VALUES (232, '四风', 1, 0, '2026-01-10 17:25:01', '2026-01-10 17:25:01');
INSERT INTO `tb_sensitive_word` VALUES (233, '4风', 1, 0, '2026-01-10 17:25:01', '2026-01-10 17:25:01');
INSERT INTO `tb_sensitive_word` VALUES (234, '双规', 1, 0, '2026-01-10 17:25:01', '2026-01-10 17:25:01');
INSERT INTO `tb_sensitive_word` VALUES (235, '南街村', 1, 0, '2026-01-10 17:25:01', '2026-01-10 17:25:01');
INSERT INTO `tb_sensitive_word` VALUES (236, '最淫官员', 1, 0, '2026-01-10 17:25:01', '2026-01-10 17:25:01');
INSERT INTO `tb_sensitive_word` VALUES (237, '警匪', 1, 0, '2026-01-10 17:25:01', '2026-01-10 17:25:01');
INSERT INTO `tb_sensitive_word` VALUES (238, '官匪', 1, 0, '2026-01-10 17:25:01', '2026-01-10 17:25:01');
INSERT INTO `tb_sensitive_word` VALUES (239, '独夫民贼', 1, 0, '2026-01-10 17:25:01', '2026-01-10 17:25:01');
INSERT INTO `tb_sensitive_word` VALUES (240, '官商勾结', 1, 0, '2026-01-10 17:25:01', '2026-01-10 17:25:01');
INSERT INTO `tb_sensitive_word` VALUES (241, '城管暴力执法', 1, 0, '2026-01-10 17:25:01', '2026-01-10 17:25:01');
INSERT INTO `tb_sensitive_word` VALUES (242, '强制捐款', 1, 0, '2026-01-10 17:25:01', '2026-01-10 17:25:01');
INSERT INTO `tb_sensitive_word` VALUES (243, '毒豺', 1, 0, '2026-01-10 17:25:01', '2026-01-10 17:25:01');
INSERT INTO `tb_sensitive_word` VALUES (244, '一党执政', 1, 0, '2026-01-10 17:25:01', '2026-01-10 17:25:01');
INSERT INTO `tb_sensitive_word` VALUES (245, '一党专制', 1, 0, '2026-01-10 17:25:01', '2026-01-10 17:25:01');
INSERT INTO `tb_sensitive_word` VALUES (246, '一党专政', 1, 0, '2026-01-10 17:25:01', '2026-01-10 17:25:01');
INSERT INTO `tb_sensitive_word` VALUES (247, '专制政权', 1, 0, '2026-01-10 17:25:01', '2026-01-10 17:25:01');
INSERT INTO `tb_sensitive_word` VALUES (248, '宪法法院', 1, 0, '2026-01-10 17:25:01', '2026-01-10 17:25:01');
INSERT INTO `tb_sensitive_word` VALUES (249, '胡平', 1, 0, '2026-01-10 17:25:01', '2026-01-10 17:25:01');
INSERT INTO `tb_sensitive_word` VALUES (250, '苏晓康', 1, 0, '2026-01-10 17:25:01', '2026-01-10 17:25:01');
INSERT INTO `tb_sensitive_word` VALUES (251, '贺卫方', 1, 0, '2026-01-10 17:25:01', '2026-01-10 17:25:01');
INSERT INTO `tb_sensitive_word` VALUES (252, '谭作人', 1, 0, '2026-01-10 17:25:01', '2026-01-10 17:25:01');
INSERT INTO `tb_sensitive_word` VALUES (253, '焦国标', 1, 0, '2026-01-10 17:25:01', '2026-01-10 17:25:01');
INSERT INTO `tb_sensitive_word` VALUES (254, '万润南', 1, 0, '2026-01-10 17:25:01', '2026-01-10 17:25:01');
INSERT INTO `tb_sensitive_word` VALUES (255, '张志新', 1, 0, '2026-01-10 17:25:01', '2026-01-10 17:25:01');
INSERT INTO `tb_sensitive_word` VALUES (256, '辛灏年', 1, 0, '2026-01-10 17:25:01', '2026-01-10 17:25:01');
INSERT INTO `tb_sensitive_word` VALUES (257, '高勤荣', 1, 0, '2026-01-10 17:25:01', '2026-01-10 17:25:01');
INSERT INTO `tb_sensitive_word` VALUES (258, '王炳章', 1, 0, '2026-01-10 17:25:01', '2026-01-10 17:25:01');
INSERT INTO `tb_sensitive_word` VALUES (259, '高智晟', 1, 0, '2026-01-10 17:25:01', '2026-01-10 17:25:01');
INSERT INTO `tb_sensitive_word` VALUES (260, '司马璐', 1, 0, '2026-01-10 17:25:01', '2026-01-10 17:25:01');
INSERT INTO `tb_sensitive_word` VALUES (261, '刘晓竹', 1, 0, '2026-01-10 17:25:01', '2026-01-10 17:25:01');
INSERT INTO `tb_sensitive_word` VALUES (262, '刘宾雁', 1, 0, '2026-01-10 17:25:01', '2026-01-10 17:25:01');
INSERT INTO `tb_sensitive_word` VALUES (263, '魏京生', 1, 0, '2026-01-10 17:25:01', '2026-01-10 17:25:01');
INSERT INTO `tb_sensitive_word` VALUES (264, '寻找林昭的灵魂', 1, 0, '2026-01-10 17:25:01', '2026-01-10 17:25:01');
INSERT INTO `tb_sensitive_word` VALUES (265, '别梦成灰', 1, 0, '2026-01-10 17:25:01', '2026-01-10 17:25:01');
INSERT INTO `tb_sensitive_word` VALUES (266, '谁是新中国', 1, 0, '2026-01-10 17:25:01', '2026-01-10 17:25:01');
INSERT INTO `tb_sensitive_word` VALUES (267, '讨伐中宣部', 1, 0, '2026-01-10 17:25:01', '2026-01-10 17:25:01');
INSERT INTO `tb_sensitive_word` VALUES (268, '异议人士', 1, 0, '2026-01-10 17:25:01', '2026-01-10 17:25:01');
INSERT INTO `tb_sensitive_word` VALUES (269, '民运人士', 1, 0, '2026-01-10 17:25:01', '2026-01-10 17:25:01');
INSERT INTO `tb_sensitive_word` VALUES (270, '启蒙派', 1, 0, '2026-01-10 17:25:01', '2026-01-10 17:25:01');
INSERT INTO `tb_sensitive_word` VALUES (271, '选国家主席', 1, 0, '2026-01-10 17:25:01', '2026-01-10 17:25:01');
INSERT INTO `tb_sensitive_word` VALUES (272, '民一主', 1, 0, '2026-01-10 17:25:01', '2026-01-10 17:25:01');
INSERT INTO `tb_sensitive_word` VALUES (273, 'min主', 1, 0, '2026-01-10 17:25:01', '2026-01-10 17:25:01');
INSERT INTO `tb_sensitive_word` VALUES (274, '民竹', 1, 0, '2026-01-10 17:25:01', '2026-01-10 17:25:01');
INSERT INTO `tb_sensitive_word` VALUES (275, '民珠', 1, 0, '2026-01-10 17:25:01', '2026-01-10 17:25:01');
INSERT INTO `tb_sensitive_word` VALUES (276, '民猪', 1, 0, '2026-01-10 17:25:01', '2026-01-10 17:25:01');
INSERT INTO `tb_sensitive_word` VALUES (277, 'chinesedemocracy', 1, 0, '2026-01-10 17:25:01', '2026-01-10 17:25:01');
INSERT INTO `tb_sensitive_word` VALUES (278, '大赦国际', 1, 0, '2026-01-10 17:25:01', '2026-01-10 17:25:01');
INSERT INTO `tb_sensitive_word` VALUES (279, '国际特赦', 1, 0, '2026-01-10 17:25:01', '2026-01-10 17:25:01');
INSERT INTO `tb_sensitive_word` VALUES (280, 'da选', 1, 0, '2026-01-10 17:25:01', '2026-01-10 17:25:01');
INSERT INTO `tb_sensitive_word` VALUES (281, '投公', 1, 0, '2026-01-10 17:25:01', '2026-01-10 17:25:01');
INSERT INTO `tb_sensitive_word` VALUES (282, '公头', 1, 0, '2026-01-10 17:25:01', '2026-01-10 17:25:01');
INSERT INTO `tb_sensitive_word` VALUES (283, '宪政', 1, 0, '2026-01-10 17:25:01', '2026-01-10 17:25:01');
INSERT INTO `tb_sensitive_word` VALUES (284, '平反', 1, 0, '2026-01-10 17:25:01', '2026-01-10 17:25:01');
INSERT INTO `tb_sensitive_word` VALUES (285, '党章', 1, 0, '2026-01-10 17:25:01', '2026-01-10 17:25:01');
INSERT INTO `tb_sensitive_word` VALUES (286, '维权', 1, 0, '2026-01-10 17:25:01', '2026-01-10 17:25:01');
INSERT INTO `tb_sensitive_word` VALUES (287, '昝爱宗', 1, 0, '2026-01-10 17:25:01', '2026-01-10 17:25:01');
INSERT INTO `tb_sensitive_word` VALUES (288, '宪章', 1, 0, '2026-01-10 17:25:01', '2026-01-10 17:25:01');
INSERT INTO `tb_sensitive_word` VALUES (289, '08宪', 1, 0, '2026-01-10 17:25:01', '2026-01-10 17:25:01');
INSERT INTO `tb_sensitive_word` VALUES (290, '08xz', 1, 0, '2026-01-10 17:25:01', '2026-01-10 17:25:01');
INSERT INTO `tb_sensitive_word` VALUES (291, '抿主', 1, 0, '2026-01-10 17:25:01', '2026-01-10 17:25:01');
INSERT INTO `tb_sensitive_word` VALUES (292, '敏主', 1, 0, '2026-01-10 17:25:01', '2026-01-10 17:25:01');
INSERT INTO `tb_sensitive_word` VALUES (293, '人拳', 1, 0, '2026-01-10 17:25:01', '2026-01-10 17:25:01');
INSERT INTO `tb_sensitive_word` VALUES (294, '人木又', 1, 0, '2026-01-10 17:25:01', '2026-01-10 17:25:01');
INSERT INTO `tb_sensitive_word` VALUES (295, '人quan', 1, 0, '2026-01-10 17:25:01', '2026-01-10 17:25:01');
INSERT INTO `tb_sensitive_word` VALUES (296, 'renquan', 1, 0, '2026-01-10 17:25:01', '2026-01-10 17:25:01');
INSERT INTO `tb_sensitive_word` VALUES (297, '中国人权', 1, 0, '2026-01-10 17:25:01', '2026-01-10 17:25:01');
INSERT INTO `tb_sensitive_word` VALUES (298, '中国新民党', 1, 0, '2026-01-10 17:25:01', '2026-01-10 17:25:01');
INSERT INTO `tb_sensitive_word` VALUES (299, '群体事件', 1, 0, '2026-01-10 17:25:01', '2026-01-10 17:25:01');
INSERT INTO `tb_sensitive_word` VALUES (300, '群体性事件', 1, 0, '2026-01-10 17:25:01', '2026-01-10 17:25:01');
INSERT INTO `tb_sensitive_word` VALUES (301, '上中央', 1, 0, '2026-01-10 17:25:01', '2026-01-10 17:25:01');
INSERT INTO `tb_sensitive_word` VALUES (302, '去中央', 1, 0, '2026-01-10 17:25:01', '2026-01-10 17:25:01');
INSERT INTO `tb_sensitive_word` VALUES (303, '讨说法', 1, 0, '2026-01-10 17:25:01', '2026-01-10 17:25:01');
INSERT INTO `tb_sensitive_word` VALUES (304, '请愿', 1, 0, '2026-01-10 17:25:01', '2026-01-10 17:25:01');
INSERT INTO `tb_sensitive_word` VALUES (305, '请命', 1, 0, '2026-01-10 17:25:01', '2026-01-10 17:25:01');
INSERT INTO `tb_sensitive_word` VALUES (306, '公开信', 1, 0, '2026-01-10 17:25:01', '2026-01-10 17:25:01');
INSERT INTO `tb_sensitive_word` VALUES (307, '联名上书', 1, 0, '2026-01-10 17:25:01', '2026-01-10 17:25:01');
INSERT INTO `tb_sensitive_word` VALUES (308, '万人大签名', 1, 0, '2026-01-10 17:25:01', '2026-01-10 17:25:01');
INSERT INTO `tb_sensitive_word` VALUES (309, '万人骚动', 1, 0, '2026-01-10 17:25:01', '2026-01-10 17:25:01');
INSERT INTO `tb_sensitive_word` VALUES (310, '截访', 1, 0, '2026-01-10 17:25:01', '2026-01-10 17:25:01');
INSERT INTO `tb_sensitive_word` VALUES (311, '上访', 1, 0, '2026-01-10 17:25:01', '2026-01-10 17:25:01');
INSERT INTO `tb_sensitive_word` VALUES (312, 'shangfang', 1, 0, '2026-01-10 17:25:01', '2026-01-10 17:25:01');
INSERT INTO `tb_sensitive_word` VALUES (313, '信访', 1, 0, '2026-01-10 17:25:01', '2026-01-10 17:25:01');
INSERT INTO `tb_sensitive_word` VALUES (314, '访民', 1, 0, '2026-01-10 17:25:01', '2026-01-10 17:25:01');
INSERT INTO `tb_sensitive_word` VALUES (681, '网络', 5, 0, '2026-01-10 17:26:43', '2026-01-10 17:26:43');
INSERT INTO `tb_sensitive_word` VALUES (682, 'QQ', 5, 0, '2026-01-10 17:26:43', '2026-01-10 17:26:43');
INSERT INTO `tb_sensitive_word` VALUES (683, '招聘', 5, 0, '2026-01-10 17:26:43', '2026-01-10 17:26:43');
INSERT INTO `tb_sensitive_word` VALUES (684, '有意者', 5, 0, '2026-01-10 17:26:43', '2026-01-10 17:26:43');
INSERT INTO `tb_sensitive_word` VALUES (685, '到货', 5, 0, '2026-01-10 17:26:43', '2026-01-10 17:26:43');
INSERT INTO `tb_sensitive_word` VALUES (686, '本店', 5, 0, '2026-01-10 17:26:43', '2026-01-10 17:26:43');
INSERT INTO `tb_sensitive_word` VALUES (687, '代购', 5, 0, '2026-01-10 17:26:43', '2026-01-10 17:26:43');
INSERT INTO `tb_sensitive_word` VALUES (688, '扣扣', 5, 0, '2026-01-10 17:26:43', '2026-01-10 17:26:43');
INSERT INTO `tb_sensitive_word` VALUES (689, '客服', 5, 0, '2026-01-10 17:26:43', '2026-01-10 17:26:43');
INSERT INTO `tb_sensitive_word` VALUES (690, '微店', 5, 0, '2026-01-10 17:26:43', '2026-01-10 17:26:43');
INSERT INTO `tb_sensitive_word` VALUES (691, '兼职', 5, 0, '2026-01-10 17:26:43', '2026-01-10 17:26:43');
INSERT INTO `tb_sensitive_word` VALUES (692, '兼值', 5, 0, '2026-01-10 17:26:43', '2026-01-10 17:26:43');
INSERT INTO `tb_sensitive_word` VALUES (693, '淘宝', 5, 0, '2026-01-10 17:26:43', '2026-01-10 17:26:43');
INSERT INTO `tb_sensitive_word` VALUES (694, '小姐', 5, 0, '2026-01-10 17:26:43', '2026-01-10 17:26:43');
INSERT INTO `tb_sensitive_word` VALUES (695, '妓女', 5, 0, '2026-01-10 17:26:43', '2026-01-10 17:26:43');
INSERT INTO `tb_sensitive_word` VALUES (696, '包夜', 5, 0, '2026-01-10 17:26:43', '2026-01-10 17:26:43');
INSERT INTO `tb_sensitive_word` VALUES (697, '3P', 5, 0, '2026-01-10 17:26:43', '2026-01-10 17:26:43');
INSERT INTO `tb_sensitive_word` VALUES (698, 'LY', 5, 0, '2026-01-10 17:26:43', '2026-01-10 17:26:43');
INSERT INTO `tb_sensitive_word` VALUES (699, 'JS', 5, 0, '2026-01-10 17:26:43', '2026-01-10 17:26:43');
INSERT INTO `tb_sensitive_word` VALUES (700, '狼友', 5, 0, '2026-01-10 17:26:43', '2026-01-10 17:26:43');
INSERT INTO `tb_sensitive_word` VALUES (701, '技师', 5, 0, '2026-01-10 17:26:43', '2026-01-10 17:26:43');
INSERT INTO `tb_sensitive_word` VALUES (702, '推油', 5, 0, '2026-01-10 17:26:43', '2026-01-10 17:26:43');
INSERT INTO `tb_sensitive_word` VALUES (703, '胸推', 5, 0, '2026-01-10 17:26:43', '2026-01-10 17:26:43');
INSERT INTO `tb_sensitive_word` VALUES (704, 'BT', 5, 0, '2026-01-10 17:26:43', '2026-01-10 17:26:43');
INSERT INTO `tb_sensitive_word` VALUES (705, '毒龙', 5, 0, '2026-01-10 17:26:43', '2026-01-10 17:26:43');
INSERT INTO `tb_sensitive_word` VALUES (706, '口爆', 5, 0, '2026-01-10 17:26:43', '2026-01-10 17:26:43');
INSERT INTO `tb_sensitive_word` VALUES (707, '楼凤', 5, 0, '2026-01-10 17:26:43', '2026-01-10 17:26:43');
INSERT INTO `tb_sensitive_word` VALUES (708, '足交', 5, 0, '2026-01-10 17:26:43', '2026-01-10 17:26:43');
INSERT INTO `tb_sensitive_word` VALUES (709, '口暴', 5, 0, '2026-01-10 17:26:43', '2026-01-10 17:26:43');
INSERT INTO `tb_sensitive_word` VALUES (710, '口交', 5, 0, '2026-01-10 17:26:43', '2026-01-10 17:26:43');
INSERT INTO `tb_sensitive_word` VALUES (711, '全套', 5, 0, '2026-01-10 17:26:43', '2026-01-10 17:26:43');
INSERT INTO `tb_sensitive_word` VALUES (712, 'SM', 5, 0, '2026-01-10 17:26:43', '2026-01-10 17:26:43');
INSERT INTO `tb_sensitive_word` VALUES (713, '桑拿', 5, 0, '2026-01-10 17:26:43', '2026-01-10 17:26:43');
INSERT INTO `tb_sensitive_word` VALUES (714, '吞精', 5, 0, '2026-01-10 17:26:43', '2026-01-10 17:26:43');
INSERT INTO `tb_sensitive_word` VALUES (715, '咪咪', 5, 0, '2026-01-10 17:26:43', '2026-01-10 17:26:43');
INSERT INTO `tb_sensitive_word` VALUES (716, '婊子', 5, 0, '2026-01-10 17:26:43', '2026-01-10 17:26:43');
INSERT INTO `tb_sensitive_word` VALUES (717, '乳方', 5, 0, '2026-01-10 17:26:43', '2026-01-10 17:26:43');
INSERT INTO `tb_sensitive_word` VALUES (718, '操逼', 5, 0, '2026-01-10 17:26:43', '2026-01-10 17:26:43');
INSERT INTO `tb_sensitive_word` VALUES (719, '全职', 5, 0, '2026-01-10 17:26:43', '2026-01-10 17:26:43');
INSERT INTO `tb_sensitive_word` VALUES (720, '性伴侣', 5, 0, '2026-01-10 17:26:43', '2026-01-10 17:26:43');
INSERT INTO `tb_sensitive_word` VALUES (721, '网购', 5, 0, '2026-01-10 17:26:43', '2026-01-10 17:26:43');
INSERT INTO `tb_sensitive_word` VALUES (722, '网络工作', 5, 0, '2026-01-10 17:26:43', '2026-01-10 17:26:43');
INSERT INTO `tb_sensitive_word` VALUES (723, '代理', 5, 0, '2026-01-10 17:26:43', '2026-01-10 17:26:43');
INSERT INTO `tb_sensitive_word` VALUES (724, '专业代理', 5, 0, '2026-01-10 17:26:43', '2026-01-10 17:26:43');
INSERT INTO `tb_sensitive_word` VALUES (725, '帮忙点一下', 5, 0, '2026-01-10 17:26:43', '2026-01-10 17:26:43');
INSERT INTO `tb_sensitive_word` VALUES (726, '帮忙点下', 5, 0, '2026-01-10 17:26:43', '2026-01-10 17:26:43');
INSERT INTO `tb_sensitive_word` VALUES (727, '请点击进入', 5, 0, '2026-01-10 17:26:43', '2026-01-10 17:26:43');
INSERT INTO `tb_sensitive_word` VALUES (728, '详情请进入', 5, 0, '2026-01-10 17:26:43', '2026-01-10 17:26:43');
INSERT INTO `tb_sensitive_word` VALUES (729, '私人侦探', 5, 0, '2026-01-10 17:26:43', '2026-01-10 17:26:43');
INSERT INTO `tb_sensitive_word` VALUES (730, '私家侦探', 5, 0, '2026-01-10 17:26:43', '2026-01-10 17:26:43');
INSERT INTO `tb_sensitive_word` VALUES (731, '针孔摄象', 5, 0, '2026-01-10 17:26:43', '2026-01-10 17:26:43');
INSERT INTO `tb_sensitive_word` VALUES (732, '调查婚外情', 5, 0, '2026-01-10 17:26:43', '2026-01-10 17:26:43');
INSERT INTO `tb_sensitive_word` VALUES (733, '信用卡提现', 5, 0, '2026-01-10 17:26:43', '2026-01-10 17:26:43');
INSERT INTO `tb_sensitive_word` VALUES (734, '无抵押贷款', 5, 0, '2026-01-10 17:26:43', '2026-01-10 17:26:43');
INSERT INTO `tb_sensitive_word` VALUES (735, '广告代理', 5, 0, '2026-01-10 17:26:43', '2026-01-10 17:26:43');
INSERT INTO `tb_sensitive_word` VALUES (736, '原音铃声', 5, 0, '2026-01-10 17:26:43', '2026-01-10 17:26:43');
INSERT INTO `tb_sensitive_word` VALUES (737, '借腹生子', 5, 0, '2026-01-10 17:26:43', '2026-01-10 17:26:43');
INSERT INTO `tb_sensitive_word` VALUES (738, '找个妈妈', 5, 0, '2026-01-10 17:26:43', '2026-01-10 17:26:43');
INSERT INTO `tb_sensitive_word` VALUES (739, '找个爸爸', 5, 0, '2026-01-10 17:26:43', '2026-01-10 17:26:43');
INSERT INTO `tb_sensitive_word` VALUES (740, '代孕妈妈', 5, 0, '2026-01-10 17:26:43', '2026-01-10 17:26:43');
INSERT INTO `tb_sensitive_word` VALUES (741, '代生孩子', 5, 0, '2026-01-10 17:26:43', '2026-01-10 17:26:43');
INSERT INTO `tb_sensitive_word` VALUES (742, '代开发票', 5, 0, '2026-01-10 17:26:43', '2026-01-10 17:26:43');
INSERT INTO `tb_sensitive_word` VALUES (743, '腾讯客服电话', 5, 0, '2026-01-10 17:26:43', '2026-01-10 17:26:43');
INSERT INTO `tb_sensitive_word` VALUES (744, '销售热线', 5, 0, '2026-01-10 17:26:43', '2026-01-10 17:26:43');
INSERT INTO `tb_sensitive_word` VALUES (745, '免费订购热线', 5, 0, '2026-01-10 17:26:43', '2026-01-10 17:26:43');
INSERT INTO `tb_sensitive_word` VALUES (746, '低价出售', 5, 0, '2026-01-10 17:26:43', '2026-01-10 17:26:43');
INSERT INTO `tb_sensitive_word` VALUES (747, '款到发货', 5, 0, '2026-01-10 17:26:43', '2026-01-10 17:26:43');
INSERT INTO `tb_sensitive_word` VALUES (748, '回复可见', 5, 0, '2026-01-10 17:26:43', '2026-01-10 17:26:43');
INSERT INTO `tb_sensitive_word` VALUES (749, '连锁加盟', 5, 0, '2026-01-10 17:26:43', '2026-01-10 17:26:43');
INSERT INTO `tb_sensitive_word` VALUES (750, '加盟连锁', 5, 0, '2026-01-10 17:26:43', '2026-01-10 17:26:43');
INSERT INTO `tb_sensitive_word` VALUES (751, '免费二级域名', 5, 0, '2026-01-10 17:26:43', '2026-01-10 17:26:43');
INSERT INTO `tb_sensitive_word` VALUES (752, '免费使用', 5, 0, '2026-01-10 17:26:43', '2026-01-10 17:26:43');
INSERT INTO `tb_sensitive_word` VALUES (753, '免费索取', 5, 0, '2026-01-10 17:26:43', '2026-01-10 17:26:43');
INSERT INTO `tb_sensitive_word` VALUES (754, '蚁力神', 5, 0, '2026-01-10 17:26:43', '2026-01-10 17:26:43');
INSERT INTO `tb_sensitive_word` VALUES (755, '婴儿汤', 5, 0, '2026-01-10 17:26:43', '2026-01-10 17:26:43');
INSERT INTO `tb_sensitive_word` VALUES (756, '售肾', 5, 0, '2026-01-10 17:26:43', '2026-01-10 17:26:43');
INSERT INTO `tb_sensitive_word` VALUES (757, '刻章办', 5, 0, '2026-01-10 17:26:43', '2026-01-10 17:26:43');
INSERT INTO `tb_sensitive_word` VALUES (758, '买小车', 5, 0, '2026-01-10 17:26:43', '2026-01-10 17:26:43');
INSERT INTO `tb_sensitive_word` VALUES (759, '套牌车', 5, 0, '2026-01-10 17:26:43', '2026-01-10 17:26:43');
INSERT INTO `tb_sensitive_word` VALUES (760, '玛雅网', 5, 0, '2026-01-10 17:26:43', '2026-01-10 17:26:43');
INSERT INTO `tb_sensitive_word` VALUES (761, '电脑传讯', 5, 0, '2026-01-10 17:26:43', '2026-01-10 17:26:43');
INSERT INTO `tb_sensitive_word` VALUES (762, '视频来源', 5, 0, '2026-01-10 17:26:43', '2026-01-10 17:26:43');
INSERT INTO `tb_sensitive_word` VALUES (763, '下载速度', 5, 0, '2026-01-10 17:26:43', '2026-01-10 17:26:43');
INSERT INTO `tb_sensitive_word` VALUES (764, '高清在线', 5, 0, '2026-01-10 17:26:43', '2026-01-10 17:26:43');
INSERT INTO `tb_sensitive_word` VALUES (765, '全集在线', 5, 0, '2026-01-10 17:26:43', '2026-01-10 17:26:43');
INSERT INTO `tb_sensitive_word` VALUES (766, '在线播放', 5, 0, '2026-01-10 17:26:43', '2026-01-10 17:26:43');
INSERT INTO `tb_sensitive_word` VALUES (767, 'txt下载', 5, 0, '2026-01-10 17:26:43', '2026-01-10 17:26:43');
INSERT INTO `tb_sensitive_word` VALUES (768, '六位qq', 5, 0, '2026-01-10 17:26:43', '2026-01-10 17:26:43');
INSERT INTO `tb_sensitive_word` VALUES (769, '6位qq', 5, 0, '2026-01-10 17:26:43', '2026-01-10 17:26:43');
INSERT INTO `tb_sensitive_word` VALUES (770, '位的qq', 5, 0, '2026-01-10 17:26:43', '2026-01-10 17:26:43');
INSERT INTO `tb_sensitive_word` VALUES (771, '个qb', 5, 0, '2026-01-10 17:26:43', '2026-01-10 17:26:43');
INSERT INTO `tb_sensitive_word` VALUES (772, '送qb', 5, 0, '2026-01-10 17:26:43', '2026-01-10 17:26:43');
INSERT INTO `tb_sensitive_word` VALUES (773, '用刀横向切腹', 5, 0, '2026-01-10 17:26:43', '2026-01-10 17:26:43');
INSERT INTO `tb_sensitive_word` VALUES (774, '完全自杀手册', 5, 0, '2026-01-10 17:26:43', '2026-01-10 17:26:43');
INSERT INTO `tb_sensitive_word` VALUES (775, '四海帮', 5, 0, '2026-01-10 17:26:43', '2026-01-10 17:26:43');
INSERT INTO `tb_sensitive_word` VALUES (776, '足球投注', 5, 0, '2026-01-10 17:26:43', '2026-01-10 17:26:43');
INSERT INTO `tb_sensitive_word` VALUES (777, '地下钱庄', 5, 0, '2026-01-10 17:26:43', '2026-01-10 17:26:43');
INSERT INTO `tb_sensitive_word` VALUES (778, '中国复兴党', 5, 0, '2026-01-10 17:26:43', '2026-01-10 17:26:43');
INSERT INTO `tb_sensitive_word` VALUES (779, '阿波罗网', 5, 0, '2026-01-10 17:26:43', '2026-01-10 17:26:43');
INSERT INTO `tb_sensitive_word` VALUES (780, '曾道人', 5, 0, '2026-01-10 17:26:43', '2026-01-10 17:26:43');
INSERT INTO `tb_sensitive_word` VALUES (781, '六合彩', 5, 0, '2026-01-10 17:26:43', '2026-01-10 17:26:43');
INSERT INTO `tb_sensitive_word` VALUES (782, '改卷内幕', 5, 0, '2026-01-10 17:26:43', '2026-01-10 17:26:43');
INSERT INTO `tb_sensitive_word` VALUES (783, '替考试', 5, 0, '2026-01-10 17:26:43', '2026-01-10 17:26:43');
INSERT INTO `tb_sensitive_word` VALUES (784, '隐形耳机', 5, 0, '2026-01-10 17:26:43', '2026-01-10 17:26:43');
INSERT INTO `tb_sensitive_word` VALUES (785, '出售答案', 5, 0, '2026-01-10 17:26:43', '2026-01-10 17:26:43');
INSERT INTO `tb_sensitive_word` VALUES (786, '考中答案', 5, 0, '2026-01-10 17:26:43', '2026-01-10 17:26:43');
INSERT INTO `tb_sensitive_word` VALUES (787, '答an', 5, 0, '2026-01-10 17:26:43', '2026-01-10 17:26:43');
INSERT INTO `tb_sensitive_word` VALUES (788, 'da案', 5, 0, '2026-01-10 17:26:43', '2026-01-10 17:26:43');
INSERT INTO `tb_sensitive_word` VALUES (789, '资金周转', 5, 0, '2026-01-10 17:26:43', '2026-01-10 17:26:43');
INSERT INTO `tb_sensitive_word` VALUES (790, '救市', 5, 0, '2026-01-10 17:26:43', '2026-01-10 17:26:43');
INSERT INTO `tb_sensitive_word` VALUES (791, '股市圈钱', 5, 0, '2026-01-10 17:26:43', '2026-01-10 17:26:43');
INSERT INTO `tb_sensitive_word` VALUES (792, '崩盘', 5, 0, '2026-01-10 17:26:43', '2026-01-10 17:26:43');
INSERT INTO `tb_sensitive_word` VALUES (793, '资金短缺', 5, 0, '2026-01-10 17:26:43', '2026-01-10 17:26:43');
INSERT INTO `tb_sensitive_word` VALUES (794, '证监会', 5, 0, '2026-01-10 17:26:43', '2026-01-10 17:26:43');
INSERT INTO `tb_sensitive_word` VALUES (795, '质押贷款', 5, 0, '2026-01-10 17:26:43', '2026-01-10 17:26:43');
INSERT INTO `tb_sensitive_word` VALUES (796, '小额贷款', 5, 0, '2026-01-10 17:26:43', '2026-01-10 17:26:43');
INSERT INTO `tb_sensitive_word` VALUES (797, '周小川', 5, 0, '2026-01-10 17:26:43', '2026-01-10 17:26:43');
INSERT INTO `tb_sensitive_word` VALUES (798, '刘明康', 5, 0, '2026-01-10 17:26:43', '2026-01-10 17:26:43');
INSERT INTO `tb_sensitive_word` VALUES (799, '尚福林', 5, 0, '2026-01-10 17:26:43', '2026-01-10 17:26:43');
INSERT INTO `tb_sensitive_word` VALUES (800, '孔丹', 5, 0, '2026-01-10 17:26:43', '2026-01-10 17:26:43');
INSERT INTO `tb_sensitive_word` VALUES (2908, '爱液', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (2909, '按摩棒', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (2910, '拔出来', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (2911, '爆草', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (2912, '包二奶', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (2913, '暴干', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (2914, '暴奸', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (2915, '暴乳', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (2916, '爆乳', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (2917, '暴淫', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (2918, '被操', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (2919, '被插', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (2920, '被干', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (2921, '逼奸', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (2922, '仓井空', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (2923, '插暴', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (2924, '操黑', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (2925, '操烂', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (2926, '肏你', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (2927, '肏死', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (2928, '操死', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (2929, '操我', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (2930, '厕奴', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (2931, '插比', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (2932, '插b', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (2933, '插逼', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (2934, '插进', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (2935, '插你', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (2936, '插我', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (2937, '插阴', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (2938, '潮吹', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (2939, '潮喷', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (2940, '成人电影', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (2941, '成人论坛', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (2942, '成人色情', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (2943, '成人网站', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (2944, '成人文学', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (2945, '成人小说', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (2946, '艳情小说', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (2947, '成人游戏', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (2948, '吃精', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (2949, '抽插', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (2950, '春药', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (2951, '大波', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (2952, '大力抽送', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (2953, '大乳', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (2954, '荡妇', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (2955, '荡女', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (2956, '盗撮', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (2957, '发浪', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (2958, '放尿', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (2959, '肥逼', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (2960, '粉穴', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (2961, '风月大陆', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (2962, '干死你', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (2963, '干穴', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (2964, '肛交', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (2965, '肛门', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (2966, '龟头', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (2967, '裹本', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (2968, '国产av', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (2969, '好嫩', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (2970, '豪乳', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (2971, '黑逼', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (2972, '后庭', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (2973, '后穴', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (2974, '虎骑', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (2975, '换妻俱乐部', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (2976, '黄片', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (2977, '几吧', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (2978, '鸡吧', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (2979, '鸡巴', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (2980, '鸡奸', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (2981, '奸情', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (2982, '叫床', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (2983, '脚交', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (2984, '精液', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (2985, '就去日', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (2986, '巨屌', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (2987, '菊花洞', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (2988, '菊门', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (2989, '巨奶', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (2990, '巨乳', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (2991, '菊穴', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (2992, '开苞', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (2993, '口活', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (2994, '口射', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (2995, '口淫', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (2996, '裤袜', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (2997, '狂操', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (2998, '狂插', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (2999, '浪逼', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3000, '浪妇', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3001, '浪叫', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3002, '浪女', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3003, '聊性', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3004, '凌辱', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3005, '漏乳', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3006, '露b', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3007, '乱交', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3008, '乱伦', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3009, '轮暴', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3010, '轮操', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3011, '轮奸', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3012, '裸陪', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3013, '买春', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3014, '美逼', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3015, '美少妇', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3016, '美乳', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3017, '美腿', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3018, '美穴', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3019, '美幼', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3020, '秘唇', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3021, '迷奸', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3022, '密穴', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3023, '蜜穴', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3024, '蜜液', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3025, '摸奶', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3026, '摸胸', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3027, '母奸', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3028, '奈美', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3029, '奶子', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3030, '男奴', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3031, '内射', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3032, '嫩逼', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3033, '嫩女', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3034, '嫩穴', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3035, '捏弄', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3036, '女优', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3037, '炮友', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3038, '砲友', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3039, '喷精', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3040, '屁眼', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3041, '前凸后翘', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3042, '强jian', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3043, '强暴', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3044, '强奸处女', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3045, '情趣用品', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3046, '情色', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3047, '拳交', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3048, '全裸', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3049, '群交', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3050, '人妻', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3051, '人兽', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3052, '日逼', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3053, '日烂', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3054, '肉棒', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3055, '肉逼', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3056, '肉唇', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3057, '肉洞', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3058, '肉缝', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3059, '肉棍', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3060, '肉茎', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3061, '肉具', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3062, '揉乳', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3063, '肉穴', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3064, '肉欲', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3065, '乳爆', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3066, '乳房', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3067, '乳沟', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3068, '乳交', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3069, '乳头', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3070, '骚逼', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3071, '骚比', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3072, '骚女', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3073, '骚水', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3074, '骚穴', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3075, '色逼', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3076, '色界', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3077, '色猫', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3078, '色盟', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3079, '色情网站', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3080, '色区', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3081, '色色', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3082, '色诱', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3083, '色欲', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3084, '色b', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3085, '少年阿宾', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3086, '射爽', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3087, '射颜', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3088, '食精', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3089, '释欲', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3090, '兽奸', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3091, '兽交', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3092, '手淫', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3093, '兽欲', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3094, '熟妇', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3095, '熟母', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3096, '熟女', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3097, '爽片', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3098, '双臀', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3099, '死逼', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3100, '丝袜', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3101, '丝诱', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3102, '松岛枫', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3103, '酥痒', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3104, '汤加丽', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3105, '套弄', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3106, '体奸', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3107, '体位', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3108, '舔脚', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3109, '舔阴', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3110, '调教', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3111, '偷欢', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3112, '脱内裤', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3113, '文做', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3114, '舞女', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3115, '无修正', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3116, '吸精', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3117, '夏川纯', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3118, '相奸', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3119, '小逼', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3120, '校鸡', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3121, '小穴', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3122, '小xue', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3123, '性感妖娆', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3124, '性感诱惑', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3125, '性虎', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3126, '性饥渴', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3127, '性技巧', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3128, '性交', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3129, '性奴', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3130, '性虐', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3131, '性息', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3132, '性欲', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3133, '穴口', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3134, '穴图', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3135, '亚情', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3136, '颜射', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3137, '阳具', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3138, '杨思敏', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3139, '要射了', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3140, '夜勤病栋', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3141, '一本道', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3142, '一夜欢', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3143, '一夜情', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3144, '一ye情', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3145, '阴部', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3146, '淫虫', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3147, '阴唇', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3148, '淫荡', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3149, '阴道', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3150, '淫电影', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3151, '阴阜', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3152, '淫妇', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3153, '淫河', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3154, '阴核', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3155, '阴户', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3156, '淫贱', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3157, '淫叫', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3158, '淫教师', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3159, '阴茎', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3160, '阴精', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3161, '淫浪', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3162, '淫媚', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3163, '淫糜', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3164, '淫魔', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3165, '淫母', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3166, '淫女', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3167, '淫虐', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3168, '淫妻', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3169, '淫情', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3170, '淫色', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3171, '淫声浪语', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3172, '淫兽学园', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3173, '淫书', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3174, '淫术炼金士', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3175, '淫水', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3176, '淫娃', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3177, '淫威', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3178, '淫亵', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3179, '淫样', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3180, '淫液', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3181, '淫照', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3182, '阴b', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3183, '应召', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3184, '幼交', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3185, '欲火', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3186, '欲女', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3187, '玉乳', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3188, '玉穴', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3189, '援交', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3190, '原味内衣', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3191, '援助交际', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3192, '招鸡', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3193, '招妓', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3194, '抓胸', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3195, '自慰', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3196, '作爱', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3197, 'a片', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3198, 'fuck', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3199, 'gay片', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3200, 'g点', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3201, 'h动画', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3202, 'h动漫', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3203, '失身粉', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3204, '淫荡自慰器', 4, 0, '2026-01-10 17:29:07', '2026-01-10 17:29:07');
INSERT INTO `tb_sensitive_word` VALUES (3205, '习近平', 3, 0, '2026-01-10 17:30:39', '2026-01-10 17:30:39');
INSERT INTO `tb_sensitive_word` VALUES (3206, '平近习', 3, 0, '2026-01-10 17:30:39', '2026-01-10 17:30:39');
INSERT INTO `tb_sensitive_word` VALUES (3207, 'xjp', 3, 0, '2026-01-10 17:30:39', '2026-01-10 17:30:39');
INSERT INTO `tb_sensitive_word` VALUES (3208, '习太子', 3, 0, '2026-01-10 17:30:39', '2026-01-10 17:30:39');
INSERT INTO `tb_sensitive_word` VALUES (3209, '习明泽', 3, 0, '2026-01-10 17:30:39', '2026-01-10 17:30:39');
INSERT INTO `tb_sensitive_word` VALUES (3210, '老习', 3, 0, '2026-01-10 17:30:39', '2026-01-10 17:30:39');
INSERT INTO `tb_sensitive_word` VALUES (3211, '温家宝', 3, 0, '2026-01-10 17:30:39', '2026-01-10 17:30:39');
INSERT INTO `tb_sensitive_word` VALUES (3212, '温加宝', 3, 0, '2026-01-10 17:30:39', '2026-01-10 17:30:39');
INSERT INTO `tb_sensitive_word` VALUES (3213, '温x', 3, 0, '2026-01-10 17:30:39', '2026-01-10 17:30:39');
INSERT INTO `tb_sensitive_word` VALUES (3214, '温jia宝', 3, 0, '2026-01-10 17:30:39', '2026-01-10 17:30:39');
INSERT INTO `tb_sensitive_word` VALUES (3215, '温宝宝', 3, 0, '2026-01-10 17:30:39', '2026-01-10 17:30:39');
INSERT INTO `tb_sensitive_word` VALUES (3216, '温加饱', 3, 0, '2026-01-10 17:30:39', '2026-01-10 17:30:39');
INSERT INTO `tb_sensitive_word` VALUES (3217, '温加保', 3, 0, '2026-01-10 17:30:39', '2026-01-10 17:30:39');
INSERT INTO `tb_sensitive_word` VALUES (3218, '张培莉', 3, 0, '2026-01-10 17:30:39', '2026-01-10 17:30:39');
INSERT INTO `tb_sensitive_word` VALUES (3219, '温云松', 3, 0, '2026-01-10 17:30:39', '2026-01-10 17:30:39');
INSERT INTO `tb_sensitive_word` VALUES (3220, '温如春', 3, 0, '2026-01-10 17:30:39', '2026-01-10 17:30:39');
INSERT INTO `tb_sensitive_word` VALUES (3221, '温jb', 3, 0, '2026-01-10 17:30:39', '2026-01-10 17:30:39');
INSERT INTO `tb_sensitive_word` VALUES (3222, '胡温', 3, 0, '2026-01-10 17:30:39', '2026-01-10 17:30:39');
INSERT INTO `tb_sensitive_word` VALUES (3223, '胡x', 3, 0, '2026-01-10 17:30:39', '2026-01-10 17:30:39');
INSERT INTO `tb_sensitive_word` VALUES (3224, '胡jt', 3, 0, '2026-01-10 17:30:39', '2026-01-10 17:30:39');
INSERT INTO `tb_sensitive_word` VALUES (3225, '胡boss', 3, 0, '2026-01-10 17:30:39', '2026-01-10 17:30:39');
INSERT INTO `tb_sensitive_word` VALUES (3226, '胡总', 3, 0, '2026-01-10 17:30:39', '2026-01-10 17:30:39');
INSERT INTO `tb_sensitive_word` VALUES (3227, '胡王八', 3, 0, '2026-01-10 17:30:39', '2026-01-10 17:30:39');
INSERT INTO `tb_sensitive_word` VALUES (3228, 'hujintao', 3, 0, '2026-01-10 17:30:39', '2026-01-10 17:30:39');
INSERT INTO `tb_sensitive_word` VALUES (3229, '胡jintao', 3, 0, '2026-01-10 17:30:39', '2026-01-10 17:30:39');
INSERT INTO `tb_sensitive_word` VALUES (3230, '胡j涛', 3, 0, '2026-01-10 17:30:39', '2026-01-10 17:30:39');
INSERT INTO `tb_sensitive_word` VALUES (3231, '胡惊涛', 3, 0, '2026-01-10 17:30:39', '2026-01-10 17:30:39');
INSERT INTO `tb_sensitive_word` VALUES (3232, '胡景涛', 3, 0, '2026-01-10 17:30:39', '2026-01-10 17:30:39');
INSERT INTO `tb_sensitive_word` VALUES (3233, '胡紧掏', 3, 0, '2026-01-10 17:30:39', '2026-01-10 17:30:39');
INSERT INTO `tb_sensitive_word` VALUES (3234, '湖紧掏', 3, 0, '2026-01-10 17:30:39', '2026-01-10 17:30:39');
INSERT INTO `tb_sensitive_word` VALUES (3235, '胡紧套', 3, 0, '2026-01-10 17:30:39', '2026-01-10 17:30:39');
INSERT INTO `tb_sensitive_word` VALUES (3236, '锦涛', 3, 0, '2026-01-10 17:30:39', '2026-01-10 17:30:39');
INSERT INTO `tb_sensitive_word` VALUES (3237, 'hjt', 3, 0, '2026-01-10 17:30:39', '2026-01-10 17:30:39');
INSERT INTO `tb_sensitive_word` VALUES (3238, '胡派', 3, 0, '2026-01-10 17:30:39', '2026-01-10 17:30:39');
INSERT INTO `tb_sensitive_word` VALUES (3239, '胡主席', 3, 0, '2026-01-10 17:30:39', '2026-01-10 17:30:39');
INSERT INTO `tb_sensitive_word` VALUES (3240, '刘永清', 3, 0, '2026-01-10 17:30:39', '2026-01-10 17:30:39');
INSERT INTO `tb_sensitive_word` VALUES (3241, '胡海峰', 3, 0, '2026-01-10 17:30:39', '2026-01-10 17:30:39');
INSERT INTO `tb_sensitive_word` VALUES (3242, '胡海清', 3, 0, '2026-01-10 17:30:39', '2026-01-10 17:30:39');
INSERT INTO `tb_sensitive_word` VALUES (3243, '江泽民', 3, 0, '2026-01-10 17:30:39', '2026-01-10 17:30:39');
INSERT INTO `tb_sensitive_word` VALUES (3244, '民泽江', 3, 0, '2026-01-10 17:30:39', '2026-01-10 17:30:39');
INSERT INTO `tb_sensitive_word` VALUES (3245, '江胡', 3, 0, '2026-01-10 17:30:39', '2026-01-10 17:30:39');
INSERT INTO `tb_sensitive_word` VALUES (3246, '江哥', 3, 0, '2026-01-10 17:30:39', '2026-01-10 17:30:39');
INSERT INTO `tb_sensitive_word` VALUES (3247, '江主席', 3, 0, '2026-01-10 17:30:39', '2026-01-10 17:30:39');
INSERT INTO `tb_sensitive_word` VALUES (3248, '江书记', 3, 0, '2026-01-10 17:30:39', '2026-01-10 17:30:39');
INSERT INTO `tb_sensitive_word` VALUES (3249, '江浙闽', 3, 0, '2026-01-10 17:30:39', '2026-01-10 17:30:39');
INSERT INTO `tb_sensitive_word` VALUES (3250, '江沢民', 3, 0, '2026-01-10 17:30:39', '2026-01-10 17:30:39');
INSERT INTO `tb_sensitive_word` VALUES (3251, '江浙民', 3, 0, '2026-01-10 17:30:39', '2026-01-10 17:30:39');
INSERT INTO `tb_sensitive_word` VALUES (3252, '择民', 3, 0, '2026-01-10 17:30:39', '2026-01-10 17:30:39');
INSERT INTO `tb_sensitive_word` VALUES (3253, '则民', 3, 0, '2026-01-10 17:30:39', '2026-01-10 17:30:39');
INSERT INTO `tb_sensitive_word` VALUES (3254, '茳泽民', 3, 0, '2026-01-10 17:30:39', '2026-01-10 17:30:39');
INSERT INTO `tb_sensitive_word` VALUES (3255, 'zemin', 3, 0, '2026-01-10 17:30:39', '2026-01-10 17:30:39');
INSERT INTO `tb_sensitive_word` VALUES (3256, 'ze民', 3, 0, '2026-01-10 17:30:39', '2026-01-10 17:30:39');
INSERT INTO `tb_sensitive_word` VALUES (3257, '老江', 3, 0, '2026-01-10 17:30:39', '2026-01-10 17:30:39');
INSERT INTO `tb_sensitive_word` VALUES (3258, '老j', 3, 0, '2026-01-10 17:30:39', '2026-01-10 17:30:39');
INSERT INTO `tb_sensitive_word` VALUES (3259, '江core', 3, 0, '2026-01-10 17:30:39', '2026-01-10 17:30:39');
INSERT INTO `tb_sensitive_word` VALUES (3260, '江x', 3, 0, '2026-01-10 17:30:39', '2026-01-10 17:30:39');
INSERT INTO `tb_sensitive_word` VALUES (3261, '江派', 3, 0, '2026-01-10 17:30:39', '2026-01-10 17:30:39');
INSERT INTO `tb_sensitive_word` VALUES (3262, '江zm', 3, 0, '2026-01-10 17:30:39', '2026-01-10 17:30:39');
INSERT INTO `tb_sensitive_word` VALUES (3263, 'jzm', 3, 0, '2026-01-10 17:30:39', '2026-01-10 17:30:39');
INSERT INTO `tb_sensitive_word` VALUES (3264, '江戏子', 3, 0, '2026-01-10 17:30:39', '2026-01-10 17:30:39');
INSERT INTO `tb_sensitive_word` VALUES (3265, '江蛤蟆', 3, 0, '2026-01-10 17:30:39', '2026-01-10 17:30:39');
INSERT INTO `tb_sensitive_word` VALUES (3266, '江某某', 3, 0, '2026-01-10 17:30:39', '2026-01-10 17:30:39');
INSERT INTO `tb_sensitive_word` VALUES (3267, '江贼', 3, 0, '2026-01-10 17:30:39', '2026-01-10 17:30:39');
INSERT INTO `tb_sensitive_word` VALUES (3268, '江猪', 3, 0, '2026-01-10 17:30:39', '2026-01-10 17:30:39');
INSERT INTO `tb_sensitive_word` VALUES (3269, '江氏集团', 3, 0, '2026-01-10 17:30:39', '2026-01-10 17:30:39');
INSERT INTO `tb_sensitive_word` VALUES (3270, '江绵恒', 3, 0, '2026-01-10 17:30:39', '2026-01-10 17:30:39');
INSERT INTO `tb_sensitive_word` VALUES (3271, '江绵康', 3, 0, '2026-01-10 17:30:39', '2026-01-10 17:30:39');
INSERT INTO `tb_sensitive_word` VALUES (3272, '王冶坪', 3, 0, '2026-01-10 17:30:39', '2026-01-10 17:30:39');
INSERT INTO `tb_sensitive_word` VALUES (3273, '江泽慧', 3, 0, '2026-01-10 17:30:39', '2026-01-10 17:30:39');
INSERT INTO `tb_sensitive_word` VALUES (3274, '邓小平', 3, 0, '2026-01-10 17:30:39', '2026-01-10 17:30:39');
INSERT INTO `tb_sensitive_word` VALUES (3275, '平小邓', 3, 0, '2026-01-10 17:30:39', '2026-01-10 17:30:39');
INSERT INTO `tb_sensitive_word` VALUES (3276, 'xiao平', 3, 0, '2026-01-10 17:30:39', '2026-01-10 17:30:39');
INSERT INTO `tb_sensitive_word` VALUES (3277, '邓xp', 3, 0, '2026-01-10 17:30:39', '2026-01-10 17:30:39');
INSERT INTO `tb_sensitive_word` VALUES (3278, '邓晓平', 3, 0, '2026-01-10 17:30:39', '2026-01-10 17:30:39');
INSERT INTO `tb_sensitive_word` VALUES (3279, '邓朴方', 3, 0, '2026-01-10 17:30:39', '2026-01-10 17:30:39');
INSERT INTO `tb_sensitive_word` VALUES (3280, '邓榕', 3, 0, '2026-01-10 17:30:39', '2026-01-10 17:30:39');
INSERT INTO `tb_sensitive_word` VALUES (3281, '邓质方', 3, 0, '2026-01-10 17:30:39', '2026-01-10 17:30:39');
INSERT INTO `tb_sensitive_word` VALUES (3282, '毛泽东', 3, 0, '2026-01-10 17:30:39', '2026-01-10 17:30:39');
INSERT INTO `tb_sensitive_word` VALUES (3283, '猫泽东', 3, 0, '2026-01-10 17:30:39', '2026-01-10 17:30:39');
INSERT INTO `tb_sensitive_word` VALUES (3284, '猫则东', 3, 0, '2026-01-10 17:30:39', '2026-01-10 17:30:39');
INSERT INTO `tb_sensitive_word` VALUES (3285, '猫贼洞', 3, 0, '2026-01-10 17:30:39', '2026-01-10 17:30:39');
INSERT INTO `tb_sensitive_word` VALUES (3286, '毛zd', 3, 0, '2026-01-10 17:30:39', '2026-01-10 17:30:39');
INSERT INTO `tb_sensitive_word` VALUES (3287, '毛zx', 3, 0, '2026-01-10 17:30:39', '2026-01-10 17:30:39');
INSERT INTO `tb_sensitive_word` VALUES (3288, 'z东', 3, 0, '2026-01-10 17:30:39', '2026-01-10 17:30:39');
INSERT INTO `tb_sensitive_word` VALUES (3289, 'ze东', 3, 0, '2026-01-10 17:30:39', '2026-01-10 17:30:39');
INSERT INTO `tb_sensitive_word` VALUES (3290, '泽d', 3, 0, '2026-01-10 17:30:39', '2026-01-10 17:30:39');
INSERT INTO `tb_sensitive_word` VALUES (3291, 'zedong', 3, 0, '2026-01-10 17:30:39', '2026-01-10 17:30:39');
INSERT INTO `tb_sensitive_word` VALUES (3292, '毛太祖', 3, 0, '2026-01-10 17:30:39', '2026-01-10 17:30:39');
INSERT INTO `tb_sensitive_word` VALUES (3293, '毛相', 3, 0, '2026-01-10 17:30:39', '2026-01-10 17:30:39');
INSERT INTO `tb_sensitive_word` VALUES (3294, '主席画像', 3, 0, '2026-01-10 17:30:39', '2026-01-10 17:30:39');
INSERT INTO `tb_sensitive_word` VALUES (3295, '改革历程', 3, 0, '2026-01-10 17:30:39', '2026-01-10 17:30:39');
INSERT INTO `tb_sensitive_word` VALUES (3296, '朱镕基', 3, 0, '2026-01-10 17:30:39', '2026-01-10 17:30:39');
INSERT INTO `tb_sensitive_word` VALUES (3297, '朱容基', 3, 0, '2026-01-10 17:30:39', '2026-01-10 17:30:39');
INSERT INTO `tb_sensitive_word` VALUES (3298, '朱镕鸡', 3, 0, '2026-01-10 17:30:39', '2026-01-10 17:30:39');
INSERT INTO `tb_sensitive_word` VALUES (3299, '朱容鸡', 3, 0, '2026-01-10 17:30:39', '2026-01-10 17:30:39');
INSERT INTO `tb_sensitive_word` VALUES (3300, '朱云来', 3, 0, '2026-01-10 17:30:39', '2026-01-10 17:30:39');
INSERT INTO `tb_sensitive_word` VALUES (3301, '李鹏', 3, 0, '2026-01-10 17:30:39', '2026-01-10 17:30:39');
INSERT INTO `tb_sensitive_word` VALUES (3302, '李peng', 3, 0, '2026-01-10 17:30:39', '2026-01-10 17:30:39');
INSERT INTO `tb_sensitive_word` VALUES (3303, '里鹏', 3, 0, '2026-01-10 17:30:39', '2026-01-10 17:30:39');
INSERT INTO `tb_sensitive_word` VALUES (3304, '李月月鸟', 3, 0, '2026-01-10 17:30:39', '2026-01-10 17:30:39');
INSERT INTO `tb_sensitive_word` VALUES (3305, '李小鹏', 3, 0, '2026-01-10 17:30:39', '2026-01-10 17:30:39');
INSERT INTO `tb_sensitive_word` VALUES (3306, '李小琳', 3, 0, '2026-01-10 17:30:39', '2026-01-10 17:30:39');
INSERT INTO `tb_sensitive_word` VALUES (3307, '华主席', 3, 0, '2026-01-10 17:30:39', '2026-01-10 17:30:39');
INSERT INTO `tb_sensitive_word` VALUES (3308, '华国', 3, 0, '2026-01-10 17:30:39', '2026-01-10 17:30:39');
INSERT INTO `tb_sensitive_word` VALUES (3309, '国锋', 3, 0, '2026-01-10 17:30:39', '2026-01-10 17:30:39');
INSERT INTO `tb_sensitive_word` VALUES (3310, '国峰', 3, 0, '2026-01-10 17:30:39', '2026-01-10 17:30:39');
INSERT INTO `tb_sensitive_word` VALUES (3311, '锋同志', 3, 0, '2026-01-10 17:30:39', '2026-01-10 17:30:39');
INSERT INTO `tb_sensitive_word` VALUES (3312, '白春礼', 3, 0, '2026-01-10 17:30:39', '2026-01-10 17:30:39');
INSERT INTO `tb_sensitive_word` VALUES (3313, '薄熙来', 3, 0, '2026-01-10 17:30:39', '2026-01-10 17:30:39');
INSERT INTO `tb_sensitive_word` VALUES (3314, '薄一波', 3, 0, '2026-01-10 17:30:39', '2026-01-10 17:30:39');
INSERT INTO `tb_sensitive_word` VALUES (3315, '蔡赴朝', 3, 0, '2026-01-10 17:30:39', '2026-01-10 17:30:39');
INSERT INTO `tb_sensitive_word` VALUES (3316, '蔡武', 3, 0, '2026-01-10 17:30:39', '2026-01-10 17:30:39');
INSERT INTO `tb_sensitive_word` VALUES (3317, '曹刚川', 3, 0, '2026-01-10 17:30:39', '2026-01-10 17:30:39');
INSERT INTO `tb_sensitive_word` VALUES (3318, '常万全', 3, 0, '2026-01-10 17:30:39', '2026-01-10 17:30:39');
INSERT INTO `tb_sensitive_word` VALUES (3319, '陈炳德', 3, 0, '2026-01-10 17:30:39', '2026-01-10 17:30:39');
INSERT INTO `tb_sensitive_word` VALUES (3320, '陈德铭', 3, 0, '2026-01-10 17:30:39', '2026-01-10 17:30:39');
INSERT INTO `tb_sensitive_word` VALUES (3321, '陈建国', 3, 0, '2026-01-10 17:30:39', '2026-01-10 17:30:39');
INSERT INTO `tb_sensitive_word` VALUES (3322, '陈良宇', 3, 0, '2026-01-10 17:30:39', '2026-01-10 17:30:39');
INSERT INTO `tb_sensitive_word` VALUES (3323, '陈绍基', 3, 0, '2026-01-10 17:30:39', '2026-01-10 17:30:39');
INSERT INTO `tb_sensitive_word` VALUES (3324, '陈同海', 3, 0, '2026-01-10 17:30:39', '2026-01-10 17:30:39');
INSERT INTO `tb_sensitive_word` VALUES (3325, '陈至立', 3, 0, '2026-01-10 17:30:39', '2026-01-10 17:30:39');
INSERT INTO `tb_sensitive_word` VALUES (3326, '戴秉国', 3, 0, '2026-01-10 17:30:39', '2026-01-10 17:30:39');
INSERT INTO `tb_sensitive_word` VALUES (3327, '丁一平', 3, 0, '2026-01-10 17:30:39', '2026-01-10 17:30:39');
INSERT INTO `tb_sensitive_word` VALUES (3328, '董建华', 3, 0, '2026-01-10 17:30:39', '2026-01-10 17:30:39');
INSERT INTO `tb_sensitive_word` VALUES (3329, '杜德印', 3, 0, '2026-01-10 17:30:39', '2026-01-10 17:30:39');
INSERT INTO `tb_sensitive_word` VALUES (3330, '杜世成', 3, 0, '2026-01-10 17:30:39', '2026-01-10 17:30:39');
INSERT INTO `tb_sensitive_word` VALUES (3331, '傅锐', 3, 0, '2026-01-10 17:30:39', '2026-01-10 17:30:39');
INSERT INTO `tb_sensitive_word` VALUES (3332, '郭伯雄', 3, 0, '2026-01-10 17:30:39', '2026-01-10 17:30:39');
INSERT INTO `tb_sensitive_word` VALUES (3333, '郭金龙', 3, 0, '2026-01-10 17:30:39', '2026-01-10 17:30:39');
INSERT INTO `tb_sensitive_word` VALUES (3334, '贺国强', 3, 0, '2026-01-10 17:30:39', '2026-01-10 17:30:39');
INSERT INTO `tb_sensitive_word` VALUES (3335, '胡春华', 3, 0, '2026-01-10 17:30:39', '2026-01-10 17:30:39');
INSERT INTO `tb_sensitive_word` VALUES (3336, '耀邦', 3, 0, '2026-01-10 17:30:39', '2026-01-10 17:30:39');
INSERT INTO `tb_sensitive_word` VALUES (3337, '华建敏', 3, 0, '2026-01-10 17:30:39', '2026-01-10 17:30:39');
INSERT INTO `tb_sensitive_word` VALUES (3338, '黄华华', 3, 0, '2026-01-10 17:30:39', '2026-01-10 17:30:39');
INSERT INTO `tb_sensitive_word` VALUES (3339, '黄丽满', 3, 0, '2026-01-10 17:30:39', '2026-01-10 17:30:39');
INSERT INTO `tb_sensitive_word` VALUES (3340, '黄兴国', 3, 0, '2026-01-10 17:30:39', '2026-01-10 17:30:39');
INSERT INTO `tb_sensitive_word` VALUES (3341, '回良玉', 3, 0, '2026-01-10 17:30:39', '2026-01-10 17:30:39');
INSERT INTO `tb_sensitive_word` VALUES (3342, '贾庆林', 3, 0, '2026-01-10 17:30:39', '2026-01-10 17:30:39');
INSERT INTO `tb_sensitive_word` VALUES (3343, '贾廷安', 3, 0, '2026-01-10 17:30:39', '2026-01-10 17:30:39');
INSERT INTO `tb_sensitive_word` VALUES (3344, '靖志远', 3, 0, '2026-01-10 17:30:39', '2026-01-10 17:30:39');
INSERT INTO `tb_sensitive_word` VALUES (3345, '李长春', 3, 0, '2026-01-10 17:30:39', '2026-01-10 17:30:39');
INSERT INTO `tb_sensitive_word` VALUES (3346, '李春城', 3, 0, '2026-01-10 17:30:39', '2026-01-10 17:30:39');
INSERT INTO `tb_sensitive_word` VALUES (3347, '李建国', 3, 0, '2026-01-10 17:30:39', '2026-01-10 17:30:39');
INSERT INTO `tb_sensitive_word` VALUES (3348, '李克强', 3, 0, '2026-01-10 17:30:39', '2026-01-10 17:30:39');
INSERT INTO `tb_sensitive_word` VALUES (3349, '李岚清', 3, 0, '2026-01-10 17:30:39', '2026-01-10 17:30:39');
INSERT INTO `tb_sensitive_word` VALUES (3350, '李沛瑶', 3, 0, '2026-01-10 17:30:39', '2026-01-10 17:30:39');
INSERT INTO `tb_sensitive_word` VALUES (3358, 'md', 6, 1, '2026-01-10 21:07:18', '2026-01-11 20:07:58');
INSERT INTO `tb_sensitive_word` VALUES (3359, 'mlgb', 6, 1, '2026-01-10 21:07:18', '2026-01-11 20:07:58');
INSERT INTO `tb_sensitive_word` VALUES (3360, 'test', 10, 1, '2026-01-11 21:16:34', '2026-01-11 21:16:55');
INSERT INTO `tb_sensitive_word` VALUES (3361, '卧槽', 6, 0, '2026-01-15 12:14:57', '2026-01-15 12:14:56');

-- ----------------------------
-- Table structure for tb_sensitive_word_category
-- ----------------------------
DROP TABLE IF EXISTS `tb_sensitive_word_category`;
CREATE TABLE `tb_sensitive_word_category`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键自增',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '分类名称',
  `delete_flag` tinyint NULL DEFAULT 0 COMMENT '删除标记，0-未删除，1-已删除',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间，默认为插入时时间戳',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间，插入时自动填充，更新时自动更新',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 13 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '敏感词分类表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_sensitive_word_category
-- ----------------------------
INSERT INTO `tb_sensitive_word_category` VALUES (1, '反动', 0, '2026-01-10 17:13:20', '2026-01-10 17:13:20');
INSERT INTO `tb_sensitive_word_category` VALUES (2, '暴恐', 0, '2026-01-10 17:13:20', '2026-01-10 17:13:20');
INSERT INTO `tb_sensitive_word_category` VALUES (3, '政治', 0, '2026-01-10 17:13:20', '2026-01-10 17:13:20');
INSERT INTO `tb_sensitive_word_category` VALUES (4, '色情', 0, '2026-01-10 17:13:20', '2026-01-10 17:13:20');
INSERT INTO `tb_sensitive_word_category` VALUES (5, '广告', 0, '2026-01-10 17:13:20', '2026-01-10 17:13:20');
INSERT INTO `tb_sensitive_word_category` VALUES (6, '粗俗', 0, '2026-01-10 17:13:20', '2026-01-10 17:13:20');
INSERT INTO `tb_sensitive_word_category` VALUES (7, 'test', 1, '2026-01-11 21:13:49', '2026-01-11 21:16:47');
INSERT INTO `tb_sensitive_word_category` VALUES (8, 'test1', 1, '2026-01-11 21:13:53', '2026-01-11 21:17:01');
INSERT INTO `tb_sensitive_word_category` VALUES (9, 'test2', 1, '2026-01-11 21:13:56', '2026-01-11 21:17:01');
INSERT INTO `tb_sensitive_word_category` VALUES (10, 'test3', 1, '2026-01-11 21:16:11', '2026-01-11 21:17:01');
INSERT INTO `tb_sensitive_word_category` VALUES (11, 'test', 1, '2026-01-11 21:20:15', '2026-01-11 21:20:30');
INSERT INTO `tb_sensitive_word_category` VALUES (12, 'test1', 1, '2026-01-11 21:20:36', '2026-01-11 21:21:42');

-- ----------------------------
-- Table structure for tb_topic
-- ----------------------------
DROP TABLE IF EXISTS `tb_topic`;
CREATE TABLE `tb_topic`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '话题ID，主键自增',
  `name` varchar(25) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '话题名称，不为空',
  `delete_flag` tinyint NULL DEFAULT 0 COMMENT '删除标记，0-未删除，1-已删除',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间，默认为插入时时间戳',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间，默认为插入时时间戳，插入时自动更新',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '话题表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_topic
-- ----------------------------
INSERT INTO `tb_topic` VALUES (1, '萌宠打卡', 0, '2025-12-18 12:20:56', '2025-12-18 12:20:56');
INSERT INTO `tb_topic` VALUES (2, '训练心得', 0, '2025-12-18 12:20:56', '2025-12-18 12:20:56');
INSERT INTO `tb_topic` VALUES (3, '救助互助', 0, '2025-12-18 12:20:56', '2025-12-18 12:20:56');
INSERT INTO `tb_topic` VALUES (4, '医疗问答', 0, '2025-12-18 12:20:56', '2025-12-18 12:20:56');
INSERT INTO `tb_topic` VALUES (5, 'test1', 1, '2026-01-07 17:21:47', '2026-01-07 17:23:53');
INSERT INTO `tb_topic` VALUES (6, 'test2', 1, '2026-01-07 17:22:19', '2026-01-07 17:23:53');
INSERT INTO `tb_topic` VALUES (7, 'test3', 1, '2026-01-07 17:22:25', '2026-01-07 17:23:56');
INSERT INTO `tb_topic` VALUES (8, 'test4', 1, '2026-01-07 17:24:08', '2026-01-07 17:25:42');

-- ----------------------------
-- Table structure for tb_topic_post
-- ----------------------------
DROP TABLE IF EXISTS `tb_topic_post`;
CREATE TABLE `tb_topic_post`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '关联ID，主键自增',
  `post_id` bigint NOT NULL COMMENT '帖子ID，不为空',
  `topic_id` bigint NOT NULL COMMENT '话题ID，不为空',
  `delete_flag` tinyint NULL DEFAULT 0 COMMENT '删除标记，0-未删除，1-已删除',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间，默认为插入时时间戳',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间，默认为插入时时间戳，插入时自动更新',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_post_id`(`post_id` ASC) USING BTREE,
  INDEX `idx_topic_id`(`topic_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 49 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '话题帖子关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_topic_post
-- ----------------------------
INSERT INTO `tb_topic_post` VALUES (1, 1, 1, 0, '2025-12-18 12:21:12', '2025-12-18 12:21:12');
INSERT INTO `tb_topic_post` VALUES (2, 1, 2, 0, '2025-12-18 12:21:12', '2025-12-18 12:21:12');
INSERT INTO `tb_topic_post` VALUES (3, 2, 3, 0, '2025-12-18 12:21:12', '2025-12-18 12:21:12');
INSERT INTO `tb_topic_post` VALUES (4, 2, 1, 0, '2025-12-18 12:21:12', '2025-12-18 12:21:12');
INSERT INTO `tb_topic_post` VALUES (5, 3, 4, 0, '2025-12-18 12:21:12', '2025-12-18 12:21:12');
INSERT INTO `tb_topic_post` VALUES (6, 3, 2, 0, '2025-12-18 12:21:12', '2025-12-18 12:21:12');
INSERT INTO `tb_topic_post` VALUES (12, 7, 2, 1, '2025-12-20 16:31:42', '2026-01-10 20:21:05');
INSERT INTO `tb_topic_post` VALUES (13, 8, 2, 1, '2025-12-20 16:33:51', '2026-01-10 20:21:28');
INSERT INTO `tb_topic_post` VALUES (14, 8, 1, 1, '2025-12-20 16:33:51', '2026-01-10 20:21:28');
INSERT INTO `tb_topic_post` VALUES (15, 9, 2, 1, '2025-12-20 16:45:40', '2026-01-10 20:21:10');
INSERT INTO `tb_topic_post` VALUES (17, 12, 2, 1, '2025-12-20 16:52:04', '2025-12-20 22:42:16');
INSERT INTO `tb_topic_post` VALUES (19, 12, 2, 1, '2025-12-20 22:46:28', '2025-12-20 22:49:24');
INSERT INTO `tb_topic_post` VALUES (20, 12, 3, 1, '2025-12-20 22:46:28', '2025-12-20 22:49:24');
INSERT INTO `tb_topic_post` VALUES (21, 12, 2, 1, '2025-12-20 22:49:24', '2025-12-20 22:50:56');
INSERT INTO `tb_topic_post` VALUES (22, 12, 4, 1, '2025-12-20 22:49:24', '2025-12-20 22:50:56');
INSERT INTO `tb_topic_post` VALUES (23, 12, 2, 0, '2025-12-20 22:50:56', '2025-12-20 22:50:56');
INSERT INTO `tb_topic_post` VALUES (24, 12, 4, 0, '2025-12-20 22:50:56', '2025-12-20 22:50:56');
INSERT INTO `tb_topic_post` VALUES (25, 13, 2, 0, '2025-12-20 23:35:39', '2025-12-20 23:35:38');
INSERT INTO `tb_topic_post` VALUES (26, 13, 4, 0, '2025-12-20 23:35:39', '2025-12-20 23:35:38');
INSERT INTO `tb_topic_post` VALUES (27, 14, 2, 1, '2025-12-21 21:18:38', '2025-12-29 16:35:14');
INSERT INTO `tb_topic_post` VALUES (28, 15, 1, 0, '2025-12-23 10:10:46', '2025-12-23 10:10:45');
INSERT INTO `tb_topic_post` VALUES (29, 16, 1, 0, '2025-12-26 20:54:15', '2025-12-26 20:54:15');
INSERT INTO `tb_topic_post` VALUES (30, 16, 2, 0, '2025-12-26 20:54:15', '2025-12-26 20:54:15');
INSERT INTO `tb_topic_post` VALUES (31, 17, 8, 1, '2026-01-07 17:24:26', '2026-01-07 17:25:15');
INSERT INTO `tb_topic_post` VALUES (32, 18, 1, 1, '2026-01-07 19:01:44', '2026-01-07 19:02:00');
INSERT INTO `tb_topic_post` VALUES (33, 19, 2, 1, '2026-01-10 10:28:16', '2026-01-10 10:30:10');
INSERT INTO `tb_topic_post` VALUES (34, 20, 2, 1, '2026-01-10 10:28:50', '2026-01-10 10:33:05');
INSERT INTO `tb_topic_post` VALUES (35, 36, 2, 1, '2026-02-10 14:36:23', '2026-02-10 14:51:18');
INSERT INTO `tb_topic_post` VALUES (36, 37, 2, 1, '2026-02-10 14:52:04', '2026-02-10 14:54:35');
INSERT INTO `tb_topic_post` VALUES (37, 38, 2, 1, '2026-02-10 14:54:47', '2026-02-10 14:56:16');
INSERT INTO `tb_topic_post` VALUES (38, 39, 4, 1, '2026-02-10 14:56:25', '2026-02-10 14:58:57');
INSERT INTO `tb_topic_post` VALUES (39, 40, 3, 1, '2026-02-10 14:59:07', '2026-02-10 15:14:58');
INSERT INTO `tb_topic_post` VALUES (40, 41, 3, 1, '2026-02-10 15:15:09', '2026-02-10 16:44:48');
INSERT INTO `tb_topic_post` VALUES (41, 42, 2, 0, '2026-02-10 17:23:52', '2026-02-10 17:23:51');
INSERT INTO `tb_topic_post` VALUES (42, 43, 1, 0, '2026-02-10 17:24:03', '2026-02-10 17:24:02');
INSERT INTO `tb_topic_post` VALUES (43, 44, 3, 0, '2026-02-10 17:30:02', '2026-02-10 17:30:02');
INSERT INTO `tb_topic_post` VALUES (44, 45, 2, 1, '2026-02-14 21:52:14', '2026-02-14 21:57:44');
INSERT INTO `tb_topic_post` VALUES (45, 46, 4, 1, '2026-02-14 21:58:34', '2026-02-14 22:00:06');
INSERT INTO `tb_topic_post` VALUES (46, 47, 2, 0, '2026-02-14 22:00:24', '2026-02-14 22:00:23');
INSERT INTO `tb_topic_post` VALUES (47, 48, 3, 0, '2026-02-21 16:50:29', '2026-02-21 16:50:29');
INSERT INTO `tb_topic_post` VALUES (48, 49, 3, 1, '2026-02-21 16:51:35', '2026-02-21 16:51:54');

-- ----------------------------
-- Table structure for tb_user
-- ----------------------------
DROP TABLE IF EXISTS `tb_user`;
CREATE TABLE `tb_user`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID，主键自增',
  `username` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名，不可为空且唯一',
  `email` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '邮箱，不可为空且唯一',
  `password` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码，不可为空',
  `gender` tinyint NULL DEFAULT 3 COMMENT '性别，1-男，2-女，3-保密',
  `real_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '用户真实姓名，用于和身份证一起做实名认证',
  `id_card` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '身份证号',
  `phone` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '手机号',
  `avatar_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/default_avatar.png' COMMENT '用户头像',
  `role_id` bigint NULL DEFAULT 1 COMMENT '角色ID，默认为1，1-普通用户，0-管理员，与角色表关联',
  `receipt_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '收货人名称，不为空',
  `receipt_id` bigint NULL DEFAULT NULL COMMENT '收货地址ID，与地址表关联',
  `profile` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '个人简介，默认为空',
  `status` tinyint NULL DEFAULT 0 COMMENT '账号状态，0-正常使用/未销户，1-已销户',
  `ban_flag` tinyint NULL DEFAULT 0 COMMENT '禁言标记，0-未禁言，1-已禁言',
  `delete_flag` tinyint NULL DEFAULT 0 COMMENT '删除标记，0-未删除，1-已删除，默认为0',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间，默认为插入时时间戳',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间，默认为插入时时间戳，插入时自动更新',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_role_id`(`role_id` ASC) USING BTREE,
  INDEX `idx_receipt_id`(`receipt_id` ASC) USING BTREE,
  INDEX `idx_username`(`username` ASC) USING BTREE,
  INDEX `idx_email`(`email` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 15 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_user
-- ----------------------------
INSERT INTO `tb_user` VALUES (1, 'user1', 'user1@example.com', '$2a$10$00ifF3WwnLMrUXa.gO8N5OXufwtp83.fR2szpUeMFahXc6t4wKmn6', 2, 'user1', '123456789012345678', '15924567856', 'https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/default_avatar.png', 1, 'user1', 11, NULL, 0, 0, 0, '2025-12-12 19:54:32', '2026-02-24 20:42:23');
INSERT INTO `tb_user` VALUES (2, 'user2', 'user2@example.com', '123456', 3, 'user2', '123456789012345679', '15926455678', 'https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/default_avatar.png', 1, 'user2', 1, NULL, 0, 0, 0, '2025-12-12 19:54:34', '2026-01-02 21:03:18');
INSERT INTO `tb_user` VALUES (3, 'user3', 'user3@example.com', '123456', 1, 'user3', '421125201112032019', '15926454022', 'https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/bfd1455e-60e1-4c81-8840-93e135e8a682_avatar.jpg', 1, 'user3', 52, '腼腆的小孩子~~', 0, 0, 0, '2025-12-12 19:54:37', '2026-01-02 21:00:45');
INSERT INTO `tb_user` VALUES (4, '小橘岛系统管理员', 'admin@example.com', '$2a$10$iOvxwAARgUbtX5Fx112BCuSRCRJ3PPEtNKVrAjAAn6iHQuLU5zWVi', 1, 'admin', '421124200308232011', '13800138011', 'https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/1f287ae8-6af9-43fa-aa00-889ab4ddb3be_avatar.jpg', 0, NULL, NULL, '小橘岛系统管理员', 0, 0, 0, '2025-12-27 21:26:30', '2026-02-24 20:20:47');
INSERT INTO `tb_user` VALUES (5, 'user', '123@qq.com', '123456', 3, 'xxx', '421124200308232011', '15942561252', 'https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/default_avatar.png', 1, '11', 53, NULL, 0, 0, 1, '2026-01-03 18:57:22', '2026-01-08 11:24:44');
INSERT INTO `tb_user` VALUES (6, '11', '124@qq.com', '123456', 3, NULL, NULL, NULL, 'https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/default_avatar.png', 1, NULL, NULL, NULL, 0, 0, 1, '2026-01-03 19:36:12', '2026-01-03 19:36:18');
INSERT INTO `tb_user` VALUES (7, 'epsda', '222@qq.com', '123456', 3, NULL, NULL, NULL, 'https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/default_avatar.png', 1, NULL, NULL, NULL, 0, 0, 0, '2026-02-14 16:47:37', '2026-02-14 16:47:37');
INSERT INTO `tb_user` VALUES (8, 'test', 'test@qq.com', '123456', 3, NULL, NULL, NULL, 'https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/default_avatar.png', 1, NULL, NULL, NULL, 0, 0, 0, '2026-02-19 20:01:58', '2026-02-19 20:01:58');
INSERT INTO `tb_user` VALUES (9, '憨八嘎', 'watneymark82@gmail.com', '123456', 3, NULL, NULL, NULL, 'https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/default_avatar.png', 1, NULL, NULL, NULL, 0, 0, 1, '2026-02-21 10:24:54', '2026-02-21 10:30:41');
INSERT INTO `tb_user` VALUES (10, 'mark', 'watneymark82@gmail.com', '123456', 3, NULL, NULL, NULL, 'https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/default_avatar.png', 1, NULL, NULL, NULL, 0, 0, 1, '2026-02-21 10:31:27', '2026-02-21 10:34:41');
INSERT INTO `tb_user` VALUES (11, '憨八嘎', 'watneymark82@gmail.com', '123456', 3, NULL, NULL, NULL, 'https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/default_avatar.png', 1, NULL, NULL, NULL, 0, 0, 1, '2026-02-21 16:04:23', '2026-02-22 23:21:19');
INSERT INTO `tb_user` VALUES (12, '憨八嘎', 'watneymark82@gmail.com', '123456', 3, NULL, NULL, NULL, 'https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/default_avatar.png', 1, NULL, NULL, NULL, 0, 0, 1, '2026-02-22 23:46:57', '2026-02-22 23:47:33');
INSERT INTO `tb_user` VALUES (13, '憨八嘎', 'watneymark82@gmail.com', '123456', 3, NULL, NULL, NULL, 'https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/default_avatar.png', 1, NULL, NULL, NULL, 0, 0, 1, '2026-02-23 00:02:19', '2026-02-23 00:03:17');
INSERT INTO `tb_user` VALUES (14, '憨八嘎', 'watneymark82@gmail.com', '123456', 3, NULL, NULL, NULL, 'https://pet-store-bucket.oss-cn-hangzhou.aliyuncs.com/default_avatar.png', 1, NULL, NULL, NULL, 0, 0, 0, '2026-02-23 00:18:44', '2026-02-23 00:18:44');

SET FOREIGN_KEY_CHECKS = 1;
