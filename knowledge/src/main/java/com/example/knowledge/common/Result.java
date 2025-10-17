package com.example.knowledge.common;

import lombok.Data;

/**
 * 通用响应结果工具类
 */
@Data
public class Result<T> {
    private int code;
    private String msg;
    private T data;

    // 成功响应（带数据）
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMsg("操作成功");
        result.setData(data);
        return result;
    }

    // 成功响应（无数据）
    public static <T> Result<T> success() {
        return success(null);
    }

    // 错误响应（自定义状态码和消息）
    public static <T> Result<T> error(int code, String msg) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMsg(msg);
        result.setData(null);
        return result;
    }

    // 错误响应（默认500状态码）
    public static <T> Result<T> error(String msg) {
        return error(500, msg);
    }
}