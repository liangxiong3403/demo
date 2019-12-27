package org.liangxiong.demo.controller;

import com.alibaba.fastjson.JSONObject;
import com.vividsolutions.jts.io.ParseException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.map.HashedMap;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.geo.ShapeRelation;
import org.elasticsearch.common.geo.builders.CoordinatesBuilder;
import org.elasticsearch.common.geo.builders.PolygonBuilder;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.liangxiong.demo.util.WktUtil;
import org.locationtech.jts.geom.Coordinate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author liangxiong
 * @version 1.0.0
 * @date 2019-12-10
 * @description 学校相关搜索业务
 **/
@Slf4j
@RequestMapping("/elastic/school")
@RestController
public class SchoolController {

    private RestHighLevelClient client;

    /**
     * 索引名称
     */
    private static final String SCHOOL_INDEX_NAME = "school";
    /**
     * id
     */
    private static final String SCHOOL_ID = "school_id";
    /**
     * 名称
     */
    private static final String SCHOOL_NAME = "school_name";
    /**
     * 地址
     */
    private static final String ADDRESS = "address";

    @Autowired
    public SchoolController(RestHighLevelClient client) {
        this.client = client;
    }

    /**
     * 数据结构初始化
     */
    @PostConstruct
    public void init() {
        // 判断索引是否存在
        boolean exists = true;
        try {
            exists = client.indices().exists(new GetIndexRequest(SCHOOL_INDEX_NAME), RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error("school索引people检查失败:{}", e.getMessage());
        }
        if (!exists) {
            XContentBuilder mapping = null;
            try {
                mapping = XContentFactory.jsonBuilder()
                        .startObject()
                        .startObject("properties")
                        // id
                        .startObject(SCHOOL_ID).field("type", "keyword").endObject()
                        // 名称
                        .startObject(SCHOOL_NAME).field("type", "text").field("analyzer", "hanlp_speed").endObject()
                        // 地址
                        .startObject(ADDRESS).field("type", "text").field("analyzer", "hanlp_index").endObject()
                        .endObject().endObject();
            } catch (IOException e) {
                log.error("索引mapping阶段构造出错:{}", e.getMessage());
            }
            // 创建索引(不能重复创建,否则报错)
            CreateIndexRequest createIndexRequest = new CreateIndexRequest(SCHOOL_INDEX_NAME).settings(Settings.builder()
                    // 分片数量
                    .put("index.number_of_shards", 1)
                    // 副本数量
                    .put("index.number_of_replicas", 0)).mapping(mapping);
            try {
                CreateIndexResponse response = client.indices().create(createIndexRequest, RequestOptions.DEFAULT);
                if (log.isInfoEnabled()) {
                    log.info(response.toString());
                }
            } catch (IOException e) {
                log.error("创建school索引失败: " + e.getMessage());
            }
        }
    }

    /**
     * 人口对应查询
     *
     * @param type       查找类型(all+模糊匹配;某个字段+模糊匹配)
     * @param fieldName  字段名称
     * @param fieldValue 字段值
     * @param queryText  带查询文本
     * @param start      查找起始点位偏移量
     * @param size       返回数量
     * @return
     */
    @PostMapping("/search")
    public List<JSONObject> selectByTermWord(@RequestParam String firstLevel, @RequestParam String orgPath, @RequestParam String type, @RequestParam String fieldName, @RequestParam String fieldValue, @RequestParam String queryText, @RequestParam int start, @RequestParam int size, @RequestBody JSONObject gps) {
        List<JSONObject> result = new ArrayList<>(10);
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        String wkt = gps.getString("gps");
        String regex = "\\d+";
        // 权限约束
        TermQueryBuilder permissionQueryBuilder = QueryBuilders.termQuery("permission", orgPath);
        // 权限条件
        boolQueryBuilder = boolQueryBuilder.must(permissionQueryBuilder);
        if (!StringUtils.isEmpty(queryText) && queryText.matches(regex)) {
            // 正则判断是对数字模糊匹配
            WildcardQueryBuilder queryBuilder = QueryBuilders.wildcardQuery("ID_CARD_NO", "*" + queryText + "*");
            constructQueryBuilder(type, fieldName, fieldValue, queryBuilder, boolQueryBuilder);
        } else if (!StringUtils.isEmpty(queryText)) {
            // 正则判断是对文本模糊匹配
            MultiMatchQueryBuilder queryBuilder = QueryBuilders.multiMatchQuery(queryText, SCHOOL_NAME, ADDRESS);
            constructQueryBuilder(type, fieldName, fieldValue, queryBuilder, boolQueryBuilder);
        } else {
            List<Coordinate> coordinates = WktUtil.getCoordinates(wkt);
            if (!CollectionUtils.isEmpty(coordinates)) {
                PolygonBuilder polygonBuilder = new PolygonBuilder(new CoordinatesBuilder().coordinates(coordinates));
                if (log.isInfoEnabled()) {
                    log.info("WKT String: {}", polygonBuilder.toWKT());
                }
                GeoShapeQueryBuilder geoShapeQueryBuilder = null;
                try {
                    geoShapeQueryBuilder = QueryBuilders.geoShapeQuery(
                            "gps", polygonBuilder);
                    geoShapeQueryBuilder.relation(ShapeRelation.WITHIN);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                // wkt条件
                boolQueryBuilder.must(geoShapeQueryBuilder);
            }
            // 用户未输入
            constructQueryBuilder(type, fieldName, fieldValue, null, boolQueryBuilder);
        }
        try {
            SearchResponse response = client.search(new SearchRequest(SCHOOL_INDEX_NAME).source(new SearchSourceBuilder().query(boolQueryBuilder).from(start).size(size)), RequestOptions.DEFAULT);
            if (response != null) {
                SearchHit[] searchHits = response.getHits().getHits();
                if (searchHits != null && searchHits.length > 0) {
                    Arrays.stream(searchHits).forEach(e -> {
                        JSONObject returnMap = new JSONObject(16);
                        Map<String, Object> source = e.getSourceAsMap();
                        returnMap.put("id", source.get(SCHOOL_ID));
                        returnMap.put("name", source.get(SCHOOL_NAME));
                        returnMap.put(ADDRESS, source.get(ADDRESS));
                        long total = response.getHits().getTotalHits().value;
                        returnMap.put("total", total);
                        Object returnGps = source.get("gps");
                        returnMap.put("gps", returnGps != null ? returnGps : "");
                        result.add(returnMap);
                    });
                }
                return result;
            }
        } catch (IOException e) {
            log.error("查询操作出现异常:{}", e.getMessage());
        }
        return Collections.emptyList();
    }


    /**
     * 公共方法
     *
     * @param type         查询类型:全部 或者  某一属性
     * @param fieldName    字段名称
     * @param fieldValue   字段值
     * @param queryBuilder 查询构造器
     * @return
     */
    private void constructQueryBuilder(String type, String fieldName, String fieldValue, QueryBuilder queryBuilder, BoolQueryBuilder boolQueryBuilder) {
        if ("single".equals(type)) {
            // 二级菜单选择某一个
            TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery(fieldName, fieldValue);
            boolQueryBuilder = boolQueryBuilder.must(termQueryBuilder);
            if (queryBuilder != null) {
                boolQueryBuilder.must(queryBuilder);
            }
        } else if ("all".equals(type)) {
            // 二级菜单选择全部
            if (queryBuilder != null) {
                boolQueryBuilder.must(queryBuilder);
            }
        }
    }


    /**
     * 匹配文档的point在多边形范围内部
     *
     * @param param WKT文本,遵循OGC规范
     * @return
     * @throws IOException
     * @throws ParseException
     */
    @GetMapping("/polygon")
    public Map<String, Object> selectByPolygon(@RequestParam String param, @RequestParam int start, @RequestParam int size) throws IOException, ParseException {
        if (StringUtils.hasLength(param)) {
            List<GeoPoint> points = WktUtil.getPoints(param);
            if (!CollectionUtils.isEmpty(points)) {
                GeoPolygonQueryBuilder builder = QueryBuilders.geoPolygonQuery("gps", points);
                if (log.isInfoEnabled()) {
                    log.info("WKT String: {}", builder.toString());
                }
                SearchResponse response = client.search(new SearchRequest(new String[]{SCHOOL_INDEX_NAME}, new SearchSourceBuilder().query(builder).sort("party_id", SortOrder.ASC).from(start).size(size)), RequestOptions.DEFAULT);
                if (response != null) {
                    List<Map<String, Object>> data = Arrays.stream(response.getHits().getHits()).map(e -> e.getSourceAsMap()).collect(Collectors.toList());
                    Map<String, Object> result = new HashedMap<>(8);
                    result.put("data", data);
                    result.put("count", response.getHits().getTotalHits().value);
                    return result;
                }
            }
        }
        return null;
    }


}
