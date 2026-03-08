package org.epsda.pets.pojo.document;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.cglib.core.Local;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.time.LocalDateTime;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2026/02/10
 * Time: 13:13
 *
 * @Author: 憨八嘎
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(indexName = "idx_favorite")
@Setting(settingPath = "elasticsearch-settings.json")
public class FavoriteDocument {
    @Id
    @Field(type = FieldType.Long)
    private Long favorId;
    @Field(type = FieldType.Long)
    private Long postId;
    @Field(type = FieldType.Long)
    private Long favorUserId;
    @Field(type = FieldType.Long)
    private Long postUserId;
    @MultiField(
            mainField = @Field(type = FieldType.Text, analyzer = "ik_smart", searchAnalyzer = "ik_max_word"), // 字段名为nickname
            otherFields = {
                    @InnerField(suffix = "ngram", type = FieldType.Text, analyzer = "ngram_analyzer") // 字段名为nickname.ngram
            }
    )
    private String postUsername;
    @Field(type = FieldType.Text)
    private String postUserAvatar;
    @MultiField(
            mainField = @Field(type = FieldType.Text, analyzer = "ik_smart", searchAnalyzer = "ik_max_word"), // 字段名为nickname
            otherFields = {
                    @InnerField(suffix = "ngram", type = FieldType.Text, analyzer = "ngram_analyzer") // 字段名为nickname.ngram
            }
    )
    private String title;
    @MultiField(
            mainField = @Field(type = FieldType.Text, analyzer = "ik_smart", searchAnalyzer = "ik_max_word"), // 字段名为nickname
            otherFields = {
                    @InnerField(suffix = "ngram", type = FieldType.Text, analyzer = "ngram_analyzer") // 字段名为nickname.ngram
            }
    )
    private String content;
    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
    private LocalDateTime createTime;
    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
    private LocalDateTime updateTime;
}
