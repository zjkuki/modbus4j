package com.serotonin.modbus4j.test.optod;

/*
/*
 * ============================================================================
 * GNU General Public License
 * ============================================================================
 *
 * Copyright (C) 2006-2011 Serotonin Software Technologies Inc. http://serotoninsoftware.com
 * @author Matthew Lohbihler
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.serotonin.modbus4j.ModbusSlaveSet;
import com.serotonin.modbus4j.base.BaseMessageParser;
import com.serotonin.modbus4j.base.BaseRequestHandler;
import com.serotonin.modbus4j.base.ModbusUtils;
import com.serotonin.modbus4j.exception.ModbusInitException;
import com.serotonin.modbus4j.ip.encap.EncapMessageParser;
import com.serotonin.modbus4j.ip.encap.EncapRequestHandler;
import com.serotonin.modbus4j.ip.xa.XaMessageParser;
import com.serotonin.modbus4j.ip.xa.XaRequestHandler;
import com.serotonin.modbus4j.sero.messaging.MessageControl;
import com.serotonin.modbus4j.sero.messaging.TestableTransport;

/**
 * <p>TcpSlave class.</p>
 *
 * @author Matthew Lohbihler
 * @version 5.0.0
 */
public class OPTODTcpSlave extends ModbusSlaveSet {
    // Configuration fields
    private final int port;
    final boolean encapsulated;

    // Runtime fields.
    private ServerSocket serverSocket;
    final ExecutorService executorService;
    final List<TcpConnectionHandler> listConnections = new ArrayList<>();

    /**
     * <p>Constructor for TcpSlave.</p>
     *
     * @param encapsulated a boolean.
     */
    public OPTODTcpSlave(boolean encapsulated) {
        this(ModbusUtils.TCP_PORT, encapsulated);
    }

    /**
     * <p>Constructor for TcpSlave.</p>
     *
     * @param port a int.
     * @param encapsulated a boolean.
     */
    public OPTODTcpSlave(int port, boolean encapsulated) {
        this.port = port;
        this.encapsulated = encapsulated;
        executorService = Executors.newCachedThreadPool();
    }

    /** {@inheritDoc} */
    @Override
    public void start() throws ModbusInitException {
        try {
            serverSocket = new ServerSocket(port);

            Socket socket;
            while (true) {
                socket = serverSocket.accept();
                TcpConnectionHandler handler = new TcpConnectionHandler(socket);
                executorService.execute(handler);
                synchronized (listConnections) {
                    listConnections.add(handler);
                }
            }
        }
        catch (IOException e) {
            throw new ModbusInitException(e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void stop() {
        // Close the socket first to prevent new messages.
        try {
            serverSocket.close();
        }
        catch (IOException e) {
            getExceptionHandler().receivedException(e);
        }

        // Close all open connections.
        synchronized (listConnections) {
            for (TcpConnectionHandler tch : listConnections)
                tch.kill();
            listConnections.clear();
        }

        // Now close the executor service.
        executorService.shutdown();
        try {
            executorService.awaitTermination(3, TimeUnit.SECONDS);
        }
        catch (InterruptedException e) {
            getExceptionHandler().receivedException(e);
        }
    }

    class TcpConnectionHandler implements Runnable {
        private final Socket socket;
        private TestableTransport transport;
        private MessageControl conn;

        TcpConnectionHandler(Socket socket) throws ModbusInitException {
            this.socket = socket;
            try {
                transport = new TestableTransport(socket.getInputStream(), socket.getOutputStream());
            }
            catch (IOException e) {
                throw new ModbusInitException(e);
            }
        }

        @Override
        public void run() {
            BaseMessageParser messageParser;
            BaseRequestHandler requestHandler;

            if (encapsulated) {
                messageParser = new EncapMessageParser(false);
                requestHandler = new EncapRequestHandler(OPTODTcpSlave.this);
            }
            else {
                messageParser = new OptodMessageParser(false);
                requestHandler = new OptodRequestHandler(OPTODTcpSlave.this);
            }

            conn = new MessageControl();
            conn.setExceptionHandler(getExceptionHandler());

            try {
                conn.start(transport, messageParser, requestHandler, null);
                executorService.execute(transport);
            }
            catch (IOException e) {
                getExceptionHandler().receivedException(new ModbusInitException(e));
            }

            // Monitor the socket to detect when it gets closed.
            while (true) {
                try {
                    transport.testInputStream();
                }
                catch (IOException e) {
                    break;
                }

                try {
                    Thread.sleep(500);
                }
                catch (InterruptedException e) {
                    // no op
                }
            }

            conn.close();
            kill();
            synchronized (listConnections) {
                listConnections.remove(this);
            }
        }

        void kill() {
            try {
                socket.close();
            }
            catch (IOException e) {
                getExceptionHandler().receivedException(new ModbusInitException(e));
            }
        }
    }
}

