package com.bubua12.mcp.nacos.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.bubua12.mcp.nacos.service.NacosWebService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 *
 *
 * @author bubua12
 * @since 2025/9/16 16:15
 */
@RestController
public class NacosEPController {

    @Resource
    private NacosWebService nacosWebService;


    @RequestMapping("/detail/instance")
    public String getServiceInstance() {

        List<Instance> serviceInstances = nacosWebService.getServiceInstances("192.168.1.242:8848", "", "service-order");
        System.out.println(serviceInstances);

        return JSON.toJSONString(serviceInstances);
    }
}
