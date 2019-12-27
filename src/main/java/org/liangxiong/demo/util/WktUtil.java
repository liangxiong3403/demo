package org.liangxiong.demo.util;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import org.elasticsearch.common.geo.GeoPoint;
import org.geotools.geometry.jts.JTSFactoryFinder;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author liangxiong
 * @Date 2019-12-02
 * @Description
 * @Version
 **/
public class WktUtil {

    public static final WKTReader wktReader = new WKTReader(JTSFactoryFinder.getGeometryFactory());

    private WktUtil() {
    }

    /**
     * 多边形地WKT转化为point
     *
     * @param polygon 边形WKT
     * @return
     */
    public static List<GeoPoint> getPoints(String polygon) {
        try {
            return Arrays.stream(wktReader.read(polygon).getCoordinates()).map(e -> new GeoPoint(e.y, e.x)).collect(Collectors.toList());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 多边形polygon转化为Coordinate
     *
     * @param polygon 多边形WKT
     * @return
     */
    public static List<org.locationtech.jts.geom.Coordinate> getCoordinates(String polygon) {
        try {
            return Arrays.stream(WktUtil.wktReader.read(polygon).getCoordinates()).map(e -> new org.locationtech.jts.geom.Coordinate(e.x, e.y, e.z)).collect(Collectors.toList());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 将WKT文本字符串转换为ES中的GeoShape字符串格式
     *
     * @param wkt
     * @return
     * @throws ParseException
     */
    public static String getESGeoTextFromWkt(String wkt) throws ParseException {
        String result = null;
        Geometry geom = wktReader.read(wkt);
        String type = geom.getClass().getSimpleName().toLowerCase();
        Point point;
        MultiPoint multiPoint;
        LineString lineString;
        MultiLineString multiLineString;
        Polygon polygon;
        MultiPolygon multiPolygon;
        if ("point".equals(type)) {
            point = (Point) geom;
            result = getESPointText(point);
        } else if ("linestring".equals(type)) {
            lineString = (LineString) geom;
            result = getESLineStringText(lineString);
        } else if ("polygon".equals(type)) {
            polygon = (Polygon) geom;
            result = getESPolygonText(polygon);
        } else if ("multipoint".equals(type)) {
            multiPoint = (MultiPoint) geom;
            result = getESMultiPointText(multiPoint);
        } else if ("multilinestring".equals(type)) {
            multiLineString = (MultiLineString) geom;
            result = getESMultiLineStringText(multiLineString);
        } else if ("multipolygon".equals(type)) {
            multiPolygon = (MultiPolygon) geom;
            result = getESMultiPolygonText(multiPolygon);
        }
        return result;
    }

    /**
     * 通过MultiPolygon对象拼接中括号表示的字符串
     *
     * @param multiPolygon
     * @return
     */
    private static String getESMultiPolygonText(MultiPolygon multiPolygon) throws ParseException {
        Polygon polygon;
        int num = multiPolygon.getNumGeometries();
        StringBuffer sb = new StringBuffer("[");
        for (int i = 0; i < num; i++) {
            polygon = (Polygon) multiPolygon.getGeometryN(i);
            sb.append(getESPolygonText(polygon) + ",");
        }
        sb.setCharAt(sb.length() - 1, ']');
        return sb.toString();
    }

    /**
     * 通过MultiLineString对象拼接中括号表示的字符串
     *
     * @param multiLineString
     * @return
     */
    private static String getESMultiLineStringText(MultiLineString multiLineString) {
        LineString lineString;
        int num = multiLineString.getNumGeometries();
        StringBuffer sb = new StringBuffer("[");
        for (int i = 0; i < num; i++) {
            lineString = (LineString) multiLineString.getGeometryN(i);
            sb.append(getESLineStringText(lineString) + ",");
        }
        sb.setCharAt(sb.length() - 1, ']');
        return sb.toString();
    }

    /**
     * 通过MultiPoint对象拼接中括号表示的字符串
     *
     * @param multiPoint
     * @return
     */
    private static String getESMultiPointText(MultiPoint multiPoint) {
        Point point;
        int num = multiPoint.getNumGeometries();
        StringBuffer sb = new StringBuffer("[");
        for (int i = 0; i < num; i++) {
            point = (Point) multiPoint.getGeometryN(i);
            sb.append(getESPointText(point) + ",");
        }
        sb.setCharAt(sb.length() - 1, ']');
        return sb.toString();
    }

    /**
     * 通过Polygon对象拼接中括号表示的字符串
     *
     * @param polygon
     * @return
     * @throws ParseException
     */
    private static String getESPolygonText(Polygon polygon) {
        String result;
        /**
         * 内部闭合环形的数量
         */
        int num = polygon.getNumInteriorRing();
        if (num > 0) {
            StringBuffer sb = new StringBuffer("[" + getESLineStringText(polygon.getExteriorRing()) + ",");
            for (int i = 0; i < num; i++) {
                sb.append(getESLineStringText(polygon.getInteriorRingN(i)) + ",");
            }
            sb.setCharAt(sb.length() - 1, ']');
            result = sb.toString();
        } else {
            result = "[" + getESLineStringText(polygon.getExteriorRing()) + "]";
        }
        return result;
    }

    /**
     * 通过LineString对象拼接中括号表示的字符串
     *
     * @param lineString
     * @return
     */
    public static String getESLineStringText(LineString lineString) {
        Coordinate[] corrds = lineString.getCoordinates();
        StringBuffer sb = new StringBuffer("[");
        for (Coordinate corrd : corrds) {
            sb.append(getCoordinateText(corrd) + ",");
        }
        sb.setCharAt(sb.length() - 1, ']');
        return sb.toString();
    }

    /**
     * 通过Point对象拼接中括号表示的字符串
     *
     * @param point
     * @return
     */
    public static String getESPointText(Point point) {
        return getCoordinateText(point.getCoordinate());
    }

    /**
     * 通过Coordinate对象拼接中括号表示的字符串
     *
     * @param coord
     * @return
     */
    public static String getCoordinateText(Coordinate coord) {
        return "[" + coord.x + "," + coord.y + "]";
    }

}
