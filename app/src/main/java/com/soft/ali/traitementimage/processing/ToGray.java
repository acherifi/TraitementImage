package com.soft.ali.traitementimage.processing;

import com.soft.ali.traitementimage.ImgProcessing;

/**
 * Created by ali on 29/03/2017.
 */

public class ToGray extends ImgProcessing implements InterProcessing {

    @Override
    public void process(int lower, int upper) {
        super.toGray(lower, upper);
    }
}
