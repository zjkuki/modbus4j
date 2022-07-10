package com.serotonin.modbus4j.test;

import com.serotonin.modbus4j.BasicProcessImage;
import com.serotonin.modbus4j.ProcessImageListener;
import com.serotonin.modbus4j.code.DataType;
import com.serotonin.modbus4j.code.RegisterRange;
import com.serotonin.modbus4j.exception.IllegalDataAddressException;
import com.serotonin.modbus4j.exception.ModbusInitException;
import com.serotonin.modbus4j.ip.tcp.TcpSlave;

import java.util.logging.Level;
import java.util.logging.Logger;

public class ListenerTestKuki1 {
    static BasicProcessImage pImage;
    static int counter = 0;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IllegalDataAddressException {
        int slaveId = 10;
        int port = 8890;
        pImage = new BasicProcessImage(slaveId);
         //pImage.setHoldingRegister(0, (short)00);
        //pImage.setHoldingRegister(1, (short)11);
        //pImage.setHoldingRegister(2, (short)22);
        //pImage.setHoldingRegister(3, (short)33);

        pImage.addListener(new BasicProcessImageListener());

        boolean encapsulated = false;
        TcpSlave slave = new TcpSlave(port, encapsulated);
        slave.addProcessImage(pImage);


        Runnable r = () -> {
            while (true) {
                counter++;
                pImage.setHoldingRegister(170, new short[] {1});
                try {
                    Thread.sleep(500);
                    Number n = pImage.getNumeric(RegisterRange.HOLDING_REGISTER,167, DataType.FOUR_BYTE_FLOAT);
                    System.out.println("value:"+n.floatValue());
                    //short s= pImage.getHoldingRegister(167);
                    //System.out.println(s);
                } catch (InterruptedException | IllegalDataAddressException ex) {
                    System.out.println(ex.getMessage());
                    //Logger.getLogger(ListenerTestKuki1.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };
        new Thread(r).start();

        try {
            slave.start();
        } catch (ModbusInitException ex) {
            //Logger.getLogger(ListenerTestKuki1.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println(ex.getMessage());
        }
    }

    static class BasicProcessImageListener implements ProcessImageListener {
        @Override
        public void coilWrite(int offset, boolean oldValue, boolean newValue) {
            System.out.println("Coil at " + offset + " was set from " + oldValue + " to " + newValue);
        }

        @Override
        public void holdingRegisterWrite(int offset, short oldValue, short newValue) {
            // Add a small delay to the processing.
            //            try {
            //                Thread.sleep(500);
            //            }
            //            catch (InterruptedException e) {
            //                // no op
            //            }
            System.out.println("HR at " + offset + " was set from " + oldValue + " to " + newValue);
        }
    }
}
