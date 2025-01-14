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
package com.serotonin.modbus4j.test.optod;

import com.serotonin.modbus4j.base.ModbusUtils;
import com.serotonin.modbus4j.ip.IpMessage;
import com.serotonin.modbus4j.msg.ModbusMessage;
import com.serotonin.modbus4j.sero.util.queue.ByteQueue;

/**
 * <p>XaMessage class.</p>
 *
 * @author Matthew Lohbihler
 * @version 5.0.0
 */
public class OptodMessage extends IpMessage {
    protected final int transactionId;

    /**
     * <p>Constructor for XaMessage.</p>
     *
     * @param modbusMessage a {@link ModbusMessage} object.
     * @param transactionId a int.
     */
    public OptodMessage(ModbusMessage modbusMessage, int transactionId) {
        super(modbusMessage);
        this.transactionId = transactionId;
    }

    /**
     * <p>getMessageData.</p>
     *
     * @return an array of {@link byte} objects.
     */
    public byte[] getMessageData() {
        ByteQueue msgQueue = new ByteQueue();

        // Write the particular message.
        modbusMessage.write(msgQueue);

        // Create the XA message
        ByteQueue xaQueue = new ByteQueue();
        ModbusUtils.pushShort(xaQueue, transactionId);
        ModbusUtils.pushShort(xaQueue, ModbusUtils.IP_PROTOCOL_ID);
        ModbusUtils.pushShort(xaQueue, msgQueue.size());
        xaQueue.push(msgQueue);

        // Return the data.
        return xaQueue.popAll();
    }

    /**
     * <p>Getter for the field <code>transactionId</code>.</p>
     *
     * @return a int.
     */
    public int getTransactionId() {
        return transactionId;
    }

    /** {@inheritDoc} */
    @Override
    public ModbusMessage getModbusMessage() {
        return modbusMessage;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "OptodMessage [transactionId=" + transactionId + ", message=" + modbusMessage + "]";
    }
}
