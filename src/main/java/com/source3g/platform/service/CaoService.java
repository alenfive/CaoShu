package com.source3g.platform.service;

import com.source3g.platform.contants.Direct;
import com.source3g.platform.contants.MapType;
import com.source3g.platform.dto.*;
import com.source3g.platform.utils.GameMapUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by alenfive on 17-9-17.
 */
@Service
@Slf4j
public class CaoService {

    @Autowired private Cao cao;

    public ClientRes move(GameMap gameMap) {

        cao.setGameMap(gameMap);

        PosInfo[][] mapArr = GameMapUtils.listToArray(cao.getGameMap().getMap());

        Position position = GameMapUtils.findPosInfoByType(mapArr, MapType.CAO);


        //获取哪个方向可走
        ArrayList<Direct> ableDirect = new ArrayList<>(Arrays.asList(Direct.values()));
        ableDirect.remove(Direct.STAY);
        //干掉绝不能走的路线
        Set<Direct> diedDire = getDiedDire(mapArr,position);

        ableDirect.removeAll(diedDire);

        List<WeightDirect> weightDirects = ableDirect.stream().map(item->{return new WeightDirect(item,10);}).collect(Collectors.toList());


        if(!ableDirect.isEmpty()){

            //权重从低到高，不允许覆盖
            //警告线
            getWarningDire(weightDirects,mapArr,position,6);

            //斜边的警察
            getWarningDire2(weightDirects,mapArr,position,7);

            //距边界二公里以内
            getBorderDire(weightDirects,mapArr,position,8);

            //障碍物方向
            getBlockDir(weightDirects,mapArr,position,9);


            //找到对应权重的路线
            List<WeightDirect> weight10 = getWeight(weightDirects,10);
            List<WeightDirect> weight9 = getWeight(weightDirects,9);
            List<WeightDirect> weight8 = getWeight(weightDirects,8);
            List<WeightDirect> weight7 = getWeight(weightDirects,7);
            List<WeightDirect> weight6 = getWeight(weightDirects,6);



            //被挑剩下的路
            if(!weight10.isEmpty()){
                ClientRes clientRes = new ClientRes();
                clientRes.setCao(weight10.get(new Random().nextInt(weight10.size())).getDirect());
                log.info("value:{},weight:{}",weight10.toString(),10);
                return clientRes;
            }

            //边界红线内
            if(!weight9.isEmpty()){
                ClientRes clientRes = new ClientRes();
                clientRes.setCao(weight9.get(new Random().nextInt(weight9.size())).getDirect());
                log.info("value:{},weight:{}",weight9.toString(),9);
                return clientRes;
            }
            //障碍物方向
            if(!weight8.isEmpty()){
                ClientRes clientRes = new ClientRes();
                clientRes.setCao(weight8.get(new Random().nextInt(weight8.size())).getDirect());
                log.info("value:{},weight:{}",weight8.toString(),8);
                return clientRes;
            }

            //斜边的警察
            if(!weight7.isEmpty()){
                ClientRes clientRes = new ClientRes();
                clientRes.setCao(weight7.get(new Random().nextInt(weight7.size())).getDirect());
                log.info("value:{},weight:{}",weight7.toString(),7);
                return clientRes;
            }

            //直边的警察
            //如果到处都有警察，随便走吧，反正马上就死了
            if(!weight6.isEmpty()){
                ClientRes clientRes = new ClientRes();
                clientRes.setCao(weight6.get(new Random().nextInt(weight6.size())).getDirect());
                log.info("value:{},weight:{}",weight6.toString(),6);
                return clientRes;
            }

        }

        log.info(ableDirect.toString());

        ClientRes clientRes = new ClientRes();
        //如果无路可走，原地等死吧
        if(ableDirect.isEmpty())ableDirect.add(Direct.STAY);
        clientRes.setCao(ableDirect.get(new Random().nextInt(ableDirect.size())));
        return clientRes;
    }

