package com.serotonin.modbus4j.test;

import com.serotonin.modbus4j.ModbusFactory;
import com.serotonin.modbus4j.ModbusMaster;
import com.serotonin.modbus4j.exception.ModbusTransportException;
import com.serotonin.modbus4j.ip.IpParameters;
import com.serotonin.modbus4j.msg.*;
import com.serotonin.modbus4j.sero.util.queue.ByteQueue;
import com.serotonin.modbus4j.test.utils.DataTransform;
import com.serotonin.modbus4j.test.utils.IEEE754;

import java.util.Arrays;
import java.util.Locale;
import java.util.Scanner;

import static java.lang.Thread.sleep;

public class TestTCPListenerTestKuki_GOOD_20220711 {

    private static int step=1;
    public static void main(String[] args) throws Exception {



        String commPortId = "COM1";
        /*int baudRate = 9600;
        int flowControlIn = 0;
        int flowControlOut = 0;
        int dataBits = 8;
        int stopBits = 2;
        int parity = 0;

        TestSerialPortWrapper wrapper = new TestSerialPortWrapper(commPortId, baudRate, flowControlIn, flowControlOut, dataBits, stopBits, parity);*/

        IpParameters ipParameters = new IpParameters();
        ipParameters.setHost("192.168.0.102");
        ipParameters.setPort(8890);


        ModbusFactory modbusFactory = new ModbusFactory();

        // ModbusMaster master = modbusFactory.createRtuMaster(wrapper, false);
        // ModbusMaster master = modbusFactory.createAsciiMaster(wrapper);
        //ModbusMaster master = modbusFactory.createTcpMaster(ipParameters, false);
        //ModbusMaster master = modbusFactory.createTcpListener(ipParameters);
        //ModbusMaster master = modbusFactory.createUdpMaster(ipParameters);
        //OPTODTcpListener listener = new OPTODTcpListener(ipParameters);
        //ModbusMaster master =new OPTODTcpListener(ipParameters);
        ModbusMaster master = modbusFactory.createTcpListener(ipParameters);
        int slaveId = 10;
        try {
            master.init();
            //listener.init();
        }
        finally {
           // master.destroy();
        }

        while (!master.isConnected()) {
            synchronized (master) {
                master.wait(2000);
            }

            if(master.isConnected()){
                System.out.println("设备已连接，请输入功能键继续下一步：");
                System.out.println("功能键：D-设备信息  I-初始化  Q-查询  X-退出");
                while(true) {
                    Scanner in = new Scanner(System.in);
                    String inputStr = in.nextLine();

                    if(inputStr.toLowerCase(Locale.ROOT).equals("x")) {
                        System.exit(0);
                    }

                    if (inputStr.toLowerCase(Locale.ROOT).equals("d")) {
                        step = 3;
                    }else if (inputStr.toLowerCase(Locale.ROOT).equals("q")) {
                        step = 2;
                    }else if (inputStr.toLowerCase(Locale.ROOT).equals("i")) {
                        step = 1;
                    }

                    doSomeThink(master, slaveId, step);
                }
            }
        }
    }

