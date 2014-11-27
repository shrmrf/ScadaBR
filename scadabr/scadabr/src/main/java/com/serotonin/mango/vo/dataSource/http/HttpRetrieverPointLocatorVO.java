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
package com.serotonin.mango.vo.dataSource.http;

import br.org.scadabr.DataType;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import com.serotonin.mango.rt.dataSource.PointLocatorRT;
import com.serotonin.mango.rt.dataSource.http.HttpRetrieverPointLocatorRT;
import com.serotonin.mango.rt.event.type.AuditEventType;
import com.serotonin.mango.vo.dataSource.AbstractPointLocatorVO;
import br.org.scadabr.util.SerializationHelper;
import br.org.scadabr.utils.i18n.LocalizableMessage;
import br.org.scadabr.utils.i18n.LocalizableMessageImpl;
import br.org.scadabr.web.taglib.Functions;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * @author Matthew Lohbihler
 */
public class HttpRetrieverPointLocatorVO extends AbstractPointLocatorVO {

    public static class HttpRetrieverPointLocatorVoValidator implements Validator {

        @Override
        public boolean supports(Class<?> clazz) {
            return HttpRetrieverPointLocatorVO.class.isAssignableFrom(clazz);
        }

        @Override
        public void validate(Object target, Errors errors) {
            final HttpRetrieverPointLocatorVO vo = (HttpRetrieverPointLocatorVO) target;
            if (vo.valueRegex.isEmpty()) {
                errors.rejectValue("valueRegex", "validate.required");
            } else {
                try {
                    Pattern pattern = Pattern.compile(vo.valueRegex);
                    if (pattern.matcher("").groupCount() < 1) {
                        errors.rejectValue("valueRegex", "validate.captureGroup");
                    }
                } catch (PatternSyntaxException e) {
                    errors.rejectValue("valueRegex", "common.default", new Object[]{e}, "common.default");
                }
            }

            if (vo.dataType == DataType.NUMERIC && !vo.valueFormat.isEmpty()) {
                try {
                    //TODO Localization with server locale !!!
                    new DecimalFormat(vo.valueFormat);
                } catch (IllegalArgumentException e) {
                    errors.rejectValue("valueFormat", "common.default", new Object[]{e}, "common.default");
                }
            }

            if (!vo.timeRegex.isEmpty()) {
                try {
                    Pattern pattern = Pattern.compile(vo.timeRegex);
                    if (pattern.matcher("").groupCount() < 1) {
                        errors.rejectValue("timeRegex", "validate.captureGroup");
                    }
                } catch (PatternSyntaxException e) {
                    errors.rejectValue("timeRegex", "common.default", new Object[]{e}, "common.default");
                }

                if (vo.timeFormat.isEmpty()) {
                    errors.rejectValue("timeFormat", "validate.required");
                } else {
                    try {
                        new SimpleDateFormat(vo.timeFormat);
                    } catch (IllegalArgumentException e) {
                        errors.rejectValue("timeFormat", "common.default", new Object[]{e}, "common.default");
                    }
                }
            }
        }
    }

    @Override
    public boolean isSettable() {
        return false;
    }

    @Override
    public PointLocatorRT createRuntime() {
        return new HttpRetrieverPointLocatorRT(this);
    }

    @Override
    public LocalizableMessage getConfigurationDescription() {
        return new LocalizableMessageImpl("dsEdit.httpRetriever.dpconn", Functions.escapeLessThan(valueRegex));
    }

    private String valueRegex;

    private boolean ignoreIfMissing;

    private String valueFormat;
    private DataType dataType;

    private String timeRegex;

    private String timeFormat;

    public String getValueRegex() {
        return valueRegex;
    }

    public void setValueRegex(String valueRegex) {
        this.valueRegex = valueRegex;
    }

    public boolean isIgnoreIfMissing() {
        return ignoreIfMissing;
    }

    public void setIgnoreIfMissing(boolean ignoreIfMissing) {
        this.ignoreIfMissing = ignoreIfMissing;
    }

    public String getValueFormat() {
        return valueFormat;
    }

    public void setValueFormat(String valueFormat) {
        this.valueFormat = valueFormat;
    }

    @Override
    public DataType getDataType() {
        return dataType;
    }

    public void setDataType(DataType dataType) {
        this.dataType = dataType;
    }

    public String getTimeRegex() {
        return timeRegex;
    }

    public void setTimeRegex(String timeRegex) {
        this.timeRegex = timeRegex;
    }

    public String getTimeFormat() {
        return timeFormat;
    }

    public void setTimeFormat(String timeFormat) {
        this.timeFormat = timeFormat;
    }

    @Override
    public void addProperties(List<LocalizableMessage> list) {
        AuditEventType.addPropertyMessage(list, "dsEdit.pointDataType", dataType);
        AuditEventType.addPropertyMessage(list, "dsEdit.httpRetriever.valueRegex", valueRegex);
        AuditEventType.addPropertyMessage(list, "dsEdit.httpRetriever.ignoreIfMissing", ignoreIfMissing);
        AuditEventType.addPropertyMessage(list, "dsEdit.httpRetriever.numberFormat", valueFormat);
        AuditEventType.addPropertyMessage(list, "dsEdit.httpRetriever.timeRegex", timeRegex);
        AuditEventType.addPropertyMessage(list, "dsEdit.httpRetriever.timeFormat", timeFormat);
    }

    @Override
    public void addPropertyChanges(List<LocalizableMessage> list, Object o) {
        HttpRetrieverPointLocatorVO from = (HttpRetrieverPointLocatorVO) o;
        AuditEventType.maybeAddPropertyChangeMessage(list, "dsEdit.pointDataType", from.dataType, dataType);
        AuditEventType.maybeAddPropertyChangeMessage(list, "dsEdit.httpRetriever.valueRegex", from.valueRegex,
                valueRegex);
        AuditEventType.maybeAddPropertyChangeMessage(list, "dsEdit.httpRetriever.ignoreIfMissing",
                from.ignoreIfMissing, ignoreIfMissing);
        AuditEventType.maybeAddPropertyChangeMessage(list, "dsEdit.httpRetriever.numberFormat", from.valueFormat,
                valueFormat);
        AuditEventType.maybeAddPropertyChangeMessage(list, "dsEdit.httpRetriever.timeRegex", from.timeRegex, timeRegex);
        AuditEventType.maybeAddPropertyChangeMessage(list, "dsEdit.httpRetriever.timeFormat", from.timeFormat,
                timeFormat);
    }

    //
    // /
    // / Serialization
    // /
    //
    private static final long serialVersionUID = -1;
    private static final int version = 1;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(version);
        SerializationHelper.writeSafeUTF(out, valueRegex);
        out.writeBoolean(ignoreIfMissing);
        out.writeInt(dataType.mangoDbId);
        SerializationHelper.writeSafeUTF(out, valueFormat);
        SerializationHelper.writeSafeUTF(out, timeRegex);
        SerializationHelper.writeSafeUTF(out, timeFormat);
    }

    private void readObject(ObjectInputStream in) throws IOException {
        int ver = in.readInt();

        // Switch on the version of the class so that version changes can be elegantly handled.
        if (ver == 1) {
            valueRegex = SerializationHelper.readSafeUTF(in);
            ignoreIfMissing = in.readBoolean();
            dataType = DataType.fromMangoDbId(in.readInt());
            valueFormat = SerializationHelper.readSafeUTF(in);
            timeRegex = SerializationHelper.readSafeUTF(in);
            timeFormat = SerializationHelper.readSafeUTF(in);
        }
    }

    @Override
    public String getName() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
