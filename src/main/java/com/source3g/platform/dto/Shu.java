package com.source3g.platform.dto;

import com.source3g.platform.contants.Direct;
import lombok.Data;
import org.springframework.stereotype.Component;

/**
 * Created by alenfive on 17-9-17.
 */
@Data
@Component
public class Shu {
    private Direct preDirect = Direct.STAY;
    public void reset(){
        preDirect = Direct.STAY;
    }
}
