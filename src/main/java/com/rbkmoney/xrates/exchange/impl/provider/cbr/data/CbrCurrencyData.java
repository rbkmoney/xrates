package com.rbkmoney.xrates.exchange.impl.provider.cbr.data;

import com.rbkmoney.xrates.exchange.impl.provider.cbr.adapter.CbrBigDecimalXmlAdapter;
import lombok.Data;
import lombok.ToString;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.math.BigDecimal;

@Data
@ToString
@XmlRootElement(name = "Valute")
@XmlAccessorType(XmlAccessType.FIELD)
public class CbrCurrencyData {

    @XmlAttribute(name = "ID")
    private String id;

    @XmlElement(name = "NumCode")
    private Integer numCode;

    @XmlElement(name = "CharCode")
    private String charCode;

    @XmlElement(name = "Nominal")
    private Integer nominal;

    @XmlElement(name = "Name")
    private String name;

    @XmlJavaTypeAdapter(CbrBigDecimalXmlAdapter.class)
    @XmlElement(name = "Value")
    private BigDecimal value;

}
