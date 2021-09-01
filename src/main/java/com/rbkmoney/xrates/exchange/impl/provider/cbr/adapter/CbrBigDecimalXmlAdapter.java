package com.rbkmoney.xrates.exchange.impl.provider.cbr.adapter;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import java.math.BigDecimal;
import java.util.Optional;

public class CbrBigDecimalXmlAdapter extends XmlAdapter<String, BigDecimal> {

    @Override
    public BigDecimal unmarshal(String stringValue) throws Exception {
        return Optional.ofNullable(stringValue)
                .map(value -> new BigDecimal(value.replace(',', '.')))
                .orElse(null);
    }

    @Override
    public String marshal(BigDecimal bigDecimalValue) throws Exception {
        return Optional.ofNullable(bigDecimalValue)
                .map(value -> value.toString().replace('.', ','))
                .orElse(null);
    }

}
