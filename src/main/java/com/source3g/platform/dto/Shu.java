package com.source3g.platform.dto;

import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by alenfive on 17-9-17.
 */
@Data
@Component
public class Shu {
    List<ShuInfo> shus;
    private Position cao = null;
    private GameMap gameMap = null;
    private ClientRes clientRes = null;
    private int[][] refreshLocal;

    public void init(int x,int y){
        this.shus = null;
        this.cao = null;
        this.gameMap = null;
        this.clientRes = null;
        refreshLocal  = new int[x][y];
        for(int i=0;i<x;i++){
            for(int j=0;j<y;j++){
                refreshLocal[i][j] = 0;
            }
        }
    }

}
