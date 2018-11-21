package com.rbkmoney.xrates.exchange.provider.impl.cbr.adapter;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.math.BigDecimal;

public class CbrBigDecimalXmlAdapter extends XmlAdapter<String, BigDecimal> {

    @Override
    public BigDecimal unmarshal(String value) throws Exception {
        if (value != null) {
            return new BigDecimal(value.replace(',', '.'));
        }
        return null;
    }

    @Override
    public String marshal(BigDecimal value) throws Exception {
        if (value != null) {
            return value.toString().replace('.', ',');
        }
        return null;
    }

}
