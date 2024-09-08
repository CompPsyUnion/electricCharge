package cn.yiming1234.electriccharge.controller;

import cn.yiming1234.electriccharge.service.ElectricService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class ElectricController {

    @Autowired
    private ElectricService electricService;

    /**
     * 获取电费balance
     * @return
     * @throws Exception
     */
    @PostMapping("/getElectricCharge")
    public String getElectricCharge() throws Exception {
        return electricService.getElectricCharge();
    }
}