    private void getWarningDire2(List<WeightDirect> weightDirects, PosInfo[][] mapArr, Position position, int weight) {
        PosInfo posInfo = null;
        //左上
        if(position.getX()-2>=0 && position.getY()-2>=0){
            posInfo = mapArr[position.getX()-2][position.getY()-2];
            if(hasShuByPosInfo(posInfo)){
                setWeightByDirect(weightDirects,Direct.LEFT,weight);
                setWeightByDirect(weightDirects,Direct.UP,weight);
            }
        }
        //右上
        if(position.getX()-2>=0 && position.getY()+2 < cao.getGameMap().getRowLen()){
            posInfo = mapArr[position.getX()-2][position.getY()+2];
            if(hasShuByPosInfo(posInfo)){
                setWeightByDirect(weightDirects,Direct.RIGHT,weight);
                setWeightByDirect(weightDirects,Direct.UP,weight);
            }
        }
        //右下
        if(position.getX()+2 < cao.getGameMap().getColLen() && position.getY()+2 < cao.getGameMap().getRowLen()){
            posInfo = mapArr[position.getX()+2][position.getY()+2];
            if(hasShuByPosInfo(posInfo)){
                setWeightByDirect(weightDirects,Direct.RIGHT,weight);
                setWeightByDirect(weightDirects,Direct.DOWN,weight);
            }
        }
        //左下

        if(position.getX()+2 < cao.getGameMap().getColLen() && position.getY()-2>=0){
            posInfo = mapArr[position.getX()+2][position.getY()-2];
            if(hasShuByPosInfo(posInfo)){
                setWeightByDirect(weightDirects,Direct.LEFT,weight);
                setWeightByDirect(weightDirects,Direct.DOWN,weight);
            }
        }
    }

    //空况的定义：在地图范围内边界减二
    private void getBorderDire(List<WeightDirect> weightDirects, PosInfo[][] mapArr, Position position, int weight) {
        //上
        if(position.getX()-2 <= 0){
            setWeightByDirect(weightDirects,Direct.UP,weight);
        }

        //右
        if(position.getY()+3 >= cao.getGameMap().getRowLen()){
            setWeightByDirect(weightDirects,Direct.RIGHT,weight);
        }

        //下
        if(position.getX()+3 >= cao.getGameMap().getColLen()){
            setWeightByDirect(weightDirects,Direct.DOWN,weight);
        }
        //左
        if(position.getY()-2 <= 0){
            setWeightByDirect(weightDirects,Direct.LEFT,weight);
        }
    }

    private List<WeightDirect> getWeight(List<WeightDirect> weightDirects, int weight) {
        return weightDirects.stream().filter(item->item.getWeight() == weight).collect(Collectors.toList());
    }

    private void getBlockDir(List<WeightDirect> weightDirects ,PosInfo[][] mapArr, Position position,int weight) {
        PosInfo posInfo = null;
        //上
        if(position.getX()-2>=0){
            posInfo = mapArr[position.getX()-2][position.getY()];
            if(posInfo.isBlock()){
                setWeightByDirect(weightDirects,Direct.UP,weight);
            }
        }
        //右
        if(position.getY()+2<cao.getGameMap().getRowLen()){
            posInfo = mapArr[position.getX()][position.getY()+2];
            if(posInfo.isBlock()){
                setWeightByDirect(weightDirects,Direct.RIGHT,weight);
            }
        }
        //下
        if(position.getX()+2<cao.getGameMap().getColLen()){
            posInfo = mapArr[position.getX()+2][position.getY()];
            if(posInfo.isBlock()){
                setWeightByDirect(weightDirects,Direct.DOWN,weight);
            }
        }
        //左
        if(position.getY()-2>=0){
            posInfo = mapArr[position.getX()][position.getY()-2];
            if(posInfo.isBlock()){
                setWeightByDirect(weightDirects,Direct.LEFT,weight);
            }
        }

    }

