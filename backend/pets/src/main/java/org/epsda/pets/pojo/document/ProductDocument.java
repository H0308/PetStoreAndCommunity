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
 * Time: 17:30
 *
 * @Author: 憨八嘎
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(indexName = "idx_product")
@Setting(settingPath = "elasticsearch-settings.json")
public class ProductDocument {
    @Id
    @Field(type = FieldType.Long)
    private Long id;
    @MultiField(
            mainField = @Field(type = FieldType.Text, analyzer = "ik_smart", searchAnalyzer = "ik_max_word"), // 字段名为nickname
            otherFields = {
                    @InnerField(suffix = "ngram", type = FieldType.Text, analyzer = "ngram_analyzer") // 字段名为nickname.ngram
            }
    )
    private String productName;
    @Field(type = FieldType.Integer)
    private Integer productType;
    @Field(type = FieldType.Text)
    private String productImageUrl;
    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
    private LocalDateTime createTime;
}
