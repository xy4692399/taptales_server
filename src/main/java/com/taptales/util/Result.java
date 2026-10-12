package com.taptales.util;

import lombok.Data;

@Data
public class Result {
    private int code;
    private String msg;
    private Object data;

    public static Result ok() {
        Result r = new Result();
        r.setCode(200);
        r.setMsg("success");
        return r;
    }

    public static Result ok(Object data) {
        Result r = ok();
        r.setData(data);
        return r;
    }

    public static Result ok(String msg, Object data) {
        Result r = ok(data);
        r.setMsg(msg);
        return r;
    }

    public static Result fail(String msg) {
        Result r = new Result();
        r.setCode(400);
        r.setMsg(msg);
        return r;
    }

    public static Result fail(int code, String msg) {
        Result r = new Result();
        r.setCode(code);
        r.setMsg(msg);
        return r;
    }
}
