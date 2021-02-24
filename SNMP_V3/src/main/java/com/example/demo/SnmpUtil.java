package com.example.demo;

import org.snmp4j.*;
import org.snmp4j.MessageDispatcher;
import org.snmp4j.MessageDispatcherImpl;
import org.snmp4j.Snmp;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.MPv1;
import org.snmp4j.mp.MPv2c;
import org.snmp4j.mp.MPv3;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.security.*;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultUdpTransportMapping;

import java.io.IOException;

public class SnmpUtil {
    //private static Logger log = LoggerFactory.getLogger(SnmpUtil.class);
    public static Snmp snmp = null;
    private static String community = "public";
    private static String ipAddress = "udp:135.27.213.163/";

    /**
     * @description 初始化snmp
     * @author YuanFY
     * @date 2017年12月16日 上午10:28:01
     * @version 1.0
     * @throws IOException
     */
    public static void initSnmp() throws IOException {
        //1、初始化多线程消息转发类
        MessageDispatcher messageDispatcher = new MessageDispatcherImpl();
        //其中要增加三种处理模型。如果snmp初始化使用的是Snmp(TransportMapping<? extends Address> transportMapping) ,就不需要增加
        messageDispatcher.addMessageProcessingModel(new MPv1());
        messageDispatcher.addMessageProcessingModel(new MPv2c());
        //当要支持snmpV3版本时，需要配置user
        OctetString localEngineID = new OctetString(MPv3.createLocalEngineID());
        USM usm = new USM(SecurityProtocols.getInstance().addDefaultProtocols(), localEngineID, 0);
        UsmUser user = new UsmUser(new OctetString("SNMPV3"), AuthSHA.ID, new OctetString("authPassword"),
                PrivAES128.ID, new OctetString("privPassword"));
        usm.addUser(user.getSecurityName(), user);
        messageDispatcher.addMessageProcessingModel(new MPv3(usm));
        //2、创建transportMapping
        UdpAddress updAddr = (UdpAddress) GenericAddress.parse("udp:135.27.213.140/161");
        //TransportMapping<?> transportMapping = new DefaultUdpTransportMapping(updAddr);
        TransportMapping<?> transportMapping = new DefaultUdpTransportMapping();
        //3、正式创建snmp
        snmp = new Snmp(messageDispatcher, transportMapping);
        //开启监听
        snmp.listen();
    }

    private static Target createTarget(int version, int port) {
        Target target = null;
        if (!(version == SnmpConstants.version3 || version == SnmpConstants.version2c || version == SnmpConstants.version1)) {
            return target;
        }
        if (version == SnmpConstants.version3) {
            target = new UserTarget();
            //snmpV3需要设置安全级别和安全名称，其中安全名称是创建snmp指定user设置的new OctetString("SNMPV3")
            target.setSecurityLevel(SecurityLevel.AUTH_PRIV);
            target.setSecurityName(new OctetString("SNMPV3"));
        } else {
            //snmpV1和snmpV2需要指定团体名名称
            target = new CommunityTarget();
            ((CommunityTarget)target).setCommunity(new OctetString(community));
            if (version == SnmpConstants.version2c) {
                target.setSecurityModel(SecurityModel.SECURITY_MODEL_SNMPv2c);
            }
        }
        target.setVersion(version);
        //必须指定，没有设置就会报错。
        target.setAddress(GenericAddress.parse(ipAddress+port));
        target.setRetries(5);
        target.setTimeout(3000);
        return target;
    }
    private static PDU createPDU(int version, int type, String oid){
        PDU pdu = null;
        if (version == SnmpConstants.version3) {
            pdu = new ScopedPDU();
        }else {
            pdu = new PDUv1();
        }
        pdu.setType(type);
        //可以添加多个变量oid
        pdu.add(new VariableBinding(new OID(oid)));
        return pdu;
    }

    public static void snmpGet(String oid){
        try {
            //1、初始化snmp,并开启监听
            initSnmp();
            //2、创建目标对象
            //Target target = createTarget(SnmpConstants.version2c, SnmpConstants.DEFAULT_COMMAND_RESPONDER_PORT);
            Target target = createTarget(SnmpConstants.version3, SnmpConstants.DEFAULT_COMMAND_RESPONDER_PORT);
            //3、创建报文
            //PDU pdu = createPDU(SnmpConstants.version2c, PDU.GET, oid);
            PDU pdu = createPDU(SnmpConstants.version3, PDU.GET, oid);
            System.out.println("-------> 发送PDU <-------");
            //4、发送报文，并获取返回结果
            ResponseEvent responseEvent = snmp.send(pdu, target);
            PDU response = responseEvent.getResponse();
            System.out.println("返回结果：" + response);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }


}