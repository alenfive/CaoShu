package com.source3g.platform.dto;

import com.source3g.platform.contants.Direct;
import com.source3g.platform.contants.MapType;
import lombok.Data;

/**
 * Created by Administrator on 9/19/2017.
 */
@Data
public class ShuInfo {
    private MapType id;
    private Position selfPosition;
    private Position caoPosition;
    private Direct nextDirect;

    public ShuInfo(MapType id){
        this.id = id;
    }
}
