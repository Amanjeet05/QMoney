package com.crio.warmup.stock;

import java.util.Comparator;

import com.crio.warmup.stock.dto.AnnualizedReturn;

public class DescendingOrder implements Comparator<AnnualizedReturn> {

    @Override
    public int compare(AnnualizedReturn ar1, AnnualizedReturn ar2)
    {
        if (ar1.getAnnualizedReturn() == ar2.getAnnualizedReturn()) {
            return 0;
        }
        else if (ar1.getAnnualizedReturn() < ar2.getAnnualizedReturn()) {
            return 1;
        }
        else {
            return -1;
        }
    }
    
}