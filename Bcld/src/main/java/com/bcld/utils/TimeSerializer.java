package com.bcld.utils;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.time.FastDateFormat;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;

import freemarker.template.SimpleDate;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;

public class TimeSerializer extends JsonSerializer<Date> implements TemplateMethodModelEx {

    @Override
    public void serialize(Date value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
        String formattedDate = FastDateFormat.getInstance(Globals.TIMEPATTERN).format(value);
        jgen.writeString(formattedDate);
    }

    @Override
    public Object exec(List arguments) throws TemplateModelException {
        if (null == arguments || null == arguments.get(0)) {
            return "";
        } else {
            return FastDateFormat.getInstance(Globals.TIMEPATTERN).format(((SimpleDate) arguments.get(0)).getAsDate());
        }
    }
}
