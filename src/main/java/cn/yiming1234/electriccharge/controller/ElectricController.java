package cn.yiming1234.electriccharge.controller;

import cn.yiming1234.electriccharge.service.ElectricService;
import cn.yiming1234.electriccharge.service.WeixinService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@Slf4j
public class ElectricController {

    @Autowired
    private ElectricService electricService;

    /**
     * 获取电费balance(自用)
     */
    @PostMapping("/getElectricCharge")
    public String getElectricCharge() throws Exception {
        return electricService.getElectricCharge();
    }
}