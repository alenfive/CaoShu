package com.source3g.platform.dto;

import com.source3g.platform.contants.Direct;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Created by Administrator on 9/18/2017.
 */
@Data
@AllArgsConstructor
public class WeightDirect {
    private Direct direct = null;
    private int weight = 0;
}
