package com.source3g.platform.service;

import com.source3g.platform.contants.Direct;
import com.source3g.platform.contants.MapType;
import com.source3g.platform.dto.*;
import com.source3g.platform.utils.AStar;
import com.source3g.platform.utils.GameMapUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
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

        if(shu.getShus() == null){
            shu.setShus(new ArrayList<>());
            shu.getShus().add(new ShuInfo(MapType.SHU1));
            shu.getShus().add(new ShuInfo(MapType.SHU2));
            shu.getShus().add(new ShuInfo(MapType.SHU3));
            shu.getShus().add(new ShuInfo(MapType.SHU4));
        }

        //默认返回
        setReturnShuDirect(MapType.SHU1,Direct.STAY);
        setReturnShuDirect(MapType.SHU2,Direct.STAY);
        setReturnShuDirect(MapType.SHU3,Direct.STAY);
        setReturnShuDirect(MapType.SHU4,Direct.STAY);

        //查询当前蜀，曹 位置
        PosInfo[][] mapArr = GameMapUtils.listToArray(shu.getGameMap().getMap());
        Position cao = GameMapUtils.findPosInfoByType(mapArr, MapType.CAO);

        //保存在全局位置
        shu.getShus().forEach(item ->{
            item.setSelfPosition(GameMapUtils.findPosInfoByType(mapArr,item.getId()));
        });
        shu.setCao(cao);


        //发现曹狗,说明在视野范围内
        if(shu.getCao() != null){
            if(ableEat(mapArr)){
                return shu.getClientRes();
            }
        }else if(isChase(mapArr)){ //是否正在跟踪，报告曹的位置
            doChase(mapArr);
        }

        if(shu.getCao() != null){

            //对非跟踪者，获取路线
            shu.getShus().stream().filter(item->item.getNextDirect() == null).forEach(item->{
                Position pos = new AStar(mapArr).findPath(shu.getCao(),item.getSelfPosition());
                List<Direct> directs =  getGradDirect(mapArr,item.getSelfPosition(),pos.getParent());
                setReturnShuDirect(item.getId(),directs.get(0));
            });
        }

        //没发现曹，也没处于跟踪状态,地图上到处跑

        //去走那些没走过的路

        //没发现，找
        if(shu.getClientRes().getShu1().equals(Direct.STAY)){
            shu.getClientRes().setShu1(Direct.values()[new Random().nextInt(4)]);
        }
        if(shu.getClientRes().getShu2().equals(Direct.STAY)){
            shu.getClientRes().setShu2(Direct.values()[new Random().nextInt(4)]);
        }
        if(shu.getClientRes().getShu3().equals(Direct.STAY)){
            shu.getClientRes().setShu3(Direct.values()[new Random().nextInt(4)]);
        }
        if(shu.getClientRes().getShu4().equals(Direct.STAY)){
            shu.getClientRes().setShu4(Direct.values()[new Random().nextInt(4)]);
        }


        return shu.getClientRes();
    }

    private void doChase(PosInfo[][] mapArr) {
        shu.getShus().forEach(item->{
            if(item.getNextDirect() != null){
                setReturnShuDirect(item.getId(),item.getNextDirect());

                switch (item.getNextDirect()){
                    case UP:item.getCaoPosition().setX(item.getCaoPosition().getX()-1);break;
                    case RIGHT:item.getCaoPosition().setY(item.getCaoPosition().getY()+1);break;
                    case DOWN:item.getCaoPosition().setX(item.getCaoPosition().getX()+1);break;
                    case LEFT:item.getCaoPosition().setY(item.getCaoPosition().getY()-1);break;
                }
                shu.setCao(item.getCaoPosition());
            }
        });
    }

    private boolean isChase(PosInfo[][] mapArr) {
        for(ShuInfo item : shu.getShus()){
            if(item.getCaoPosition() != null && item.getNextDirect() != null){
                return true;
            }
        }
        return false;
    }

    private boolean ableEat(PosInfo[][] mapArr) {

        for(ShuInfo item : shu.getShus()){
            ArrayList<Direct> directs = getGradDirect(mapArr,item.getSelfPosition(),shu.getCao());
            if(directs != null && directs.size() == 1){
                setReturnShuDirect(item.getId(),directs.get(0));
                return true;
            }else if(directs != null && directs.size() == 2){
                setReturnShuDirect(item.getId(),directs.get(0));
                item.setNextDirect(directs.get(1));
                item.setCaoPosition(shu.getCao());
            }
        }
        return false;
    }


    private void setReturnShuDirect(MapType mapType,Direct direct){
        switch (mapType){
            case SHU1:shu.getClientRes().setShu1(direct);break;
            case SHU2:shu.getClientRes().setShu2(direct);break;
            case SHU3:shu.getClientRes().setShu3(direct);break;
            case SHU4:shu.getClientRes().setShu4(direct);break;
        }
    }

    //如果发现了曹，检查它的位置，并给出可通行的路线
    private ArrayList<Direct> getGradDirect(PosInfo[][] mapArr,Position position, Position cao) {
        ArrayList<Direct> directs = new ArrayList<>();

        if(position.getX()-1 == cao.getX() && position.getY() == cao.getY()){//上
            directs.add(Direct.UP);
        }else if(position.getX() == cao.getX() && position.getY()+1 == cao.getY()){//右
            directs.add(Direct.RIGHT);
        }else if(position.getX()+1 == cao.getX() && position.getY() == cao.getY()){//下
            directs.add(Direct.DOWN);
        }else if(position.getX() == cao.getX() && position.getY()-1 == cao.getY()){//左
            directs.add(Direct.LEFT);
        }else if(position.getX()-1 == cao.getX() && position.getY()-1 == cao.getY()){//左上

            if(!(mapArr[position.getX()][position.getY()-1]).isBlock()){  //尝试先向左
                directs.add(Direct.LEFT);
                directs.add(Direct.UP);
            }else if(!(mapArr[position.getX()-1][position.getY()]).isBlock()) { //尝试先向上
                directs.add(Direct.UP);
                directs.add(Direct.LEFT);
            }


        }else if(position.getX()-1 == cao.getX() && position.getY()+1 == cao.getY()) {//右上
            if(!(mapArr[position.getX()][position.getY()+1]).isBlock()){  //尝试先向右
                directs.add(Direct.RIGHT);
                directs.add(Direct.UP);
            }else if(!(mapArr[position.getX()-1][position.getY()]).isBlock()) { //尝试先向上
                directs.add(Direct.UP);
                directs.add(Direct.RIGHT);
            }


        }else if(position.getX()+1 == cao.getX() && position.getY()+1 == cao.getY()) {//右下
            if(!(mapArr[position.getX()][position.getY()+1]).isBlock()){  //尝试先向右
                directs.add(Direct.RIGHT);
                directs.add(Direct.DOWN);
            }else if(!(mapArr[position.getX()+1][position.getY()]).isBlock()) { //尝试先向上
                directs.add(Direct.DOWN);
                directs.add(Direct.RIGHT);
            }

        }else if(position.getX()+1 == cao.getX() && position.getY()-1 == cao.getY()) {//左下
            if(!(mapArr[position.getX()][position.getY()-1]).isBlock()){  //尝试先向左
                directs.add(Direct.LEFT);
                directs.add(Direct.DOWN);
            }else if(!(mapArr[position.getX()+1][position.getY()]).isBlock()) { //尝试先向下
                directs.add(Direct.DOWN);
                directs.add(Direct.LEFT);
            }

        }
        return directs;
    }

}
