package com.source3g.platform.dto;

import lombok.Data;

/**
 * Created by alenfive on 17-9-17.
 */
@Data
public class Position {
    public int x = 0;
    public int y = 0;

    public int F;
    public int G;
    public int H;

    public Position(int x,int y){
        this.x = x;
        this.y = y;
    }

    public void calcF() {
        this.F = this.G + this.H;
    }

    public Position parent;
}
