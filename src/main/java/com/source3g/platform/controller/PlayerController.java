package com.source3g.platform.controller;

import com.source3g.platform.contants.MapType;
import com.source3g.platform.dto.*;
import com.source3g.platform.service.CaoService;
import com.source3g.platform.service.ShuService;
import com.source3g.platform.utils.AStar;
import com.source3g.platform.utils.GameMapUtils;
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

        PosInfo[][] mapArr = GameMapUtils.listToArray(gameMap.getMap());
        if(GameMapUtils.findPosInfoByType(mapArr, MapType.SHU1) != null){
            shu.init(gameMap.getColLen(),gameMap.getRowLen());
        }
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
        if(shu.getRefreshLocal() == null){
            shu.init(gameMap.getColLen(),gameMap.getRowLen());
        }
        return shuService.move(gameMap);
    }
}
