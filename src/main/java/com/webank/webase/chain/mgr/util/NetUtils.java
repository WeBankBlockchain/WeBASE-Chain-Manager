package com.webank.webase.chain.mgr.util;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;

import lombok.extern.slf4j.Slf4j;

/**
 *
 */

@Slf4j
public class NetUtils {

    public static Pair<Boolean, Integer> anyPortInUse(String ip, String sshUser,int sshPort,String privateKey, int ... portArray) {
        if (ArrayUtils.isEmpty(portArray)) {
            return Pair.of(false, 0);
        }

        for (int port : portArray) {
            boolean inUse = SshUtil.portInUse(port,ip,sshUser,sshPort,privateKey);
            if (inUse) {
                return Pair.of(true, port);
            }
        }
        return Pair.of(false, 0);
    }

    public static Pair<Boolean, Integer> anyPortNotInUse(String ip, String sshUser,int sshPort,String privateKey, Integer[] portArray) {
        if (ArrayUtils.isEmpty(portArray)) {
            return Pair.of(false, 0);
        }

        for (int port : portArray) {
            boolean inUse = SshUtil.portInUse(port,ip,sshUser,sshPort,privateKey);
            if (!inUse) {
                return Pair.of(true, port);
            }
        }
        return Pair.of(false, 0);
    }


    public static boolean checkAddress(String ip, int port, int timeout) {
        int newTimeout = timeout > 0 ? timeout : 2000;

        try {
            try (Socket crunchifySocket = new Socket()) {
                // Connects this socket to the server with a specified timeout value.
                crunchifySocket.connect(new InetSocketAddress(ip, port), newTimeout);
            }
            // Return true if connection successful
            return true;
        } catch (IOException e) {
            log.error("Connect to host:[{}] and port:[{}] with timeout:[{}] is error.", ip, port, newTimeout, e.getMessage());
        }
        return false;
    }

    /**
     * Check ip:port accessible.
     *
     * @param address format ip:port, like 127.0.0.1:6004;
     * @param timeout
     * @return
     */
    public static boolean checkAddress(String address, int timeout) {
        String[] ipPortArray = address.split(":");
        if (ArrayUtils.getLength(ipPortArray) != 2) {
            log.error("Address:[{}] format error, should be [ip:port], like 127.0.0.1:6004.", address);
            return false;
        }
        try {
            int port = Integer.parseInt(ipPortArray[1]);
            return checkAddress(ipPortArray[0], port, timeout);
        } catch (Exception e) {
            log.error("Address:[{}] port error, should be [ip:port], like 127.0.0.1:6004.", address);
        }
        return false;
    }
}