package org.liangxiong.demo.entity;

import lombok.Getter;
import lombok.Setter;

/**
 * @Author liangxiong
 * @Date 2019-12-02
 * @Description
 * @Version
 **/
@Setter
@Getter
public class House {

    /**
     * 主键
     */
    private String houseId;

    /**
     * 坐标位置(维度,经度)
     */
    private String location;

    /**
     * GEO图形,多边形表示
     */
    private String polygon;
}
