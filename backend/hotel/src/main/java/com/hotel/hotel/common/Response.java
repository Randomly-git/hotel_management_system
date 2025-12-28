package com.hotel.hotel.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 统一响应格式
 * @param <T> 响应数据类型
 */
@Data
@Schema(description = "统一响应格式")
public class Response<T> {

    @Schema(description = "响应码", example = "200")
    private Integer code;

    @Schema(description = "响应消息", example = "操作成功")
    private String message;

    @Schema(description = "响应数据")
    private T data;

    @Schema(description = "时间戳", example = "1703001234567")
    private Long timestamp;

    public Response() {
        this.timestamp = System.currentTimeMillis();
    }

    public Response(Integer code, String message) {
        this.code = code;
        this.message = message;
        this.timestamp = System.currentTimeMillis();
    }

    public Response(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * 成功响应
     */
    public static <T> Response<T> success() {
        return new Response<>(200, "操作成功");
    }

    /**
     * 成功响应（带数据）
     */
    public static <T> Response<T> success(T data) {
        return new Response<>(200, "操作成功", data);
    }

    /**
     * 成功响应（自定义消息）
     */
    public static <T> Response<T> success(String message, T data) {
        return new Response<>(200, message, data);
    }

    /**
     * 失败响应
     */
    public static <T> Response<T> error(Integer code, String message) {
        return new Response<>(code, message);
    }

    /**
     * 失败响应（默认500）
     */
    public static <T> Response<T> error(String message) {
        return new Response<>(500, message);
    }

    /**
     * 参数错误（400）
     */
    public static <T> Response<T> badRequest(String message) {
        return new Response<>(400, message);
    }

    /**
     * 未授权（401）
     */
    public static <T> Response<T> unauthorized(String message) {
        return new Response<>(401, message);
    }

    /**
     * 禁止访问（403）
     */
    public static <T> Response<T> forbidden(String message) {
        return new Response<>(403, message);
    }

    /**
     * 资源未找到（404）
     */
    public static <T> Response<T> notFound(String message) {
        return new Response<>(404, message);
    }
}