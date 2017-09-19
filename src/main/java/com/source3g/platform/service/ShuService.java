package com.source3g.platform.service;

import com.source3g.platform.contants.Direct;
import com.source3g.platform.contants.MapType;
import com.source3g.platform.dto.*;
import com.source3g.platform.utils.GameMapUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Random;

/**
 * Created by alenfive on 17-9-17.
 */
@Service
@Slf4j
public class ShuService {

    @Autowired private Shu shu;

    public ClientRes move(GameMap gameMap) {
        shu.setGameMap(gameMap);
        shu.setClientRes(new ClientRes());

        shu.getClientRes().setShu1(Direct.STAY);
        shu.getClientRes().setShu2(Direct.STAY);
        shu.getClientRes().setShu3(Direct.STAY);
        shu.getClientRes().setShu4(Direct.STAY);

        //查询蜀，曹 位置
        PosInfo[][] mapArr = GameMapUtils.listToArray(shu.getGameMap().getMap());
        Position shu1 = GameMapUtils.findPosInfoByType(mapArr, MapType.SHU1);
        Position shu2 = GameMapUtils.findPosInfoByType(mapArr, MapType.SHU2);
        Position shu3 = GameMapUtils.findPosInfoByType(mapArr, MapType.SHU3);
        Position shu4 = GameMapUtils.findPosInfoByType(mapArr, MapType.SHU4);

        Position cao = GameMapUtils.findPosInfoByType(mapArr, MapType.CAO);
        shu.setShu1(shu1);
        shu.setShu2(shu2);
        shu.setShu3(shu3);
        shu.setShu4(shu4);
        shu.setCao(cao);
        //发现
        //跟踪
        //包围

        //发现曹狗
        if(shu.getCao() != null){

            //能抓，抓
            if(isGrab()){
                return shu.getClientRes();
            }else{//不能抓，追

                //return shu.getClientRes();
            }
        }

        //没发现，找
        shu.getClientRes().setShu1(Direct.values()[new Random().nextInt(4)]);
        shu.getClientRes().setShu2(Direct.values()[new Random().nextInt(4)]);
        shu.getClientRes().setShu3(Direct.values()[new Random().nextInt(4)]);
        shu.getClientRes().setShu4(Direct.values()[new Random().nextInt(4)]);

        return shu.getClientRes();
    }

    private boolean isGrab() {
        //可视范围内有

        Direct direct = getGradDirect(shu.getShu1(),shu.getCao());
        if((direct = getGradDirect(shu.getShu1(),shu.getCao()))!= null){
            shu.getClientRes().setShu1(direct);
            return true;
        }else if((direct = getGradDirect(shu.getShu2(),shu.getCao()))!= null){
            shu.getClientRes().setShu2(direct);
            return true;
        }else if((direct = getGradDirect(shu.getShu3(),shu.getCao()))!= null){
            shu.getClientRes().setShu3(direct);
            return true;
        }else if((direct = getGradDirect(shu.getShu4(),shu.getCao()))!= null){
            shu.getClientRes().setShu4(direct);
            return true;
        }

        return false;
    }

    private Direct getGradDirect(Position position, Position cao) {

        if(position.getRowIndex()-1 == cao.getRowIndex() && position.getColIndex() == cao.getColIndex()){//上
            return Direct.UP;
        }else if(position.getRowIndex() == cao.getRowIndex() && position.getColIndex()+1 == cao.getColIndex()){//右
            return Direct.RIGHT;
        }else if(position.getRowIndex()+1 == cao.getRowIndex() && position.getColIndex() == cao.getColIndex()){//下
            return Direct.DOWN;
        }else if(position.getRowIndex() == cao.getRowIndex() && position.getColIndex()-1 == cao.getColIndex()){//左
            return Direct.LEFT;
        }
        return null;
    }

}
