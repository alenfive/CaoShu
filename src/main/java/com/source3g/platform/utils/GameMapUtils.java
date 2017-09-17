package com.source3g.platform.utils;

import com.source3g.platform.dto.PosInfo;

import java.util.ArrayList;
import java.util.List;


public class GameMapUtils {


    public static List<List<PosInfo>> arrayToList(PosInfo[][] posInfos){
        List<List<PosInfo>>  list = new ArrayList<>();//先定义一个集合对象

        for(int i=0; i<posInfos.length; i++){//遍历二维数组，对集合进行填充
            List<PosInfo> listSub=new ArrayList<>();//初始化一个ArrayList集合
            for(int j=0; j<posInfos[i].length; j++){
                listSub.add(posInfos[i][j]);//数组的列放到集合中

            }
            list.add(listSub);//二维数组放到集合中
        }
        return list;
    }


    public static PosInfo[][] listToArray(List<List<PosInfo>> posInfos){
        PosInfo[][] arrPosInfos = new PosInfo[posInfos.size()][posInfos.get(0).size()];
        for (int i = 0; i < posInfos.size(); i++) {
            for (int j = 0; j < posInfos.get(0).size(); j++) {
                arrPosInfos[i][j] = posInfos.get(i).get(j);
            }
        }
        return arrPosInfos;
    }


}
