package org.epsda.pets.pojo.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.time.LocalDateTime;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2026/02/09
 * Time: 21:14
 *
 * @Author: 憨八嘎
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(indexName = "idx_post")
@Setting(settingPath = "elasticsearch-settings.json")
public class PostDocument {
    @Id
    @Field(type = FieldType.Long)
    private Long postId;
    @Field(type = FieldType.Long)
    private Long userId;
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
    @Field(type = FieldType.Text)
    private String postMediaUrl;
    @Field(type = FieldType.Integer)
    private Integer postMediaType;
    @Field(type = FieldType.Integer)
    private Integer status;
    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
    private LocalDateTime createTime;
}