    private void getWarningDire(List<WeightDirect> weightDirects ,PosInfo[][] mapArr, Position position,int weight) {
        //可视范围内是否有蜀

        PosInfo posInfo = null;
        //上
        if(position.getX()-2>=0 && position.getY()-1>=0){
            posInfo = mapArr[position.getX()-2][position.getY()-1];
            if(hasShuByPosInfo(posInfo)){
                setWeightByDirect(weightDirects,Direct.UP,weight);
            }
        }
        if(position.getX()-2>=0){
            posInfo = mapArr[position.getX()-2][position.getY()];
            if(hasShuByPosInfo(posInfo)){
                setWeightByDirect(weightDirects,Direct.UP,weight);
            }
        }
        if(position.getX()-2>=0 && position.getY()+1<cao.getGameMap().getRowLen()){
            posInfo = mapArr[position.getX()-2][position.getY() +1];
            if(hasShuByPosInfo(posInfo)){
                setWeightByDirect(weightDirects,Direct.UP,weight);
            }
        }

        //右
        if(position.getX()-1>=0 && position.getY()+2 < cao.getGameMap().getRowLen()){
            posInfo = mapArr[position.getX()-1][position.getY()+2];
            if(hasShuByPosInfo(posInfo)){
                setWeightByDirect(weightDirects,Direct.RIGHT,weight);
            }
        }

        if(position.getY()+2 < cao.getGameMap().getRowLen()){
            posInfo = mapArr[position.getX()][position.getY()+2];
            if(hasShuByPosInfo(posInfo)){
                setWeightByDirect(weightDirects,Direct.RIGHT,weight);
            }
        }
        if(position.getX()+1 < cao.getGameMap().getColLen() && position.getY()+2 < cao.getGameMap().getRowLen()){
            posInfo = mapArr[position.getX()+1][position.getY()+2];
            if(hasShuByPosInfo(posInfo)){
                setWeightByDirect(weightDirects,Direct.RIGHT,weight);
            }
        }

        //下
        if(position.getX()+2 < cao.getGameMap().getColLen() && position.getY()-1 >=0 ){
            posInfo = mapArr[position.getX()+2][position.getY()-1];
            if(hasShuByPosInfo(posInfo)){
                setWeightByDirect(weightDirects,Direct.DOWN,weight);
            }
        }
        if(position.getX()+2 < cao.getGameMap().getColLen()){
            posInfo = mapArr[position.getX()+2][position.getY()];
            if(hasShuByPosInfo(posInfo)){
                setWeightByDirect(weightDirects,Direct.DOWN,weight);
            }
        }
        if(position.getX()+2 < cao.getGameMap().getColLen() && position.getY()+1 < cao.getGameMap().getRowLen() ){
            posInfo = mapArr[position.getX()+2][position.getY()+1];
            if(hasShuByPosInfo(posInfo)){
                setWeightByDirect(weightDirects,Direct.DOWN,weight);
            }
        }

        //左
        if(position.getX()-1 >=0  && position.getY()-2 >=0 ){
            posInfo = mapArr[position.getX()-1][position.getY()-2];
            if(hasShuByPosInfo(posInfo)){
                setWeightByDirect(weightDirects,Direct.LEFT,weight);
            }
        }
        if(position.getY()-2 >=0 ){
            posInfo = mapArr[position.getX()][position.getY()-2];
            if(hasShuByPosInfo(posInfo)){
                setWeightByDirect(weightDirects,Direct.LEFT,weight);
            }
        }
        if(position.getX()+1 < cao.getGameMap().getColLen()  && position.getY()-2 >=0 ){
            posInfo = mapArr[position.getX()+1][position.getY()-2];
            if(hasShuByPosInfo(posInfo)){
                setWeightByDirect(weightDirects,Direct.LEFT,weight);
            }
        }


        //直上
        if(position.getX()-1>=0){
            posInfo = mapArr[position.getX()-1][position.getY()];
            if(hasShuByPosInfo(posInfo)){
                setWeightByDirect(weightDirects,Direct.LEFT,weight);
                setWeightByDirect(weightDirects,Direct.RIGHT,weight);
            }
        }
        //直在
        if(position.getY()+1 < cao.getGameMap().getRowLen()){
            posInfo = mapArr[position.getX()][position.getY()+1];
            if(hasShuByPosInfo(posInfo)){
                setWeightByDirect(weightDirects,Direct.UP,weight);
                setWeightByDirect(weightDirects,Direct.DOWN,weight);
            }
        }
        //直下
        if(position.getX()+1 < cao.getGameMap().getColLen()){
            posInfo = mapArr[position.getX()+1][position.getY()];
            if(hasShuByPosInfo(posInfo)){
                setWeightByDirect(weightDirects,Direct.LEFT,weight);
                setWeightByDirect(weightDirects,Direct.RIGHT,weight);
            }
        }
        //直左
        if(position.getY()-1 >= 0 ){
            posInfo = mapArr[position.getX()][position.getY()-1];
            if(hasShuByPosInfo(posInfo)){
                setWeightByDirect(weightDirects,Direct.UP,weight);
                setWeightByDirect(weightDirects,Direct.DOWN,weight);
            }
        }
    }

