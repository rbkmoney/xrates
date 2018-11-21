package com.rbkmoney.xrates.exchange.provider.impl.cbr.adapter;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class CbrLocalDateXmlAdapter extends XmlAdapter<String, LocalDate> {

    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    @Override
    public LocalDate unmarshal(String value) throws Exception {
        if (value != null) {
            return LocalDate.from(DATE_FORMATTER.parse(value));
        }
        return null;
    }

    @Override
    public String marshal(LocalDate value) throws Exception {
        if (value != null) {
            return DATE_FORMATTER.format(value);
        }
        return null;
    }
}
