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
            clearShuNext();
            if(ableEat(mapArr)){
                return shu.getClientRes();
            }
        }else if(isChase(mapArr)){ //是否正在跟踪，报告曹的位置
            doChase(mapArr);
        }

        if(shu.getCao() != null){

            //对非跟踪者，获取路线
            shu.getShus().stream().filter(item->item.getNextDirect() == null).forEach(item->{
                Position tar = null;
                switch (item.getId()){
                    case SHU1:tar = new Position(shu.getCao().getX()-1,shu.getCao().getY());break;
                    case SHU2:tar = new Position(shu.getCao().getX(),shu.getCao().getY()+1);break;
                    case SHU3:tar = new Position(shu.getCao().getX()+1,shu.getCao().getY());break;
                    case SHU4:tar = new Position(shu.getCao().getX(),shu.getCao().getY()-1);break;
                }
                if(tar != null){
                    Position pos = new AStar(mapArr).findPath(tar,item.getSelfPosition());
                    if(pos == null)return;
                    List<Direct> directs =  getGradDirect(mapArr,item.getSelfPosition(),pos.getParent());
                    setReturnShuDirect(item.getId(),directs.get(0));
                }

            });
        }

        //没发现曹，也没处于跟踪状态,地图上到处跑

        //去走那些没走过的路
        //涂黑走过的路
        flagLocalPosition(1);
        printlnLocal();
        //上一半地图,主shu1,shu2
        int xTop = shu.getGameMap().getColLen()/2-1;
        int xDown = xTop+1;
        Position map1Start = new Position(0,0);
        Position map1End = new Position(xTop,shu.getGameMap().getRowLen()-1);
        Position map2Start = new Position(xDown,0);
        Position map2End = new Position(shu.getGameMap().getColLen()-1,shu.getGameMap().getRowLen()-1);

        Position shu1 = randomReginPoint(MapType.SHU1,map1Start,map1End);
        Position shu2 = randomReginPoint(MapType.SHU2,map1Start,map1End);
        //下一斗地图，主shu3,shu4
        Position shu3 = randomReginPoint(MapType.SHU3,map2Start,map2End);
        Position shu4 = randomReginPoint(MapType.SHU4,map2Start,map2End);



        shu.getShus().forEach(item->{
            Position currShu = null;
            switch (item.getId()){
                case SHU1:currShu = shu1;break;
                case SHU2:currShu = shu2;break;
                case SHU3:currShu = shu3;break;
                case SHU4:currShu = shu4;break;
            }
            Position pos = new AStar(mapArr).findPath(currShu,item.getSelfPosition());
            if(pos == null){
                setAbleReginFlag(currShu.getX(),currShu.getY(),1);
                return;
            }
            List<Direct> directs =  getGradDirect(mapArr,item.getSelfPosition(),pos.getParent());
            setAbleReturnShuDirect(item.getId(),directs.get(0));
        });


        //没发现，找
        setAbleReturnShuDirect(MapType.SHU1,Direct.values()[new Random().nextInt(4)]);
        setAbleReturnShuDirect(MapType.SHU2,Direct.values()[new Random().nextInt(4)]);
        setAbleReturnShuDirect(MapType.SHU3,Direct.values()[new Random().nextInt(4)]);
        setAbleReturnShuDirect(MapType.SHU4,Direct.values()[new Random().nextInt(4)]);


        return shu.getClientRes();
    }

    private void printlnLocal() {
        for(int i = 0;i<shu.getRefreshLocal().length;i++){
            for(int k=0;k<shu.getRefreshLocal()[i].length;k++){
                System.out.print(shu.getRefreshLocal()[i][k]);
                System.out.print(",");
            }
            System.out.println();
        }
    }

    private void clearShuNext() {
        shu.getShus().forEach(item->{
            item.setNextDirect(null);
            item.setCaoPosition(null);
        });
    }

    private void flagLocalPosition(int flag) {
        Position pos = null;
        for(ShuInfo item : shu.getShus()){
            pos = item.getSelfPosition();

            setAbleReginFlag(pos.getX(),pos.getY(),flag);
            setAbleReginFlag(pos.getX()-1,pos.getY()-1,flag);
            setAbleReginFlag(pos.getX()-1,pos.getY(),flag);
            setAbleReginFlag(pos.getX()-1,pos.getY()+1,flag);
            setAbleReginFlag(pos.getX(),pos.getY()+1,flag);
            setAbleReginFlag(pos.getX()+1,pos.getY()+1,flag);
            setAbleReginFlag(pos.getX()+1,pos.getY(),flag);
            setAbleReginFlag(pos.getX()+1,pos.getY()-1,flag);
            setAbleReginFlag(pos.getX(),pos.getY()-1,flag);

        }
    }

    private void setAbleReginFlag(int x,int y,int flag){
        if(x>=0 && x<shu.getGameMap().getColLen() && y>=0 && y<shu.getGameMap().getRowLen()){
            shu.getRefreshLocal()[x][y] = flag;
        }
    }

    private Position randomReginPoint(MapType mapType,Position startPos,Position endPos) {


        for(ShuInfo item : shu.getShus()){
            if(item.getId().equals(mapType)){
                if(item.getTarget() != null){
                    if(shu.getRefreshLocal()[item.getTarget().getX()][item.getTarget().getY()] == 0 ){
                        return item.getTarget();
                    }
                }
                int count = getZeroCount(startPos,endPos);
                if(count == 0){
                    clearRefreshLocal(startPos,endPos);
                    count = getZeroCount(startPos,endPos);
                }
                Position target = getRandomPosition(new Random().nextInt(count),startPos,endPos);
                item.setTarget(target);
                return target;
            }


        }
        return null;
    }

    private Position getRandomPosition(int index,Position startPos,Position endPos) {
        int currIndex = -1;
        for(int i = 0;i<shu.getRefreshLocal().length;i++){
            for(int k=0;k<shu.getRefreshLocal()[i].length;k++){
                if(i>=startPos.getX() && i<=endPos.getX() && k>=startPos.getY() && k<=endPos.getY() && shu.getRefreshLocal()[i][k] == 0){

                    currIndex ++;
                    if(currIndex == index){
                        return new Position(i,k);
                    }
                }
            }
        }
        return null;
    }

    private void clearRefreshLocal(Position startPos,Position endPos) {
        for(int i = 0;i<shu.getRefreshLocal().length;i++){
            for(int k=0;k<shu.getRefreshLocal()[i].length;k++){
                if(i>=startPos.getX() && i<=endPos.getX() && k>=startPos.getY() && k<=endPos.getY()){
                    shu.getRefreshLocal()[i][k] = 0;
                }
            }
        }
    }

    private int getZeroCount(Position startPos,Position endPos) {
        int count = 0;
        for(int i = 0;i<shu.getRefreshLocal().length;i++){
            for(int k=0;k<shu.getRefreshLocal()[i].length;k++){

                if(i>=startPos.getX() && i<=endPos.getX() && k>=startPos.getY() && k<=endPos.getY()) {
                    if (shu.getRefreshLocal()[i][k] == 0) {
                        count++;
                    }
                }
            }
        }
        return count;
    }

    private void doChase(PosInfo[][] mapArr) {
        shu.getShus().forEach(item->{
            if(item.getNextDirect() != null){
                setReturnShuDirect(item.getId(),item.getNextDirect());

                if(Direct.UP.equals(item.getNextDirect())){
                    item.getCaoPosition().setX(item.getCaoPosition().getX()-1);
                    if(item.getCaoPosition().getX()<0){
                        item.setNextDirect(null);
                        item.setCaoPosition(null);
                    }
                }else if(Direct.RIGHT.equals(item.getNextDirect())){
                    item.getCaoPosition().setY(item.getCaoPosition().getY()+1);
                    if(item.getCaoPosition().getY()>=shu.getGameMap().getRowLen()){
                        item.setNextDirect(null);
                        item.setCaoPosition(null);
                    }
                }else if(Direct.DOWN.equals(item.getNextDirect())){
                    item.getCaoPosition().setX(item.getCaoPosition().getX()+1);
                    if(item.getCaoPosition().getX() >= shu.getGameMap().getColLen()){
                        item.setNextDirect(null);
                        item.setCaoPosition(null);
                    }
                }else if(Direct.LEFT.equals(item.getNextDirect())){
                    item.getCaoPosition().setY(item.getCaoPosition().getY()-1);
                    if(item.getCaoPosition().getY()<0){
                        item.setNextDirect(null);
                        item.setCaoPosition(null);
                    }
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
        boolean isRealdy = true;
        for(ShuInfo item : shu.getShus()){
            ArrayList<Direct> directs = getGradDirect(mapArr,item.getSelfPosition(),shu.getCao());
            if(directs != null && directs.size() == 1){
                setReturnShuDirect(item.getId(),directs.get(0));
                return true;
            }else if(directs != null && directs.size() == 2){
                setReturnShuDirect(item.getId(),directs.get(0));
                if(isRealdy){
                    item.setNextDirect(directs.get(1));
                    item.setCaoPosition(shu.getCao());
                    isRealdy = false;
                }

            }
        }
        return false;
    }

    private void setAbleReturnShuDirect(MapType mapType,Direct direct){

        switch (mapType){
            case SHU1:if(shu.getClientRes().getShu1().equals(Direct.STAY))shu.getClientRes().setShu1(direct);break;
            case SHU2:if(shu.getClientRes().getShu2().equals(Direct.STAY))shu.getClientRes().setShu2(direct);break;
            case SHU3:if(shu.getClientRes().getShu3().equals(Direct.STAY))shu.getClientRes().setShu3(direct);break;
            case SHU4:if(shu.getClientRes().getShu4().equals(Direct.STAY))shu.getClientRes().setShu4(direct);break;
        }
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
