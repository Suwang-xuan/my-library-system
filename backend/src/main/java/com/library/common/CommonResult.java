package com.library.common;

import lombok.Data;
import java.io.Serializable;

@Data
public class CommonResult<T> implements Serializable {

    private Integer code;
    private String message;
    private T data;

    public static <T> CommonResult<T> success() {
        return success(null);
    }

    public static <T> CommonResult<T> success(T data) {
        CommonResult<T> result = new CommonResult<>();
        result.setCode(200);
        result.setMessage("操作成功");
        result.setData(data);
        return result;
    }

    public static <T> CommonResult<T> success(String message, T data) {
        CommonResult<T> result = new CommonResult<>();
        result.setCode(200);
        result.setMessage(message);
        result.setData(data);
        return result;
    }

    public static <T> CommonResult<T> error() {
        return error("操作失败");
    }

    public static <T> CommonResult<T> error(String message) {
        CommonResult<T> result = new CommonResult<>();
        result.setCode(500);
        result.setMessage(message);
        return result;
    }

    public static <T> CommonResult<T> error(Integer code, String message) {
        CommonResult<T> result = new CommonResult<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }
    
    // 添加failed方法，调用现有的error方法，解决编译错误
    public static <T> CommonResult<T> failed(String message) {
        return error(message);
    }
    
    public static <T> CommonResult<T> failed(Integer code, String message) {
        return error(code, message);
    }
    
    public static <T> CommonResult<T> failed() {
        return error();
    }

}
