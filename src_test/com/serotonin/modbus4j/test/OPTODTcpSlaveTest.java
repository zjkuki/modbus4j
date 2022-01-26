package com.serotonin.modbus4j.test;

import com.serotonin.modbus4j.*;
import com.serotonin.modbus4j.code.DataType;
import com.serotonin.modbus4j.code.RegisterRange;
import com.serotonin.modbus4j.exception.IllegalDataAddressException;
import com.serotonin.modbus4j.exception.ModbusInitException;
import com.serotonin.modbus4j.ip.IpParameters;
import com.serotonin.modbus4j.ip.tcp.TcpSlave;
import com.serotonin.modbus4j.locator.BaseLocator;
import com.serotonin.modbus4j.test.optod.OPTODTcpSlave;

public class OPTODTcpSlaveTest {
    static BasicProcessImage pImage;
    static int counter = 0;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        IpParameters params = new IpParameters();
        String host = "127.0.0.1";
        params.setHost(host );
        int slaveId = 10;
        int port = 20108;


        boolean encapsulated = false;
        final ModbusSlaveSet listener = new OPTODTcpSlave(port,encapsulated);
        // TcpSlave slave = new TcpSlave(port, encapsulated);
        listener.addProcessImage(getModscanProcessImage(slaveId));


        new Thread(new Runnable() {
            public void run() {
                try {
                    listener.start();
                }
                catch (ModbusInitException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        while (true) {
            synchronized (listener) {
                listener.wait(500);
            }

            for (ProcessImage processImage : listener.getProcessImages())
                updateProcessImage((BasicProcessImage) processImage);
        }
    }

    static void updateProcessImage(BasicProcessImage processImage) throws IllegalDataAddressException {
       // processImage.setInput(10, !processImage.getInput(10));
        //processImage.setInput(13, !processImage.getInput(13));
        BaseLocator<Number> loc = BaseLocator.holdingRegister(10, 85, DataType.FOUR_BYTE_FLOAT);
        Number value = processImage.getRegister(loc);

        System.out.println("返回含氧量oxygen %信息："+ value);

    }

    static BasicProcessImage getModscanProcessImage(int slaveId) {
        BasicProcessImage processImage = new BasicProcessImage(slaveId);
        processImage.setAllowInvalidAddress(false);
        processImage.setInvalidAddressValue(Short.MIN_VALUE);

        processImage.setHoldingRegister(332, new short[] {0,0});
        //重置 标准+运算符+临时校准日期(004C=76) 0A 10 00 4C 00 01 02 00 01 1A AC
        processImage.setHoldingRegister( 76, new short[] {1});

        //设置测量参数1的类型(00A6=166) 0A 10 00 A6 00 01 02 00 00 CD A6
        processImage.setHoldingRegister( 166, new short[] {0});

        //设置测量参数2的类型(00A7=167) 0A 03 00 A7 00 01 34 92
        processImage.setHoldingRegister( 167, new short[] {0});

        //设置测量参数3的类型(00A8=168) 0A 03 00 A8 00 01 04 91
        processImage.setHoldingRegister( 168, new short[] {0});

        //除了温度以外的所有参数的平均值(AA=170)。0A 10 00 AA 00 01 02 00 01 0C AA
        processImage.setHoldingRegister( 170, new short[] {1});

        //写0x0001 启动一个或多个参数的测量（同时）
        processImage.setHoldingRegister( 1, new short[] {31});
        try {
            Thread.sleep(400);
        }catch (Exception e){
            e.printStackTrace();
        }

        processImage.setExceptionStatus((byte) 151);

        // Add an image listener.
        processImage.addListener(new BasicProcessImageListener());

        return processImage;
    }

    static class BasicProcessImageListener implements ProcessImageListener {
        public void coilWrite(int offset, boolean oldValue, boolean newValue) {
            System.out.println("Coil at " + offset + " was set from " + oldValue + " to " + newValue);
        }

        public void holdingRegisterWrite(int offset, short oldValue, short newValue) {
            System.out.println("HR at " + offset + " was set from " + oldValue + " to " + newValue);
        }
    }
}