    private static void doSomeThink(ModbusMaster master, int slaveId, int step) throws InterruptedException {
        switch(step) {
            case 3:
                readHoldingRegisters2String(master,slaveId, 3328, 49);
                sleep(500);
                break;
            case 1:
                //临时系数设置(014C=332)，0A 10 01 4C 00 02 04 00 00 00 00 DF 7E。
                writeRegistersTest(master, slaveId, 332, new short[] {0,0});
                sleep(500);

                //重置 标准+运算符+临时校准日期(004C=76) 0A 10 00 4C 00 01 02 00 01 1A AC
                writeRegistersTest(master, slaveId, 76, new short[] {1});
                sleep(500);

                //设置测量参数1的类型(00A6=166) 0A 10 00 A6 00 01 02 00 00 CD A6
                writeRegistersTest(master, slaveId, 166, new short[] {0});
                sleep(500);

                //设置测量参数2的类型(00A7=167) 0A 03 00 A7 00 01 34 92
                writeRegistersTest(master, slaveId, 167, new short[] {0});
                sleep(500);

                //设置测量参数3的类型(00A8=168) 0A 03 00 A8 00 01 04 91
                writeRegistersTest(master, slaveId, 168, new short[] {0});
                sleep(500);

                //除了温度以外的所有参数的平均值(AA=170)。0A 10 00 AA 00 01 02 00 01 0C AA
                writeRegistersTest(master, slaveId, 170, new short[] {1});
                sleep(500);

                //写0x0001 启动一个或多个参数的测量（同时）
                writeRegistersTest(master, slaveId, 1, new short[] {31});
                try {
                    sleep(400);
                }catch (Exception e){
                    e.printStackTrace();
                }
                //读0x00A6的1个寄存器
                readHoldingRegistersTest1(master, slaveId, 166, 1);
                sleep(500);

                //读0x00A7的1个寄存器
                readHoldingRegistersTest1(master, slaveId, 167, 1);
                sleep(500);

                //读0x00A8的1个寄存器
                readHoldingRegistersTest1(master, slaveId, 168, 1);
                sleep(500);

                step++;
                doSomeThink(master,slaveId,step);
                break;
            case 2:
                readHoldingRegistersTest2(master, slaveId, 82, 11);
                sleep(500);
                break;
        }
    }
    public static void readCoilTest(ModbusMaster master, int slaveId, int start, int len) {
        try {
            ReadCoilsRequest request = new ReadCoilsRequest(slaveId, start, len);
            ReadCoilsResponse response = (ReadCoilsResponse) master.send(request);

            if (response.isException())
                System.out.println("Exception response: message=" + response.getExceptionMessage());
            else
                System.out.println(Arrays.toString(response.getBooleanData()));
        }
        catch (ModbusTransportException e) {
            e.printStackTrace();
        }
    }

    public static void readDiscreteInputTest(ModbusMaster master, int slaveId, int start, int len) {
        try {
            ReadDiscreteInputsRequest request = new ReadDiscreteInputsRequest(slaveId, start, len);
            ReadDiscreteInputsResponse response = (ReadDiscreteInputsResponse) master.send(request);

            if (response.isException())
                System.out.println("Exception response: message=" + response.getExceptionMessage());
            else
                System.out.println(Arrays.toString(response.getBooleanData()));
        }
        catch (ModbusTransportException e) {
            e.printStackTrace();
        }
    }

    public static void readHoldingRegistersTest(ModbusMaster master, int slaveId, int start, int len) {
        try {
            ReadHoldingRegistersRequest request = new ReadHoldingRegistersRequest(slaveId, start, len);
            ReadHoldingRegistersResponse response = (ReadHoldingRegistersResponse) master.send(request);

            if (response.isException())
                System.out.println("Exception response: message=" + response.getExceptionMessage());
            else
                System.out.println(Arrays.toString(response.getShortData()));
        }
        catch (ModbusTransportException e) {
            e.printStackTrace();
        }
    }

    public static void readInputRegistersTest(ModbusMaster master, int slaveId, int start, int len) {
        try {
            ReadInputRegistersRequest request = new ReadInputRegistersRequest(slaveId, start, len);
            ReadInputRegistersResponse response = (ReadInputRegistersResponse) master.send(request);

            if (response.isException())
                System.out.println("Exception response: message=" + response.getExceptionMessage());
            else
                System.out.println(Arrays.toString(response.getShortData()));
        }
        catch (ModbusTransportException e) {
            e.printStackTrace();
        }
    }

