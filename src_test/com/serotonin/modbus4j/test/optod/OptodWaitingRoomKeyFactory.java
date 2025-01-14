package com.serotonin.modbus4j.test.optod;

import com.serotonin.modbus4j.msg.ModbusMessage;
import com.serotonin.modbus4j.sero.messaging.IncomingResponseMessage;
import com.serotonin.modbus4j.sero.messaging.OutgoingRequestMessage;
import com.serotonin.modbus4j.sero.messaging.WaitingRoomKey;
import com.serotonin.modbus4j.sero.messaging.WaitingRoomKeyFactory;

/**
 * <p>XaWaitingRoomKeyFactory class.</p>
 *
 * @author Matthew Lohbihler
 * @version 5.0.0
 */
public class OptodWaitingRoomKeyFactory implements WaitingRoomKeyFactory {
    /** {@inheritDoc} */
    @Override
    public WaitingRoomKey createWaitingRoomKey(OutgoingRequestMessage request) {
        return createWaitingRoomKey((OptodMessage) request);
    }

    /** {@inheritDoc} */
    @Override
    public WaitingRoomKey createWaitingRoomKey(IncomingResponseMessage response) {
        return createWaitingRoomKey((OptodMessage) response);
    }

    /**
     * <p>createWaitingRoomKey.</p>
     *
     * @param msg a {@link OptodMessage} object.
     * @return a {@link WaitingRoomKey} object.
     */
    public WaitingRoomKey createWaitingRoomKey(OptodMessage msg) {
        return new OptodWaitingRoomKey(msg.getTransactionId(), msg.getModbusMessage());
    }

    class OptodWaitingRoomKey implements WaitingRoomKey {
        private final int transactionId;
        private final int slaveId;
        private final byte functionCode;

        public OptodWaitingRoomKey(int transactionId, ModbusMessage msg) {
            this.transactionId = transactionId;
            this.slaveId = msg.getSlaveId();
            this.functionCode = msg.getFunctionCode();
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + functionCode;
            result = prime * result + slaveId;
            result = prime * result + transactionId;
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            OptodWaitingRoomKey other = (OptodWaitingRoomKey) obj;
            if (functionCode != other.functionCode)
                return false;
            if (slaveId != other.slaveId)
                return false;
            if (transactionId != other.transactionId)
                return false;
            return true;
        }
    }
}
