package com.rbkmoney.xrates.endpoint;

import com.rbkmoney.machinegun.stateproc.ProcessorSrv;
import com.rbkmoney.woody.thrift.impl.http.THServiceBuilder;
import lombok.RequiredArgsConstructor;

import javax.servlet.*;
import javax.servlet.annotation.WebServlet;

import java.io.IOException;

@WebServlet("/v1/processor")
@RequiredArgsConstructor
public class ProcessorEndpoint extends GenericServlet {

    private final ProcessorSrv.Iface processorHandler;
    private Servlet thriftServlet;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        thriftServlet = new THServiceBuilder()
                .build(ProcessorSrv.Iface.class, processorHandler);
    }

    @Override
    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
        thriftServlet.service(req, res);
    }

}
