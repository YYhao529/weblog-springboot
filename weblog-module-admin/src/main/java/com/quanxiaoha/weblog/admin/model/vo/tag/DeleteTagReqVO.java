package com.quanxiaoha.weblog.admin.model.vo.tag;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ApiModel("删除标签 VO")
public class DeleteTagReqVO {

    /**
     * 分类 ID
     */
    @NotNull(message = "标签 ID 不能为空")
    private Long id;
}
