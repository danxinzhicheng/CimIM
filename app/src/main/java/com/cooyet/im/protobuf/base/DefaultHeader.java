package com.cooyet.im.protobuf.base;

import com.cooyet.im.config.SysConstant;
import com.cooyet.im.imservice.support.SequenceNumberMaker;
import com.cooyet.im.utils.Logger;

public class DefaultHeader extends Header {
    private Logger logger = Logger.getLogger(DefaultHeader.class);

    public DefaultHeader(int serviceId, int commandId) {
        setVersion((short) SysConstant.PROTOCOL_VERSION);
//        setFlag((short) SysConstant.PROTOCOL_FLAG);
        setServiceId((short) serviceId);
        setCommandId((short) commandId);
        short seqNo = SequenceNumberMaker.getInstance().make();
        setSeqnum(seqNo);
        setUserId((short) SysConstant.PROTOCOL_USERID);
        setChannel((short) SysConstant.PROTOCOL_CHANNEL);
//        setReserved((short)SysConstant.PROTOCOL_RESERVED);
        logger.d("packet#construct Default Header -> serviceId:%d, commandId:%d, seqNo:%d", serviceId, commandId, seqNo);
    }
}
