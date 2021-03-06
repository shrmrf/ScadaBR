/*
 Mango - Open Source M2M - http://mango.serotoninsoftware.com
 Copyright (C) 2006-2011 Serotonin Software Technologies Inc.
 @author Matthew Lohbihler
    
 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.serotonin.mango.view.stats;

import com.serotonin.mango.rt.dataImage.types.DoubleValue;

/**
 * @author Matthew Lohbihler
 */
public class DoubleDataQuantizer extends AbstractDataQuantizer<DoubleValue> {

    private double valueSum;

    public DoubleDataQuantizer(long start, long end, int buckets, DataQuantizerCallback callback) {
        super(start, end, buckets, callback);
    }

    @Override
    protected void periodData(DoubleValue value) {
        valueSum += value.getDoubleValue();
    }

    @Override
    protected DoubleValue donePeriod(int valueCounter) {
        double result = valueSum / valueCounter;
        valueSum = 0;
        return new DoubleValue(result);
    }
}
