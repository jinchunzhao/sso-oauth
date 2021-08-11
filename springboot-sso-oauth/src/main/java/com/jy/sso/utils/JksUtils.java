package com.jy.sso.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

/**
 * $start$
 *
 * @author JinChunZhao
 * @version 1.0
 * @date 2021-08-08 9:38
 */
@Slf4j
public class JksUtils {

    /**
     * 获取公钥
     *
     * @param publicKey
     *        公钥文件名称
     * @return
     *        公钥
     */
    public static String getPubKey(String publicKey) {
        Resource resource = new ClassPathResource(publicKey);
        try {
            InputStreamReader inputStreamReader = new InputStreamReader(resource.getInputStream());
            BufferedReader br = new BufferedReader(inputStreamReader);
            return br.lines().collect(Collectors.joining("\n"));
        } catch (IOException ioe) {
            log.error("获取公钥异常");
            return null;
        }
    }


}