    private void setWeightByDirect(List<WeightDirect> weightDirects, Direct direct,int weight) {
        weightDirects.stream().filter(item->item.getDirect().equals(direct) && item.getWeight() == 10).forEach(item->{
            item.setWeight(weight);
        });
    }

    private Set<Direct> getDiedDire(PosInfo[][] mapArr, Position position) {
        Set<Direct> diedDire = new HashSet<>();
        PosInfo posInfo = null;
        //上右下左，四个方向是否越界


        if(position.getX()-1<0){
            diedDire.add(Direct.UP);
        }else{
            posInfo = mapArr[position.getX()-1][position.getY()];
            if(posInfo.isBlock() || hasShuByPosInfo(posInfo)){
                diedDire.add(Direct.UP);
            }
        }

        if(position.getY()+1>=cao.getGameMap().getRowLen()){
            diedDire.add(Direct.RIGHT);
        }else {
            posInfo = mapArr[position.getX()][position.getY()+1];
            if(posInfo.isBlock() || hasShuByPosInfo(posInfo)){
                diedDire.add(Direct.RIGHT);
            }
        }

        if(position.getX()+1>=cao.getGameMap().getColLen()){
            diedDire.add(Direct.DOWN);
        }else {
            posInfo = mapArr[position.getX()+1][position.getY()];
            if(posInfo.isBlock() || hasShuByPosInfo(posInfo)){
                diedDire.add(Direct.DOWN);
            }
        }

        if(position.getY()-1<0){
            diedDire.add(Direct.LEFT);
        }else{
            posInfo = mapArr[position.getX()][position.getY()-1];
            if(posInfo.isBlock() || hasShuByPosInfo(posInfo)){
                diedDire.add(Direct.LEFT);
            }
        }

        //左上，存在
        if(position.getX()-1>=0 && position.getY()-1>=0){
            posInfo = mapArr[position.getX()-1][position.getY()-1];
            if(hasShuByPosInfo(posInfo)){
                diedDire.add(Direct.LEFT);
                diedDire.add(Direct.UP);
            }
        }

        //右上，存在
        if(position.getX()-1>=0 && position.getY()+1<cao.getGameMap().getRowLen()){
            posInfo = mapArr[position.getX()-1][position.getY()+1];
            if (hasShuByPosInfo(posInfo)){
                diedDire.add(Direct.UP);
                diedDire.add(Direct.RIGHT);
            }
        }

        //右下，存在
        if(position.getX()+1<cao.getGameMap().getColLen() && position.getY()+1<cao.getGameMap().getRowLen()){
            posInfo = mapArr[position.getX()+1][position.getY()+1];
            if (hasShuByPosInfo(posInfo)){
                diedDire.add(Direct.DOWN);
                diedDire.add(Direct.RIGHT);
            }
        }

        //左下，存在
        if(position.getX()+1<cao.getGameMap().getColLen() && position.getY()-1>=0){
            posInfo = mapArr[position.getX()+1][position.getY()-1];
            if (hasShuByPosInfo(posInfo)){
                diedDire.add(Direct.DOWN);
                diedDire.add(Direct.LEFT);
            }
        }
        return diedDire;
    }


    private boolean hasShuByPosInfo(PosInfo p11) {
        return p11.isShu1() || p11.isShu2() || p11.isShu3() || p11.isShu4();
    }

}
