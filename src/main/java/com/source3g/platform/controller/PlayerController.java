package com.source3g.platform.controller;

import com.source3g.platform.dto.Cao;
import com.source3g.platform.dto.ClientRes;
import com.source3g.platform.dto.GameMap;
import com.source3g.platform.dto.Shu;
import com.source3g.platform.service.CaoService;
import com.source3g.platform.service.ShuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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

    @Autowired private ShuService shuService;

    @PostMapping(path = "/start")
    public void start(@RequestBody GameMap gameMap){
        System.out.println("----------");
        gameMap.getMap().forEach(System.out::println);
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
        System.out.println("---------- shu Move");
        gameMap.getMap().forEach(System.out::println);
        return shuService.move(gameMap);
    }
}
