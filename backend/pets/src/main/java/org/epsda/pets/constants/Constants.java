package org.epsda.pets.constants;

import jakarta.validation.constraints.NotNull;
import org.apache.coyote.OutputBuffer;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2025/12/08
 * Time: 10:07
 *
 * @Author: 憨八嘎
 */
public record Constants() {
    public static final Integer NORMAL = 0;
    public static final Integer SERVER_ERROR = 1;
    public static final Integer SYSTEM_ERROR = 2;
    public static final Integer RESOURCE_NOT_FOUND = 3;
    public static final String SERVER_ERROR_MESSAGE = "服务器异常";
    public static final String SYSTEM_ERROR_MESSAGE = "系统异常";
    public static final String RESOURCE_NOT_FOUND_MESSAGE = "资源不存在";

    public static final String TOKEN_HEADER = "Authorization";
    public static final String TOKEN_START_FLAG = "Bearer ";
    public static final long TOKEN_EXPIRATION_TIME = 24 * 60 * 60 * 1000;

    public static final String GOOGLE_EXCHANGE_ACCESS_TOKEN_URL = "https://oauth2.googleapis.com/token";
    public static final String GOOGLE_USER_INFO_URL = "https://www.googleapis.com/oauth2/v2/userinfo";

    // Redis邮箱验证码
    public static final String REDIS_EMAIL_CAPTCHA_PREFIX = "auth:email:";
    // 商城主页一级分类
    public static final String REDIS_INDEX_CATEGORY_PREFIX = "user:index:category:";
    // 帖子栏目
    public static final String REDIS_POST_COLUMN = "user:post:column";
    // Jwt Token黑名单
    public static final String JWT_TOKEN_BLACK_LIST_PREFIX = "blacklist:token:";
    // 验证码过期时间
    public static final Integer CAPTCHA_EXPIRE_TIME_MINUTES = 1;
    // 验证码长度
    public static final Integer CAPTCHA_LENGTH = 6;
    // 邮箱验证码后缀-用于防止多次请求验证码
    public static final String CAPTCHA_LIMIT_SUFFIX = "reject";
    // 邮箱验证码1分钟限制发送
    public static final Integer CAPTCHA_LIMIT_TIME_MINUTES = 1;
    // 分类有效期
    public static final Integer INDEX_CATEGORY_EXPIRE_TIME_DAY = 1;
    // 栏目有效期
    public static final Integer COLUMN_EXPIRE_TIME_DAY = 1;
    // 空值过期时间
    public static final Integer NULL_EXPIRE_TIME_MINUTES = 30;

    // 地球半径（米）
    public static final double EARTH_RADIUS = 6371200;
    // 距离阈值
    public static final Double DIS_THRESHOLD = 500.0;

    // 验证码有效时间
    public static final long VALID_CAPTCHA_TIME = 60 * 1000;

    // 角色标记
    public static final Long ADMIN_FLAG = 0L;
    public static final Long NORMAL_USER_FLAG = 1L;

    // 媒体类型
    public static final Integer MEDIA_TYPE_IMAGE = 1;
    public static final Integer MEDIA_TYPE_VIDEO = 2;

    // 评论类型
    public static final Integer PRODUCT_COMMENT = 1;
    public static final Integer POST_COMMENT = 2;

    // 删除标记
    public static final Integer NOT_DELETED_FLAG = 0;
    public static final Integer DELETED_FLAG = 1;

    // 主图标记
    public static final Integer NOT_MAIN_IMAGE_FLAG = 0;
    public static final Integer MAIN_IMAGE_FLAG = 1;

    // 点赞状态
    public static final Integer ON_LIKE_DISLIKE_FLAG = 0;
    public static final Integer CANCEL_LIKE_DISLIKE_FLAG = 1;

    // 收藏状态
    public static final Integer ON_FAVOR_FLAG = 0;
    public static final Integer CANCEL_FAVOR_FLAG = 1;

    // 订单状态
    public static final Integer ORDER_TO_PAY = 1;
    public static final Integer ORDER_TO_DELIVER = 2;
    public static final Integer ORDER_DELIVERED = 3;
    public static final Integer ORDER_TO_SIGN = 4;
    public static final Integer ORDER_SIGNED = 5;
    public static final Integer ORDER_CANCELLED = 6;

    // 退款状态
    public static final Integer ORDER_NOT_REFUND = 0;
    public static final Integer ORDER_TO_REFUND = 1;

    // 商品状态
    public static final Integer ON_SELLING = 1;
    public static final Integer SOLD_OUT = 2;
    public static final Integer OFF_SHELF = 3;

