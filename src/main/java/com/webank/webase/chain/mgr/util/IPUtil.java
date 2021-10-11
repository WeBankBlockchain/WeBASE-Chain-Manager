package com.webank.webase.chain.mgr.util;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;

/**
 *
 */

@Slf4j
public class IPUtil {

    public final static Set<String> LOCAL_IP_SET = new HashSet<>();

    static {
        LOCAL_IP_SET.add("127.0.0.1");
        LOCAL_IP_SET.add("localhost");

//        LOCAL_IP_SET.addAll(getLocalIPSet());
    }


    /**
     * Check ip is local.
     *
     * @param ip
     * @return
     */
    public static boolean isLocal(String ip) {
        return LOCAL_IP_SET.stream().anyMatch(ip::equalsIgnoreCase);
    }


//    public static Set<String> getLocalIPSet(){
//        Set<String> result =  new HashSet<>();
//
//        try {
//            Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
//            for (NetworkInterface netint : Collections.list(nets)) {
//                Enumeration<InetAddress> inetAddresses = netint.getInetAddresses();
//                for (InetAddress inetAddress : Collections.list(inetAddresses)) {
//                    String ip = inetAddress.getHostAddress();
//                    if (inetAddress instanceof Inet4Address) {
//                        result.add(ip);
//                    }
//                }
//            }
//        } catch (Exception e) {
//            log.warn("Get ip address from network interface error.",e);
//        }
//        return result;
//    }


}