package com.example.hehe;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/hehe")
@Slf4j
//@SecurityResource(code = "refund", name = "退款单")
public class RefundApplyController {

    @GetMapping(value = "")
    @ResponseStatus(HttpStatus.OK)
    public String getRefund() {
        return "";
    }
}