    // 对象类型
    public static final Integer PRODUCT = 1;
    public static final Integer POST = 2;

    // 商品类型
    public static final Integer PET = 1;
    public static final Integer SUPPLY = 2;

    // 帖子状态
    public static final Integer POST_DRAFT = 1;
    public static final Integer POST_ON_AUDIT = 2;
    public static final Integer POST_AUDIT_PASS = 3;
    public static final Integer POST_AUDIT_FAIL = 4;

    // 评论状态
    public static final Integer COMMENT_ON_AUDIT = 1;
    public static final Integer COMMENT_AUDIT_PASS = 2;
    public static final Integer COMMENT_AUDIT_FAIL = 3;

    // 账号状态
    public static final Integer NOT_DEACTIVATE_FLAG = 0;
    public static final Integer DEACTIVATED_FLAG = 1;

    // 实名认证状态
    public static final Integer REAL_NAME_NOT_AUTHENTICATE_FLAG = 0;
    public static final Integer REAL_NAME_AUTHENTICATED_FLAG = 1;

    // 禁言状态
    public static final Integer NOT_BANNED_FLAG = 0;
    public static final Integer BANNED_FLAG = 1;

    // 退款操作
    public static final Integer REFUSE_REFUND = 0;
    public static final Integer ALLOW_REFUND = 1;

    // 审核操作
    public static final Integer REFUSE_VERIFY = 0;
    public static final Integer ALLOW_VERIFY = 1;

    // 物流方式
    public static final Integer AIR_TRANSPORT = 1;
    public static final Integer LAND_TRANSPORT = 2;

    // 排序方式
    public static final Integer DESC_FLAG = 1;
    public static final Integer ASC_FLAG = 2;

    // 消息类型
    public static final Integer SYSTEM_MESSAGE = 1;
    public static final Integer CHAT_MESSAGE = 2;
    public static final Integer PRODUCT_MESSAGE = 3;
    public static final Integer ORDER_MESSAGE = 4;
    public static final Integer MEDIA_MESSAGE = 5;
    public static final Integer STATUS_MESSAGE = 6;

    // 最近消息列表消息呈现值
    public static final String ORDER_MESSAGE_SHOWUP = "[订单信息]";
    public static final String PRODUCT_MESSAGE_SHOWUP = "[商品信息]";
    public static final String MEDIA_MESSAGE_SHOWUP = "[媒体文件信息]";

    // 在线状态
    public static final Integer ONLINE = 1;
    public static final Integer OFFLINE = 2;

    // 未读/已读标记
    public static final Integer UNREAD_FLAG = 0;
    public static final Integer READ_FLAG = 1;

    // 系统通知类型
    public static final Integer ORDER_SYSTEM_MESSAGE = 1;
    public static final Integer AUDIT_SYSTEM_MESSAGE = 2;
    public static final Integer LIKE_REPLY_SYSTEM_MESSAGE = 3;
    public static final Integer BAN_SYSTEM_MESSAGE = 4;

    // 系统通知标题
    public static final String ORDER_SYSTEM_MESSAGE_TITLE = "订单通知";
    public static final String AUDIT_SYSTEM_MESSAGE_TITLE = "审核通知";
    public static final String BAN_SYSTEM_MESSAGE_TITLE = "禁言通知";
    public static final String LIKE_SYSTEM_MESSAGE_TITLE = "帖子点赞通知";
    public static final String REPLY_SYSTEM_MESSAGE_TITLE = "帖子回复通知";

    // 默认推荐数量
    public static final Integer DEFAULT_TOP_N = 10;
    // 参与相似度计算的最近邻用户数
    public static final Integer TOP_K_NEIGHBORS = 10;

    // 注册邮件发送队列
    public static final String REGISTER_EMAIL_QUEUE = "register-queue";
    // 通知邮件发送队列
    public static final String NOTIFY_EMAIL_QUEUE = "notify-queue";
    // 帖子审核队列
    public static final String POST_AUDIT_QUEUE = "post-audit-queue";
    // 评论审核队列
    public static final String COMMENT_AUDIT_QUEUE = "comment-audit-queue";
    // 审核交换机
    public static final String AUDIT_EXCHANGE = "audit-exchange";
    // 帖子审核Binding Key
    public static final String POST_AUDIT_ROUTING_KEY = "post";
    // 评论审核Binding Key
    public static final String COMMENT_AUDIT_ROUTING_KEY = "comment";
    // 帖子绑定标记
    public static final String POST_AUDIT_BINDING_TAG = "post-audit-binding-tag";
    // 评论绑定标记
    public static final String COMMENT_AUDIT_BINDING_TAG = "comment-audit-binding-tag";
}
