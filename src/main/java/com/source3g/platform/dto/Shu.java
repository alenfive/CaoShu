package com.source3g.platform.dto;

import lombok.Data;
import org.springframework.stereotype.Component;

/**
 * Created by alenfive on 17-9-17.
 */
@Data
@Component
public class Shu {
    private Position shu1 = null;
    private Position shu2 = null;
    private Position shu3 = null;
    private Position shu4 = null;
    private Position cao = null;
    private GameMap gameMap = null;
    private ClientRes clientRes = null;

}
