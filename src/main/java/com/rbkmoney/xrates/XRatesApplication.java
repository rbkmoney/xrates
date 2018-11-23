package com.rbkmoney.xrates;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

@ServletComponentScan
@SpringBootApplication
public class XRatesApplication {

    public static void main(String[] args) {
        SpringApplication.run(XRatesApplication.class, args);
    }

}
