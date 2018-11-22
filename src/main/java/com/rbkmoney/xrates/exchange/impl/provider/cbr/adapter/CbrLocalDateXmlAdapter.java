package com.rbkmoney.xrates.exchange.impl.provider.cbr.adapter;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class CbrLocalDateXmlAdapter extends XmlAdapter<String, LocalDate> {

    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    @Override
    public LocalDate unmarshal(String stringValue) throws Exception {
        return Optional.ofNullable(stringValue)
                .map(value -> LocalDate.from(DATE_FORMATTER.parse(value)))
                .orElse(null);
    }

    @Override
    public String marshal(LocalDate dateValue) throws Exception {
        return Optional.ofNullable(dateValue)
                .map(value -> DATE_FORMATTER.format(value))
                .orElse(null);
    }
}