    public static String readHoldingRegisters2String(ModbusMaster master, int slaveId, int start, int len) {
        try {
            ReadHoldingRegistersRequest request = new ReadHoldingRegistersRequest(slaveId, start, len);
            ReadHoldingRegistersResponse response = (ReadHoldingRegistersResponse) master.send(request);

            String result ="";
            if (response.isException())
                System.out.println("Exception response: message=" + response.getExceptionMessage());
            else {
                System.out.println("bytes:"+Arrays.toString(response.getShortData()));
                //result = Arrays.toString((response.getShortData()));
                String hex = bytesToHex(response.getData());
                System.out.println("Hex:" + hex);
                result = DataTransform.hexToAscii(hex);
                System.out.println("ASCii:"+ result);

                System.out.println("Device NAME:"+result.substring(0,32));
                System.out.println("Device SN:"+result.substring(32,64));
                System.out.println("固件版本号（Firmware version):"+Integer.parseInt(hex.substring(128,130))+"."+Long.parseLong(hex.substring(130,132),16));
                System.out.println("硬件版本号（Hardware version):"+Integer.parseInt(hex.substring(132,134))+"."+Long.parseLong(hex.substring(134,136),16));
                System.out.println("温度单位符号（Unit of temperature）:"+result.substring(68,74));
                System.out.println("参数1的单位符号（Unit of Parameter 1):"+result.substring(74,80));
                System.out.println("参数2的单位符号（Unit of Parameter 2):"+result.substring(80,86));
                System.out.println("参数3的单位符号（Unit of Parameter 3):"+result.substring(86,92));
                System.out.println("参数4的单位符号（Unit of Parameter 4):"+result.substring(92,98));


            }

            return  result;
        }
        catch (ModbusTransportException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static short[] readHoldingRegisters2Short(ModbusMaster master, int slaveId, int start, int len) {
        try {
            ReadHoldingRegistersRequest request = new ReadHoldingRegistersRequest(slaveId, start, len);
            ReadHoldingRegistersResponse response = (ReadHoldingRegistersResponse) master.send(request);

            if (response.isException())
                System.out.println("Exception response: message=" + response.getExceptionMessage());
            else
                System.out.println(Arrays.toString(response.getShortData()));

            return  response.getShortData();
        }
        catch (ModbusTransportException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] readHoldingRegisters2Byte(ModbusMaster master, int slaveId, int start, int len) {
        try {
            ReadHoldingRegistersRequest request = new ReadHoldingRegistersRequest(slaveId, start, len);
            ReadHoldingRegistersResponse response = (ReadHoldingRegistersResponse) master.send(request);

            if (response.isException())
                System.out.println("Exception response: message=" + response.getExceptionMessage());
            else
                System.out.println(Arrays.toString(response.getShortData()));

            return  response.getData();
        }
        catch (ModbusTransportException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void readHoldingRegistersTest1(ModbusMaster master, int slaveId, int start, int len) {
        try {
            short[] data=readHoldingRegisters2Short(master,slaveId,start,len);
            System.out.println(Arrays.toString(data));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void readHoldingRegistersTest2(ModbusMaster master, int slaveId, int start, int len) {
        try {
            byte[] data=readHoldingRegisters2Byte(master,slaveId,start,len);
            System.out.println(Arrays.toString(data));

            byte[] temperature = new byte[]{data[2],data[3], data[4], data[5]};
            System.out.println("温度：" + IEEE754.hex2FloatIeee(temperature)+"℃");

            byte[] oxygen = new byte[]{data[6],data[7], data[8], data[9]};
            System.out.println("溶解氢占比：" + IEEE754.hex2FloatIeee(oxygen)+"%");

            oxygen = new byte[]{data[10],data[11], data[12], data[13]};
            System.out.println("溶解氢每毫克含量：" + IEEE754.hex2FloatIeee(oxygen)+"mg/L");

            oxygen = new byte[]{data[14],data[15], data[16], data[17]};
            System.out.println("溶解氢含量：" + IEEE754.hex2FloatIeee(oxygen)+"PPM");

            oxygen = new byte[]{data[18],data[19], data[20], data[21]};
            System.out.println("溶解氢占比状态：" + Integer.valueOf(bytesToHex(oxygen),16));
            System.out.println("溶解氢含量状态：" + Integer.valueOf(bytesToHex(oxygen),16));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String bytesToHex(byte[] bytes) {
        StringBuilder sbuffer = new StringBuilder();

        for (byte aByte : bytes) {
            String hex = Integer.toHexString(aByte & 0xFF);

            if (hex.length() < 2) {
                sbuffer.append(0);

            }

            sbuffer.append(hex);

        }

        return sbuffer.toString();

    }

    public static void writeCoilTest(ModbusMaster master, int slaveId, int offset, boolean value) {
        try {
            WriteCoilRequest request = new WriteCoilRequest(slaveId, offset, value);
            WriteCoilResponse response = (WriteCoilResponse) master.send(request);

            if (response.isException())
                System.out.println("Exception response: message=" + response.getExceptionMessage());
            else
                System.out.println("Success");
        }
        catch (ModbusTransportException e) {
            e.printStackTrace();
        }
    }

    public static void writeRegisterTest(ModbusMaster master, int slaveId, int offset, int value) {
        try {
            WriteRegisterRequest request = new WriteRegisterRequest(slaveId, offset, value);
            WriteRegisterResponse response = (WriteRegisterResponse) master.send(request);

            if (response.isException())
                System.out.println("Exception response: message=" + response.getExceptionMessage());
            else
                System.out.println("Success");
        }
        catch (ModbusTransportException e) {
            e.printStackTrace();
        }
    }

    public static void readExceptionStatusTest(ModbusMaster master, int slaveId) {
        try {
            ReadExceptionStatusRequest request = new ReadExceptionStatusRequest(slaveId);
            ReadExceptionStatusResponse response = (ReadExceptionStatusResponse) master.send(request);

            if (response.isException())
                System.out.println("Exception response: message=" + response.getExceptionMessage());
            else
                System.out.println(response.getExceptionStatus());
        }
        catch (ModbusTransportException e) {
            e.printStackTrace();
        }
    }

    public static void reportSlaveIdTest(ModbusMaster master, int slaveId) {
        try {
            ReportSlaveIdRequest request = new ReportSlaveIdRequest(slaveId);
            ReportSlaveIdResponse response = (ReportSlaveIdResponse) master.send(request);

            if (response.isException())
                System.out.println("Exception response: message=" + response.getExceptionMessage());
            else
                System.out.println(Arrays.toString(response.getData()));
        }
        catch (ModbusTransportException e) {
            e.printStackTrace();
        }
    }

    public static void writeCoilsTest(ModbusMaster master, int slaveId, int start, boolean[] values) {
        try {
            WriteCoilsRequest request = new WriteCoilsRequest(slaveId, start, values);
            WriteCoilsResponse response = (WriteCoilsResponse) master.send(request);

            if (response.isException())
                System.out.println("Exception response: message=" + response.getExceptionMessage());
            else
                System.out.println("Success");
        }
        catch (ModbusTransportException e) {
            e.printStackTrace();
        }
    }

    public static void writeRegistersTest(ModbusMaster master, int slaveId, int start, short[] values) {
        try {
            WriteRegistersRequest request = new WriteRegistersRequest(slaveId, start, values);
            WriteRegistersResponse response = (WriteRegistersResponse) master.send(request);

            if (response.isException())
                System.out.println("Exception response: message=" + response.getExceptionMessage());
            else
                System.out.println("Success");
        }
        catch (ModbusTransportException e) {
            e.printStackTrace();
        }
    }

    public static void writeMaskRegisterTest(ModbusMaster master, int slaveId, int offset, int and, int or) {
        try {
            WriteMaskRegisterRequest request = new WriteMaskRegisterRequest(slaveId, offset, and, or);
            WriteMaskRegisterResponse response = (WriteMaskRegisterResponse) master.send(request);

            if (response.isException())
                System.out.println("Exception response: message=" + response.getExceptionMessage());
            else
                System.out.println("Success");
        }
        catch (ModbusTransportException e) {
            e.printStackTrace();
        }
    }
}
