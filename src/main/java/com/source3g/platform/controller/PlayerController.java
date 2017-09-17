package com.source3g.platform.controller;

import com.source3g.platform.contants.Direct;
import com.source3g.platform.dto.Cao;
import com.source3g.platform.dto.ClientRes;
import com.source3g.platform.dto.GameMap;
import com.source3g.platform.dto.Shu;
import com.source3g.platform.service.CaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Random;

/**
 * Created by huhuaiyong on 2017/9/11.
 */
@RestController
@RequestMapping(path = "/player")
public class PlayerController {

    @Autowired
    private Cao cao;

    @Autowired private Shu shu;

    @Autowired
    private CaoService caoService;

    @PostMapping(path = "/start")
    public void start(@RequestBody GameMap gameMap){
        System.out.println("----------");
        gameMap.getMap().forEach(System.out::println);

        cao.reset();
        shu.reset();
    }

    @GetMapping(path = "/stop")
    public void stop(){

    }

    @PostMapping(path = "/caoMove")
    public ClientRes caoMove(@RequestBody GameMap gameMap){
        System.out.println("---------- cao Move");
        gameMap.getMap().forEach(System.out::println);


        return caoService.move(gameMap);
    }


    @PostMapping(path = "/shuMove")
    public ClientRes shuMove(@RequestBody GameMap gameMap){
        ClientRes clientRes = new ClientRes();
        clientRes.setShu1(Direct.values()[new Random().nextInt(4)]);
        clientRes.setShu2(Direct.values()[new Random().nextInt(4)]);
        clientRes.setShu3(Direct.values()[new Random().nextInt(4)]);
        clientRes.setShu4(Direct.values()[new Random().nextInt(4)]);
        return clientRes;
    }
}
