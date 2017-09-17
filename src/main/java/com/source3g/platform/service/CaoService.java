package com.source3g.platform.service;

import com.source3g.platform.contants.Direct;
import com.source3g.platform.dto.*;
import com.source3g.platform.utils.GameMapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Created by alenfive on 17-9-17.
 */
@Service
public class CaoService {

    @Autowired private Cao cao;

    public ClientRes move(GameMap gameMap) {

        cao.setGameMap(gameMap);

        PosInfo[][] mapArr = GameMapUtils.listToArray(cao.getGameMap().getMap());

        Position position = findCaoPosInfo(mapArr);


        //获取哪个方向可走
        ArrayList<Direct> ableDirect = new ArrayList<>(Arrays.asList(Direct.values()));
        ableDirect.remove(Direct.STAY);
        //干掉绝不能走的路线
        removeAbs(ableDirect,mapArr,position);


        //跑边界不足2公里

        ClientRes clientRes = new ClientRes();
        //如果无路可走，原地等死吧
        if(ableDirect.isEmpty())ableDirect.add(Direct.STAY);
        clientRes.setCao(ableDirect.get(new Random().nextInt(ableDirect.size()-1)));
        return clientRes;
    }

    private void removeAbs(List<Direct> ableDirect, PosInfo[][] mapArr, Position position) {
        PosInfo posInfo = null;
        //上右下左，四个方向是否越界


        if(position.getRowIndex()-1<0){
            ableDirect.remove(Direct.DOWN);
        }else{
            posInfo = mapArr[position.getRowIndex()-1][position.getColIndex()];
            if(posInfo.isBlock() || hasShuByPosInfo(posInfo)){
                ableDirect.remove(Direct.DOWN);
            }
        }

        if(position.getColIndex()+1>=cao.getGameMap().getColLen()){
            ableDirect.remove(Direct.RIGHT);
        }else {
            posInfo = mapArr[position.getRowIndex()][position.getColIndex()+1];
            if(posInfo.isBlock() || hasShuByPosInfo(posInfo)){
                ableDirect.remove(Direct.RIGHT);
            }
        }

        if(position.getRowIndex()+1>=cao.getGameMap().getRowLen()){
            ableDirect.remove(Direct.UP);
        }else {
            posInfo = mapArr[position.getRowIndex()+1][position.getColIndex()];
            if(posInfo.isBlock() || hasShuByPosInfo(posInfo)){
                ableDirect.remove(Direct.UP);
            }
        }

        if(position.getColIndex()-1<0){
            ableDirect.remove(Direct.LEFT);
        }else{
            posInfo = mapArr[position.getRowIndex()][position.getColIndex()-1];
            if(posInfo.isBlock() || hasShuByPosInfo(posInfo)){
                ableDirect.remove(Direct.LEFT);
            }
        }

        //左上，存在
        if(position.getRowIndex()-1>=0 && position.getColIndex()-1>=0){
            posInfo = mapArr[position.getRowIndex()-1][position.getColIndex()-1];
            if(hasShuByPosInfo(posInfo)){
                ableDirect.remove(Direct.LEFT);
                ableDirect.remove(Direct.DOWN);
            }
        }

        //右上，存在
        if(position.getRowIndex()-1>=0 && position.getColIndex()+1<cao.getGameMap().getColLen()){
            posInfo = mapArr[position.getRowIndex()-1][position.getColIndex()+1];
            if (hasShuByPosInfo(posInfo)){
                ableDirect.remove(Direct.DOWN);
                ableDirect.remove(Direct.RIGHT);
            }
        }

        //右下，存在
        if(position.getRowIndex()+1<cao.getGameMap().getRowLen() && position.getColIndex()+1<cao.getGameMap().getColLen()){
            posInfo = mapArr[position.getRowIndex()+1][position.getColIndex()+1];
            if (hasShuByPosInfo(posInfo)){
                ableDirect.remove(Direct.UP);
                ableDirect.remove(Direct.RIGHT);
            }
        }

        //左下，存在
        if(position.getRowIndex()+1<cao.getGameMap().getRowLen() && position.getColIndex()-1>=0){
            posInfo = mapArr[position.getRowIndex()+1][position.getColIndex()-1];
            if (hasShuByPosInfo(posInfo)){
                ableDirect.remove(Direct.UP);
                ableDirect.remove(Direct.LEFT);
            }
        }
    }


    private boolean hasShuByPosInfo(PosInfo p11) {
        return p11.isShu1() || p11.isShu2() || p11.isShu3() || p11.isShu4();
    }



    private Position findCaoPosInfo(PosInfo[][] mapArr) {
        for(int i=0;i<mapArr.length;i++){
            for (int k=0;k<mapArr[i].length;k++){
                if(mapArr[i][k].isCao()){
                    return new Position(i,k);
                }
            }
        }
        return null;
    }

}
