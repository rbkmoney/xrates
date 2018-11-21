package com.rbkmoney.xrates.exchange.provider.impl.cbr.data;

import com.rbkmoney.xrates.exchange.provider.impl.cbr.adapter.CbrLocalDateXmlAdapter;
import lombok.Data;
import lombok.ToString;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.time.LocalDate;
import java.util.List;

@Data
@ToString
@XmlRootElement(name = "ValCurs")
@XmlAccessorType(XmlAccessType.FIELD)
public class CbrExchangeRateData {

    @XmlAttribute
    private String name;

    @XmlAttribute(name = "Date")
    @XmlJavaTypeAdapter(CbrLocalDateXmlAdapter.class)
    private LocalDate date;

    @XmlElement(name = "Valute")
    private List<CbrCurrencyData> currencies;

}
