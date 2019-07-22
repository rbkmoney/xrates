package com.rbkmoney.xrates.endpoint;

import com.rbkmoney.woody.thrift.impl.http.THServiceBuilder;
import com.rbkmoney.xrates.rate.EventSinkSrv;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import java.io.IOException;

@WebServlet("/v1/event_sink")
@RequiredArgsConstructor
public class EventSinkEndpoint extends GenericServlet {

    private Servlet thriftServlet;

    private final EventSinkSrv.Iface processorHandler;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        thriftServlet = new THServiceBuilder()
                .build(EventSinkSrv.Iface.class, processorHandler);
    }

    @Override
    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
        thriftServlet.service(req, res);
    }

}
