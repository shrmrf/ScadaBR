/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.org.scadabr.dao;

import br.org.scadabr.DataType;
import com.serotonin.mango.rt.dataImage.PointValueTime;
import com.serotonin.mango.rt.dataImage.SetPointSource;
import com.serotonin.mango.vo.DataPointVO;
import java.util.List;
import javax.inject.Named;

/**
 *
 * @author aploese
 */
@Named
public interface PointValueDao {

    /**
     * Save the value asynchonous
     * @param <T>
     * @param newValue Value currently not in store
     * @param source currently ignored ???
     */
    <T extends PointValueTime> void savePointValueAsync(T newValue, SetPointSource source);

    <T extends PointValueTime> T savePointValueSync(T newValue, SetPointSource source);

    <T extends PointValueTime> T getLatestPointValue(DataPointVO<T> dpVo);

    long getInceptionDate(DataPointVO dpVo);

    <T extends PointValueTime> Iterable<T> getPointValues(DataPointVO<T> dpVo, long from);

    <T extends PointValueTime> Iterable<T> getPointValuesBetween(DataPointVO<T> dpVo, long from, long to);

    List<Long> getFiledataIds();

    <T extends PointValueTime> T getPointValueBefore(DataPointVO<T> dpVo, long reportStartTime);

    public PointValueTime getPointValueBefore(int id, DataType dataType, long l);

}
