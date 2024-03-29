package com.rbkmoney.xrates.endpoint;

import com.rbkmoney.woody.thrift.impl.http.THServiceBuilder;
import com.rbkmoney.xrates.rate.RatesSrv;
import lombok.RequiredArgsConstructor;

import javax.servlet.*;
import javax.servlet.annotation.WebServlet;

import java.io.IOException;

@WebServlet("/v1/rates")
@RequiredArgsConstructor
public class RatesEndpoint extends GenericServlet {

    private final RatesSrv.Iface ratesHandler;
    private Servlet thriftServlet;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        thriftServlet = new THServiceBuilder()
                .build(RatesSrv.Iface.class, ratesHandler);
    }

    @Override
    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
        thriftServlet.service(req, res);
    }

}
